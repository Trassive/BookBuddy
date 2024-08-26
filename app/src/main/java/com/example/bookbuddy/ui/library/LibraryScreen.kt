
package com.example.bookbuddy.ui.library

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.LibraryBook
import com.example.bookbuddy.ui.util.CustomTopBar
import com.example.bookbuddy.ui.util.LibraryBookList
import com.example.compose.BookBuddyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: LibraryScreenViewModel, onClick: (Int) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CustomTopBar(
                topBarTitle = "Library"
            )
        }
    ){innerPadding->
        CustomTabs(
            savedbooks = state.savedTabBooks,
            downloadedLibraryBooks = state.downloadedTabBooks,
            onClick = onClick,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}
@Composable
fun CustomTabs(
    savedbooks: List<LibraryBook>,
    downloadedLibraryBooks: List<LibraryBook>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()

    val list = listOf("Saved", "Download")

    Column(Modifier.background(Color(0xF0FFFFF0))){

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .padding(
                    vertical = dimensionResource(id = R.dimen.small_padding),
                    horizontal = dimensionResource(id = R.dimen.medium_padding)
                )
                .clip(RoundedCornerShape(50)),
            indicator = {tabPositions ->
                CustomTabIndicator(tabPositions = tabPositions, pagerState = pagerState)
            },
        ) {
            list.forEachIndexed { index, text ->
                val isSelected = pagerState.currentPage == index
                val transition = updateTransition(isSelected, label = "TabTransition")

                val textColor by transition.animateColor(
                    label = "TextColor",
                    targetValueByState = { if (it) Color(0xFF5D4037) else Color(0xFD8D6E63) }
                )

                val scale by transition.animateFloat(
                    label = "Scale",
                    targetValueByState = { if (it) 1.05f else 1f }
                )

                Tab(
                    selected = isSelected,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleSmall,
                            color = textColor
                        )
                    },
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(50.dp))
                )
            }
        }
        HorizontalPager(state = pagerState) {item->
            val books = if(item == 0) savedbooks else downloadedLibraryBooks
            LibraryBookList(
                books = books,
                onClick = onClick,
                memoryCachePolicy = CachePolicy.ENABLED,
                modifier = Modifier.fillMaxSize(),
                diskCachePolicy = CachePolicy.ENABLED
            )
        }
    }
}
@Composable
fun CustomTabIndicator(tabPositions: List<TabPosition>,pagerState: PagerState){

    val transition = updateTransition(pagerState.currentPage, label = "Tab indicator")
    val indicatorLeft by transition.animateDp(
        transitionSpec = {
            if(0 isTransitioningTo 1){
                spring(stiffness = Spring.StiffnessVeryLow)
            } else{
                spring(stiffness =  Spring.StiffnessMedium)
            }
        },
        label = "Indicator left"
    ) { page ->
        tabPositions[page].left
    }
    val indicatorRight by transition.animateDp(
        transitionSpec = {
            if(1 isTransitioningTo 0){
                spring(stiffness = Spring.StiffnessMedium)
            } else{
                spring(stiffness =   Spring.StiffnessVeryLow)
            }
        },
        label = "Indicator right"
    ) { page ->
        tabPositions[page].right
    }
    val color by transition.animateColor(label = "Border color") { page ->
        if (page == 0)  MaterialTheme.colorScheme.inversePrimary else MaterialTheme .colorScheme.primary
    }
    Box(
        Modifier
            .fillMaxSize()
            .wrapContentSize(align = Alignment.BottomStart)
            .offset(x = indicatorLeft)
            .width(indicatorRight - indicatorLeft)
            .padding(4.dp)
            .fillMaxSize()
            .border(
                BorderStroke(2.dp, color),
                RoundedCornerShape(40.dp)
            )
    )
}
@Preview
@Composable
fun CustomTabPreview(){
    BookBuddyTheme {
        CustomTabs(fakeData.libraryBooks,fakeData.libraryBooks, onClick =  {})
    }
}
