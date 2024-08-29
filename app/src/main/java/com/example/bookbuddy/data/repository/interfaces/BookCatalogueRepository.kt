package com.example.bookbuddy.data.repository.interfaces

import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.Update
import kotlinx.coroutines.flow.Flow

interface BookCatalogueRepository {
    suspend fun getCatalogue(query: String? = null): List<Book>
    suspend fun updateCatalogue(update: Update): Flow<List<Book>>
    suspend fun saveBook(book: Book)
    suspend fun unSaveBook(id: Int)
}
