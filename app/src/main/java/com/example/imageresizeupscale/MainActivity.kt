package com.example.imageresizeupscale

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.imageresizeupscale.ui.theme.ImageResizeUpscaleTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var permissionGranted by mutableStateOf(false)

        // TODO: Fix permissions for differing android versions
        // (both write ext storage and push notifications)
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                permissionGranted = true
            } else {
                // Permission not granted
                // TODO: Tell user that they will not receive notifications
            }
        }

        setContent {
            ImageResizeUpscaleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val context = LocalContext.current

                    if (permissionGranted) {
                        AppScreen()
                    }

                    when (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS)
                    ) {
                        PackageManager.PERMISSION_GRANTED -> permissionGranted = true
                        // Check if permission needs to be granted
                        else -> {
                            when (android.os.Build.VERSION.SDK_INT) {
                                in 0..32 -> {
                                    // No POST_NOTIFICATIONS permission required
                                    permissionGranted = true
                                }
                                else -> {
                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ImageResizeUpscaleTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            AppScreen()
        }
    }
}