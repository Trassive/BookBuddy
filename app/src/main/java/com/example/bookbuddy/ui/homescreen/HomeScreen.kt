package com.example.bookbuddy.ui.homescreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.ui.util.BookList
import com.example.bookbuddy.ui.util.HomeScreenTopBar
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import com.example.compose.BookBuddyTheme


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
            is HomeUiState.IsLoading -> {
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
                    carouselBooks = fakeData.books,
                    bookList = fakeData.books
                )
            )
        }
    }
}


