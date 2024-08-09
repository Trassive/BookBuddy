package com.example.bookbuddy.ui

import android.util.Log
import androidx.compose.animation.core.EaseOut
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
import com.example.bookbuddy.ui.util.CustomSearchBar
import com.example.bookbuddy.ui.util.HomeScreenTopBar
import com.example.compose.BookBuddyTheme
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
            EmptyScreen(modifier = Modifier.fillMaxSize())
        }
        when(val state = homeScreenUiState){
            is HomeUiState.isLoading -> {}
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
fun EmptyScreen(modifier: Modifier) {

}
@Composable
fun HomeViewContent(homeUiState: HomeUiState.HomeView, modifier: Modifier = Modifier) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isSnapped by remember{ derivedStateOf { lazyListState.firstVisibleItemIndex>0 } }
    val height = with(LocalDensity.current){
        min(400.dp, LocalConfiguration.current.screenHeightDp.dp*0.45f)
    }

    val alpha by animateFloatAsState(
        targetValue = if(isSnapped) 0f else 1f,
        label = " Carousel Snap",
        animationSpec = tween(durationMillis = 1000, easing = EaseOut)
    )
    val stop = with(LocalDensity.current){ 50.dp.toPx()}
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = dimensionResource(id = R.dimen.medium_padding),
            vertical = dimensionResource(id = R.dimen.large_padding)
        )
    ){

        item{
            Box(
                modifier = Modifier
                    .height(height)
                    .fillMaxWidth()
                    .alpha(alpha)
                    .graphicsLayer {
                        lerp(
                            start = 0f,
                            stop = stop.absoluteValue,
                            fraction = 1-alpha
                        ).also { translationY = it }
                    }
            ){
                CarouselPager(
                    books = homeUiState.carauselBooks,
                    onDrag = {},
                    modifier = Modifier
                        .height(height)
                        .fillMaxWidth()
                )
            }
            Spacer(Modifier.height(40.dp))
        }
        items(items = homeUiState.bookList, key = { it.id }) { book ->
            BookCard(
                book = book,
                onClick = {  },
                onLongPress = {  },
                diskCachePolicy = CachePolicy.DISABLED,
                memoryCachePolicy = CachePolicy.ENABLED,
                modifier = Modifier.height(150.dp)
            )
        }
    }
    val px = with(LocalDensity.current){ height*0.7f.dp.toPx()}
    val px2 = with(LocalDensity.current){ height*0.2f.dp.toPx()}
    LaunchedEffect(Unit){
        snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
            .collect{offset->
                if(offset> px2.value && !isSnapped ){
                    coroutineScope.launch { lazyListState.animateScrollToItem(1) }
                }else if(offset.toFloat() == px.value&& isSnapped){
                    Log.d("inverse Scroll","true")
                    coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                }
                Log.d("inverse offset", "$offset   ${px.value}  ${offset < px.value}")
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
        }    }
}


