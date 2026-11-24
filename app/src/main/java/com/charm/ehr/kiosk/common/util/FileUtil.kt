package com.charm.ehr.kiosk.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlin.math.log10
import kotlin.math.pow

object FileUtil {

    //Note:Do not change these values after production
    const val tempInputPathName = "temp_input_"
    const val compressedPathName = "comp_"

    enum class FileType(val extension: String) {
        JPG("jpg"),
        PNG("png"),
        PDF("pdf"),
        WEBP("webp")
    }

    /**
     * Specifies where to store the file: cache or files directory.
     */
    enum class StorageDir { CACHE }

    private fun getDirectory(context: Context, storageDir: StorageDir): File =
        when (storageDir) {
            StorageDir.CACHE -> context.cacheDir
//            StorageDir.FILES -> context.filesDir
        }

    /**
     * Suspends and compresses a Bitmap into a file in the chosen storage directory.
     */
    suspend fun bitmapToFile(
        context: Context,
        bitmap: Bitmap,
        quality: Int = 100,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        storageDir: StorageDir = StorageDir.CACHE
    ): File? = withContext(Dispatchers.IO) {
        try {
            val extension = getExtension(format)
            val fileName = "${extension.uppercase()}_${UUID.randomUUID()}.$extension"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
            }
            file
        } catch (e: Exception) {
            Log.e("FileUtil", "bitmapToFile failed: ${e.message}", e)
            null
        }
    }

    /**
     * Suspends and writes a ResponseBody to a file in the cache directory.
     * Useful for downloading images or files from a network response.
     */
    suspend fun responseBodyToFile(
        responseBody: ResponseBody, context: Context,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): File? = withContext(Dispatchers.IO) {
        try {
            val extension = getExtension(format)
            val fileName = "${extension.uppercase()}_${UUID.randomUUID()}.$extension"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).buffered().use { fos ->
                fos.write(responseBody.bytes())
                fos.close()
            }
            file
        } catch (e: Exception) {
            Log.e("FileUtil", "responseBodyToFile failed: ${e.message}", e)
            null
        }
    }

    /**
     * Suspends and decodes a Bitmap from a File with scaling.
     */
    suspend fun fileToBitmap(
        file: File,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            Log.e("FileUtil", "fileToBitmap failed: ${e.message}", e)
            null
        }
    }

    /**
     * Suspends and decodes a Bitmap from a Uri with scaling.
     */
    suspend fun uriToBitmap(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
                options.inJustDecodeBounds = false
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            }
        } catch (e: Exception) {
            Log.e("FileUtil", "uriToBitmap failed: ${e.message}", e)
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun getExtension(format: Bitmap.CompressFormat): String {
        return when {
            format == Bitmap.CompressFormat.JPEG -> "jpg"
            format == Bitmap.CompressFormat.PNG -> "png"
            // Handle WEBP_LOSSY and WEBP_LOSSLESS for API 30+
            Build.VERSION.SDK_INT >= 30 && format.name == "WEBP_LOSSY" -> "webp"
            Build.VERSION.SDK_INT >= 30 && format.name == "WEBP_LOSSLESS" -> "webp"
            // Fallback for deprecated WEBP
            format == Bitmap.CompressFormat.WEBP -> "webp"
            else -> "img"
        }
    }

    suspend fun clearTempFiles(context: Context) = withContext(Dispatchers.IO) {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith(tempInputPathName) ||
                    file.name.startsWith(compressedPathName)
                ) {
                    file.deleteRecursively()
                }
            }
        } catch (e: Exception) {
            Log.e("FileUtil", "clearTempFiles failed: ${e.message}", e)
        }
    }

    fun createFileForType(context: Context, type: FileType): File? {
        return try {
            val fileName = "${type.name}_${UUID.randomUUID()}.${type.extension}"
            File(context.cacheDir, fileName)
        } catch (e: Exception) {
            Log.e("FileUtil", "createFileForType failed: ${e.message}", e)
            null
        }
    }

    suspend fun compressImageUriAndWriteToFile(
        context: Context,
        imageUri: Uri,
        maxSizeBytes: Long = 1200_000L,
        quality: Int = 80
    ): File? = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(imageUri)
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/jpeg", "image/jpg" -> "jpg"
            else -> "jpg"
        }

        val tempInputFile = File(context.cacheDir, "${tempInputPathName}${UUID.randomUUID()}.$extension")
        val compressedFile = File(context.cacheDir, "${UUID.randomUUID()}.$extension")

        try {
            // Copy URI to temp file
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(tempInputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext null

            if (!tempInputFile.exists() || tempInputFile.length() == 0L) {
                return@withContext null
            }

            // Get and validate dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(tempInputFile.absolutePath, options)

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                Log.e("FileUtil", "Invalid image dimensions")
                return@withContext null
            }

            // Compress
            Compressor.compress(context, tempInputFile) {
                default(
                    width = options.outWidth,
                    height = options.outHeight,
                    format = when (extension) {
                        "png" -> Bitmap.CompressFormat.PNG
                        "webp" -> Bitmap.CompressFormat.WEBP
                        else -> Bitmap.CompressFormat.JPEG
                    },
                    quality = quality
                )
                size(maxSizeBytes)
                destination(compressedFile)
            }

            // Validate compressed file
            if (!compressedFile.exists() || compressedFile.length() == 0L) {
                Log.e("FileUtil", "Compression produced empty file")
                return@withContext null
            }

            return@withContext compressedFile

        } catch (e: Exception) {
            Log.e("FileUtil", "Compression failed: ${e.message}", e)
            tempInputFile.delete()
            compressedFile.delete()
            return@withContext null
        } finally {
            tempInputFile.delete()
        }
    }

    fun String?.getFileFromFilePath(): File? {
        return try {
            this ?: return null
            val file = File(this)
            if (!file.exists()) return null
            file
        } catch (e: Exception) {
            Log.e("FileUtil", "getFileFromFilePath failed: ${e.message}", e)
            null
        }
    }

    fun createRequestBodyForFile(file: File?): Pair<RequestBody, String>? {
        return try {
            if (file?.exists() != true) return null
            val fileName = file.name ?: ""
            val typeStr = when {
                fileName.endsWith("pdf") -> "pdf/*"
                fileName.endsWith("wav") -> "/*"
                else -> "image/*"
            }
            if (typeStr.isNotBlank())
                Pair(file.asRequestBody(typeStr.toMediaTypeOrNull()), file.name)
            else null
        } catch (e: Exception) {
            Log.e("FileUtil", "createRequestBodyForFile failed: ${e.message}", e)
            null
        }
    }

    fun createRequestBodyFromUri(context: Context, uri: Uri, mimeType: String): RequestBody {
        return object : RequestBody() {
            override fun contentType() = mimeType.toMediaTypeOrNull()

            override fun contentLength(): Long {
                return try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use {
                        it.statSize
                    } ?: -1
                } catch (e: Exception) {
                    Log.e("FileUtil", "contentLength failed: ${e.message}", e)
                    -1
                }
            }

            override fun writeTo(sink: BufferedSink) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        sink.writeAll(inputStream.source())
                    } ?: throw IOException("Unable to open InputStream for URI: $uri")
                }
            }
        }

    fun getUriFromFilePath(context: Context, filePath: String?): Uri {
        if (filePath.isNullOrBlank()) return Uri.EMPTY
        val file = File(filePath)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    suspend fun getMultipartBodyFromUris(uris: List<Uri>, context: Context): ArrayList<MultipartBody.Part> = withContext(Dispatchers.IO) {
        val multipartBody = ArrayList<MultipartBody.Part>()
        uris.forEach { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val requestBody = inputStream.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                    multipartBody.add(MultipartBody.Part.createFormData("file", uri.lastPathSegment, requestBody))
                }
            } catch (e: Exception) {
                Log.d("getMultipartBody", "e:$e")
            }
        }
        multipartBody
    }

    /**
     * Converts bytes into a readable size string (e.g. "1.5 MB", "780 KB").
     */
    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeBytes.toDouble()) / log10(1024.0)).toInt()
        val value = sizeBytes / 1024.0.pow(digitGroups.toDouble())
        return if (digitGroups == 0) {
            String.format("%d %s", sizeBytes, units[0])
        } else {
            String.format("%.1f %s", value, units[digitGroups])
        }
    }

    /**
     * Copies a Uri to a file in the cache directory and returns the File.
     */
    suspend fun copyUriToFile(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {

        val extension = getFileExtension(context, uri) ?: ""
        val fileName = "${UUID.randomUUID()}.$extension"
        val file = File(context.cacheDir, fileName)
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext null
            if (!file.exists() || file.length() == 0L) return@withContext null
            return@withContext file
        } catch (e: Exception) {
            file.delete()
            Log.e("FileUtil", "copyUriToFile failed: ${e.message}", e)
            return@withContext null
        }
    }

    fun getFileExtension(context: Context, uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            if (mimeType != null) {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            } else {
                // Fallback: try to get extension from Uri path
                val path = uri.path ?: return null
                val dotIndex = path.lastIndexOf('.')
                if (dotIndex != -1 && dotIndex < path.length - 1) {
                    path.substring(dotIndex + 1)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FileUtil", "getFileExtension failed: ${e.message}", e)
            null
        }
    }


}
