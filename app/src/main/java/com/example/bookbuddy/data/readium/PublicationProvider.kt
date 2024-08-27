package com.example.bookbuddy.data.readium

import android.content.Context
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.format.FormatHints
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.mediatype.MediaType
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import java.io.File

class PublicationProvider(private val context: Context){
    suspend operator fun invoke(fileName: String): Publication{
        val httpClient = DefaultHttpClient()
        val assetRetriever = AssetRetriever(context.contentResolver, httpClient = httpClient)

        val file = File(context.filesDir, fileName)
        val asset = assetRetriever.retrieve(file, formatHints = FormatHints(mediaType = MediaType.EPUB)).getOrElse{
            throw IllegalArgumentException("Error retrieving asset")
        }
        val publicationParser = DefaultPublicationParser(context, httpClient, assetRetriever, null)

        return PublicationOpener(publicationParser).run{
            open(asset = asset, allowUserInteraction = false).getOrElse {
                throw IllegalArgumentException("Error opening publication")
            }
        }
    }
}