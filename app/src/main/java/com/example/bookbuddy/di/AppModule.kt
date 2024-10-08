package com.example.bookbuddy.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.bookbuddy.data.local.dao.BooksDao
import com.example.bookbuddy.data.local.database.AppDatabase
import com.example.bookbuddy.data.readium.PublicationProvider
import com.example.bookbuddy.data.repository.implementation.BookDataRepository
import com.example.bookbuddy.data.repository.implementation.ConfigurationsRepository
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
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

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile("prefrences") }
        )
    }
    @Provides
    fun providesConfigurationsRepository(dataStore: DataStore<Preferences>): ConfigurationsRepository = ConfigurationsRepository(dataStore)

}

private fun createRetrofit(baseUrl: String): Retrofit{
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set log level
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectionPool(connectionPool = ConnectionPool(10, 1, TimeUnit.MINUTES))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()



    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}