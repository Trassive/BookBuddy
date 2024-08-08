package com.example.bookbuddy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Queries both saved or downloaded books
@Dao
interface BooksDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBook(book: SavedBook)


    @Update
    suspend fun updateProgress(locator: SavedLocator)

    //For Saved Book list
    @Query("SELECT * FROM offline_books WHERE isDownloaded = :isDownloaded")
    fun getBooksList(isDownloaded: Boolean): Flow<List<SavedBook>>

    //Provide book with locator
    @Query("""
        SELECT * From offline_books
        JOIN locator ON offline_books.id = locator.bookId
        WHERE id = :id
        """)
    suspend fun getBook(id: Int): Map<SavedBook,SavedLocator>

    @Query("SELECT isDownloaded FROM offline_books WHERE id = :id")
    suspend fun isDownloaded(id: Int): Boolean

    @Query("DELETE FROM offline_books WHERE id = :id")
    suspend fun deleteBookEntity(id: Int)

    @Query("DELETE FROM locator WHERE bookId = :id")
    suspend fun deleteLocatoryEntity(id: Int)

    @Transaction
    suspend fun deleteBook(id: Int){
        deleteBookEntity(id)
        deleteLocatoryEntity(id)
    }
}