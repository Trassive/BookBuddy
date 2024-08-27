package com.example.bookbuddy.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Streaming
import retrofit2.http.Url

interface BooksApi {
    @GET("/books")
    suspend fun getBooks( @QueryMap(encoded = true) queries: Map<String,String> ): RemoteBookList

    @GET
    suspend fun getPageBooks(@Url url: String): RemoteBookList

    @Streaming
    @GET
    suspend fun downloadBook(@Url downloadUrl: String): Response<ResponseBody>

}

@Serializable
data class RemoteBookList(
    @SerialName("count") val count: Int,
    @SerialName("next") val  next: String?,
    @SerialName("previous") val  previous: String?,
    @SerialName("results") val books: List<RemoteBook>
)

@Serializable
data class RemoteBook(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("subjects") val categories: List<String>,
    @SerialName("authors") val authors: List<Person>,
    @SerialName("formats") val formats: ePub
)
@Serializable
data class Person(
    val name: String
)
@Serializable
data class ePub(
    @SerialName("application/epub+zip") val epub: String,
    @SerialName("image/jpeg") val cover: String
)