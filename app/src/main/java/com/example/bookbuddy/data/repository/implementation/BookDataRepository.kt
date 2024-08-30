package com.example.bookbuddy.data.repository.implementation

import android.util.Log
import com.example.bookbuddy.data.exception.InvalidRequestException
import com.example.bookbuddy.data.exception.OutOfDataException
import com.example.bookbuddy.data.local.datasource.BookLocalDataSource
import com.example.bookbuddy.data.local.entities.BookResource
import com.example.bookbuddy.data.local.entities.SavedBook
import com.example.bookbuddy.data.local.entities.SavedLocator
import com.example.bookbuddy.data.remote.BookRemoteDataSource
import com.example.bookbuddy.data.repository.interfaces.BookCatalogueRepository
import com.example.bookbuddy.data.repository.interfaces.BookDetailsRepository
import com.example.bookbuddy.data.repository.interfaces.OfflineBookRepository
import com.example.bookbuddy.data.repository.interfaces.ReadiumRepository
import com.example.bookbuddy.data.util.FileHandler
import com.example.bookbuddy.data.util.InternalDownloadState
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.BookWithResources
import com.example.bookbuddy.model.DownloadState
import com.example.bookbuddy.model.LibraryBook
import com.example.bookbuddy.model.Update
import com.example.bookbuddy.model.toBook
import com.example.bookbuddy.network.RemoteBook
import com.example.bookbuddy.network.VolumeInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class BookDataRepository @Inject constructor(
    private val bookLocalDataSource: BookLocalDataSource,
    private val bookRemoteDataSource: BookRemoteDataSource,
    private val fileHandler: FileHandler,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
): BookCatalogueRepository, BookDetailsRepository, OfflineBookRepository, ReadiumRepository {
    private var nextHomePageLink: String? = null
    private var nextSearchPageLink: String? = null
    override suspend fun getCatalogue(query: String?): Flow<List<Book>> = withContext(dispatcherIO){
        val books = bookRemoteDataSource.getBooks((gutenbergQuery(search = query)))
            .also {
                if(query.isNullOrEmpty())  nextHomePageLink = it.next
                else nextSearchPageLink = it.next
            }.books
        Log.d("BookDataRepository", "getCatalogue: ${books.size}")
        return@withContext flow {
            books.chunked(15).forEach { chunk ->
                Log.d("BookDataRepository", "getCatalogue: ${chunk.size}")
                emit(mergeMetaData(chunk))
            }
        }
    }


    override suspend fun updateCatalogue(update: Update): Flow<List<Book>> = withContext(dispatcherIO) {
        val updatePageLink = when(update){
            Update.HOME -> ::nextHomePageLink
            Update.SEARCH -> ::nextSearchPageLink
        }
        Log.d("BookDataRepository", "updateCatalogue: ${updatePageLink.name} ${updatePageLink.get()}")
        return@withContext flow {
            while(true){
                if (updatePageLink.get() == null) throw OutOfDataException(message = "No Data to update")

                bookRemoteDataSource.getBooks(updatePageLink.get()!!).also {data->
                    updatePageLink.set(data.next)
                }.books.chunked(15).forEach { chunk ->
                    emit(mergeMetaData(chunk))
                }
            }
        }
    }

    override suspend fun getBookDetails(id: Int): Book = coroutineScope {
        val bookLocalDeferred = async(dispatcherIO) {
            bookLocalDataSource.getBook(id)
        }

        val bookRemoteDeferred = async(dispatcherIO) {
            val book = bookRemoteDataSource.getBooks(mapOf("ids" to id.toString())).books
            ensureActive()
            mergeMetaData(book)
        }

        val bookLocal = bookLocalDeferred.await()
        Log.d("BookDataRepository", "getBookDetails: $bookLocal")
        return@coroutineScope bookLocal?.constructBook() ?: with(bookRemoteDeferred.await()){
           this[0]
        }
    }

    override suspend fun saveBook(book: Book) {
        val (savedBook,resource) = destructBook(book)
        bookLocalDataSource.saveBook(book = savedBook, resource = resource)
    }

    override suspend fun unSaveBook(id: Int) {
        bookLocalDataSource.unSaveBook(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun downloadBook(book: Book): Flow<DownloadState> {
        return withContext(dispatcherIO){
            if (fileHandler.fileExists(book.id.toString())) return@withContext flow { DownloadState.Failed(InvalidRequestException("File Already Exists")) }
            Log.d("BookDataRepository", "downloadBook: ${book.downloadLink}")
            val response = bookRemoteDataSource.downloadBooks(book.downloadLink)
            return@withContext fileHandler.saveFile(response = response, fileName = book.id.toString())
                .distinctUntilChanged()
                .mapLatest { state ->
                    when (state) {
                        is InternalDownloadState.Downloading -> DownloadState.Downloading(state.progress)
                        is InternalDownloadState.Failed -> DownloadState.Failed(state.error)
                        is InternalDownloadState.Finished -> {
                            withContext(dispatcherIO){
                                val (savedBook, resource) = destructBook(book, state.filePath)
                                bookLocalDataSource.saveBook(book = savedBook, resource = resource)
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

    override suspend fun getDownloadedBooks(): Flow<List<LibraryBook>> = withContext(dispatcherIO){
        return@withContext bookLocalDataSource.getBooks(true).mapLatest { books->
            books.map {book-> book.toLibraryBook(true) }
        }
    }

    override suspend fun getBookUrl(id: Int): String = withContext(dispatcherIO){
            val book = bookLocalDataSource.getBook(id)?: throw IllegalArgumentException("Book Not Found")
            return@withContext book.resource.downloadPath?: throw IllegalArgumentException("Book Not Downloaded")
    }


    override suspend fun updateProgress(id: Int,locator: Locator) = withContext(dispatcherIO){
            bookLocalDataSource.saveProgress(locator = locator.toSavedLocator(id))
    }


    override suspend fun getLoctor(id: Int): Locator? = withContext(dispatcherIO){
            return@withContext bookLocalDataSource.getLocator(id)?.toLocator()
    }


    private suspend fun mergeMetaData(remoteBooks: List<RemoteBook>): List<Book> = withContext(dispatcherIO){
        val jobs = remoteBooks.map { book ->
            async {
                val metaData: VolumeInfo? = try {
                    val metadata = bookRemoteDataSource.getAdditionalMetadata(
                        googleBooksQuery(
                            title = book.title,
                            author = book.authors.firstOrNull()?.name ?: ""
                        )
                    )
                    metadata.items.firstOrNull()?.volumeInfo
                } catch (e: Exception) {
                    null
                } catch (e: TimeoutCancellationException){
                    Log.d("BookDataRepository", "${e.message} ${e.cause}")
                    null
                }

                book.toBook(description = metaData?.description ?: "Description Not Found")
            }
        }
        jobs.awaitAll()
    }
    private companion object{
        fun gutenbergQuery(search: String? = null, topic: String? = null): Map<String, String> {
            val mutableMap = mutableMapOf("mime_type" to "application%2Fepub%2Bzip")

            search?.let{
                mutableMap.put("search" , it.replace(" ", "%20"))
            }
            topic?.let {
                mutableMap.put("topic", it.replace(" ", "%20"))
            }
            return mutableMap
        }
        fun googleBooksQuery(title: String, author: String): Map<String,String> {

            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val encodedAuthor = URLEncoder.encode(author, StandardCharsets.UTF_8.toString())

            val query = "intitle:\"${encodedTitle}\"+inauthor:\"${encodedAuthor}\""

            return mapOf("q" to query, "printType" to "books", "fields" to "items/volumeInfo/description", "orderBy" to "relevance")

        }

        fun destructBook(book: Book, downloadedPath: String? = null): BookWithResources{
            return BookWithResources(
                SavedBook(
                id = book.id,
                title = book.title,
                authors = book.authors.joinToString(),
                categories = book.categories.joinToString(),
                coverImage = book.coverImage,
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
                coverImage = this.book.coverImage,
                isDownloaded = this.resource.downloadPath!=null,
                isSaved = true,
                description = this.book.description,
                downloadLink = this.resource.downloadLink
            )
        }
        fun Locator.toSavedLocator(id: Int) = SavedLocator(

            bookId = id,
            href = this.href.toString(),
            type = this.mediaType.toString(),
            totalProgression =  this.locations.totalProgression,
            progression = this.locations.progression,
            textBefore = this.text.before,
            position = this.locations.position,
            textAfter = this.text.before,
            textHighlight = this.text.highlight,
            title = this.title
        )
        fun SavedLocator.toLocator() = Locator(
                href = Url(this.href)!!,
                mediaType = MediaType.invoke(this.type)!!,
                locations = Locator.Locations(
                    progression = this.progression,
                    totalProgression = this.totalProgression,
                    position = this.position
                ),
                text = Locator.Text(
                    before = this.textBefore,
                    after = this.textAfter,
                    highlight = this.textHighlight
                ),
                title = this.title
        )
    }
}
