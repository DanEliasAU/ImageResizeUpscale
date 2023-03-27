package com.example.imageresizeupscale.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.net.toUri

data class AppState (
    var urlText: String = "",
    var pcStatus: String = "PC is offline",
    var imageUri: Uri = "".toUri(),
    var imageBitmap: Bitmap? = null,
    var imageSizeInBytes: Int = 0,
    var fileName: String = "",
    var compressionFactor: Float = 0.86F,
    var scaleFactor: Float = 1.00F,
    var fillColour: Int = Color.WHITE,
    var upscaleFactor: Int = 0
)