package com.example.bookbuddy.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.data.repository.interfaces.OfflineBookRepository
import com.example.bookbuddy.model.LibraryBook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryScreenViewModel(private val offlineBookRepository: OfflineBookRepository): ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private fun getBooks() {
        viewModelScope.launch{
            combine(
                flow = offlineBookRepository.getSavedBooks(),
                flow2 = offlineBookRepository.getDownloadedBooks()
            ) { savedBooks, downloadedBooks ->
                LibraryUiState(savedTabBooks = savedBooks, downloadedTabBooks = downloadedBooks)
            }.distinctUntilChanged()
            .collectLatest {state->
                _uiState.update {
                    LibraryUiState(state.savedTabBooks, state.downloadedTabBooks)
                }
            }
        }
    }

}
data class LibraryUiState(
    val savedTabBooks: List<LibraryBook> = listOf(),
    val downloadedTabBooks: List<LibraryBook> = listOf()
)