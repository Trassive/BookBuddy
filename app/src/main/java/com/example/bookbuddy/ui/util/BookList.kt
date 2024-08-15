package com.example.bookbuddy.ui.util

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.Book
import com.example.compose.BookBuddyTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BookList(
    books: List<Book>,
    onClick: (Int)-> Unit,
    onLongPress:(Int)-> Unit,
    modifier: Modifier,
    diskCachePolicy: CachePolicy,
    memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
    contentPadding: PaddingValues = PaddingValues(dimensionResource(id = R.dimen.large_padding)),

){
        LazyColumn(
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.medium_padding)),
            modifier = modifier
                .fillMaxSize()
        ) {
            items(items = books, key = { it.id }) { book ->
                BookCard(
                    book = book,
                    onClick = onClick,
                    onLongPress = onLongPress,
                    diskCachePolicy = diskCachePolicy,
                    memoryCachePolicy = memoryCachePolicy,
                    modifier = Modifier.height(150.dp)
                )
            }

        }


}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCard(
    book: Book,
    onClick: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
    diskCachePolicy: CachePolicy,
    memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier
        .combinedClickable(
            onClickLabel = stringResource(id = R.string.expand),
            onClick = { onClick(book.id) },
            onLongClick = {onLongPress(book.id)}
        )
        .padding(dimensionResource(id = R.dimen.small_padding)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            hoveredElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.large
    ){
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ){
            CoilImage(
                id = book.id,
                imageUrl = book.cover,
                onError = {},
                diskCachePolicy = diskCachePolicy,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .fillMaxHeight()
                    .aspectRatio(2 / 3f)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.medium_padding))
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.small_padding))

                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = book.authors.first())
                    if (book.authors.size > 1) {
                        Text(
                            text = "+${book.authors.size - 1} more",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.english),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.small_padding))
                )
                Text(
                    text = book.categories.joinToString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ListPreview(){
    BookBuddyTheme {
        BookList(
            books = fakeData.books,
            modifier = Modifier,
            diskCachePolicy = CachePolicy.DISABLED,
            onClick = {},
            onLongPress = {}
        )
    }
}
/*
item {
    Row() {
        Button(onClick = onPrevious) {
            Row() {
                Icon(
                    painter = painterResource(id = R.drawable.round_navigate_before_24),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.prev)
                )
            }
        }
        Button(onClick = onNext) {
            Row() {
                Text(
                    text = stringResource(R.string.next)
                )
                Icon(
                    painter = painterResource(id = R.drawable.round_navigate_next_24),
                    contentDescription = null
                )
            }
        }
    }
}
 */