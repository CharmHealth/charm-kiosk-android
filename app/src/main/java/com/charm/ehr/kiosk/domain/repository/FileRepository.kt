package com.charm.ehr.kiosk.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import okhttp3.ResponseBody
import java.io.File

interface FileRepository {
    fun getUriFromFilePath(filePath: String): Uri
    suspend fun processUriToFile(uri: Uri): File?
    suspend fun getUriSize(uri: Uri): Long
    suspend fun compressImageUriAndWriteToFile(imageUri: Uri, maxSizeBytes: Long, quality: Int): File?
    suspend fun copyUriToFile(uri: Uri): File?
    suspend fun getResponseBodyToFile(responseBody: ResponseBody, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): File?
}
