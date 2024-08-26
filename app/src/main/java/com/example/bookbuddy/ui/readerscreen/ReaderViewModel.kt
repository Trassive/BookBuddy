package com.example.bookbuddy.ui.readerscreen

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.bookbuddy.data.readium.PublicationProvider
import com.example.bookbuddy.data.repository.implementation.ConfigurationsRepository
import com.example.bookbuddy.data.repository.interfaces.ReadiumRepository
import com.example.bookbuddy.navigation.LeafScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.shared.DelicateReadiumApi
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import javax.inject.Inject

@OptIn(ExperimentalReadiumApi::class)
@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val publicationProvider: PublicationProvider,
    private val readiumRepository: ReadiumRepository,
//    private val configurationsRepository: ConfigurationsRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.IsLoading)
    val uiState = _uiState.asStateFlow()
    private lateinit var factory: FragmentFactory

    init{
        savedStateHandle.toRoute<LeafScreen.Reader>().run{
            loadBook(this.id, this.url)
        }
    }
    @OptIn(DelicateReadiumApi::class)
    private fun loadBook(id: Int, link: String?){
        viewModelScope.launch {
            val publication = publicationProvider(readiumRepository.getBookUrl(id))

            val initialLocator = readiumRepository.getLoctor(id)

            factory = EpubNavigatorFactory(publication).createFragmentFactory(
                initialLocator = initialLocator,
                paginationListener = object: EpubNavigatorFragment.PaginationListener{
                    override fun onPageChanged(pageIndex: Int, totalPages: Int, locator: Locator) {
                        super.onPageChanged(pageIndex, totalPages, locator)
                        updateProgress(locator)
                    }
                }
            )

            _uiState.update {
                ReaderUiState.Success(
                    fragment = factory.instantiate(ClassLoader.getSystemClassLoader(),EpubNavigatorFragment::class.java.name) as EpubNavigatorFragment,
                    bookTitle = publication.metadata.title?: "Title not found",
                    readingProgression = initialLocator?.locations?.totalProgression
                ).also { state->
                    val link = link?.let{
                        Url (it)
                    }
                    if(link == null) return@also
                    state.fragment.go(
                        Link(link)
                    )
                    updateProgress(state.fragment.currentLocator.value)
                }
            }
        }
    }
    private fun updateProgress(locator: Locator){
        viewModelScope.launch {
            readiumRepository.updateProgress(locator)
            _uiState.update {
                (it as ReaderUiState.Success).copy(readingProgression = locator.locations.totalProgression)
            }
        }
    }

}

sealed interface ReaderUiState{
    data object IsLoading: ReaderUiState
    data object Error: ReaderUiState
    data class Success(
        val fragment : EpubNavigatorFragment,
        val bookTitle: String,
        val readingProgression: Double?,
    ): ReaderUiState
}