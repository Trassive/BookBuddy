package com.example.bookbuddy.model

interface BaseBook {
    val id: Int
    val title: String
    val categories: List<String>
    val authors: List<String>
    val coverImage: String
}