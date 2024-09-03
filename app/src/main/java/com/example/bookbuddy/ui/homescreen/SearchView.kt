package com.example.bookbuddy.ui.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import coil.request.CachePolicy
import com.example.bookbuddy.R
import com.example.bookbuddy.data.fakeData
import com.example.bookbuddy.model.Book
import com.example.bookbuddy.ui.util.BookList
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import com.example.compose.BookBuddyTheme

@Composable
fun SearchView(
    homeUiState: HomeUiState.SearchView,
    onToggleSave: (Int, Book) -> Unit,
    updateBottomSheetBook: (Int) -> Unit,
    loadMore: ()->Unit,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
){
    if(homeUiState.isSearching){
        LottieAnimationComposable(R.raw.loading, modifier = Modifier.fillMaxSize())
    } else {
        BookList(
            books = homeUiState.bookList,
            isLoading = homeUiState.isLoading,
            updateBottomSheetBook = updateBottomSheetBook,
            onToggleSave = onToggleSave,
            onClick = onClick,
            loadMore = loadMore,
            diskCachePolicy = CachePolicy.DISABLED,
            memoryCachePolicy = CachePolicy.ENABLED,
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.large_padding)),
            modifier = modifier
                .background(Color(0xD3FFDEB9))
                .fillMaxSize()
        )
    }

}
@Preview(showBackground = true)
@Composable
fun SearchViewPreview(){
    BookBuddyTheme {
        SearchView(
            homeUiState = HomeUiState.SearchView(searchText = "", bookList = fakeData.books),
            onClick = {},
            loadMore = {},
            onToggleSave = {_,_ ->},
            updateBottomSheetBook = {}
        )
    }
}