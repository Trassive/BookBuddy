package com.example.bookbuddy.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface BookDetailsApi {
    @GET("/volumes")
    suspend fun getDescription(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int =0,
        @Query("maxResults") maxResults: Int =1
    ): BookMetadata
}

@Serializable
data class BookMetadata(
    @SerialName("items") val items: Items
)
@Serializable
data class Items(
    @SerialName("volumeInfo") val volumeInfo: VolumeInfo
)
@Serializable
data class VolumeInfo(
    @SerialName("description") val description: String
)