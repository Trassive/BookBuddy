@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bookbuddy.ui.util

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bookbuddy.R
import com.example.compose.BookBuddyTheme

private val EMPTY = {}

@Composable
fun CustomTopBar(
    topBarTitle: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
    onArrowClick: () -> Unit = EMPTY,
    actions: @Composable (RowScope.() -> Unit) = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    ){
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = topBarTitle,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.large_padding),
                    vertical = dimensionResource(id = R.dimen.medium_padding)
                )
            )
        },
        navigationIcon = {
            if(onArrowClick != EMPTY){
                IconButton(onClick = onArrowClick) {
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
            .clip(RoundedCornerShape(60.dp)),
        color = TopAppBarDefaults.centerAlignedTopAppBarColors().containerColor
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
                    tint =TopAppBarDefaults.centerAlignedTopAppBarColors().actionIconContentColor,
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
        )
    }
}
@Composable
fun HomeScreenTopBar(
    isSearching: Boolean = false,
    searchTextState: String = "",
    onValueChange: (String)-> Unit,
    onStateToggle: () -> Unit,
    onSearchClicked: () -> Unit,
){
    AnimatedContent(
        targetState = isSearching,
        transitionSpec = {
            if(targetState){
                (
                    slideInHorizontally(
                    initialOffsetX = { width -> width }
                    ) + fadeIn(
                        tween(400)
                    )
                ).togetherWith(
                      fadeOut()
                )
            } else {
                fadeIn() togetherWith  shrinkHorizontally(shrinkTowards = Alignment.Start) +fadeOut()
            }
        },
        label = "TopBarAnimation"
    ) {state->
        if(state ){
            CustomSearchBar(
                searchTextState = searchTextState,
                onValueChange = onValueChange,
                onCloseClick = onStateToggle,
                onSearch = onSearchClicked,
                modifier = Modifier
                    .windowInsetsPadding(insets = WindowInsets.statusBars)
                    .padding(dimensionResource(id = R.dimen.medium_padding))
            )
        } else{
            CustomTopBar(
                topBarTitle = "HomeScreen",
                actions = {
                    IconButton(onClick = onStateToggle) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_search_24),
                            contentDescription = stringResource(R.string.click_to_search),
                            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.medium_padding))
                        )
                    }
                }
            )
        }
    }
}
@Preview
@Composable
fun PreviewCustomTopBar(){
    BookBuddyTheme{
        CustomTopBar(
            topBarTitle = "HomeScreen",
            actions = {},
        )
    }
}
@Preview
@Composable
fun PreviewCustomSearchBar(){
    BookBuddyTheme{
        CustomSearchBar(
            searchTextState = "Search",
            onSearch = {},
            onCloseClick = {},
            onValueChange = {}
        )
    }
}
@Preview
@Composable
fun PreviewCustomHomeScreenTopBar(){
    BookBuddyTheme{
        var isSearching by remember {
            mutableStateOf(false)
        }
        HomeScreenTopBar(
            isSearching = isSearching,
            searchTextState = "",
            onValueChange = {},
            onStateToggle = {  isSearching = !isSearching},
            onSearchClicked = {

            }
        )
    }
}
