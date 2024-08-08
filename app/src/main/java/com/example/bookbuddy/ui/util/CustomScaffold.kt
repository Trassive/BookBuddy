package com.example.bookbuddy.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

