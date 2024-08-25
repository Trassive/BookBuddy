package com.example.bookbuddy.navigation


import android.os.Parcelable
import androidx.navigation.NavType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.readium.r2.shared.publication.Href



sealed interface RouteScreen{
    @Serializable
    data object Home: RouteScreen
    @Serializable
    data object Library: RouteScreen
    @Serializable
    data object Settings: RouteScreen
}

sealed interface LeafScreen{
    @Serializable
    data object Home : LeafScreen
    @Serializable
    data object Library: LeafScreen
    @Serializable
    data object Settings: LeafScreen
    @Serializable
    data class BookDetail(val id: Int): LeafScreen
    @Serializable
    data class TableOfContent(val id: Int): LeafScreen
    @Serializable
    data class Reader(
        val id: Int,
        val url: String?
    ): LeafScreen
}

