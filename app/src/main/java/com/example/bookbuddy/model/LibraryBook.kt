package com.example.bookbuddy.model

//Book for library screen
data class LibraryBook(
    val id: Int,
    val title: String,
    val categories: List<String>,
    val authors: List<String>,
    val coverImage: String,
    val isDownloaded: Boolean,
)
