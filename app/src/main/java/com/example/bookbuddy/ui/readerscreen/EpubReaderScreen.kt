package com.example.bookbuddy.ui.readerscreen

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookbuddy.R
import com.example.bookbuddy.ui.util.CustomTopBar
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import org.readium.r2.navigator.epub.EpubNavigatorFragment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(viewModel: ReaderViewModel){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            CustomTopBar(
                topBarTitle = " "
            )
        }
    ) {innerPadding ->
        Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())){
            when(uiState){
                ReaderUiState.IsLoading -> {
                    LottieAnimationComposable(
                        id = R.raw.loading,
                        modifier = Modifier.padding(dimensionResource(id =R.dimen.large_padding))
                    )
                }
                is ReaderUiState.Success -> {
                    ReaderContent((uiState as ReaderUiState.Success).fragment, viewModel::onViewInflated)
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
fun ReaderContent(fragment: EpubNavigatorFragment, onViewInflated: ()->Unit = {}){
    var isViewInflated by remember { mutableStateOf(false) }
    AndroidView(
        factory = {context->
            FragmentContainerView(context).apply {
                id = View.generateViewId()
                val fragmentManager = (context as FragmentActivity).supportFragmentManager

                fragmentManager.beginTransaction()
                    .replace(id,fragment)
                    .commit()
            }
        },
        update = {
            if(!isViewInflated){
                onViewInflated()
                isViewInflated = true
            }
        }
    )
}

