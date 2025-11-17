package com.example.lab_week_11_b

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class FileHelper(private val context: Context) {

    // Generate URI dari FileProvider
    fun getUriFromFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "com.example.lab_week_11_b.camera",
            file
        )
    }

    // Nama folder Pictures (sesuai file_provider_paths.xml)
    fun getPicturesFolder(): String = Environment.DIRECTORY_PICTURES

    // Nama folder Movies (sesuai file_provider_paths.xml)
    fun getVideosFolder(): String = Environment.DIRECTORY_MOVIES
}
