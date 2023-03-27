package com.example.imageresizeupscale.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.media.ThumbnailUtils
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.imageresizeupscale.R
import com.example.imageresizeupscale.data.DataLayerFunctions
import com.smarttoolfactory.zoom.ZoomableImage
import java.io.File
import java.text.DecimalFormat
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OptionsScreen(
    scaleFactor: Float,
    compressFactor: Float,
    onScaleChange: (Float) -> Unit,
    onCompressChange: (Float) -> Unit,
    imageBitmap: Bitmap?,
    imageSizeInBytes: Int,
    fileName: String,
    updateFileName: (String) -> Unit,
    fillColour: Int,
    setTransparencyFill: (String) -> Unit,
    navigateBack: () -> Unit,
    createNotification: (String, String, Bitmap) -> Unit
) {

    val localFocusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(0.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.Center
    ) {
        BackHandler(onBack = navigateBack)

        val localConfiguration = LocalConfiguration.current
        val imageSizeInMB: Float = imageSizeInBytes.toFloat() / (1000*1000F)

        if (imageBitmap != null) {
            // Change layout depending on screen orientation
            when (localConfiguration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImageComposable(
                            imageBitmap = imageBitmap,
                            imageSizeInMB = imageSizeInMB,
                            modifier = Modifier.weight(1f)
                        )
                        OptionsComposable(
                            imageBitmap = imageBitmap,
                            imageSizeInMB = imageSizeInMB,
                            scaleFactor = scaleFactor,
                            compressFactor = compressFactor,
                            onScaleChange = onScaleChange,
                            onCompressChange = onCompressChange,
                            fillColour = fillColour,
                            setTransparencyFill = setTransparencyFill,
                            fileName = fileName,
                            updateFileName = updateFileName,
                            createNotification = createNotification,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ImageComposable(
                            imageBitmap = imageBitmap,
                            imageSizeInMB = imageSizeInMB,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        OptionsComposable(
                            imageBitmap = imageBitmap,
                            imageSizeInMB = imageSizeInMB,
                            scaleFactor = scaleFactor,
                            compressFactor = compressFactor,
                            onScaleChange = onScaleChange,
                            onCompressChange = onCompressChange,
                            fillColour = fillColour,
                            setTransparencyFill = setTransparencyFill,
                            fileName = fileName,
                            updateFileName = updateFileName,
                            createNotification = createNotification,
                            modifier = Modifier.weight(1f)
                        )
                    }

                }
            }
        } else {
            // In case there is not bitmap in state, go back
            navigateBack()
        }
    }
}

@Composable
fun ImageComposable(
    imageBitmap: Bitmap,
    imageSizeInMB: Float,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        var contentScale by remember { mutableStateOf(ContentScale.Fit) }

        ZoomableImage(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .aspectRatio(1f)
                .padding(8.dp)
                .weight(12f),
            imageBitmap = imageBitmap.asImageBitmap(),
            contentScale = contentScale,
            maxZoom = 8f,
            clipTransformToContentScale = false
        )
        Text(
            text = stringResource(id = R.string.image_details, imageSizeInMB,
                imageBitmap.width, imageBitmap.height),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp)
                .weight(1f),
            textAlign = TextAlign.Center
        )
    }

}

