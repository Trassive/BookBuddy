package com.example.bookbuddy.ui.homescreen

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
            val result = runCatching {
                bookCatalogueRepository.getCatalogue().also { previousLoadedBooks = it }
            }.getOrElse { e ->
                val error = if (e.isOneOf(IOException::class, CancellationException::class)) {
                    listOf(R.string.network_Error)
                } else {
                    listOf(R.string.unknown_error)
                }

                val previousError: MutableList<Int> = (_uiState.value as? HomeUiState.Error)?.error?.toMutableList()?: mutableListOf()
                HomeUiState.Error(error = previousError + error)
            }


            if (_uiState.value is HomeUiState.SearchView) return@launch

            _uiState.update {
                when (result) {
                    is HomeUiState.Error -> result
                    else -> {
                        val books = (result as? List<Book> )!!
                        HomeUiState.HomeView(
                            carouselBooks = books.take(7),
                            bookList = books.drop(7),
                            isLoading = false
                        )
                    }
                }
            }

            ensureActive()
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
                (it as? HomeUiState.SearchView)?.copy(isSearching = true)?: it
            }
            _uiState.update { currentState ->
                try {
                    val books = bookCatalogueRepository.getCatalogue(query)
                    if(books.isEmpty()) throw OutOfDataException("No books found")
                    HomeUiState.SearchView(
                        searchText = query,
                        bookList = books,
                        isSearching = false
                    )
                } catch (e: Exception) {
                    val currError = if (e.isOneOf(IOException::class, CancellationException::class) ) {
                        listOf(R.string.network_Error)
                    } else if (e is OutOfDataException){
                        listOf(R.string.no_books_found)
                    }else {
                        listOf(R.string.unknown_error)
                    }
                    val errors = ((currentState as? HomeUiState.Error)?.error?.plus(currError)) ?: currError
                    HomeUiState.Error(error = errors)
                }
            }
        }
    }
    fun onSearchQueryChange(query: String){
        _uiState.update {
            HomeUiState.SearchView(searchText = query)
        }
    }
    fun retry(){
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
        viewModelScope.launch {
            _uiState.update {currentState->
                (currentState as? HomeUiState.HomeView)?.copy(isLoading = true) ?:
                (currentState as HomeUiState.SearchView).copy(isLoading = true)
            }
        }
        viewModelScope.launch {
            val books = try{
                bookCatalogueRepository.updateCatalogue(Update.SEARCH).first()
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
