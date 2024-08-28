package com.example.bookbuddy.di

import android.content.Context
import com.example.bookbuddy.data.local.dao.BooksDao
import com.example.bookbuddy.data.local.database.AppDatabase
import com.example.bookbuddy.data.readium.PublicationProvider
import com.example.bookbuddy.data.repository.implementation.BookDataRepository
import com.example.bookbuddy.data.repository.interfaces.BookCatalogueRepository
import com.example.bookbuddy.data.repository.interfaces.BookDetailsRepository
import com.example.bookbuddy.data.repository.interfaces.OfflineBookRepository
import com.example.bookbuddy.data.repository.interfaces.ReadiumRepository
import com.example.bookbuddy.data.util.FileHandler
import com.example.bookbuddy.network.BookDetailsApi
import com.example.bookbuddy.network.BooksApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideBooksApi(): BooksApi{
        val retrofit = createRetrofit("https://gutendex.com/")
        val booksApi: BooksApi by lazy{
            retrofit.create(BooksApi::class.java)
        }
        return booksApi
    }
    @Provides
    @Singleton
    fun providesBookDetailsApi(): BookDetailsApi{
        val retrofit = createRetrofit("https://www.googleapis.com/")

        val bookDetailsApi: BookDetailsApi by lazy{
            retrofit.create(BookDetailsApi::class.java)
        }
        return bookDetailsApi
    }
    @Provides
    fun providesDao(@ApplicationContext context: Context): BooksDao = AppDatabase.getDatabase(context).booksDao()

    @Provides
    fun providesFileHandler(@ApplicationContext context: Context): FileHandler = FileHandler(context)

    @Provides
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    fun providesBookDetailRepository(bookRepository: BookDataRepository): BookDetailsRepository = bookRepository

    @Provides
    fun providesBookCatalogueRepository(bookRepository: BookDataRepository): BookCatalogueRepository = bookRepository

    @Provides
    fun providesOfflineBookRepository(bookRepository: BookDataRepository): OfflineBookRepository = bookRepository

    @Provides
    fun providesReadiumRepository(bookRepository: BookDataRepository): ReadiumRepository = bookRepository

    @Provides
    fun providesPublicationProvider(@ApplicationContext context: Context): PublicationProvider = PublicationProvider(context)
}

private fun createRetrofit(baseUrl: String): Retrofit{
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set log level
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}