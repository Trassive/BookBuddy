package com.example.bookbuddy.ui.detailscreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.DownloadState
import com.example.bookbuddy.ui.util.CoilImage
import com.example.bookbuddy.ui.util.CustomTopBar
import com.example.bookbuddy.ui.util.DownloadButton
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import com.example.compose.BookBuddyTheme

@ExperimentalMaterial3Api
@Composable
fun DetailScreen(detailScreenViewModel: DetailScreenViewModel, onArrow: () -> Unit, onClick: (Int)->Unit) {
    val detailScreenState by detailScreenViewModel.uiState.collectAsStateWithLifecycle()
    val downloadState by detailScreenViewModel.downloadState.collectAsStateWithLifecycle()
    Log.d("DownloadButton", "D Detail Screen ownloadState: $downloadState")
    Scaffold(
        topBar = {
            CustomTopBar(
                topBarTitle = stringResource(R.string.detail),
                actions = {
                    (detailScreenState as? DetailScreenState.DetailView)?.book?.let{book->
                        if(book.isDownloaded) {
                            IconButton(onClick = detailScreenViewModel::toggleBookState) {
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_delete_24),
                                    contentDescription = stringResource(R.string.delete)
                                )
                            }
                        } else {
                            if (book.isSaved) {
                                IconButton(onClick = detailScreenViewModel::toggleBookState) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.round_bookmark_24),
                                        contentDescription = stringResource(R.string.unsave)
                                    )
                                }
                            } else {
                                IconButton(onClick = detailScreenViewModel::toggleBookState) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_bookmark_add_24),
                                        contentDescription = stringResource(R.string.save)
                                    )
                                }
                            }
                        }
                    }
                },
                onArrowClick = onArrow
            )
        },
        
    ){innerPadding->
        when (val state = detailScreenState) {
            is DetailScreenState.Loading -> {
                LottieAnimationComposable(R.raw.empty, modifier = Modifier
                    .padding(innerPadding.calculateTopPadding())
                    .fillMaxSize())
            }

            is DetailScreenState.Error -> {
                LottieAnimationComposable(R.raw.empty, modifier = Modifier
                    .padding(innerPadding.calculateTopPadding())
                    .fillMaxSize())
            }

            is DetailScreenState.DetailView -> {
                DetailView(
                    detailViewState = state,
                    downloadState = downloadState,
                    onClick = onClick,
                    onDownloadClick = {
                        detailScreenViewModel.downloadBook()
                    },
                    modifier = Modifier
                        .padding(top = innerPadding.calculateTopPadding())
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun DetailView(
    detailViewState: DetailScreenState.DetailView,
    downloadState: DownloadState,
    onClick: (Int) -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier){
        Column {
            BoxWithConstraints(
                Modifier
                    .weight(6f)
                    .background(
                        Brush.verticalGradient(
                            listOf(

                                Color(0xD0E4B56C),
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
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = dimensionResource(id = R.dimen.large_padding),
                            end = dimensionResource(id = R.dimen.large_padding),
                            top = 40.dp,
                            bottom = 0.dp
                        )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(vertical = dimensionResource(id = R.dimen.large_padding))
                            .background(MaterialTheme.colorScheme.surface)

                    ){
                        ExpandableAuthorList(
                            listName = "Categories",
                            list = detailViewState.book.categories,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )

                        ExpandableAuthorList(
                            listName = "Authors",
                            list = detailViewState.book.authors,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.large_padding))
                    )
                    Text(
                        text = "${detailViewState.book.description}\r\n\n\n",
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
        DownloadButton(
            downloadState = downloadState,
            downloaded = detailViewState.book.isDownloaded,
            afterDownloadClick = { onClick(detailViewState.book.id) },
            onDownloadClick = onDownloadClick,
            modifier = Modifier
                .width(maxWidth * 0.68f)
                .align(Alignment.TopCenter)
                .offset(y = (maxHeight * 0.6f) - 24.dp)
        )
    }
}

@Composable
fun ExpandableAuthorList(listName: String, list: List<String>, modifier: Modifier = Modifier) {

    var toggleTextState by remember{ mutableStateOf(Pair(false,"Read More....")) }

    val annotatedString = buildAnnotatedString {
        if(list.size<= 2){
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.W500,
                fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
                fontSize = MaterialTheme.typography.labelLarge.fontSize
            )){
                append(list.joinToString(", "))
            }
            return@buildAnnotatedString
        }
        val baseString = list.take(2).joinToString(", ") + " "
        withStyle(style = SpanStyle(
            fontWeight = FontWeight.W500,
            fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
            fontSize = MaterialTheme.typography.labelLarge.fontSize
        )
        ){
            append(baseString)

            if(toggleTextState.first){
                append(
                    list.drop(2).joinToString(", ") + " "
                )
            }

        }
        val style = SpanStyle(
            color = Color.Blue.copy(alpha = 0.48f),
            fontSize = MaterialTheme.typography.labelLarge.fontSize,
            fontWeight = FontWeight.W600,
            fontStyle = MaterialTheme.typography.labelLarge.fontStyle
        )
        val link = LinkAnnotation.Clickable(
            tag = "collapse",
            styles = TextLinkStyles(
                style = style,
                pressedStyle = style.copy(color = Color.LightGray)
            ),
            linkInteractionListener = {
                toggleTextState = if(toggleTextState.first){
                    Pair(false, "Read More....")
                } else {
                    Pair(true, "Read Less....")
                }
            }
        )
        withLink(link){
            append(toggleTextState.second)
        }

    }
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(listName, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height( dimensionResource( R.dimen.medium_padding)))
        Text(
            text = annotatedString,
            lineHeight = MaterialTheme.typography.labelMedium.lineHeight,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = dimensionResource(id = R.dimen.medium_padding))
                .verticalScroll(rememberScrollState())

        )

    }
}

@Preview
@Composable
fun DetailViewPreview(){
    BookBuddyTheme {
        DetailView(
            detailViewState = DetailScreenState.DetailView(fakeData.books[0]) ,
            downloadState = DownloadState.Idle,
            onClick = {},
            onDownloadClick = {}
        )
    }
}
