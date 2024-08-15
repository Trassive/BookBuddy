package com.example.bookbuddy.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//stores the book metadata

@Entity(tableName = "offline_books")
data class SavedBook(
    @PrimaryKey() val id: Int,
    val title: String,
    val description: String,
    val categories:String,
    val authors: String,
    @ColumnInfo("image_url") val coverImage: String,
)

//stores the last read position for bookmarks
@Entity("locator")
data class SavedLocator(
    @PrimaryKey val bookId: Int,
    val href: String,
    val type: String,
    val progression: Float,
    val totalProgression: Float?,
    val position: Int?,
    val selector: String?,
    val textBefore: String?,
    val textAfter: String?,
    val textHighlight: String?,
    val title: String?
)

//For storing the resource to access the book
@Entity("book_resource")
data class BookResource(
    @PrimaryKey() val bookId: Int,
    @ColumnInfo("downloaded_path") val downloadPath: String?,
    @ColumnInfo("download_link")val downloadLink: String
)