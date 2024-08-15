 package com.example.bookbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.bookbuddy.data.local.entities.BookResource
import com.example.bookbuddy.data.local.entities.SavedBook
import com.example.bookbuddy.data.local.entities.SavedLocator
import kotlinx.coroutines.flow.Flow


@Dao
interface BooksDao{

    @Query("SELECT * FROM offline_books WHERE id = :id")
    suspend fun getBook(id: Int): SavedBook?

    @Update
    suspend fun updateLocator(savedLocator: SavedLocator)

    @Insert
    suspend fun insertLocator(savedLocator: SavedLocator)

    @Query("SELECT * FROM locator WHERE bookId = :bookId")
    suspend fun getLocator(bookId: Int): SavedLocator?

    @Query("SELECT * FROM book_resource WHERE bookId = :bookId")
    suspend fun getResource(bookId: Int): BookResource?

    @Transaction
    suspend fun deleteBook(bookId: Int){
        deleteResource(bookId)
        deleteSavedBook(bookId)
        deleteLocator(bookId)
    }

    @Transaction
    suspend fun insertBook(savedBook: SavedBook, resource: BookResource){
        insertSavedBook(savedBook)
        insertResources(resource)
    }


    @Query("""
        SELECT b.* FROM offline_books b 
        LEFT JOIN book_resource res ON res.bookId = b.id
        WHERE
        (downloaded_path IS NOT NULL AND :isDownloaded = 1)
        OR (downloaded_path IS NULL AND :isDownloaded = 0)
        """)
    fun getBooks(isDownloaded: Boolean): Flow<List<SavedBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resource: BookResource)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedBook(book: SavedBook)

    @Query("DELETE FROM book_resource WHERE bookId = :bookId")
    suspend fun deleteResource(bookId: Int)

    @Query("DELETE FROM locator WHERE bookId = :bookId")
    suspend fun deleteLocator(bookId: Int)

    @Query("DELETE FROM offline_books WHERE id = :bookId")
    suspend fun deleteSavedBook(bookId: Int)
}

