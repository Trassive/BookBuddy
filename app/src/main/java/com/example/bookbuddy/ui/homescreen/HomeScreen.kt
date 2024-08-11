package com.example.bookbuddy.ui.homescreen

import android.util.Log
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.ui.util.BookCard
import com.example.bookbuddy.ui.util.BookList
import com.example.bookbuddy.ui.util.CarouselPager
import com.example.bookbuddy.ui.util.HomeScreenTopBar
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import com.example.compose.BookBuddyTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@Composable
fun HomeScreen(viewModel: HomeScreenViewModel){
    val homeScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember{ SnackbarHostState()}
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
                HomeScreenTopBar(
                    isSearching = homeScreenUiState !is HomeUiState.HomeView,
                    searchTextState = (homeScreenUiState as? HomeUiState.SearchView)?.searchText,
                    onStateToggle = viewModel::toggleSearchState,
                    onValueChange = viewModel::onSearchQueryChange,
                    onSearchClicked = viewModel::onSearchClick
                )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {innerPadding ->

        var errorScreeen by remember{mutableStateOf(true)}
        if(errorScreeen){
            LottieAnimationComposable(R.raw.empty,modifier = Modifier.fillMaxSize())
        }
        when(val state = homeScreenUiState){
            is HomeUiState.isLoading -> {
                LottieAnimationComposable(R.raw.loading,modifier = Modifier.fillMaxSize())
            }
            is HomeUiState.HomeView ->{
                HomeViewContent(state, Modifier.padding(innerPadding))
                errorScreeen = false
            }
            is HomeUiState.SearchView ->{
                SearchView(state, Modifier.padding(innerPadding))
            }
            is HomeUiState.Error ->{
// swipe left
                if(state.error.isNotEmpty()){
                    val message = stringResource(state.error.first())
                    LaunchedEffect(key1 = message) {
                        val action = snackbarHostState.showSnackbar(
                            message = message,
                            actionLabel = "Retry",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                        when(action){
                            SnackbarResult.Dismissed -> (viewModel::messageDismissed)()
                            SnackbarResult.ActionPerformed -> (viewModel::retry)()
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun HomeViewContent(homeUiState: HomeUiState.HomeView, modifier: Modifier = Modifier) {
    val spacer= with(LocalDensity.current){50.dp.toPx()*this.density.toInt()}
    val height = min(400.dp, LocalConfiguration.current.screenHeightDp.dp * 0.6f)


    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }


    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = dimensionResource(id = R.dimen.medium_padding),
            vertical = dimensionResource(id = R.dimen.large_padding)
        )
    ) {
        item {
            Box(
                modifier = Modifier
                    .height(height)
                    .fillMaxWidth()
            ) {
                CarouselPager(
                    books = homeUiState.carauselBooks,
                    onDrag = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(40.dp))
        }
        items(items = homeUiState.bookList, key = { it.id }) { book ->
            BookCard(
                book = book,
                onClick = {},
                onLongPress = {},
                diskCachePolicy = CachePolicy.DISABLED,
                memoryCachePolicy = CachePolicy.ENABLED,
                modifier = Modifier.height(150.dp)
            )
        }
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
                } else if (currentIndex == 1 && isScrollingUp && currentScrollOffset-previousScrollOffset> spacer) {
                    coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                }

                previousIndex = currentIndex
                previousScrollOffset = currentScrollOffset

            }
    }
}
@Composable
fun SearchView(homeUiState: HomeUiState.SearchView, modifier: Modifier = Modifier){
    BookList(
        books = homeUiState.bookList,
        onClick = {},
        onLongPress = {},
        diskCachePolicy = CachePolicy.DISABLED,
        memoryCachePolicy = CachePolicy.ENABLED,
        contentPadding = PaddingValues(
            horizontal = dimensionResource(id = R.dimen.large_padding),
            vertical = 60.dp
        ),
        modifier = Modifier
            .fillMaxSize()
    )
}
@Preview(showBackground = true)
@Composable
fun HomeViewPreview(){
    BookBuddyTheme {
        Surface{
            HomeViewContent(
                homeUiState = HomeUiState.HomeView(
                    carauselBooks = fakeData.books,
                    bookList = fakeData.books
                )
            )
        }
    }
}


