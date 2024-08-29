@file:OptIn(ExperimentalFoundationApi::class)

package com.example.bookbuddy.ui.util

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.lerp
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.Book
import com.example.compose.BookBuddyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun CarouselPager(
    books: List<Book>,
    onDrag: (Int) -> Unit,
    modifier: Modifier = Modifier
){
    BoxWithConstraints(modifier = modifier.fillMaxSize()){
        val pagerState = rememberPagerState {
            books.size
        }
        val cardWidth = min(maxWidth*0.6f,200.dp)
        LaunchedEffect(key1 = Unit) {
            launch {
                while(true){
                    delay(6000)
                    with(pagerState) {
                        val target = if (currentPage < pageCount - 1) currentPage + 1 else 0
                        animateScrollToPage(
                            page = target,
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = EaseOut
                            )
                        )
                    }
                }
            }
        }

        HorizontalPager(
            pageSpacing = (maxWidth-cardWidth)*0.3f,
            state = pagerState,
            contentPadding = PaddingValues(
                horizontal = (maxWidth-cardWidth)/2,
                vertical = dimensionResource(id = R.dimen.large_padding)),
            pageSize = PageSize.Fill,
            modifier = Modifier.fillMaxSize()
            ) {page->
            Box(modifier = Modifier.fillMaxSize()) {
                CarouselCard(
                    book = books[page],
                    pagerState = pagerState,
                    page = page,
                    onDrag = onDrag,
                    modifier = Modifier
                )
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselCard(
    book: Book,
    modifier: Modifier,
    pagerState: PagerState,
    onDrag: (Int) -> Unit,
    page: Int
) {
    val density = LocalDensity.current
    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Start,
            positionalThreshold = {totalDistance: Float -> totalDistance*0.8f },
            velocityThreshold = { density.run{ 20.dp.toPx()*2f }},
            snapAnimationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioMediumBouncy
           ),
            decayAnimationSpec = FloatExponentialDecaySpec(0.1f).generateDecayAnimationSpec()
        )
    }
    SideEffect {
        anchoredDraggableState.updateAnchors(
            DraggableAnchors{
                DragAnchors.Start at 0f
                DragAnchors.End at density.run { 30.dp.toPx() }
            }
        )
    }
    LaunchedEffect(key1 = anchoredDraggableState.settledValue) {
        if(anchoredDraggableState.currentValue == DragAnchors.End){

            onDrag(book.id)
            anchoredDraggableState.animateTo(DragAnchors.Start)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val pageOffset = pagerState.calculateCurrentOffsetForPage(page).absoluteValue
    val dragProgress = anchoredDraggableState.progress(DragAnchors.Start,DragAnchors.End)


    Box(
        modifier
            .anchoredDraggable(
                state = anchoredDraggableState,
                orientation = Orientation.Vertical,
                enabled = pagerState.currentPage == page
            )
            .offset {
                IntOffset(
                    y = anchoredDraggableState
                        .requireOffset()
                        .toInt(), x = 0
                )
            }
            .graphicsLayer {
                val scale = lerp(1f, 1.1f, dragProgress)
                scaleX = scale
                scaleY = scale
            }
        ){
        ElevatedCard(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.medium_padding))
            ) {
                CoilImage(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.large)
                        .graphicsLayer {

                            val scale = lerp(1f, 1.75f, pageOffset)
                            scaleX = scale
                            scaleY = scale
                        },
                    id = book.id,
                    imageUrl = book.coverImage
                )
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(dimensionResource(id = R.dimen.small_padding))
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.small_padding))
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
                DragToListen(
                    pageOffset = pageOffset,
                    dragProgress = dragProgress,
                    onDrag = {
                        coroutineScope.launch{ anchoredDraggableState.animateTo(DragAnchors.End) }
                        onDrag(book.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun DragToListen(pageOffset: Float, dragProgress: Float, onDrag: () -> Unit) {
    Box(
        modifier = Modifier
            .height(50.dp * (1 - pageOffset))
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = lerp(0f, 50f, dragProgress).toInt()) }
            .graphicsLayer {
                alpha = 1 - pageOffset
            }
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DRAG TO READ",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.graphicsLayer {
                    scaleX = lerp(1f, 1.3f, dragProgress)
                    scaleY = lerp(1f, 1.2f, dragProgress)
                }
            )
            Spacer(
                modifier = Modifier.size(dimensionResource(id = R.dimen.small_padding))
            )
            IconButton(onClick = onDrag){
                Icon(
                    painterResource(id = R.drawable.round_expand_more_24), "down",
                    modifier = Modifier
                        .background(Color.Transparent)
                )
            }
        }
    }
}


fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage - page) + currentPageOffsetFraction
}
enum class DragAnchors {
    Start,
    End,
}
@Preview
@Composable
fun SliderPreview(){
    BookBuddyTheme {
        Surface(Modifier.fillMaxSize()){
            CarouselPager(
                books = fakeData.books,
                onDrag = {},
                modifier = Modifier.requiredHeight(400.dp)
            )
        }
    }
}
