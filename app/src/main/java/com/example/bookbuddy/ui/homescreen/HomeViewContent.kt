@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bookbuddy.ui.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.ui.theme.AppShapes.bottomRoundedLarge
import com.example.bookbuddy.ui.util.BookCard
import com.example.bookbuddy.ui.util.CarouselPager
import com.example.bookbuddy.ui.util.CustomBottomSheet
import com.example.bookbuddy.ui.util.reachedBottom
import com.example.compose.BookBuddyTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeViewContent(
    homeUiState: HomeUiState.HomeView,
    onToggleSave: (Int,Book) -> Unit,
    onClick: (Int) -> Unit,
    loadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bottomSheetBook by remember{ mutableStateOf<Pair<Int,Book>?>(null) }


    val height = min(400.dp, LocalConfiguration.current.screenHeightDp.dp * 0.6f)


    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    val reachedBottom: Boolean by remember {
        derivedStateOf {
            lazyListState.reachedBottom(4)
        }
    }
    LaunchedEffect(key1 = reachedBottom) {
         if(reachedBottom) loadMore()
    }
    LazyColumn(
        state = lazyListState,
        modifier = modifier.background(Color(0xD3FFDEB9)),
    ) {
        item {
            Box(
                modifier = Modifier
                    .height(height)
                    .clip(MaterialTheme.shapes.bottomRoundedLarge)
                    .fillMaxWidth()
                    .background(Color(0x23080808))
                    .zIndex(2f)
            ) {
                CarouselPager(
                    books = homeUiState.carouselBooks,
                    onDrag = onClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        item{
            Spacer(Modifier.height(40.dp))
        }
        itemsIndexed(items = homeUiState.bookList, key = {_ ,it-> it.id }) { index, book ->
            BookCard(
                book = book,
                onClick = { onClick(book.id) },
                onLongPress = {
                    bottomSheetBook = index to book },
                diskCachePolicy = CachePolicy.DISABLED,
                memoryCachePolicy = CachePolicy.ENABLED,
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.medium_padding))
                    .height(150.dp)
            )
        }
        if(homeUiState.isLoading){
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
    bottomSheetBook?.let{( index, book )->
        CustomBottomSheet(
            book = book,
            onToggleSave = { onToggleSave(index,book) },
            onDismiss = { bottomSheetBook = null},
            onExpand = { onClick(book.id) }
        )
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset }
            .filter {(currentIndex,_)-> currentIndex<2 }
            .distinctUntilChanged()
            .collect { (currentIndex, currentScrollOffset) ->
                val isScrollingUp = if (currentIndex != previousIndex) {
                    currentIndex < previousIndex
                } else {
                    currentScrollOffset <= previousScrollOffset
                }

                if (currentScrollOffset > 0 && currentIndex == 0 && !isScrollingUp) {
                    coroutineScope.launch { lazyListState.animateScrollToItem(1) }
                } else if (currentIndex == 1 && isScrollingUp ) {
                    coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                }

                previousIndex = currentIndex
                previousScrollOffset = currentScrollOffset

            }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeViewPreview(){
    BookBuddyTheme {
        var state by remember {
            mutableStateOf(fakeData.books)
        }
        Surface{
            HomeViewContent(
                homeUiState = HomeUiState.HomeView(
                    carouselBooks = fakeData.books,
                    bookList = state
                ),
                onClick = {},
                loadMore = { state = state+state},
                onToggleSave = { _,_ -> }
            )
        }
    }
}
