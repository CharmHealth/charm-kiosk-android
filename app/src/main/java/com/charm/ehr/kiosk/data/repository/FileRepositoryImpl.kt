package com.charm.ehr.kiosk.data.repository
import com.charm.ehr.kiosk.domain.repository.FileRepository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.charm.ehr.kiosk.common.util.FileUtil
import com.charm.ehr.kiosk.common.util.FileUtil.getUriFromFilePath
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext   private val appContext: Context
) : FileRepository {

    override fun getUriFromFilePath(filePath: String) = getUriFromFilePath(appContext, filePath)

    override suspend fun processUriToFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        val size = getUriSize(uri)
        return@withContext if (size > 2 * 1024 * 1024) {
            FileUtil.compressImageUriAndWriteToFile(appContext, uri)
        } else {
            FileUtil.copyUriToFile(appContext, uri)
        }
    }

    override suspend fun getUriSize(uri: Uri): Long = withContext(Dispatchers.IO) {
        appContext.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            pfd.statSize
        } ?: 0L
    }

    override suspend fun compressImageUriAndWriteToFile(imageUri: Uri, maxSizeBytes: Long, quality: Int) =
        FileUtil.compressImageUriAndWriteToFile(appContext, imageUri, maxSizeBytes, quality)

    override suspend fun copyUriToFile(uri: Uri) = FileUtil.copyUriToFile(context = appContext, uri = uri)

    override suspend fun getResponseBodyToFile(responseBody: ResponseBody,
                                              format: Bitmap.CompressFormat) =  FileUtil.responseBodyToFile(
        responseBody,
        appContext,
        format
    )
}