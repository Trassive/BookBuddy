package com.example.bookbuddy.data.repository.interfaces

import com.example.bookbuddy.model.Book
import kotlinx.coroutines.flow.Flow

interface BookCatalogueRepository {
    suspend fun getCatalogue(query: String = ""): List<Book>
    suspend fun updateCatalogue(): Flow<List<Book>>
    suspend fun saveBook(book: Book)
    suspend fun unSaveBook(id: Int)
}
