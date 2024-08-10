package com.example.bookbuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.data.DetailsRepository
import com.example.bookbuddy.data.DownloadState
import com.example.bookbuddy.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailScreenViewModel(private val detailsRepository: DetailsRepository): ViewModel() {
    private val _UiState: MutableStateFlow<DetailScreenState> = MutableStateFlow(DetailScreenState.Loading)
    val uiState = _UiState.asStateFlow()

    init{
        viewModelScope.launch {

        }
    }

}

sealed interface DetailScreenState{
    data object Loading: DetailScreenState
    data class Error(val error: List<Int>): DetailScreenState
    data class DetailView(
        val book: Book,
        val downloadState: DownloadState = DownloadState.Idle
    ): DetailScreenState
}