package com.example.bookbuddy.model

import com.example.bookbuddy.network.RemoteBook

//Book with additional data
data class Book(
    val id: Int,
    val title: String,
    val categories: List<String>,
    val authors: List<String>,
    val cover: String,
    val description: String,
    val downloadLink: String,
    val isDownloaded: Boolean,
    val isSaved: Boolean
)
fun RemoteBook.toBook(description: String, isDownloaded: Boolean = false, isSaved: Boolean = false): Book {
    return Book(
        id = this.id,
        title = this.title,
        categories = this.categories,
        authors = this.authors.map{ it.name},
        cover = this.formats.cover,
        description = description,
        downloadLink = this.formats.epub,
        isDownloaded = isDownloaded,
        isSaved = false
    )
}