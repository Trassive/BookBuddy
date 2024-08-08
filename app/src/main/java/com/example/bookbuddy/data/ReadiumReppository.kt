package com.example.bookbuddy.data

import android.content.Context
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient

class ReadiumReppository(private val readium: ReadiumBookRepository, private val context: Context){
    suspend fun PublicationProvider(id: Int){
        val asset = AssetRetriever(context.contentResolver, httpClient = DefaultHttpClient())
//        asset.retrieve()
    }
}