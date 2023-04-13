package com.example.imageresizeupscale.ui

import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.imageresizeupscale.R
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel

@Composable
fun StartScreen(
    onSelectButtonPressed: () -> Unit,
    onStartButtonPressed: () -> Unit,
    onTextChanged: (String) -> Unit,
    urlTextFieldValue: String,
    updateUpscaleFactor: (Int) -> Unit,
    clearImage: () -> Unit
) {
    val localFocusManager = LocalFocusManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.Center
    ) {
        RadioButtonsForUpscaleFactor(updateUpscaleFactor)
        Button(
            onClick = {
                clearImage()
                onSelectButtonPressed()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(id = R.string.select_image_label))
        }

        Text(
            text = stringResource(id = R.string.or),
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                label = { Text(text = stringResource(id = R.string.url_label)) },
                value = urlTextFieldValue,
                onValueChange = onTextChanged,
                singleLine = true,
                modifier = Modifier.width(250.dp),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (URLUtil.isValidUrl(urlTextFieldValue)) {
                            clearImage()
                            onStartButtonPressed()
                        } else {
                            Toast.makeText(context, "URL is not valid", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    if (URLUtil.isValidUrl(urlTextFieldValue)) {
                        clearImage()
                        onStartButtonPressed()
                    } else {
                        Toast.makeText(context, "URL is not valid", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(id = R.string.go))
            }
        }
    }
}

@Composable
fun RadioButtonsForUpscaleFactor(
    updateUpscaleFactor: (Int) -> Unit
) {
    val options: Array<String> = stringArrayResource(id = R.array.upscale_options)

    var selectedNumber by remember { mutableStateOf("0") }
    updateUpscaleFactor(selectedNumber.toInt())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.upscale_label),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        options.forEach { text ->
            Row {
                RadioButton(
                    selected = text == selectedNumber,
                    onClick = {
                        updateUpscaleFactor(text.toInt())
                        selectedNumber = text
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    text = text + "x",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}