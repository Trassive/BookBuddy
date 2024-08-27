package com.example.bookbuddy.data.util

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import okio.IOException
import retrofit2.Response
import java.io.File


class FileHandler(private val context: Context) {
    suspend fun saveFile(response: Response<ResponseBody>, fileName: String): Flow<InternalDownloadState> =flow{
        val body = response.body()?: throw IOException("Response Body is null")
        emit(InternalDownloadState.Downloading(0))
        val destinationFile = File(context.filesDir,fileName)

        try {
            body.byteStream().use { inputStream->
                destinationFile.outputStream().use { outputStream->
                    val totalBytes = body.contentLength()
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var progressBytes = 0L
                    var bytes = inputStream.read(buffer)
                    while (bytes >= 0) {
                        outputStream.write(buffer, 0, bytes)
                        progressBytes += bytes
                        bytes = inputStream.read(buffer)
                        emit(InternalDownloadState.Downloading(((progressBytes * 100) / totalBytes).toInt()))
                    }
                }
            }

            emit(InternalDownloadState.Finished(destinationFile.name))
        } catch (e: Exception) {
            emit(InternalDownloadState.Failed(e))
        }finally {
            body.close()
        }
    }
    fun deleteFile(fileName: String){
        val file = File(context.filesDir,fileName)
        if(file.exists()){
            file.delete()
        } else {
            throw IllegalArgumentException("File Doesn't exists")
        }
    }
    fun fileExists(fileName: String): Boolean {
       return File(context.filesDir, fileName).exists()
    }
}
sealed class InternalDownloadState {
    data class Downloading(val progress: Int) : InternalDownloadState()
    data class Finished(val filePath: String) : InternalDownloadState()
    data class Failed(val error: Throwable? = null) : InternalDownloadState()
}