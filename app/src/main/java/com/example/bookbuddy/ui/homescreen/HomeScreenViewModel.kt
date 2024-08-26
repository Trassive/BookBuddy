package com.example.bookbuddy.ui.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.R
import com.example.bookbuddy.data.repository.interfaces.BookCatalogueRepository
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.ui.util.isOneOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
        viewModelScope.launch{
            _uiState.update { state->
                try {
                    val books = bookCatalogueRepository.getCatalogue()
                    HomeUiState.HomeView(
                        carouselBooks = books.subList(0,7),
                        bookList = books.drop(7),
                        isLoading = false
                    )
                } catch (e: Exception) {
                    val currError = if (e is IOException) {
                        listOf(R.string.network_Error)
                    } else {
                        listOf(R.string.error)
                    }
                    val errors = ((state as? HomeUiState.Error)?.error?.plus(currError)) ?: currError
                    HomeUiState.Error(error = errors)
                }
            }
        }
    }
    fun toggleSearchState(){
        _uiState.update { currentState->
            when(currentState){
                is HomeUiState.HomeView -> {
                    previousLoadedBooks = currentState.carouselBooks + currentState.bookList
                    HomeUiState.SearchView()
                }
                else -> {
                    HomeUiState.HomeView(
                        carouselBooks = previousLoadedBooks.subList(7, min(7, previousLoadedBooks.size)),
                        bookList = previousLoadedBooks.drop(min(7, previousLoadedBooks.size)),
                        isLoading = false
                    )
                }
            }
        }
    }


    fun onSearchClick(){
        viewModelScope.launch{
            val query = (_uiState.value as? HomeUiState.SearchView)!!.searchText
            if(query.isBlank()) return@launch
            _uiState.update { currentState ->
                try {
                    val books = bookCatalogueRepository.getCatalogue(query)
                    HomeUiState.HomeView(
                        carouselBooks = books.subList(0,7),
                        bookList = books.drop(7),
                        isLoading = false
                    )
                } catch (e: Exception) {
                    val currError = if (e.isOneOf(IOException::class, CancellationException::class) ) {
                        listOf(R.string.network_Error)
                    } else {
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
        _uiState.update {
            HomeUiState.Error(error = (it as HomeUiState.Error).error.drop(1))
        }
    }

    fun updateBooks(){
        viewModelScope.launch {
            _uiState.update {currentState->
                (currentState as? HomeUiState.HomeView)?.copy(isLoading = true)?: currentState
            }
        }
        viewModelScope.launch {
            _uiState.update { currentState ->
                (currentState as? HomeUiState.HomeView)?.copy(
                    bookList = currentState.bookList + bookCatalogueRepository.updateCatalogue()
                        .first(),
                    isLoading = false
                )?:currentState
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
        val isLoading: Boolean = true,
        val isSearching: Boolean = false
    ): HomeUiState
    data class Error(val error: List<Int>): HomeUiState

}
