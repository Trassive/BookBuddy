package com.example.bookbuddy.ui.util

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookbuddy.R
import com.example.bookbuddy.model.DownloadState
import com.example.compose.BookBuddyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@Composable
fun DownloadButton(
    downloaded: Boolean,
    downloadState: DownloadState,
    onDownloadClick: ()-> Unit,
    afterDownloadClick: ()-> Unit,
    modifier: Modifier = Modifier
){

    FilledTonalButton(
        onClick = {
            if(!downloaded) onDownloadClick() else afterDownloadClick()
        },
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.medium_padding)),
        colors = ButtonDefaults.buttonColors(Color(0xFFFFB959)),
        modifier = modifier
            .height(60.dp)
            .fillMaxWidth()
    ){
        Row(horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            when (downloadState) {
                is DownloadState.Downloading -> {
                    Text(
                        text = stringResource(R.string.downloading),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = dimensionResource(id = R.dimen.medium_padding))
                    )
                    Box(Modifier.fillMaxHeight()){
                        val progress = downloadState.progress.toFloat()
                        CircularProgressIndicator(
                            progress = { progress/100f},
                            color = Color(0xFFFECE97),
                        )
                        Text(
                            text = stringResource(R.string.progress, progress.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = modifier
                                .align(Alignment.Center)
                                .padding(dimensionResource(id = R.dimen.medium_padding))
                        )
                    }
                }

                is DownloadState.Idle -> {
                    Text(
                        text = stringResource(R.string.download),
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.medium_padding))
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_download_24),
                        contentDescription = null
                    )
                }

                is DownloadState.Finished -> {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = dimensionResource(id = R.dimen.medium_padding))){
                        Icon(
                            painter = painterResource(id = R.drawable.round_menu_book_24),
                            contentDescription = null,
                            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.medium_padding))
                        )
                        Text(
                            text = stringResource(R.string.start_reading)
                        )
                    }
                }

                is DownloadState.Failed -> {
                    Text(
                        text = stringResource(R.string.error_try_again),
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
    val downloaded by remember{ mutableStateOf(false) }
    var flow: Flow<DownloadState>
    var downloadState by remember{ mutableStateOf<DownloadState>(DownloadState.Idle) }
    var triggerDownload by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = triggerDownload) {
        if(triggerDownload){
            flow = downloadBook()
            scope.launch {
                flow.collectLatest {
                    downloadState = it
                }
            }
            Log.d("DownloadButton", "triggerDownload: $downloadState")
        }
    }
    BookBuddyTheme {
        DownloadButton(
            downloadState = downloadState,
            downloaded = downloaded,
            onDownloadClick = { triggerDownload = true },
            afterDownloadClick = {}
        )
    }
}
private fun downloadBook(): Flow<DownloadState> = flow {
    emit(DownloadState.Downloading(0))
    for (i in 1..100){
        delay(100)
        emit(DownloadState.Downloading(i))
    }
    emit(DownloadState.Finished)
}
