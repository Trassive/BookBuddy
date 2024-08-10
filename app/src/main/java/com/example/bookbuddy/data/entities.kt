package com.example.bookbuddy.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*  Stores books both saved or download
    using *isDownloaded*
    distinct properties are nullable
*/

@Entity(tableName = "offline_books")
data class SavedBook(
    @PrimaryKey() val id: Int,
    val title: String,
    val description: String?,
    val categories:String,
    val authors: String,
    @ColumnInfo("download_path") val downloadPath: String?,
    @ColumnInfo("image_url") val coverImage: String,
//    https for saved file for downloaded
    @ColumnInfo("file_url")val fileUrl: String
)

@Entity("locator")
data class SavedLocator(
    @PrimaryKey val bookId: String,
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