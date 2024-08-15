package com.example.bookbuddy.ui.tableofcontent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.readium.r2.shared.publication.Link

class ContentViewModel(
    savedStateHandle: SavedStateHandle,

): ViewModel() {
    lateinit var tableOfContent: List<Link>
}