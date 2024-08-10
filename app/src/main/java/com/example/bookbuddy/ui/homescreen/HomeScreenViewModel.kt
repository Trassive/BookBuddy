package com.example.bookbuddy.ui.homescreen

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.R
import com.example.bookbuddy.data.Filter
import com.example.bookbuddy.data.HomeFeedRepository
import com.example.bookbuddy.model.Book
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import kotlin.math.min

class HomeScreenViewModel(private val homeFeedRepository: HomeFeedRepository): ViewModel() {
    private val _UiState:MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.isLoading)
    val uiState = _UiState.asStateFlow()

    private var previous: String? = null
    private var next: String? = null
    private var retryJob: Job? = null
    private var previousLoadedBooks: List<Book> = listOf()
//    check IOdispatcher
    init{
        viewModelScope.launch{
            getBooks()
        }
    }

    private suspend fun getBooks(){
        _UiState.update {
            try {
                val books = homeFeedRepository.getBooks()
                HomeUiState.HomeView(
                    carauselBooks = books.books.subList(0, 5),
                    bookList = books.books.drop(5),
                ).also {
                    previous = books.previous
                    next = books.next 
                }
            } catch (e: Exception) {
                if (e is IOException) {
                    HomeUiState.Error(listOf(R.string.network_Error))
                } else {
                    HomeUiState.Error(listOf(R.string.error))
                }
            }
        }

    }
    fun toggleSearchState(){
        _UiState.update {currentState->
            when(currentState){
                is HomeUiState.HomeView -> {
                    previousLoadedBooks = currentState.carauselBooks + currentState.bookList
                    HomeUiState.SearchView(searchText = "")
                }
                else -> {
                    HomeUiState.HomeView(
                        carauselBooks = previousLoadedBooks.subList(
                            0,
                            min(5, previousLoadedBooks.size)
                        ),
                        bookList = previousLoadedBooks.drop(min(5, previousLoadedBooks.size))
                    )
                }
            }
        }
    }
    fun isPagingAvailable(page: Page) = if(page == Page.PREVIOUS) !previous.isNullOrEmpty() else !next.isNullOrEmpty()
    fun getAnotherPage(page: Page){

        viewModelScope.launch{
            _UiState.update {
                try {
                    val books = (if (page == Page.PREVIOUS) previous else next)!!.let { it ->
                        homeFeedRepository.updateBooks(it)
                    }
                    previous = books.previous
                    next = books.next
                    HomeUiState.HomeView(
                        carauselBooks = books.books.subList(0, min(5, books.books.size)),
                        bookList = books.books.drop(min(5, books.books.size))
                    )
                } catch (e: Exception) {
                    if (e is IOException) {
                        HomeUiState.Error(listOf(R.string.network_Error))
                    } else {
                        HomeUiState.Error(listOf(R.string.error))
                    }
                }
            }
        }
    }

    fun onSearchClick(){
        val query = (_UiState.value as? HomeUiState.SearchView)!!.searchText
        if(query.isNotEmpty()){
            viewModelScope.launch {
                _UiState.update {currentState->
                    try {
                        val books = homeFeedRepository.getBooks(Filter.SEARCH to query)
                        HomeUiState.SearchView(searchText = query, bookList = books.books).also {
                            previous = books.previous
                            next = books.next
                        }
                    } catch (e: Exception) {
                        if (e is IOException) {
                            HomeUiState.Error(listOf(R.string.network_Error))
                        } else {
                            HomeUiState.Error(listOf(R.string.error))
                        }
                    }
                }
            }
        }
    }
    fun onSearchQueryChange(query: String){
        _UiState.update {
            HomeUiState.SearchView(searchText = query)
        }
    }
    fun retry(){

        if(retryJob?.isActive != true){
            retryJob = viewModelScope.launch{
                getBooks()
            }
        }
    }
    fun messageDismissed(){
        _UiState.update {
            HomeUiState.Error(error = (it as HomeUiState.Error).error.drop(1))
        }
    }
    fun addMessage(@StringRes id: Int){
        _UiState.update {
            HomeUiState.Error(error = (it as HomeUiState.Error).error + id)
        }
    }
}
sealed interface HomeUiState{
    data object isLoading: HomeUiState
    data class HomeView(
        val carauselBooks: List<Book> = listOf(),
        val bookList: List<Book> = listOf(),
    ): HomeUiState
    data class SearchView(
        val searchText: String,
        val bookList: List<Book> = listOf(),
    ): HomeUiState
    data class Error(val error: List<Int>): HomeUiState
}
enum class Page{
    NEXT,
    PREVIOUS
}