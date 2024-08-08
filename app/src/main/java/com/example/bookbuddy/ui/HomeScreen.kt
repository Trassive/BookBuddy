package com.example.bookbuddy.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.ui.theme.AppShapes.bottomRoundedLarge
import com.example.bookbuddy.ui.util.BookList
import com.example.bookbuddy.ui.util.CarouselPager
import com.example.bookbuddy.ui.util.parallaxLayoutModifier
import com.example.compose.BookBuddyTheme


@Composable
fun HomeScreen(viewModel: HomeScreenViewModel){
    val homeScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
        }
    ) {innerPadding ->

        val errorScreeen by remember{mutableStateOf(true)}
        when(homeScreenUiState){
            is HomeUiState.Loading -> {}
            is HomeUiState.HomeView ->{
                HomeViewContent(homeScreenUiState as HomeUiState.HomeView, Modifier.padding(innerPadding))
            }
            is HomeUiState.SearchView ->{}
            is HomeUiState.Error ->{}
        }
    }
}
@Composable
fun EmptyScreen(modifier: Modifier) {

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


