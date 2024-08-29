@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.bookbuddy.ui.util

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.BaseBook
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.model.LibraryBook
import com.example.compose.BookBuddyTheme

@Composable
fun BookList(
    books: List<Book>,
    onClick: (Int)-> Unit,
    loadMore: ()-> Unit,
    modifier: Modifier,
    diskCachePolicy: CachePolicy,
    memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
    contentPadding: PaddingValues = PaddingValues(dimensionResource(id = R.dimen.large_padding)),
){
    var bottomSheetBook by remember{ mutableStateOf<Book?>(null) }
    val lazyListState = rememberLazyListState()
    val reachedBottom: Boolean by remember {
        derivedStateOf {
            lazyListState.reachedBottom(4)
        }
    }
    LaunchedEffect(key1 = reachedBottom) {
        Log.d("BookList", "reachedBottom: $reachedBottom")
        if(reachedBottom) loadMore()
    }
    LazyColumn(
        state = lazyListState,
        contentPadding = contentPadding,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.medium_padding)),
        modifier = modifier
            .fillMaxSize()
    ) {
        items(items = books, key = { it.id }) { book ->
            BookCard(
                book = book,
                onClick = { onClick(book.id) },
                onLongPress = {
                    bottomSheetBook = book
                },
                diskCachePolicy = diskCachePolicy,
                memoryCachePolicy = memoryCachePolicy,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
        }
    }
    bottomSheetBook?.let{book->
        CustomBottomSheet(
            book = book,
            onDismiss = { bottomSheetBook = null},
            onExpand = { onClick(book.id) }
        )
    }
}

@Composable
fun LibraryBookList(
    books: List<LibraryBook>,
    onClick: (Int)-> Unit,
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
                onClick = { onClick(book.id) },
                onLongPress = {},
                diskCachePolicy = diskCachePolicy,
                memoryCachePolicy = memoryCachePolicy,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
        }
    }

}
@Composable
fun BookCard(
    book: BaseBook,
    diskCachePolicy: CachePolicy,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClickLabel = stringResource(id = R.string.expand),
                onClick = onClick,
                onLongClick = onLongPress
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
            modifier = Modifier
                .fillMaxSize()
        ){
            CoilImage(
                id = book.id,
                imageUrl = book.coverImage,
                onError = {},
                memoryCachePolicy = memoryCachePolicy,
                diskCachePolicy = diskCachePolicy,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.small_padding))
                    .fillMaxHeight()
                    .aspectRatio(2 / 3f)
                    .clip(MaterialTheme.shapes.medium)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.medium_padding))
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier. padding(bottom = dimensionResource(id = R.dimen.small_padding))

                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = book.authors.firstOrNull()?: stringResource(R.string.no_author_found))
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
            loadMore = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
fun CardPreview(){
    BookBuddyTheme {
        BookCard(
            book = fakeData.books[0],
            onClick = {},
            onLongPress = {},
            diskCachePolicy = CachePolicy.DISABLED,
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
        )
    }
}