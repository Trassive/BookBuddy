package com.example.bookbuddy.ui.detailscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.R
import com.example.bookbuddy.data.repository.interfaces.BookDetailsRepository
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.DownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    saveStateHandle: SavedStateHandle,
    private val detailsRepository: BookDetailsRepository
): ViewModel() {
    private val id: Int = checkNotNull(saveStateHandle["//TODO"])
    private val _uiState: MutableStateFlow<DetailScreenState> = MutableStateFlow(DetailScreenState.Loading)
    val uiState: StateFlow<DetailScreenState> = _uiState.asStateFlow()
    init{
        getBook()
    }
    private fun getBook() {
        viewModelScope.launch{
            _uiState.update {
                try {
                    DetailScreenState.DetailView(book = detailsRepository.getBookDetails(id))
                } catch (e: Exception) {
                    val error: Int = if(e is IllegalArgumentException){
                        R.string.illegal_arguement
                    } else {
                        R.string.error
                    }
                    DetailScreenState.Error(listOf(error))
                }
            }
        }
    }

    fun downloadBook(): StateFlow<DownloadState> = flow<DownloadState>{

        detailsRepository.downloadBook((uiState.value as? DetailScreenState.DetailView)!!.book)
    }.map{state ->
        if(state is DownloadState.Failed){
            getBook()
        }
        state
    }.stateIn(
        scope = viewModelScope,
        initialValue = DownloadState.Idle,
        started = SharingStarted.WhileSubscribed(5000)
    ).also{
        getBook()
    }
    fun toggleBookState(){
        viewModelScope.launch {
            with((uiState.value as? DetailScreenState.DetailView)!!.book){
                if (this.isDownloaded) {
                    detailsRepository.deleteBook(this.id)
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