package com.example.bookbuddy.ui.readerscreen

import android.util.Log
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.withStarted
import androidx.navigation.toRoute
import com.example.bookbuddy.data.readium.PublicationProvider
import com.example.bookbuddy.data.repository.implementation.ConfigurationsRepository
import com.example.bookbuddy.data.repository.interfaces.ReadiumRepository
import com.example.bookbuddy.navigation.LeafScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
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
    private val configurationsRepository: ConfigurationsRepository
): ViewModel() {
    private val id: Int = savedStateHandle.toRoute<LeafScreen.Reader>().id
    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.IsLoading)
    val uiState = _uiState.asStateFlow()
    private lateinit var factory: FragmentFactory
    private var link: String? = null
    init{
        savedStateHandle.toRoute<LeafScreen.Reader>().run{
            link = this.url
            loadBook(this.id)
        }
    }
    private fun loadBook(id: Int){
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
                },
                initialPreferences = EpubPreferences().copy(scroll = configurationsRepository.isScrollEnabled.first())
            )

            _uiState.update {
                ReaderUiState.Success(
                    fragment = factory.instantiate(ClassLoader.getSystemClassLoader(),EpubNavigatorFragment::class.java.name) as EpubNavigatorFragment,
                    bookTitle = publication.metadata.title?: "Title not found",
                    readingProgression = initialLocator?.locations?.totalProgression
                )
            }
        }
    }
    @OptIn(DelicateReadiumApi::class)
    fun onViewInflated(){
        val url = link?.let{link ->
            Url(link)
        }
        if(url == null) return

        viewModelScope.launch{
            val fragment = (_uiState.value as? ReaderUiState.Success)?.fragment ?: return@launch
            fragment.lifecycle.withStarted {
                val boolean = fragment.go(link = Link(url), animated = true)
                Log.d("ReaderViewModel", "onViewInflated: $boolean")
            }
        }
    }

    private fun updateProgress(locator: Locator){
        viewModelScope.launch {
            readiumRepository.updateProgress(id, locator)
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