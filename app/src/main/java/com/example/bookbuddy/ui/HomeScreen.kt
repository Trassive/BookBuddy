package com.example.bookbuddy.ui

import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
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
fun LottieAnimationComposable(@RawRes id: Int, modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(id))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    Surface(modifier.fillMaxSize()){
        LottieAnimation(
            composition = composition,
            progress = { progress },
            alignment = Alignment.Center,
        )
    }
}
@Composable
fun HomeViewContent(homeUiState: HomeUiState.HomeView, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier){
        CarouselPager(
            books = homeUiState.carauselBooks,
            onDrag = {},
            modifier = Modifier
                .fillMaxWidth()
                .weight(4.5f)

//                .parallaxLayoutModifier(scrollState, 2)
        )
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
                .fillMaxWidth()
                .weight(5.5f),
            )
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


