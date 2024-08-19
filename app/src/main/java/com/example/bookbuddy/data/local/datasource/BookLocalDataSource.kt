package com.example.bookbuddy.data.local.datasource

import com.example.bookbuddy.data.local.entities.BookResource
import com.example.bookbuddy.data.local.dao.BooksDao
import com.example.bookbuddy.data.local.entities.SavedLocator
import com.example.bookbuddy.data.local.entities.SavedBook
import com.example.bookbuddy.model.BookWithResources
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookLocalDataSource @Inject constructor(private val booksDao: BooksDao) {
    suspend fun saveBook(book: SavedBook, resource: BookResource){
        booksDao.insertBook(savedBook = book,resource = resource)
    }
    suspend fun unSaveBook(id: Int){
        booksDao.deleteBook(id)
    }

    suspend fun saveProgress(locator: SavedLocator){

        if(booksDao.getLocator(locator.bookId)!=null){
            booksDao.updateLocator(locator)
        } else {
            booksDao.insertLocator(locator)
        }
    }
    fun getBooks(isDownloaded: Boolean): Flow<List<SavedBook>> {
        return booksDao.getBooks(isDownloaded)
    }
    suspend fun getLocator(id: Int): SavedLocator? {
        return booksDao.getLocator(id)
    }
    suspend fun getBook(id: Int): BookWithResources? {
        val savedBook = booksDao.getBook(id)
        val resources = booksDao.getResource(id)
        return if(savedBook == null) {
            null
        } else {
            BookWithResources(book = savedBook, resource = resources!!)
        }
    }
    suspend fun deleteBook(id: Int){
        booksDao.deleteBook(id)
    }
    suspend fun isDownloaded(id: Int): Boolean{
        return booksDao.getResource(id)?.downloadPath != null
    }
}

