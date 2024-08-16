package com.example.bookbuddy.model

//Book for library screen
data class LibraryBook(
    override val id: Int,
    override val title: String,
    override val categories: List<String>,
    override val authors: List<String>,
    override val coverImage: String,
    val isDownloaded: Boolean,
): BaseBook
