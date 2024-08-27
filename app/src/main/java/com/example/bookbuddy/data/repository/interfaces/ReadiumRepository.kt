package com.example.bookbuddy.data.repository.interfaces

import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Url

interface ReadiumRepository {
    suspend fun getBookUrl(id: Int): String
    suspend fun updateProgress(id: Int,locator: Locator)
    suspend fun getLoctor(id: Int): Locator?
}