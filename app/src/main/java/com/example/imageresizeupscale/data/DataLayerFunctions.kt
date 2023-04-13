package com.example.imageresizeupscale.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.scale
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.URL


class DataLayerFunctions {
    data class PostData(val dataURL: String, val scaleFactor: Int, val type: String)

    object JSON {
        fun stringify(e: Any) = ObjectMapper().writeValueAsString(e) ?: ""
    }

    fun testPcConnection(ipAddress: String): Boolean {
        val address: InetAddress = InetAddress.getByName(ipAddress)
        return address.isReachable(3000)
    }

    fun compressAndScaleBitmap(bitmap: Bitmap, scaleFactor: Float,
                               compressFactor: Float, fillColour: Int): Pair<Bitmap, Int> {
        val filledBitmap = fillTransparency(bitmap, fillColour)
        val scaledBitmap = filledBitmap.scale((scaleFactor*filledBitmap.width).toInt(),
            (scaleFactor*filledBitmap.height).toInt())
        val stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, (compressFactor*100).toInt(), stream)
        val byteArray = stream.toByteArray()
        val bitmapInMemory = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        return Pair(bitmapInMemory, byteArray.size)
    }

    private fun fillTransparency(image: Bitmap, colour: Int): Bitmap {
        val newBitmap: Bitmap = image.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(colour)
        canvas.drawBitmap(image, 0f, 0f, null)
        return newBitmap
    }

    suspend fun upscaleImage(image: Bitmap, scaleFactor: Int): Bitmap? {
        // Convert image to bytearray
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        // Convert bytearray to Base64 encoded string
        val dataUrl = Base64.encodeToString(byteArray, Base64.DEFAULT)
        // Stringify json data for POST requests
        val data = PostData(dataUrl, scaleFactor, "image/png")
        val dataStringified = JSON.stringify(data)
        // Upscale image request to server
        val url = URL("http://192.168.20.8:6969/upscale")
        val client = OkHttpClient()
        val requestBody = dataStringified.toRequestBody("application/json".toMediaType())
        // Send request
        try {
            val request = Request.Builder().run {
                url(url)
                post(requestBody)
                build()
            }
            val byteArrayBody = client.newCall(request).execute().use { response ->
                response.body!!.bytes()
            }
            return BitmapFactory.decodeByteArray(byteArrayBody, 0,
                byteArrayBody.size)
        } catch (e: Exception) {
            Log.e("MainActivity", e.message ?: "Could not complete request to upscale")
            return null
        }
    }

    /*
    * For saving the bitmap image to a given location
    * Should also give notification of saved image
    * @param bitmap
    * @param compressFactor
    * @param path
    * @param fileName
    * */
    fun saveBitmapToDirectory(bitmap: Bitmap, compressFactor: Int, file: File) {
        try {
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressFactor, stream)
            }
        } catch (e: IOException) {
            Log.e("DataLayerFunctions", "Error message: ${e.message}")
        }
    }

    /*
    * https://stackoverflow.com/questions/70795185/android-how-to-get-file-name
    * */
    @SuppressLint("Range")
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        val fileName: String?
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        fileName = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        cursor?.close()
        return fileName ?: ""
    }

    fun getFileNameFromUrl(url: String): String {
        val startIndex = url.lastIndexOf("/")
        val endIndex = url.lastIndexOf(".")
        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            val unfiltered = url.substring(startIndex + 1, endIndex)
            DataLayerFunctions().filterFileNameString(unfiltered)
        } else {
            ""
        }
    }

    fun filterFileNameString(fileName: String): String {
        val reservedChars = Regex("[/.,?\\[\\]'\"\\\\<*|:>+]")
        return fileName.replace(reservedChars, "")
    }
}