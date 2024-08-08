package com.example.bookbuddy.data.local

import com.example.bookbuddy.data.BooksDao
import com.example.bookbuddy.data.SavedLocator
import com.example.bookbuddy.data.SavedBook
import kotlinx.coroutines.flow.Flow

class BooksLocalDataSource(private val booksDao: BooksDao) {
    suspend fun saveBook(book: SavedBook,locator: SavedLocator? = null){
        booksDao.saveBook(book)
        locator?.let{
            booksDao.updateProgress(it)
        }
    }
    suspend fun saveProgress(locator: SavedLocator){
        booksDao.updateProgress(locator)
    }
    suspend fun getSavedBooks(isDownloaded: Boolean): Flow<List<SavedBook>> {
        return booksDao.getBooksList(isDownloaded)
    }
    suspend fun getBook(id: Int): Map<SavedBook, SavedLocator> {
        return booksDao.getBook(id)
    }
    suspend fun deleteBook(id: Int){
        booksDao.deleteBook(id)
    }
    suspend fun isDownloaded(id: Int): Boolean{
        return booksDao.isDownloaded(id)
    }
}

