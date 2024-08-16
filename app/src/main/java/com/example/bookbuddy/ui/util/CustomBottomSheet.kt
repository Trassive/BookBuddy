package com.example.bookbuddy.ui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.ui.theme.AppShapes.topRoundedLarge
import com.example.compose.BookBuddyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), book: Book,onDismiss: ()->Unit,  onExpand: (Int)->Unit){
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = {}
    ) {

        BoxWithConstraints(modifier = Modifier.fillMaxSize()){
            val maxHeight = maxHeight
            val coverWidth = min(maxWidth,200.dp)
            val coverHeight = coverWidth*1.5f
            Column(Modifier.matchParentSize()){
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .clickable {
                        coroutineScope
                            .launch { sheetState.hide() }
                            .invokeOnCompletion {
                                onDismiss()
                            }
                    }
                )
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.topRoundedLarge)
                        .weight(7f)
                ){
                    Column(
                        modifier = Modifier
                            .offset(y = coverHeight * 0.6f)
                            .fillMaxSize()
                    ){
                        BookContent(
                            book = book,
                            modifier = Modifier
                                .padding(
                                    vertical = dimensionResource(R.dimen.medium_padding),
                                    horizontal = dimensionResource(R.dimen.large_padding)
                                )
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )

                    }
                }
                Surface{
                    BottomButtons(
                        save = {},
                        expand = {},
                        modifier = Modifier
                            .requiredHeight(60.dp)
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.medium_padding))
                    )
                }
            }
            IconButton(
                onClick = {
                coroutineScope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion {
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(dimensionResource(id = R.dimen.large_padding))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.round_close_24),
                    contentDescription = stringResource(R.string.close),
                )
            }
            CoilImage(
                id = book.id,
                imageUrl = book.coverImage,
                onError = {},
                diskCachePolicy = CachePolicy.ENABLED,
                modifier = Modifier
                    .offset(
                        y = maxHeight * 0.3f - coverHeight * 0.4f - 60.dp,
                        x = maxWidth / 2 - coverWidth / 2
                    )
                    .clip(MaterialTheme.shapes.large)
                    .background(Color.Cyan)
                    .size(width = coverWidth, height = coverHeight)
            )

        }
    }
}

@Composable
private fun BookContent(
    book: Book,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .padding(
                dimensionResource(id = R.dimen.medium_padding)
            )
            .align(Alignment.CenterHorizontally)) {
            Text(text = book.authors[0], style = MaterialTheme.typography.titleSmall)
            if (book.authors.size > 1) {
                Text(
                    text = "+${book.authors.size - 1} more",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.categories),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.small_padding))
        )
        Text(
            text = book.categories.joinToString(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.medium_padding))
        )
        Text(
            text = stringResource(R.string.synopsis),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.small_padding))
        )
        Text(
            text = book.description !!,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.medium_padding))
        )
    }
}

@Composable
fun BottomButtons(
    expand: () -> Unit,
    save: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.medium_padding)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Button(
            onClick = expand,
            modifier = Modifier
            .weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding)),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = stringResource(R.string.read_more),
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    painter = painterResource(R.drawable.baseline_open_in_new_24),
                    contentDescription = null,
                )
            }
        }

        IconButton(onClick = save,modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Icon(
                painter =   painterResource(id = R.drawable.outline_bookmark_add_24),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun BottomSheetPreview(){
    BookBuddyTheme {
        CustomBottomSheet(
            onExpand = {},
            onDismiss = {},
            book = Book(
            id = 4,
            title ="Abcd Efgh",
            categories = listOf("Kids","Horror", "Adventure"),
            authors = listOf("hariab ahbcbj", "kbfkawb dbwwd"),
            coverImage = "",
            isDownloaded = false,
            downloadLink = "",
                isSaved = false,
            description ="In general: Density is essentially a measurement of how tightly matter is packed together."+
"            Same here, density means how many no. of pixels are present in a particular area on your screen."+
"                The more the no. of pixels present in an area more will be the density and screen quality."+
"                The screen with high resolution has comparatively smaller pixel sizes so that there can be more densitymore pixels tightly packed together.",
        ))
    }
}
