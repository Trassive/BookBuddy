package com.example.bookbuddy.ui.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.lerp
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.ui.theme.AppShapes.bottomRoundedLarge
import com.example.compose.BookBuddyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

//Drag left
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselPager(
    books: List<Book>,
    onDrag: (Int) -> Unit,
    modifier: Modifier = Modifier
){
    BoxWithConstraints(modifier = modifier
        .fillMaxSize()
        .background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        )
    ){

        val pagerState = rememberPagerState {
            books.size
        }
        val cardWidth = min(maxWidth,200.dp)
        LaunchedEffect(key1 = Unit) {
            launch {
                while(true){
                    delay(6000)
                    with(pagerState) {
                        val target = if (currentPage < pageCount - 1) currentPage + 1 else 0

                        animateScrollToPage(
                            page = target,
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                }
            }
        }

        HorizontalPager(
            pageSpacing = maxWidth-cardWidth,
            state = pagerState,
            contentPadding = PaddingValues(horizontal = (maxWidth-cardWidth)/2, vertical = dimensionResource(
                id = R.dimen.large_padding
            )),
            pageSize = PageSize.Fixed(cardWidth),
            modifier = Modifier.fillMaxSize()
            ) {page->
            CarouselCard(
                book = books[page],
                pagerState = pagerState,
                page = page,
                modifier = Modifier
            )
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselCard(
    book: Book,
    modifier: Modifier,
    pagerState: PagerState,
    page: Int
) {
    val pageOffset = pagerState.calculateCurrentOffsetForPage(page).absoluteValue
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.large_padding))

        ) {
            CoilImage(
                modifier = Modifier
                    .padding(bottom = dimensionResource(id = R.dimen.large_padding))
                    .weight(1f)
                    .align(Alignment.CenterHorizontally)
                    .aspectRatio(2 / 3f)
                    .clip(MaterialTheme.shapes.large)
                    .graphicsLayer {

                        val scale = lerp(1f, 1.75f, pageOffset)
                        scaleX *= scale
                        scaleY *= scale
                    },
                id = book.id,
                imageUrl = book.cover
            )
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = dimensionResource(id = R.dimen.small_padding))
            )
             Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = dimensionResource(id = R.dimen.small_padding))
             ) {
                Text(
                    text = book.authors.first(),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (book.authors.size > 1) {
                    Text(
                        text = "+${book.authors.size - 1} more",
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.medium_padding)))
            DragToListen(pageOffset)
        }
    }
}

@Composable
private fun DragToListen(pageOffset: Float) {
    Box(
        modifier = Modifier
            .height(50.dp * (1 - pageOffset))
            .fillMaxWidth()
            .graphicsLayer {
                alpha = 1 - pageOffset
            }
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("DRAG TO READ",style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.small_padding)))
            DragArea()
        }
    }
}
@Composable
private fun DragArea() {
    Box {

        Icon(
            painterResource(id = R.drawable.round_expand_more_24), "down",
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Transparent)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage - page) + currentPageOffsetFraction
}
@Preview(showBackground = true)
@Composable
fun SliderPreview(){
    BookBuddyTheme {
        Column(Modifier.fillMaxSize()){
            CarouselPager(
                books = fakeData.books,
                onDrag = {},
                modifier = Modifier.weight(4f)
            )
            Surface(Modifier.weight(6f)) {

            }
        }
    }
}