package com.example.bookbuddy.ui.readerscreen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookbuddy.R
import com.example.bookbuddy.ui.util.CustomTopBar
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import org.readium.r2.navigator.epub.EpubNavigatorFragment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(viewModel: ReaderViewModel, onArrowClick: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            CustomTopBar(
                topBarTitle = (state as? ReaderUiState.Success)?.bookTitle?:"",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                onArrowClick = onArrowClick
            )
        }
    ) {innerPadding ->
        Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())){
            when(state){
                ReaderUiState.IsLoading -> {
                    LottieAnimationComposable(
                        id = R.raw.loading,
                        modifier = Modifier.padding(dimensionResource(id =R.dimen.large_padding))
                    )
                }
                is ReaderUiState.Success -> {
                    ReaderContent(
                        fragmentFactory = (state as ReaderUiState.Success).fragment,
                        onUpdate = viewModel::onUpdate,
                    )
                }
                is ReaderUiState.Error -> {
                    LottieAnimationComposable(
                        id = R.raw.empty,
                        modifier = Modifier.padding(dimensionResource(id =R.dimen.large_padding))
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderContent(fragmentFactory: FragmentFactory, onUpdate: (EpubNavigatorFragment)->Unit){
    val fragmentManager = (LocalContext.current as FragmentActivity).supportFragmentManager
    fragmentManager.fragmentFactory = fragmentFactory
    AndroidFragment<EpubNavigatorFragment>(
        onUpdate = {
            onUpdate(it)
        }
    )

}

