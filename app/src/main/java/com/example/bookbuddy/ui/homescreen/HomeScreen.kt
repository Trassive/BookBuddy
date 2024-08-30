package com.example.bookbuddy.ui.homescreen

import androidx.compose.foundation.layout.Box
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
        var error by remember{ mutableStateOf(true) }
        Box(
            Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()) {
            when (val state = homeScreenUiState) {
                is HomeUiState.IsLoading -> {
                    LottieAnimationComposable(R.raw.loading, modifier = Modifier.fillMaxSize())
                }

                is HomeUiState.HomeView -> {
                    error = false
                    HomeViewContent(
                        homeUiState = state,
                        loadMore = viewModel::updateBooks,
                        onClick = onClick,
                        onToggleSave = {position,book-> viewModel.toggleSave(position = position, book = book) },
                        modifier = Modifier
                            .fillMaxSize()
                    )

                }

                is HomeUiState.SearchView -> {
                    error = false
                    SearchView(
                        homeUiState = state,
                        loadMore = viewModel::updateBooks,
                        onToggleSave = {position,book-> viewModel.toggleSave(position = position, book = book) },
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                }

                is HomeUiState.Error -> {
                    if(error){
                        LottieAnimationComposable(R.raw.empty, modifier = Modifier.fillMaxSize())
                    }
                    if (state.error.isNotEmpty()) {

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
}

@Composable
private fun HomeScreenSnackBar(
    state: HomeUiState.Error,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    var messageId by remember {
        mutableStateOf<Int?>(state.error[0])
    }
    val message = messageId?.run{ stringResource(id = this)}
    LaunchedEffect(key1 = message) {
        if(message == null) return@LaunchedEffect
        val action = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Retry",
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
        when (action) {
            SnackbarResult.Dismissed -> {
                messageId = null
                onDismiss()
            }
            SnackbarResult.ActionPerformed -> onRetry()
        }
    }
}









