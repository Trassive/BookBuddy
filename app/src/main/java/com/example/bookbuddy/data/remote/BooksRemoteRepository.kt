package com.example.bookbuddy.data.remote

import android.content.Context
import androidx.core.net.toUri
import com.example.bookbuddy.network.BooksApi
import com.example.bookbuddy.network.RemoteBookList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import org.readium.r2.shared.publication.Locator
import java.io.File
import java.net.URL

interface BooksRemoteRepository{
    suspend fun downloadBook(url: String, id: Int): Flow<InternalDownloadState>
    suspend fun getBooksList(url: String? = null, search: String? = null, topic: String? = null): RemoteBookList
}
class BooksRemoteRepositoryImpl(
    private val booksApi: BooksApi,
    private val context: Context
): BooksRemoteRepository{
    override suspend fun downloadBook(url: String, id: Int): Flow<InternalDownloadState> {

        return booksApi.downloadBook(downloadUrl = url).saveFile(id.toString())
    }

    override suspend fun getBooksList(url: String?, search: String?, topic: String?): RemoteBookList {

        return booksApi.getBooks(url,createQueryMap(search = search, topic = topic))
    }

    private fun ResponseBody.saveFile(fileName: String): Flow<InternalDownloadState> {
        return flow{
            emit(InternalDownloadState.Downloading(0))
            val destinationFile = File(context.filesDir,fileName)

            try {
                byteStream().use { inputStream->
                    destinationFile.outputStream().use { outputStream->
                        val totalBytes = contentLength()
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
                emit(InternalDownloadState.Finished("file://${destinationFile.absolutePath}"))
            } catch (e: Exception) {
                emit(InternalDownloadState.Failed(e))
            }
        }
            .flowOn(Dispatchers.IO).distinctUntilChanged()
    }
    private fun createQueryMap( search: String? = null, topic: String? = null): Map<String, String> {
        val mutableMap = mutableMapOf("mime_type" to "application%2Fepub%2Bzip")

        search?.let{search->
            mutableMap.put("search" , search.replace(" ", "%20"))
        }
        topic?.let { topic->
            mutableMap.put("topic", topic.replace(" ", "%20"))
        }
        return mutableMap
    }

}
sealed class InternalDownloadState {
    data class Downloading(val progress: Int) : InternalDownloadState()
    data class Finished(val filePath: String) : InternalDownloadState()
    data class Failed(val error: Throwable? = null) : InternalDownloadState()
}
private val locator = Locator



