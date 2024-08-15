package com.example.bookbuddy.data.repository.interfaces

import com.example.bookbuddy.model.DownloadState
import com.example.bookbuddy.model.Book
import kotlinx.coroutines.flow.Flow

interface BookDetailsRepository {
    suspend fun getBookDetails(id: Int): Book
    suspend fun saveBook(book: Book)
    suspend fun unSaveBook(id: Int)
    suspend fun downloadBook(book: Book): Flow<DownloadState>
    suspend fun deleteBook(id: Int)
}