@Composable
fun OptionsComposable(
    imageBitmap: Bitmap,
    imageSizeInMB: Float,
    scaleFactor: Float,
    compressFactor: Float,
    onScaleChange: (Float) -> Unit,
    onCompressChange: (Float) -> Unit,
    fillColour: Int,
    setTransparencyFill: (String) -> Unit,
    fileName: String,
    updateFileName: (String) -> Unit,
    createNotification: (String, String, Bitmap) -> Unit,
    modifier: Modifier
) {
    val df = DecimalFormat("#.##")
    val context = LocalContext.current

    var tempScaleFactor by remember { mutableStateOf(scaleFactor) }
    var tempCompressFactor by remember { mutableStateOf(compressFactor) }
    var saveStatus by remember { mutableStateOf<Int>(0) }

    val dismissAlert = { dialog: DialogInterface, which: Int ->
        saveStatus = 0
    }

    when (saveStatus) {
        -1 -> {
            // File already exists
            val alertBuilder = AlertDialog.Builder(context)
             with (alertBuilder) {
                 setMessage(stringResource(id = R.string.file_already_exists_error_message,
                     "${fileName}.jpeg"))
                 setCancelable(false)
                 setNeutralButton("OK",
                     DialogInterface.OnClickListener(function = dismissAlert))
                 show()
             }
        }
        100 -> {
            Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show()
            saveStatus = 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.slider_scale_label, (tempScaleFactor*100).toInt()))
        Slider(
            value = tempScaleFactor,
            valueRange = 0.01F..1F,
            onValueChange = { newValue -> tempScaleFactor = df.format(newValue).toFloat() },
            onValueChangeFinished = { onScaleChange(tempScaleFactor) }
        )
        Text(text = stringResource(id = R.string.slider_compress_label, tempCompressFactor))
        Slider(
            value = tempCompressFactor,
            valueRange = 0.01F..1F,
            onValueChange = { newValue -> tempCompressFactor = df.format(newValue).toFloat() },
            onValueChangeFinished = { onCompressChange(tempCompressFactor) }
        )
        RadioButtonsForTransparencyFill(
            fillColour = fillColour,
            setTransparencyFill = setTransparencyFill
        )
        OutlinedTextField(
            label = { Text(text = stringResource(id = R.string.file_name_label)) },
            value = fileName,
            onValueChange = { newValue: String -> updateFileName(newValue) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
        Button(
            onClick = {
                saveStatus = saveButtonOnClick(context, createNotification, imageBitmap, compressFactor,
                    fileName, imageSizeInMB, df)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Save Image"
            )
        }
    }
}

fun saveButtonOnClick(
    context: Context,
    createNotification: (String, String, Bitmap) -> Unit,
    imageBitmap: Bitmap,
    compressFactor: Float,
    fileName: String,
    imageSizeInMB: Float,
    df: DecimalFormat
): Int {
    val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val filePathPlusName = filePath.absolutePath + "/" + fileName + ".jpeg"
    val file = File(filePathPlusName)
    return if (file.exists()) {
        // Alert that file already exists
        return -1
    } else {
        // Save the file
        DataLayerFunctions().saveBitmapToDirectory(imageBitmap, (compressFactor*100).toInt(), file)
        // Pass file to media content provider (to show in gallery)
        MediaScannerConnection.scanFile(context, arrayOf(filePathPlusName), arrayOf("image/jpeg"), null)
        // Create notification to tell user image was downloaded
        val message = "Image saved to Download folder (${df.format(imageSizeInMB)}MB)"
        val title = "${fileName}.jpeg"
        val thumbnail = ThumbnailUtils.extractThumbnail(imageBitmap, 128, 128)
        createNotification(title, message, thumbnail)
        return 100
    }
}

@Composable
fun RadioButtonsForTransparencyFill(
    fillColour: Int,
    setTransparencyFill: (String) -> Unit
) {
    val options: Array<String> = stringArrayResource(id = R.array.transparency_fill_options)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(id = R.string.transparency_fill_label))
        options.forEach { text ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == "White" && fillColour == Color.WHITE) ||
                            (text == "Black" && fillColour == Color.BLACK),
                    onClick = { setTransparencyFill(text) }
                )
                Text(text = text)
            }
        }
    }
}

@Preview
@Composable
fun OptionsScreenPreview() {
    Surface(color = androidx.compose.ui.graphics.Color.DarkGray) {
        val viewModel: CustomViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsState()
        OptionsScreen(
            scaleFactor = uiState.scaleFactor,
            compressFactor = uiState.compressionFactor,
            onScaleChange = {},
            onCompressChange = {},
            imageBitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.cursed_homer),
            imageSizeInBytes = 0,
            setTransparencyFill = { fillColour -> viewModel.updateTransparencyFill(fillColour) },
            navigateBack = {},
            fileName = "",
            createNotification = {a, b, c -> },
            updateFileName = {},
            fillColour = Color.WHITE
        )
    }
}