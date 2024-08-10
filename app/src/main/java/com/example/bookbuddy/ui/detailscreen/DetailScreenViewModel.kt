package com.example.bookbuddy.ui.detailscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.data.DetailsRepository
import com.example.bookbuddy.data.DownloadState
import com.example.bookbuddy.data.SavedBook
import com.example.bookbuddy.model.Book
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

class DetailScreenViewModel(
    saveStateHandle: SavedStateHandle,
    private val detailsRepository: DetailsRepository
): ViewModel() {
    private val id: Int = checkNotNull(saveStateHandle["//TODO"])
    val uiState: StateFlow<DetailScreenState> = getBook().stateIn(
        scope = viewModelScope,
        initialValue = DetailScreenState.Loading,
        started = SharingStarted.WhileSubscribed(5000)
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getBook(): Flow<DetailScreenState> = flow {
        emit(DetailScreenState.Loading)
        try {
            val book = detailsRepository.getBook(id).transformLatest<Book,DetailScreenState> {book->
                emit(DetailScreenState.DetailView(book = book))
            }
        } catch (e: Exception) {
            emit(DetailScreenState.Error(listOf()))
        }
    }

    fun downloadBook(): StateFlow<DownloadState> = flow<DownloadState>{
        detailsRepository.downloadBook((uiState.value as? DetailScreenState.DetailView)!!.book)
    }.stateIn(
        scope = viewModelScope,
        initialValue = DownloadState.Idle,
        started = SharingStarted.WhileSubscribed(5000)
    )
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
        }
    }
}

sealed interface DetailScreenState{
    data object Loading: DetailScreenState
    data class Error(val error: List<Int>): DetailScreenState
    data class DetailView(val book: Book ): DetailScreenState
}