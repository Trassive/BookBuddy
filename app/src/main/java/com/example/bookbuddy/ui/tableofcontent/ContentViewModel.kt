package com.example.bookbuddy.ui.tableofcontent

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.bookbuddy.data.readium.PublicationProvider
import com.example.bookbuddy.data.repository.interfaces.ReadiumRepository
import com.example.bookbuddy.navigation.LeafScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Link
import javax.inject.Inject

@HiltViewModel
class ContentViewModel @Inject constructor(
    readerRepository: ReadiumRepository,
    savedStateHandle: SavedStateHandle,
    publicationProvider: PublicationProvider
): ViewModel() {
    private val _uiState = MutableStateFlow<TableOfContentUiState>(TableOfContentUiState.IsLoading)
    val uiState = _uiState.asStateFlow()
    private val id: Int = savedStateHandle.toRoute<LeafScreen.TableOfContent>().id
    init{
        viewModelScope.launch {
            val publication = publicationProvider(readerRepository.getBookUrl(id))
            _uiState.update {
                TableOfContentUiState.Loaded(
                    id,
                    publication.metadata.title?:"No title found",
                    publication.tableOfContents
                )
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        Log.d("ReaderViewModel toc" , "onCleared")
    }
}
sealed interface TableOfContentUiState {
    data object IsLoading: TableOfContentUiState
    data class Loaded(val id: Int, val title: String, val tableOfContent: List<Link> ): TableOfContentUiState
    data object Error: TableOfContentUiState
}
fun Link.String() = this.url().toString()
