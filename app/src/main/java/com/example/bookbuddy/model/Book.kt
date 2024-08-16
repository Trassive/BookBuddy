package com.example.bookbuddy.model

import com.example.bookbuddy.network.RemoteBook

//Book with additional data
data class Book(
    override val id: Int,
    override val title: String,
    override val categories: List<String>,
    override val authors: List<String>,
    override val coverImage: String,
    val description: String,
    val downloadLink: String,
    val isDownloaded: Boolean,
    val isSaved: Boolean
): BaseBook
fun RemoteBook.toBook(description: String, isDownloaded: Boolean = false, isSaved: Boolean = false): Book {
    return Book(
        id = this.id,
        title = this.title,
        categories = this.categories,
        authors = this.authors.map{ it.name},
        coverImage = this.formats.cover,
        description = description,
        downloadLink = this.formats.epub,
        isDownloaded = isDownloaded,
        isSaved = false
    )
}