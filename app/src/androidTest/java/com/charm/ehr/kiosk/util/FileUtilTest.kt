package com.charm.ehr.kiosk.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.charm.ehr.kiosk.common.util.FileUtil
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class FileUtilTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun bitmapToFile_and_fileToBitmap_roundtrip() = runBlocking {
        // Create a simple bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val file = FileUtil.bitmapToFile(context, bitmap)
        assertTrue(file.exists())

        val decoded = FileUtil.fileToBitmap(file)
        assertNotNull(decoded)
        assertEquals(100, decoded!!.width)
        assertEquals(100, decoded.height)
    }

    @Test
    fun uriToBitmap_returnsBitmap() = runBlocking {
        // Write a bitmap to a temp file and get its Uri
        val bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val tempFile = File(context.cacheDir, "test_img.png")
        FileOutputStream(tempFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = Uri.fromFile(tempFile)

        val decoded = FileUtil.uriToBitmap(context, uri)
        assertNotNull(decoded)
        assertEquals(50, decoded!!.width)
        assertEquals(50, decoded.height)
    }
}