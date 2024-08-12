package com.example.bookbuddy.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.data.LibraryRepository
import com.example.bookbuddy.model.Book
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn

class LibraryScreenViewModel(private val libraryRepository: LibraryRepository): ViewModel() {
    val uiState: StateFlow<LibraryUiState> = combineTransform(flow = libraryRepository.getSavedBooks(),flow2 = libraryRepository.getDownloadedBooks()){ savedBooks, downloadedBooks->
        emit(LibraryUiState(savedTabBooks = savedBooks,downloadedTabBooks = downloadedBooks))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

}
data class LibraryUiState(
    val savedTabBooks: List<Book> = listOf(),
    val downloadedTabBooks: List<Book> = listOf()
)