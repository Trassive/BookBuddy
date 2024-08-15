package com.example.bookbuddy.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bookbuddy.R
import com.example.bookbuddy.model.DownloadState
import com.example.compose.BookBuddyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@Composable
fun DownloadButton(
    download: ()-> StateFlow<DownloadState>,
    onFail: ()->Unit,
    modifier: Modifier = Modifier
){
    var isDownloading by remember{ mutableStateOf(false) }
    var downloadState: DownloadState by remember{ mutableStateOf(DownloadState.Idle) }
    LaunchedEffect(isDownloading) {
        if(isDownloading){
            download().collectLatest { state->
                downloadState = state
                if (state is DownloadState.Finished || state is DownloadState.Failed) {
                    isDownloading = false
                }
            }
        }
    }
    FilledTonalButton(
        onClick = {if(!isDownloading){ isDownloading = true} },
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.medium_padding)),
        colors = ButtonDefaults.buttonColors( Color.Transparent),
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color(0xFFF8DBA4), Color(0xFFE09082))))
    ){
        Row(horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            when (downloadState) {
                is DownloadState.Downloading -> {
                    Text(
                        text = "Downloading..",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = dimensionResource(id = R.dimen.medium_padding))
                    )
                    Box(Modifier.fillMaxHeight()){
                        val progress = (downloadState as DownloadState.Downloading).progress.toFloat()
                        CircularProgressIndicator(
                            progress = { progress/100f},
                        )
                        Text(
                            text = "${progress.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = modifier
                                .align(Alignment.Center)
                                .padding(dimensionResource(id = R.dimen.medium_padding))
                        )
                    }
                }

                is DownloadState.Idle -> {
                    Text(
                        text = "Download",
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.medium_padding))
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_download_24),
                        contentDescription = null
                    )
                }

                is DownloadState.Finished -> {
                    Text(
                        text = "Downloaded",
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.medium_padding))
                    )
                }

                is DownloadState.Failed -> {
                    Text(
                        text = "Downloaded",
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.medium_padding))
                    )
                }
            }
        }
    }
}



@Preview
@Composable
fun ButtonPreview(){
    val scope = rememberCoroutineScope()
    lateinit var flow: StateFlow<DownloadState>
    LaunchedEffect(Unit) {
        flow = flow{
            delay(1000)
            emit(DownloadState.Downloading(20))
            delay(1000)
            emit(DownloadState.Downloading(40))
            delay(1000)
            emit(DownloadState.Downloading(60))
            delay(1000)
            emit(DownloadState.Downloading(80))
            delay(1000)
            emit(DownloadState.Downloading(100))
            delay(2000)
        }.stateIn(scope)
    }
    BookBuddyTheme {
        DownloadButton(download = { flow }, onFail = { /*TODO*/ })
    }
}
