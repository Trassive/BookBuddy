package com.example.bookbuddy.data

import android.content.Context
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.Books
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

object fakeData {
    val books = listOf(
            Book(
                id = 1,
                title = "The Lord of the Rings",
                categories = listOf("Fantasy", "Adventure"),
                authors = listOf("J.R.R. Tolkien","J.R.R. Tolkien"),
                cover = "https://example.com/lotr.jpg",
                description = "Epic high fantasy trilogy following the journey of Frodo Baggins.",
                downloadLink = "https://example.com/lotr.pdf",
                isDownloaded = false,
                isSaved = false
            ),
            Book(
                id = 2,
                title = "Pride and Prejudice",
                categories = listOf("Romance", "Classic"),
                authors = listOf("Jane Austen"),
                cover = "https://example.com/pride.jpg",
                description = "A witty social commentary on love and class in 19th century England.",
                downloadLink = "https://example.com/pride.pdf",
                isDownloaded = true,
                isSaved = false
            ),
            Book(
                id = 3,
                title = "The Hitchhiker's Guide to the Galaxy",
                categories = listOf("Science Fiction", "Comedy"),
                authors = listOf("Douglas Adams"),
                cover = "https://example.com/hitchhiker.jpg",
                description = "A hilarious and thought-provoking journey through the universe.",
                downloadLink = "https://example.com/hitchhiker.pdf",
                isDownloaded = false,
                isSaved = false
            ),
            Book(
                id = 4,
                title = "To Kill a Mockingbird",
                categories = listOf("Classic", "Historical Fiction"),
                authors = listOf("Harper Lee"),
                cover = "https://example.com/mockingbird.jpg",
                description = "A coming-of-age story set in the American South during the 1930s.",
                downloadLink = "https://example.com/mockingbird.pdf",
                isDownloaded = false,
                isSaved = false
            ),
            Book(
                id = 5,
                title = "1984",
                categories = listOf("Dystopian", "Science Fiction"),
                authors = listOf("George Orwell"),
                cover = "https://example.com/1984.jpg",
                description = "A chilling vision of a totalitarian future where Big Brother is always watching.",
                downloadLink = "https://example.com/1984.pdf",
                isDownloaded = true,
                isSaved = false
            ),
            Book(
                id = 6,
                title = "The Great Gatsby",
                categories = listOf("Classic", "Tragedy"),
                authors = listOf("F. Scott Fitzgerald"),
                cover = "https://example.com/gatsby.jpg",
                description = "A tale of wealth, love, and the American Dream in the Jazz Age.",
                downloadLink = "https://example.com/gatsby.pdf",
                isDownloaded = false,
                isSaved = false
            ),
            Book(
                id = 7,
                title = "One Hundred Years of Solitude",
                categories = listOf("Magical Realism", "Literary Fiction"),
                authors = listOf("Gabriel García Márquez"),
                cover = "https://example.com/solitude.jpg",
                description = "A multi-generational saga of the Buendía family in the fictional town of Macondo.",
                downloadLink = "https://example.com/solitude.pdf",
                isDownloaded = true,
                isSaved = false
            ),
            Book(
                id = 8,
                title = "And Then There Were None",
                categories = listOf("Mystery", "Thriller"),
                authors = listOf("Agatha Christie"),
                cover = "https://example.com/none.jpg",
                description = "Ten strangers are invited to a secluded island where they are killed off one by one.",
                downloadLink = "https://example.com/none.pdf",
                isDownloaded = false,
                isSaved = false
            ),
            Book(
                id = 9,
                title = "The Little Prince",
                categories = listOf("Children's Literature", "Fantasy"),
                authors = listOf("Antoine de Saint-Exupéry"),
                cover = "https://example.com/prince.jpg",
                description = "A poetic and philosophical tale about a pilot who meets a young prince from another planet.",
                downloadLink = "https://example.com/prince.pdf",
                isDownloaded = false,
                isSaved = false
            )
        )

}

