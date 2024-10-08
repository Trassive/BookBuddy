package com.example.bookbuddy.model

sealed class DownloadState {
    data class Downloading(val progress: Int) : DownloadState()
    data object Finished : DownloadState()
    data class Failed(val error: Throwable? = null) : DownloadState()
    data object Idle : DownloadState()
}