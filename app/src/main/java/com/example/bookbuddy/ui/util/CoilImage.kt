package com.example.bookbuddy.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.example.bookbuddy.R
import kotlinx.coroutines.Dispatchers

// placeholder error shape animation
@Composable
fun CoilImage(
    id: Int,
    imageUrl: String,
    onError: () ->Unit = {},
    memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
    diskCachePolicy: CachePolicy = CachePolicy.DISABLED,
    modifier: Modifier = Modifier
) {

    val listener = object : ImageRequest.Listener{
        override fun onError(request: ImageRequest, result: ErrorResult) {
            super.onError(request, result)
            onError()
        }
    }
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context)
        .data(imageUrl)
        .listener(listener)
        .dispatcher(Dispatchers.IO)
        .diskCacheKey(id.toString())
        .memoryCacheKey(id.toString())
        .diskCachePolicy(diskCachePolicy)
        .memoryCachePolicy(memoryCachePolicy)
        .build()

    Box(modifier.fillMaxSize()){
        AsyncImage(
            model = imageRequest,
            contentDescription = stringResource(R.string.cover_image),
            contentScale = ContentScale.Crop,
        )
    }
}