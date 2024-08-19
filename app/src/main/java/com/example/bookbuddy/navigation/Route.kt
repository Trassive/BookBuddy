package com.example.bookbuddy.navigation


import kotlinx.serialization.Serializable
import org.readium.r2.shared.publication.Href

@Serializable
sealed interface RouteScreen{
    @Serializable
    data object Home: RouteScreen
    @Serializable
    data object Library: RouteScreen
    @Serializable
    data object Settings: RouteScreen
}
@Serializable
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

}
object HrefStringUtils {
    fun hrefToString(href: Href): String {
        return "${href.toString()}|${href.isTemplated}"
    }

    fun stringToHref(str: String): Href? {
        val parts = str.split("|")
        if (parts.size != 2) return null

        val (url, isTemplatedStr) = parts
        val isTemplated = isTemplatedStr.toBoolean()

        return Href(url, isTemplated)
    }
}