package com.example.bookbuddy.ui.homescreen

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookbuddy.R
import com.example.bookbuddy.ui.util.HomeScreenTopBar
import com.example.bookbuddy.ui.util.LottieAnimationComposable


@Composable
fun HomeScreen(viewModel: HomeScreenViewModel, onClick: (Int) -> Unit) {
    val homeScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember{ SnackbarHostState()}

    Scaffold(
        topBar = {
            HomeScreenTopBar(
                isSearching = homeScreenUiState is HomeUiState.SearchView,
                searchTextState = (homeScreenUiState as? HomeUiState.SearchView)?.searchText?:"",
                onStateToggle = viewModel::toggleSearchState,
                onValueChange = viewModel::onSearchQueryChange,
                onSearchClicked = viewModel::onSearchClick
            )
        },

        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {innerPadding ->

        var errorScreen by remember{mutableStateOf(true)}
        if(errorScreen){
            LottieAnimationComposable(R.raw.empty,modifier = Modifier.fillMaxSize())
        }
        when(val state = homeScreenUiState){
            is HomeUiState.IsLoading -> {
                Log.d("HomeScreen", "HomeUiState.IsLoading")
                LottieAnimationComposable(R.raw.loading,modifier = Modifier.fillMaxSize())
            }
            is HomeUiState.HomeView ->{
                HomeViewContent(
                    homeUiState = state,
                    loadMore = viewModel::updateBooks,
                    onClick = onClick,
                    modifier = Modifier.padding(top =innerPadding.calculateTopPadding()).fillMaxSize()
                )
                errorScreen = false
            }
            is HomeUiState.SearchView ->{
                SearchView(
                    homeUiState = state,
                    onClick = onClick,
                    modifier = Modifier.padding(top =innerPadding.calculateTopPadding()).fillMaxSize()
                )
            }
            is HomeUiState.Error ->{
                if(state.error.isNotEmpty()){
                    HomeScreenSnackBar(
                        state = state,
                        snackbarHostState = snackbarHostState,
                        onDismiss = viewModel::messageDismissed,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreenSnackBar(
    state: HomeUiState.Error,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val message = stringResource(state.error.first())
    LaunchedEffect(key1 = message) {
        val action = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Retry",
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
        when (action) {
            SnackbarResult.Dismissed -> onDismiss()
            SnackbarResult.ActionPerformed -> onRetry()
        }
    }
}









