package com.example.bookbuddy.data

import com.example.bookbuddy.data.local.BooksLocalRepository
import com.example.bookbuddy.data.remote.BookDetailsRepository
import com.example.bookbuddy.data.remote.BooksRemoteRepository
import com.example.bookbuddy.data.remote.InternalDownloadState
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.Books
import com.example.bookbuddy.network.RemoteBookList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import java.net.URL

interface HomeFeedRepository{
    suspend fun getBooks(): Books
    suspend fun updateBooks(url: String): Books
    suspend fun getBooks(vararg filters: Pair<Filter,String>): Books
//    suspend fun saveBook(book: Book)
//    suspend fun deleteBook(id: Int)
}
interface DetailsRepository{
    suspend fun getBookDetails(book: Book): Book
    suspend fun downloadBook(book: Book): Flow<DownloadState>
    suspend fun deleteBook(id: Int)
    suspend fun saveBook(book: Book)
}
interface LibraryRepository{
    suspend fun getSavedBooks(): Flow<List<Book>>
    suspend fun getDownloadedBooks(): Flow<List<Book>>
    suspend fun deleteBook(id: Int)
}
interface ReadiumBookRepository{
    suspend fun updateProgress()
    suspend fun bookProvider(id: Int): Book
}
class BooksRepository(
    private val booksRemoteRepository: BooksRemoteRepository,
    private val bookDetailsRepository: BookDetailsRepository,
    private val booksLocalRepository: BooksLocalRepository
): HomeFeedRepository, LibraryRepository, DetailsRepository{
    override suspend fun getBooks(): Books {
        return booksRemoteRepository.getBooksList().toBooks()
    }

    override suspend fun getBooks(vararg filters: Pair<Filter,String>): Books {
        return booksRemoteRepository.getBooksList(
            search = filters.find { it.first == Filter.SEARCH }?.second,
            topic = filters.find { it.first== Filter.TOPIC }?.second
        ).toBooks()
    }
    override suspend fun updateBooks(url: String): Books{
        return booksRemoteRepository.getBooksList(url = url).toBooks()
    }

    override suspend fun saveBook(book: Book) {
        val description = bookDetailsRepository.getBookDetails(title = book.title, author = book.authors[0])
        booksLocalRepository.saveBook(book.toSavedBook(false, description))
       }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getSavedBooks(): Flow<List<Book>> {
        return booksLocalRepository.getSavedBook(false)
            .distinctUntilChanged()
            .flatMapLatest {savedBooks->
                flow{
                    emit(savedBooks.map { it.toBook(false) } )
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getDownloadedBooks(): Flow<List<Book>> {
        return booksLocalRepository.getSavedBook(false)
            .distinctUntilChanged()
            .flatMapLatest {savedBooks->
                flow{
                    emit(savedBooks.map { it.toBook(true) } )
                }
            }
    }

    override suspend fun deleteBook(id: Int) {
        booksLocalRepository.deleteBook(id)
    }

    override suspend fun getBookDetails(book: Book): Book {
        return book.copy(
            description = bookDetailsRepository.getBookDetails(title = book.title, author = book.authors[0])
        )
    }

    override suspend fun downloadBook(book: Book): Flow<DownloadState> {
        return if(!booksLocalRepository.isDownloaded(book.id)){
            booksRemoteRepository.downloadBook(book.downloadLink, book.id).map { internalDownloadState->
                when(internalDownloadState){
                    is InternalDownloadState.Downloading-> DownloadState.Downloading(internalDownloadState.progress)
                    is InternalDownloadState.Failed -> DownloadState.Failed(internalDownloadState.error)
                    is InternalDownloadState.Finished -> {
                        booksLocalRepository.saveBook(book.copy(downloadLink = internalDownloadState.filePath).toSavedBook(true,book.description!!))
                        DownloadState.Finished
                    }
                }
            }
        } else{
            flowOf(DownloadState.Finished)
        }
    }
}
private fun RemoteBookList.toBooks(): Books{

    val books = this.books.map { book->
        Book(
            id = book.id,
            title = book.title,
            categories = book.categories,
            authors = book.authors.map { it.name },
            cover = book.formats.cover,
            downloadLink = book.formats.epub,
            isDownloaded = false
        )

    }
    return Books(
        count = this.count,
        next = this.next,
        previous = this.previous,
        books = books
    )
}
private fun Book.toSavedBook(isDownloaded: Boolean, description: String): SavedBook{
    return SavedBook(
        id = this.id,
        title = this.title,
        categories = this.categories.joinToString(),
        authors = this.authors.joinToString(),
        isDownloaded = isDownloaded,
        coverImage = this.cover,
        fileUrl = this.downloadLink,
        description = description
    )
}
private fun SavedBook.toBook(isDownloaded: Boolean): Book{
    return Book(
        id = this.id,
        title = this.title,
        categories = this.categories.split(","),
        authors = this.authors.split(","),
        cover = this.coverImage,
        downloadLink = this.fileUrl,
        description = this.description,
        isDownloaded = isDownloaded
    )
}
enum class Filter(title: String){
    TOPIC(title = "topic"),
    SEARCH(title = "search")
}
sealed class DownloadState {
    data class Downloading(val progress: Int) : DownloadState()
    object Finished : DownloadState()
    data class Failed(val error: Throwable? = null) : DownloadState()
}
