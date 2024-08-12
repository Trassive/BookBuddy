package com.example.bookbuddy.data.local

import com.example.bookbuddy.data.SavedBook
import com.example.bookbuddy.data.SavedLocator
import kotlinx.coroutines.flow.Flow

class BooksLocalRepository(private val booksLocalDataSource: BooksLocalDataSource){
    suspend fun saveBook(book: SavedBook){
        booksLocalDataSource.saveBook(book)
    }
    suspend fun updateProgress(locator: SavedLocator){
        booksLocalDataSource.saveProgress(locator)
    }
    suspend fun getBook(id: Int): Flow<SavedBook?> {
        return booksLocalDataSource.getBook(id)
    }
    fun getSavedBook(isDownloaded: Boolean): Flow<List<SavedBook>> {
       return  booksLocalDataSource.getSavedBooks(isDownloaded)
    }
    suspend fun deleteBook(id: Int){
        booksLocalDataSource.deleteBook(id)
    }
    suspend fun unSaveBook(id: Int){
        booksLocalDataSource.unSaveBook(id)
    }
    suspend fun isDownloaded(id: Int): Boolean{
        return booksLocalDataSource.isDownloaded(id)
    }
}