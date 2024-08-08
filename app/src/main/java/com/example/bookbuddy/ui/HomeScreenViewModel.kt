package com.example.bookbuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.data.HomeFeedRepository
import com.example.bookbuddy.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException

class HomeScreenViewModel(private val homeFeedRepository: HomeFeedRepository): ViewModel() {
    private val _UiState:MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Loading)
    val uiState = _UiState.asStateFlow()
    private var previous: String? = null
    private var next: String? = null
//    check IOdispatcher
    init{
        viewModelScope.launch{
            _UiState.update {
                try {
                    val books = homeFeedRepository.getBooks()
                    HomeUiState.HomeView(
                        carauselBooks = books.books.subList(0,5),
                        bookList = books.books.drop(5)
                    )
                } catch (e: Exception){
                    if(e is IOException){
                        HomeUiState.Error(listOf("Network connection error. Try again later"))
                    } else{
                        HomeUiState.Error(listOf(e.message?:""))
                    }
                }
            }
        }
    }
}
sealed interface HomeUiState{
    data object Loading: HomeUiState
    data class HomeView(
        val carauselBooks: List<Book>,
        val bookList: List<Book>
    ): HomeUiState
    data class SearchView(
        val searchText: String,
        val BookList: List<Book> = listOf()
    ): HomeUiState
    data class Error(val error: List<String>): HomeUiState
}