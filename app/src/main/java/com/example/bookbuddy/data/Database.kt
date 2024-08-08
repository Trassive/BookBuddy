package com.example.bookbuddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedBook::class,SavedLocator::class], version = 1)
abstract class AppDatabase: RoomDatabase(){
    abstract fun booksDao(): BooksDao

    companion object{
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            return instance?: synchronized(this){
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