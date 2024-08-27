package com.example.bookbuddy.data.remote

import android.util.Log
import com.example.bookbuddy.network.BookDetailsApi
import com.example.bookbuddy.network.BookMetadata
import com.example.bookbuddy.network.BooksApi
import com.example.bookbuddy.network.RemoteBookList
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class BookRemoteDataSource @Inject constructor(
    private val booksApi: BooksApi,
    private val booksDetailsApi: BookDetailsApi
) {

    suspend fun getBooks(queries: Map<String,String>): RemoteBookList{
        Log.d("BookRemoteDataSource", "getBooks: $queries")
        return booksApi.getBooks(queries = queries)
    }

    suspend fun getBooks(url: String): RemoteBookList{
    return booksApi.getPageBooks(url = url)
    }

    suspend fun downloadBooks(url: String): Response<ResponseBody>{
     return booksApi.downloadBook(url)
    }


    suspend fun getAdditionalMetadata(query: String): BookMetadata {
        return booksDetailsApi.getDescription(query)
    }
}
