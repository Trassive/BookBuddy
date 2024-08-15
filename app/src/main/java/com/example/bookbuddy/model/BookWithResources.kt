package com.example.bookbuddy.model

import com.example.bookbuddy.data.local.entities.BookResource
import com.example.bookbuddy.data.local.entities.SavedBook

data class BookWithResources(
    val book: SavedBook,
    val resource: BookResource
)
