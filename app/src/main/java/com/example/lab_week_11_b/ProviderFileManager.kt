package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.concurrent.Executor

// Helper untuk mengelola file di MediaStore
class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {

    // Generate FileInfo untuk foto
    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_$time.jpg"
        val file = File(
            context.getExternalFilesDir(fileHelper.getPicturesFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getPicturesFolder(),
            "image/jpeg"
        )
    }

    // Generate FileInfo untuk video
    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_$time.mp4"
        val file = File(
            context.getExternalFilesDir(fileHelper.getVideosFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getVideosFolder(),
            "video/mp4"
        )
    }

    // Masukkan image ke MediaStore
    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                it,
                mediaContentHelper.getImageContentUri(),
                mediaContentHelper.generateImageContentValues(it)
            )
        }
    }

    // Masukkan video ke MediaStore
    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                it,
                mediaContentHelper.getVideoContentUri(),
                mediaContentHelper.generateVideoContentValues(it)
            )
        }
    }

    // Proses insert ke MediaStore (copy file dari app ke lokasi MediaStore)
    private fun insertToStore(
        fileInfo: FileInfo,
        contentUri: Uri,
        contentValues: ContentValues
    ) {
        executor.execute {
            val insertedUri = contentResolver.insert(contentUri, contentValues)
            insertedUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(fileInfo.uri)
                val outputStream = contentResolver.openOutputStream(uri)
                if (inputStream != null && outputStream != null) {
                    IOUtils.copy(inputStream, outputStream)
                    inputStream.close()
                    outputStream.close()
                }
            }
        }
    }
}
