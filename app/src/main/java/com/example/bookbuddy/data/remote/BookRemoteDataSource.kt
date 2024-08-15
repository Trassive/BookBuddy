package com.example.bookbuddy.data.remote

import com.example.bookbuddy.network.BookDetailsApi
import com.example.bookbuddy.network.BookMetadata
import com.example.bookbuddy.network.BooksApi
import com.example.bookbuddy.network.RemoteBookList
import okhttp3.ResponseBody
import retrofit2.Response

class BookRemoteDataSource(
    private val booksApi: BooksApi,
    private val booksDetailsApi: BookDetailsApi
) {

    suspend fun getBooks(queries: Map<String,String>?): RemoteBookList{
     return booksApi.getBooks(queries = queries)
    }

    suspend fun getBooks(url: String): RemoteBookList{
    return booksApi.getBooks(url = url)
    }
    suspend fun downloadBooks(url: String): Response<ResponseBody>{
     return booksApi.downloadBook(url)
    }

    suspend fun getBook(id: Int): RemoteBookList {
     return booksApi.getBooks(queries = mapOf("ids" to id.toString()))
    }

    suspend fun getAdditionalMetadata(query: String): BookMetadata {
        return booksDetailsApi.getDescription(query)
    }
}
