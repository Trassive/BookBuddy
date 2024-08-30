package com.example.bookbuddy.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookbuddy.R
import com.example.bookbuddy.ui.util.CustomTopBar
import com.example.compose.BookBuddyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewmodel: SettingsViewModel){
    val state by viewmodel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CustomTopBar(topBarTitle = "Settings")
        }
    ){innerPadding->
        SettingView(
            state = state,
            onClick = viewmodel::onScrollToggle,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        )

    }
}

@Composable
private fun SettingView(
    state: SettingsUiState,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedCard {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            dimensionResource(id = R.dimen.medium_padding)
                        )
                ) {
                    Column{
                        Text(text = "Scroll", style = MaterialTheme.typography.titleMedium)
                        Text( text = " Enable or disable scroll", style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(
                        checked = state.isScrollEnabled, onCheckedChange = onClick
                    )
                }

            }
        }
    }
}
@Preview
@Composable
fun PreviewSettingsScreen(){
    var state by remember {
        mutableStateOf(SettingsUiState(false))
    }
    BookBuddyTheme{
        SettingView(
            state = state,
            onClick = { state = state.copy(isScrollEnabled = !state.isScrollEnabled) }
        )
    }
}