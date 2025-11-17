package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    // Helper untuk manage file di MediaStore
    private lateinit var providerFileManager: ProviderFileManager

    // Data model untuk file foto & video
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null

    // Menandai apakah user sedang capture video atau bukan
    private var isCapturingVideo: Boolean = false

    // ActivityResultLauncher untuk capture image & video
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi ProviderFileManager
        providerFileManager = ProviderFileManager(
            applicationContext,
            FileHelper(applicationContext),
            contentResolver,
            Executors.newSingleThreadExecutor(),
            MediaContentHelper()
        )

        // Inisialisasi launcher untuk foto
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) {
                // setelah foto diambil, simpan ke MediaStore
                providerFileManager.insertImageToStore(photoInfo)
            }

        // Inisialisasi launcher untuk video
        takeVideoLauncher =
            registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
                // setelah video diambil, simpan ke MediaStore
                providerFileManager.insertVideoToStore(videoInfo)
            }

        // Tombol Photo
        findViewById<Button>(R.id.photo_button).setOnClickListener {
            // flag: kita sedang capture foto
            isCapturingVideo = false
            // cek izin storage, kalau OK buka kamera foto
            checkStoragePermission {
                openImageCapture()
            }
        }

        // Tombol Video
        findViewById<Button>(R.id.video_button).setOnClickListener {
            // flag: kita sedang capture video
            isCapturingVideo = true
            // cek izin storage, kalau OK buka kamera video
            checkStoragePermission {
                openVideoCapture()
            }
        }
    }

    // Buka kamera untuk ambil foto
    private fun openImageCapture() {
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())
        photoInfo?.let { info ->
            takePictureLauncher.launch(info.uri)
        }
    }

    // Buka kamera untuk rekam video
    private fun openVideoCapture() {
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())
        videoInfo?.let { info ->
            takeVideoLauncher.launch(info.uri)
        }
    }

    // Cek permission WRITE_EXTERNAL_STORAGE (hanya Android 9 ke bawah)
    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        // Android 10 ke atas tidak perlu permission ini
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
                // permission sudah diberikan
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                // belum diberikan, minta ke user
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            // Android 10+ langsung jalan
            onPermissionGranted()
        }
    }

    // Handle hasil request permission (Android 9 ke bawah)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Kalau permission sudah diberi, buka kamera sesuai flag
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
                return
            }
            else -> {
                // request code lain: tidak melakukan apa-apa
            }
        }
    }
}
