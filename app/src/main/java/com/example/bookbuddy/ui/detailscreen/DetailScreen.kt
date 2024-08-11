package com.example.bookbuddy.ui.detailscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.ui.util.CoilImage
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import com.example.compose.BookBuddyTheme

@Composable
fun DetailScreen(detailScreenViewModel: DetailScreenViewModel){
    val detailScreenState by detailScreenViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    when(val state = detailScreenState){
        is DetailScreenState.Loading -> {
            LottieAnimationComposable(R.raw.empty,modifier = Modifier.fillMaxSize())
        }
        is DetailScreenState.Error ->{
            LottieAnimationComposable(R.raw.empty,modifier = Modifier.fillMaxSize())
        }
        is DetailScreenState.DetailView ->{
            DetailView(state)
        }
    }
}

@Composable
fun DetailView(detailViewState: DetailScreenState.DetailView) {
    BoxWithConstraints{
        Column {
            BoxWithConstraints(
                Modifier
                    .weight(6f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                Color(0xFFECF6E5),
                            )
                        )
                    )
                    .fillMaxWidth()
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(maxWidth * 0.4f)
                        .aspectRatio(2 / 3f)
                ) {
                    CoilImage(
                        id = detailViewState.book.id,
                        imageUrl = detailViewState.book.downloadLink,
                        diskCachePolicy = if (detailViewState.book.isSaved) CachePolicy.ENABLED else CachePolicy.DISABLED,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Cyan)
                    )
                }
                Text(
                    text = detailViewState.book.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    color = Color(0xFF3C4142),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(maxWidth * 0.6f)
                        .align(Alignment.TopCenter)
                        .offset(y = maxHeight / 2 + maxWidth * 0.2f * 1.5f + 30.dp)

                )
            }

            Surface(
                Modifier
                    .weight(4f)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(id = R.dimen.large_padding),
                        vertical = 50.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    customStickyHeader(
                        authors = detailViewState.book.authors,
                        categories = detailViewState.book.categories
                    )
                    item {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.large_padding))
                        )
                        Text(text = detailViewState.book.description!!)
                    }
                }
            }
        }
        Button({},modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = (maxHeight * 0.6f) - 24.dp)){
            Text(text = "Action")
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.customStickyHeader(authors: List<String>, categories: List<String>){
    stickyHeader{
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 20.dp)
            ){
            ExpandableAuthorList(listName = "Categories", list = categories,modifier = Modifier
                .height(70.dp)
                .weight(1f)
            )

            ExpandableAuthorList(listName = "Authors", list = authors,modifier = Modifier
                .height(70.dp)
                .weight(1f)
            )
        }
    }
}
@Composable
fun ExpandableAuthorList(listName: String, list: List<String>, modifier: Modifier = Modifier) {
    var expandedState by remember { mutableStateOf(false) }
    var visibleItems by remember { mutableIntStateOf(1) }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(listName, style = MaterialTheme.typography.titleMedium,modifier = Modifier.alpha(0.7f))
        Spacer(modifier = Modifier.height( dimensionResource( R.dimen.medium_padding)))
        Text(
            text = list.subList(0, visibleItems).joinToString(),
            style = MaterialTheme.typography.labelMedium
        )

        if (list.size > 1) {
            AnimatedVisibility(visible = !expandedState) {
                Text(
                    text = "...+${list.size - 1} more",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Blue,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable {
                            expandedState = true
                            visibleItems = list.size
                        }
                )
            }

            AnimatedVisibility(visible = expandedState,) {
                Text(
                    text = "Show less",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Blue,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable {
                            expandedState = false
                            visibleItems = 1
                        }
                )
            }
        }
    }
}

@Preview
@Composable
fun DetailViewPreview(){
    BookBuddyTheme {
        DetailView(detailViewState = DetailScreenState.DetailView(fakeData.books[0]))
    }
}
