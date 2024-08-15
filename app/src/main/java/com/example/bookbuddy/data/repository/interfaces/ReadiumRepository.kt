package com.example.bookbuddy.data.repository.interfaces

import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url

interface ReadiumRepository {
    suspend fun getBookUrl(id: Int): Url
    suspend fun updateProgress(locator: Locator)
}