package com.example.bookbuddy.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bookbuddy.data.local.dao.BooksDao
import com.example.bookbuddy.data.local.entities.BookResource
import com.example.bookbuddy.data.local.entities.SavedBook
import com.example.bookbuddy.data.local.entities.SavedLocator

@Database(entities = [SavedBook::class, SavedLocator::class, BookResource::class], version = 1)
abstract class AppDatabase: RoomDatabase(){
    abstract fun booksDao(): BooksDao

    companion object{
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this){
                 Room.databaseBuilder(
                    context = context,
                    klass = AppDatabase::class.java,
                    name = "books_database"
                 )
                     .fallbackToDestructiveMigration()
                     .build()
                     .also{
                    instance = it
                 }
            }
        }
    }
}