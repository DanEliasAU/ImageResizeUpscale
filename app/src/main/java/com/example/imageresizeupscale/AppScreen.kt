package com.example.imageresizeupscale

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.imageresizeupscale.data.GetContent
import com.example.imageresizeupscale.data.NotificationHelper
import com.example.imageresizeupscale.ui.CustomViewModel
import com.example.imageresizeupscale.ui.OptionsScreen
import com.example.imageresizeupscale.ui.StartScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreens(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Options(title = R.string.app_options)
}

@Composable
fun AppScreen(
    viewModel: CustomViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AppScreens.valueOf(
        backStackEntry?.destination?.route ?: AppScreens.Start.name
    )
    val context = LocalContext.current
    val notificationHelper = remember {
        NotificationHelper(context)
    }

    LaunchedEffect(Unit) {
        notificationHelper.setupNotificationChannel()
    }

    Scaffold(
        topBar = {
            AppBar(
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = {
                    navController.navigateUp()
                    viewModel.clearImage()
                 },
                currentScreen = currentScreen
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        // Variable to set flag for the main activity to navigate to the Options screen
        var imageCoroutineFinished by remember { mutableStateOf(false) }
        // On coroutine completion, reset flag and navigate
        if (imageCoroutineFinished) {
            imageCoroutineFinished = false
            navController.navigate(AppScreens.Options.name)
        }

        // Callback function to dispatch coroutine function onStartButtonPressed
        val requestImageUpscale: () -> Unit = {
            coroutineScope.launch {
                val dispatch = withContext(Dispatchers.IO) {
                    viewModel.updateImageFromUri(context, uiState.imageUri, uiState.upscaleFactor)
                    imageCoroutineFinished = true
                }
            }
        }

        // The launcher and associated handler for the GetContent ACTION_PICK Intent
        val launcher = rememberLauncherForActivityResult(GetContent()) { uri ->
            if (uri != null) {
                //viewModel.updateImageUri(context, uri, uiState.upscaleFactor)
                viewModel.updateImageUri(uri)
                requestImageUpscale()
            }
        }

        // Callback function to dispatch coroutine function onStartButtonPressed
        val requestImageOnClick: () -> Unit = {
            coroutineScope.launch {
                 val dispatch = withContext(Dispatchers.IO) {
                     viewModel.onStartButtonPressed(uiState.urlText, uiState.upscaleFactor)
                     imageCoroutineFinished = true
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = AppScreens.Start.name,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = AppScreens.Start.name) {
                StartScreen(
                    onSelectButtonPressed = { launcher.launch("image/*") },
                    onStartButtonPressed = requestImageOnClick,
                    onTextChanged = { text -> viewModel.updateUrlText(text) },
                    urlTextFieldValue = uiState.urlText,
                    updateUpscaleFactor = { newValue -> viewModel.updateUpscaleFactor(newValue) }
                )
            }
            composable(route = AppScreens.Options.name) {
                OptionsScreen(
                    scaleFactor = uiState.scaleFactor,
                    compressFactor = uiState.compressionFactor,
                    onScaleChange = { value -> viewModel.onScaleFactorChange(value) },
                    onCompressChange = { value -> viewModel.onCompressFactorChange(value) },
                    imageBitmap = uiState.imageBitmap,
                    imageSizeInBytes = uiState.imageSizeInBytes,
                    fillColour = uiState.fillColour,
                    setTransparencyFill = { fillColour -> viewModel.updateTransparencyFill(fillColour) },
                    createNotification = { title, message, image ->
                        notificationHelper.createNotification(title, message, image)
                                         },
                    fileName = uiState.fileName,
                    updateFileName = { newFileName -> viewModel.updateFileName(newFileName) },
                    navigateBack = {
                        navController.navigateUp()
                        // Clears any loaded image within state when the user hits the back nav button
                        // Stops the 'Go' button from displaying previously loaded image without
                        // inputting a valid url
                        viewModel.clearImage()
                    }
                )
            }
        }
    }
}

@Composable
fun AppBar(
    currentScreen: AppScreens,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = ""
                    )
                }
            }
        }
    )
}