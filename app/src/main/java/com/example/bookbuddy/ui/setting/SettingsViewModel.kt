package com.example.bookbuddy.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuddy.data.repository.implementation.ConfigurationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configurationsRepository: ConfigurationsRepository
): ViewModel() {
    val uiState: StateFlow<SettingsUiState> = configurationsRepository.isScrollEnabled.map{
        SettingsUiState(
            isScrollEnabled = it
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(false)
    )
    fun onScrollToggle(boolean: Boolean){
        viewModelScope.launch{
            configurationsRepository.updateScroll(boolean)
        }
    }
}
data class SettingsUiState(
    val isScrollEnabled: Boolean
)