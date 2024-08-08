package com.example.bookbuddy.data.remote

import com.example.bookbuddy.network.BookDetailsApi

class BookDetailsRepository(private val bookDetailsApi: BookDetailsApi) {
    suspend fun getBookDetails(title: String, author: String): String {

        return bookDetailsApi.getDescription(generateBooksApiQuery(title,author))
            .items
            .volumeInfo
            .description
    }
    private fun generateBooksApiQuery(bookName: String, authorName: String): String {
        // Encode parameters to ensure proper URL formatting
        val encodedBookName = java.net.URLEncoder.encode(bookName, "UTF-8")
        val encodedAuthorName = java.net.URLEncoder.encode(authorName, "UTF-8")

        // Construct the query
        val query = "q=$encodedBookName+inauthor:$encodedAuthorName"

        return query
    }

}