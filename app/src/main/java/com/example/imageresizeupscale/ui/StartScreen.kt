package com.example.imageresizeupscale.ui

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.sp

@Composable
fun StartScreen(
    onSelectButtonPressed: () -> Unit,
    onStartButtonPressed: () -> Unit,
    onTextChanged: (String) -> Unit,
    urlTextFieldValue: String,
    updateUpscaleFactor: (Int) -> Unit
) {
    val localFocusManager = LocalFocusManager.current

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
            onClick = onSelectButtonPressed,
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
                    onDone = { onStartButtonPressed() }
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onStartButtonPressed,
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