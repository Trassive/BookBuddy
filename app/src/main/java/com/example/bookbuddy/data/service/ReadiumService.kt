package com.example.bookbuddy.data.service

import android.content.Context
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient

class ReadiumService( private val context: Context){
    suspend fun PublicationProvider(id: Int){
        val asset = AssetRetriever(context.contentResolver, httpClient = DefaultHttpClient())
//        asset.retrieve()
    }

}