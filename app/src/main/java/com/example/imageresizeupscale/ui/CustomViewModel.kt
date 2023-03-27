package com.example.imageresizeupscale.ui

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import com.example.imageresizeupscale.data.DataLayerFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.DecimalFormat

class CustomViewModel : ViewModel() {
    // Create the state variables along with public readability of state
    private val _uiState = MutableStateFlow(AppState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()

    private val df = DecimalFormat("#.##")
    private var originalImage: Bitmap? = null

    /*
    * Updates the urlText input state variable
    * */
    fun updateUrlText(text: String) {
        _uiState.update { currentState ->
            currentState.copy(
                urlText = text
            )
        }
    }

    /*
    * Updates the pcStatus state variable
    * TODO: link to a worker that pings the pc (this should be in the data layer)
    * */
    fun updatePcStatus(isOn: Boolean) {
        var status = if (isOn) "PC Online" else "PC Offline"

        _uiState.update { currentState ->
            currentState.copy(
                pcStatus = status
            )
        }
    }

    suspend fun updateImageFromUri(context: Context, uri: Uri, upscaleFactor: Int) {
        val source: ImageDecoder.Source = ImageDecoder.createSource(context.contentResolver, uri)
        // Create software bitmap image
        // This was required as using ImageDecoder to decode the bitmap directly resulted in a
        // hardware bitmap that is incompatible with android.graphics.Canvas (utilised later)
        val stream = ByteArrayOutputStream()
        ImageDecoder.decodeBitmap(source).compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        originalImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        // Upscale image if the option is given
        if (upscaleFactor != 0) {
            originalImage = DataLayerFunctions().upscaleImage(originalImage!!, upscaleFactor)
        }
        // Compress and scale image for initial viewing
        val fileName = DataLayerFunctions().getFileNameFromUri(context, uri).split(".")[0]
        val (image, imageSize) = DataLayerFunctions().compressAndScaleBitmap(originalImage!!,
            _uiState.value.scaleFactor, _uiState.value.compressionFactor, _uiState.value.fillColour)
        // Update the state
        _uiState.update { currentState ->
            currentState.copy(
                imageBitmap = image,
                imageSizeInBytes = imageSize,
                fileName = fileName,
                urlText = ""
            )
        }
    }

    fun updateImageUri(uri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(
                imageUri = uri
            )
        }
    }

    // TODO: Need to catch exceptions when trying to generate bitmap from a failed URL fetch
    suspend fun onStartButtonPressed(url: String, upscaleFactor: Int) {
        if (URLUtil.isValidUrl(url)) {
            // Obtain image data from url and generate bitmap
            val formattedUrl = URL(url)
            formattedUrl.openStream()
            val byteArray = formattedUrl.readBytes()
            originalImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            // Upscale image if the option is given
            if (upscaleFactor != 0) {
                originalImage = DataLayerFunctions().upscaleImage(originalImage!!, upscaleFactor)
            }
            // Compress and scale image for initial viewing
            val (image, imageSize) = DataLayerFunctions().compressAndScaleBitmap(originalImage!!,
                _uiState.value.scaleFactor, _uiState.value.compressionFactor, _uiState.value.fillColour)
            // Update state
            _uiState.update { currentState ->
                currentState.copy(
                    imageBitmap = image,
                    imageSizeInBytes = imageSize,
                    fileName = getFileNameFromUrl(url),
                    urlText = ""
                )
            }
        }
    }

    private fun getFileNameFromUrl(url: String): String {
        val startIndex = url.lastIndexOf("/") + 1
        val endIndex = url.lastIndexOf(".")
        return if (startIndex != -1 && endIndex != -1) {
            url.substring(startIndex, endIndex)
        } else {
            ""
        }
    }

    fun updateTransparencyFill(fillColour: String) {
        val colour = if (fillColour == "White") {
            Color.WHITE
        } else {
            Color.BLACK
        }
        val (image, imageSize) = DataLayerFunctions().compressAndScaleBitmap(originalImage!!,
            _uiState.value.scaleFactor, _uiState.value.compressionFactor, colour)
        _uiState.update { currentState ->
            currentState.copy(
                fillColour = colour,
                imageBitmap = image,
                imageSizeInBytes = imageSize
            )
        }
    }

    fun onScaleFactorChange(value: Float) {
        _uiState.update { currentState ->
            currentState.copy(
                scaleFactor = df.format(value).toFloat()
            )
        }
        updateImageBitmap()
    }

    fun onCompressFactorChange(value: Float) {
        _uiState.update { currentState ->
            currentState.copy(
                compressionFactor = df.format(value).toFloat()
            )
        }
        updateImageBitmap()
    }

    private fun updateImageBitmap() {
        val (image, imageSize) = DataLayerFunctions().compressAndScaleBitmap(originalImage!!,
            _uiState.value.scaleFactor, _uiState.value.compressionFactor, _uiState.value.fillColour)
        _uiState.update { currentState ->
            currentState.copy(
                imageBitmap = image,
                imageSizeInBytes = imageSize
            )
        }
    }

    fun clearImage() {
        _uiState.update { currentState ->
            currentState.copy(
                imageBitmap = null,
                upscaleFactor = 0
            )
        }
    }

    fun updateFileName(newFileName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                fileName = newFileName
            )
        }
    }

    fun updateUpscaleFactor(newValue: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                upscaleFactor = newValue
            )
        }
    }
}