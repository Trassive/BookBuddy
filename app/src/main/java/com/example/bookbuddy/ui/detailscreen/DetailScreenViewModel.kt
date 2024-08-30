package com.example.bookbuddy.ui.detailscreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.bookbuddy.R
import com.example.bookbuddy.data.repository.interfaces.BookDetailsRepository
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.DownloadState
import com.example.bookbuddy.navigation.LeafScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    saveStateHandle: SavedStateHandle,
    private val detailsRepository: BookDetailsRepository
): ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    private val id: Int = saveStateHandle.toRoute<LeafScreen.BookDetail>().id
    private val _uiState: MutableStateFlow<DetailScreenState> = MutableStateFlow(DetailScreenState.Loading)
    val uiState: StateFlow<DetailScreenState> = _uiState.asStateFlow()
    init{
        getBook()
    }
    private fun getBook() {
        viewModelScope.launch{
            _uiState.update {
                try {
                    DetailScreenState.DetailView(book = detailsRepository.getBookDetails(id)).also{
                        if(it.book.isDownloaded){
                            _downloadState.update {
                                DownloadState.Finished
                            }
                        }
                    }
                } catch (e: Exception) {
                    val error: Int = if(e is IllegalArgumentException){
                        R.string.illegal_arguement
                    } else {
                        R.string.error
                    }
                    Log.d("DetailScreenViewModel", "getBook: $error")
                    DetailScreenState.Error(listOf(error))
                }
            }
            Log.d("DetailScreenViewModel", "getBook: ${_uiState.value} $id")
        }
    }

    fun downloadBook(){
        _downloadState.update {
            DownloadState.Downloading(0)
        }
        viewModelScope.launch {
            detailsRepository.downloadBook((uiState.value as DetailScreenState.DetailView).book).collectLatest { state->
                _downloadState.update {
                    state
                }
                delay(20)
                Log.d("DownloadButton", "ViewModel DownloadState: ${downloadState.value}")
                if(state is DownloadState.Finished || state is DownloadState.Failed) getBook()
            }
        }
    }
    fun toggleBookState(){
        viewModelScope.launch {
            with((uiState.value as? DetailScreenState.DetailView)!!.book){
                if (this.isDownloaded) {
                    detailsRepository.deleteBook(this.id)
                    _uiState.update {
                        DetailScreenState.DetailView(this.copy(isDownloaded = false, isSaved = false))
                    }
                    _downloadState.update { DownloadState.Idle }
                } else if (this.isSaved) {
                    detailsRepository.unSaveBook(this.id)
                } else{
                    detailsRepository.saveBook(this)
                }
            }
            getBook()
        }
    }
}

sealed interface DetailScreenState{
    data object Loading: DetailScreenState
    data class Error(val error: List<Int>): DetailScreenState
    data class DetailView(val book: Book ): DetailScreenState
}