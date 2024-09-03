package com.example.bookbuddy.ui.homescreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.R
import com.example.bookbuddy.data.exception.OutOfDataException
import com.example.bookbuddy.data.repository.interfaces.BookCatalogueRepository
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.Update
import com.example.bookbuddy.ui.util.isOneOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class HomeScreenViewModel @Inject constructor(private val bookCatalogueRepository: BookCatalogueRepository): ViewModel() {
    private val _uiState:MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.IsLoading)
    val uiState = _uiState.asStateFlow()

    private var retryJob: Job? = null
    private var previousLoadedBooks: List<Book> = listOf()

    init{
        getBooks()
    }

    private fun getBooks(){
        viewModelScope.launch {
            try{
                bookCatalogueRepository.getCatalogue().collect { books ->
                    previousLoadedBooks = previousLoadedBooks.plus(books)
                if (_uiState.value is HomeUiState.SearchView) return@collect
                    _uiState.update {
                        ensureActive()
                        HomeUiState.HomeView(
                            carouselBooks = previousLoadedBooks.take(
                                min(
                                    7,
                                    previousLoadedBooks.size
                                )
                            ),
                            bookList = previousLoadedBooks.drop(
                                min(
                                    7,
                                    previousLoadedBooks.size
                                )
                            ),
                            isLoading = true
                        )
                    }
                }
            } catch(e: Exception) {
                Log.d("HomeScreenViewModel", "getBooks: ${e.message} ${e.cause} ")
                val error = if (e.isOneOf(IOException::class, CancellationException::class)) {
                    listOf(R.string.network_Error)
                } else {
                    listOf(R.string.unknown_error)
                }
                val previousError: MutableList<Int> = (_uiState.value as? HomeUiState.Error)?.error?.toMutableList()?: mutableListOf()
                _uiState.update{ HomeUiState.Error(error = previousError + error) }
                Log.d("HomeScreenViewModel", "getBooks: ${_uiState.value} ")
            }
        }
    }
    fun toggleSearchState(){
        _uiState.update { currentState->
            when(currentState){
                is HomeUiState.HomeView, HomeUiState.IsLoading -> {
                    HomeUiState.SearchView()
                }
                is HomeUiState.SearchView -> {
                    HomeUiState.HomeView(
                        carouselBooks = previousLoadedBooks.take( min(7, previousLoadedBooks.size)),
                        bookList = previousLoadedBooks.drop(min(7, previousLoadedBooks.size)),
                        isLoading = false
                    )
                }
                else -> currentState
            }
        }
    }


    fun onSearchClick(){
        viewModelScope.launch{
            val query = (_uiState.value as? HomeUiState.SearchView)?.searchText
            if(query.isNullOrEmpty()) return@launch

            _uiState.update {
                (it as HomeUiState.SearchView).copy(isSearching = true)
            }

            try {
                bookCatalogueRepository.getCatalogue(query).collect{books->
                    if(_uiState.value !is HomeUiState.SearchView) return@collect
                    _uiState.update {
                        HomeUiState.SearchView(
                            searchText = query,
                            bookList = (it as HomeUiState.SearchView).bookList.plus(books),
                            isLoading = false,
                            isSearching = false
                        )
                    }
                }
            } catch (e: Exception) {
                val currError = if (e.isOneOf(IOException::class, CancellationException::class) ) {
                    listOf(R.string.network_Error)
                } else if (e is OutOfDataException){
                    listOf(R.string.no_books_found)
                }else {
                    listOf(R.string.unknown_error)
                }
                val errors = ((_uiState.value as? HomeUiState.Error)?.error?.plus(currError)) ?: currError

                _uiState.update { HomeUiState.Error(error = errors) }
            }
        }
    }
    fun onSearchQueryChange(query: String){
        _uiState.update {
            HomeUiState.SearchView(searchText = query)
        }
    }
    fun retry(){
        messageDismissed()
        if(retryJob?.isActive!= true) return
        else retryJob = viewModelScope.launch {
            getBooks()
        }
    }
    fun messageDismissed(){
        _uiState.update {currentState->
            (currentState as HomeUiState.Error).copy(error = currentState.error.drop(1))
        }
    }

    fun updateBooks(){
        var isHome = false
        viewModelScope.launch {
            _uiState.update {currentState->
                (currentState as? HomeUiState.HomeView)?.copy(isLoading = true).also{ isHome = true} ?:
                (currentState as HomeUiState.SearchView).copy(isLoading = true).also{ isHome = false}
            }
        }
        viewModelScope.launch {
            val books = try{
                bookCatalogueRepository.updateCatalogue(
                    if(isHome) Update.HOME else Update.SEARCH
                ).first()
            }catch (e:Exception){
                listOf()
            }
            _uiState.update { currentState ->
                (currentState as? HomeUiState.HomeView)
                    ?.copy(bookList = currentState.bookList + books, isLoading = false)?.also { previousLoadedBooks+= it.bookList } ?:

                (currentState as HomeUiState.SearchView)
                    .copy( bookList = currentState.bookList + books, isLoading = false)
            }
        }
    }
    fun updateBottomSheetBook(index: Int){
        viewModelScope.launch{
            getBookAt(index)?.let {
                if (bookCatalogueRepository.isSaved(it.id)) {
                    val updatedBook = it.copy(isSaved = true)
                    Log.d("HomeScreenViewModel", "updateBottomSheetBook: $updatedBook")
                    _uiState.update {state->
                        (state as? HomeUiState.HomeView)?.copy(bookList = state.bookList.updateAt(index) { updatedBook })
                            ?: (state as HomeUiState.SearchView).copy(bookList = state.bookList.updateAt(index) { updatedBook })
                    }
                }
            }
        }
    }

    fun toggleSave(book: Book, position: Int){
        viewModelScope.launch {
            val updatedBook = book.copy(isSaved = !book.isSaved)

            if (book.isSaved) {
                bookCatalogueRepository.unSaveBook(book.id)
            } else {
                bookCatalogueRepository.saveBook(book)
            }

            _uiState.update {
                (it as? HomeUiState.HomeView)?.copy(bookList = it.bookList.updateAt(position) { updatedBook })?:
                (it as HomeUiState.SearchView).copy(bookList = it.bookList.updateAt(position) { updatedBook })
            }
            Log.d("HomeScreenViewModel", "toggleSave: ${(uiState.value as HomeUiState.HomeView).bookList[position]} ")
        }
    }
    private fun List<Book>.updateAt(position: Int, update: Book.() -> Book): List<Book> =
        toMutableList().apply { this[position] = this[position].update() }

    private fun getBookAt(index: Int): Book? =
        (_uiState.value as? HomeUiState.HomeView)?.bookList?.getOrNull(index) ?:
        (_uiState.value as? HomeUiState.SearchView)?.bookList?.getOrNull(index)

}

sealed interface HomeUiState{
    data object IsLoading: HomeUiState
    data class HomeView(
        val carouselBooks: List<Book> = listOf(),
        val bookList: List<Book> = listOf(),
        val isLoading: Boolean = false
    ): HomeUiState
    data class SearchView(
        val searchText: String = "",
        val bookList: List<Book> = listOf(),
        val isLoading: Boolean = false,
        val isSearching: Boolean = false
    ): HomeUiState
    data class Error(val error: List<Int>): HomeUiState

}
