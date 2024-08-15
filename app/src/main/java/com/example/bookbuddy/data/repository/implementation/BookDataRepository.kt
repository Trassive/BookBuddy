package com.example.bookbuddy.data.repository.implementation

import com.example.bookbuddy.data.exception.OutOfCacheMemoryException
import com.example.bookbuddy.data.exception.OutOfDataException
import com.example.bookbuddy.model.DownloadState
import com.example.bookbuddy.data.exception.InvalidRequestException
import com.example.bookbuddy.data.local.datasource.BookLocalDataSource
import com.example.bookbuddy.data.util.FileHandler
import com.example.bookbuddy.data.util.InternalDownloadState
import com.example.bookbuddy.data.local.entities.BookResource
import com.example.bookbuddy.data.local.entities.SavedBook
import com.example.bookbuddy.data.remote.BookRemoteDataSource
import com.example.bookbuddy.data.repository.interfaces.BookCatalogueRepository
import com.example.bookbuddy.data.repository.interfaces.BookDetailsRepository
import com.example.bookbuddy.data.repository.interfaces.OfflineBookRepository
import com.example.bookbuddy.data.repository.interfaces.ReadiumRepository
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.BookWithResources
import com.example.bookbuddy.model.LibraryBook
import com.example.bookbuddy.model.toBook
import com.example.bookbuddy.network.RemoteBook
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BookDataRepository(
    private val bookLocalDataSource: BookLocalDataSource,
    private val bookRemoteDataSource: BookRemoteDataSource,
    private val fileHandler: FileHandler,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Default
    ): BookCatalogueRepository, BookDetailsRepository, OfflineBookRepository, ReadiumRepository {
    private var nextPageLink: String? = null
    private var count: Int = 0
    override suspend fun getCatalogue(query: String): List<Book> = withContext(dispatcherIO){

        val books = bookRemoteDataSource.getBooks((gutenbergQuery(search = query)))
             .also {
                 nextPageLink = it.next
                 count = it.books.size
             }.books
        ensureActive()

        return@withContext mergeMetaData(books)
        }


    override suspend fun updateCatalogue(): Flow<List<Book>> = withContext(dispatcherIO) {
        return@withContext flow {
            while(true){
                if (nextPageLink == null) throw OutOfDataException(message = "No Data to update")

                val books = bookRemoteDataSource.getBooks(nextPageLink!!).also {
                    count += it.books.size
                    nextPageLink = it.next
                }.books

                emit(mergeMetaData(books))


                if (count >= MAX_ITEMS) throw OutOfCacheMemoryException("Shrink the list")
            }
        }
    }

    override suspend fun getBookDetails(id: Int): Book = coroutineScope {
        val bookLocalDeferred = async(dispatcherMain) {
            bookLocalDataSource.getBook(id)
        }

        val bookRemoteDeferred = async(dispatcherIO) {
            val book = bookRemoteDataSource.getBook(id).books
            ensureActive()
            mergeMetaData(book)
        }

        val bookLocal = bookLocalDeferred.await()


        return@coroutineScope bookLocal?.constructBook() ?: with(bookRemoteDeferred.await()){
            if(this.size!=1){
                throw IllegalArgumentException("Invalid Book")
            } else{
                this[0]
            }
        }
    }

    override suspend fun saveBook(book: Book) {
        val (book,resource) = destructBook(book)
        bookLocalDataSource.saveBook(book = book, resource = resource)
    }

    override suspend fun unSaveBook(id: Int) {
        bookLocalDataSource.unSaveBook(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun downloadBook(book: Book): Flow<DownloadState> {
        return withContext(dispatcherIO){
            if (fileHandler.fileExists(book.id.toString())) return@withContext flow { DownloadState.Failed(InvalidRequestException("File Already Exists")) }
            val response = bookRemoteDataSource.downloadBooks(book.downloadLink)
            return@withContext fileHandler.saveFile(response = response, fileName = book.id.toString())
                .distinctUntilChanged()
                .mapLatest { state ->
                    when (state) {
                        is InternalDownloadState.Downloading -> DownloadState.Downloading(state.progress)
                        is InternalDownloadState.Failed -> DownloadState.Failed(state.error)
                        is InternalDownloadState.Finished -> {
                            withContext(dispatcherMain){
                                val (book, resource) = destructBook(book, state.filePath)
                                bookLocalDataSource.saveBook(book = book, resource = resource)
                            }
                            DownloadState.Finished
                        }
                    }
                }
        }
    }

    override suspend fun deleteBook(id: Int) {
        bookLocalDataSource.deleteBook(id)
        if(fileHandler.fileExists(id.toString())) fileHandler.deleteFile(id.toString())
    }


    override suspend fun getSavedBooks(): Flow<List<LibraryBook>> = withContext(dispatcherIO){
        return@withContext bookLocalDataSource.getBooks(false).mapLatest { books->
            books.map {book-> book.toLibraryBook(false) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getDownloadedBooks(): Flow<List<LibraryBook>> = withContext(dispatcherIO){
        return@withContext bookLocalDataSource.getBooks(true).mapLatest { books->
            books.map {book-> book.toLibraryBook(true) }
        }
    }

    override suspend fun getBookUrl(id: Int): Url {
        TODO("Not yet implemented")
    }

    override suspend fun updateProgress(locator: Locator) {
        TODO("Not yet implemented")
    }
    private suspend fun mergeMetaData(remoteBooks: List<RemoteBook>): List<Book> = coroutineScope{
        return@coroutineScope remoteBooks.map{ book: RemoteBook->
            val metaData = bookRemoteDataSource.getAdditionalMetadata(googleBooksQuery(title = book.title, author = book.authors[0].name ))
            ensureActive()
            book.toBook(description = metaData.items.volumeInfo.description)
        }
    }
    private companion object{
        const val MAX_ITEMS: Int = 50
        fun gutenbergQuery(search: String? = null, topic: String? = null): Map<String, String> {
            val mutableMap = mutableMapOf("mime_type" to "application%2Fepub%2Bzip")

            search?.let{search->
                mutableMap.put("search" , search.replace(" ", "%20"))
            }
            topic?.let { topic->
                mutableMap.put("topic", topic.replace(" ", "%20"))
            }
            return mutableMap
        }
        fun googleBooksQuery(title: String, author: String): String {

            val encodedTitle = URLEncoder.encode("intitle:$title", StandardCharsets.UTF_8.toString())
            val encodedAuthor = URLEncoder.encode("inauthor:$author", StandardCharsets.UTF_8.toString())

            val query = "$encodedTitle+$encodedAuthor"

            return "?q=$query"
        }

        fun destructBook(book: Book, downloadedPath: String? = null): BookWithResources{
            return BookWithResources(
                SavedBook(
                id = book.id,
                title = book.title,
                authors = book.authors.joinToString(),
                categories = book.categories.joinToString(),
                coverImage = book.cover,
                description = book.description
                ) ,
                BookResource(
                    bookId = book.id,
                    downloadLink = book.downloadLink,
                    downloadPath = downloadedPath
                )
            )
        }
        fun SavedBook.toLibraryBook(isDownloaded: Boolean) = LibraryBook(
            id = this.id,
            title = this.title,
            categories = this.categories.split(","),
            authors = this.authors.split(","),
            coverImage = this.coverImage,
            isDownloaded = isDownloaded
        )
        fun BookWithResources.constructBook(): Book {
            return Book(
                id = this.book.id,
                title = this.book.title,
                authors = this.book.authors.split(","),
                categories = this.book.categories.split(","),
                cover = this.book.coverImage,
                isDownloaded = this.resource.downloadPath!=null,
                isSaved = true,
                description = this.book.description,
                downloadLink = this.resource.downloadLink
            )
        }
    }
}
