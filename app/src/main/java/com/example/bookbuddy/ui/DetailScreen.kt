package com.example.bookbuddy.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookbuddy.R
import com.example.bookbuddy.ui.util.CoilImage
import com.example.bookbuddy.ui.util.LottieAnimationComposable
import com.example.bookbuddy.ui.util.parallaxLayoutModifier

@Composable
fun DetailScreen(detailScreenViewModel: DetailScreenViewModel){
    val detailScreenState by detailScreenViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    when(val state = detailScreenState){
        is DetailScreenState.Loading -> {
            LottieAnimationComposable(R.raw.empty,modifier = Modifier.fillMaxSize())
        }
        is DetailScreenState.Error ->{
            LottieAnimationComposable(R.raw.empty,modifier = Modifier.fillMaxSize())
        }
        is DetailScreenState.DetailView ->{
            DetailView(state)
        }
    }
}

@Composable
fun DetailView(detailViewState: DetailScreenState.DetailView) {
    val scrollState = rememberScrollState()
    Column(){
        Box(
            Modifier.weight(6f)
                .fillMaxWidth()
        ){
            CoilImage(
                id = detailViewState.book.id,
                imageUrl = detailViewState.book.downloadLink,
//                diskCachePolicy = ,
                modifier = Modifier
                    .parallaxLayoutModifier(scrollState,2)
            )
        }
    }
}

