package com.example.bookbuddy.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface BookDetailsApi {
    @GET("books/v1/volumes")
    suspend fun getDescription(
        @QueryMap(encoded = true ) query: Map<String,String>
    ): BookMetadata
}

@Serializable
data class BookMetadata(
    @SerialName("items") val items: List<Item> = listOf()
)
@Serializable
data class Item(
    @SerialName("volumeInfo") val volumeInfo: VolumeInfo? = null
)
@Serializable
data class VolumeInfo(
    @SerialName("description") val description: String? = null
)