package com.example.bookbuddy.ui.readerscreen

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookbuddy.R
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import org.readium.r2.navigator.epub.EpubNavigatorFragment

@Composable
fun ReaderScreen(viewModel: ReaderViewModel){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold {innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)){
            when(uiState){
                ReaderUiState.IsLoading -> {
                    LottieAnimationComposable(
                        id = R.raw.loading,
                        modifier = Modifier.padding(dimensionResource(id =R.dimen.large_padding))
                    )
                }
                is ReaderUiState.Success -> {
                    ReaderContent((uiState as ReaderUiState.Success).fragment)
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
fun ReaderContent(fragment: EpubNavigatorFragment){
    AndroidView(
        factory = {context->
            FragmentContainerView(context).apply {
                id = View.generateViewId()
                val fragmentManager = (context as FragmentActivity).supportFragmentManager

                fragmentManager.beginTransaction()
                    .replace(id,fragment)
                    .commit()
            }
        }
    )
}

