package com.example.bookbuddy.data.repository.interfaces

import com.example.bookbuddy.model.LibraryBook
import kotlinx.coroutines.flow.Flow

interface OfflineBookRepository {
    suspend fun getSavedBooks(): Flow<List<LibraryBook>>
    suspend fun getDownloadedBooks(): Flow<List<LibraryBook>>
}