package com.example.bookbuddy.model

data class Books(
    val count: Int,
    val next: String?,
    val previous: String?,
    val books: List<Book>
)
data class Book(
    val id: Int,
    val title: String,
    val categories: List<String>,
    val authors: List<String>,
    val cover: String,
    val description: String? =null,
    val downloadLink: String,
    val isDownloaded: Boolean,
    val isSaved: Boolean
)
