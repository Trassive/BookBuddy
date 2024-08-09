package com.example.bookbuddy.ui.util

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.bookbuddy.R

private val EMPTY = {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    topBarTitle: String,
    onNavigationIconClick: () -> Unit = EMPTY,
    actions: @Composable() (RowScope.() -> Unit),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier,
    ){
    CenterAlignedTopAppBar(
        title = {
            Text( text = topBarTitle, style = MaterialTheme.typography.displayMedium)
        },
        navigationIcon = {
            if(onNavigationIconClick != EMPTY){
                IconButton(onClick = onNavigationIconClick) {
                    Icon(
                        painter = painterResource(R.drawable.round_arrow_back_24),
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

@Composable
fun CustomSearchBar(
    searchTextState: String,
    onSearch: () -> Unit,
    onCloseClick: () -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(MaterialTheme.shapes.large)
    ){
        TextField(
            value = searchTextState,
            onValueChange = { onValueChange(it) },
            singleLine = true,
            placeholder = { Text(text = stringResource(R.string.search_here))},
            leadingIcon = { 
                Icon(
                    painter = painterResource(id = R.drawable.round_search_24),
                    contentDescription = null,
                    modifier = Modifier.alpha(0.7f)
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    if(searchTextState.isNotEmpty())  onValueChange("") else onCloseClick()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_close_24),
                        contentDescription = stringResource(id = R.string.close),
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.medium_padding))
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTopBar(
    isSearching: Boolean = false,
    searchTextState: String? = null,
    onValueChange: (String)-> Unit,
    onStateToggle: () -> Unit,
    onSearchClicked: () -> Unit,
){
    if(isSearching){
        CustomSearchBar(
            searchTextState = searchTextState!!,
            onValueChange = onValueChange,
            onCloseClick = onStateToggle,
            onSearch = onSearchClicked
        )
    } else{
        CustomTopBar(
            topBarTitle = "HomeScreen",
            actions = {
                Icon(
                    painter = painterResource(id = R.drawable.round_search_24),
                    contentDescription = stringResource(R.string.click_to_search),
                )
            }
        )
    }
}