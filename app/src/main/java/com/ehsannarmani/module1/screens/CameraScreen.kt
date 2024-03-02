package com.ehsannarmani.module1.screens

import android.content.Context
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ehsannarmani.module1.LocalAppState
import com.ehsannarmani.module1.Navigation
import com.ehsannarmani.module1.view_model.RecordViewModel
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen() {

    var permissionGranted by remember {
        mutableStateOf(false)
    }

    var permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            permissionGranted = it
        }
    )

    LaunchedEffect(Unit){
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }
    LaunchedEffect(Unit){

    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        AnimatedVisibility(visible = permissionGranted) {
            Camera()
        }
        AnimatedVisibility(visible = !permissionGranted) {
            Text(text = "Permission Needed!")
        }
    }
}

@Composable
fun Camera() {
    val appState = LocalAppState.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraSelector = CameraSelector
        .Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    val previewView = remember {
        PreviewView(appState.context)
    }
    val preview = remember {
        Preview.Builder().build()
    }
    val imageCapture = remember {
        ImageCapture.Builder()
                .setJpegQuality(10)
            .build()
    }
    val outputFile = remember {
        val outputFile = File(appState.context.cacheDir.path+"/image-${System.currentTimeMillis()}.jpg")
        outputFile.createNewFile()
        outputFile
    }
    LaunchedEffect(Unit ){
        val cameraProvider = appState.context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
        AndroidView(modifier=Modifier.fillMaxSize(), factory = { previewView })
        Box(modifier = Modifier
            .padding(22.dp)
            .size(50.dp)
            .clip(CircleShape)
            .border(
                4.dp,
                Color.White,
                CircleShape
            )
            .clickable {
                val captureOptions = OutputFileOptions.Builder(
                    outputFile
                )

                    .build()
                imageCapture.takePicture(
                    captureOptions,
                    ContextCompat.getMainExecutor(appState.context),
                    object :OnImageSavedCallback{
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                            val uri = FileProvider.getUriForFile(
                                appState.context,
                                appState.context.packageName+".provider",
                                outputFile
                            )
                            uri?.let {
                                RecordViewModel.checkpointPicture = uri
                                appState.navController.navigate(Navigation.Checkpoint.route){
                                    popUpTo(Navigation.Record.route)
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {

                        }

                    }
                )
            })
    }
}

suspend fun Context.getCameraProvider() = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this)
        .also {
            it.addListener({
                           continuation.resume(
                               it.get()
                           )
            },ContextCompat.getMainExecutor(this))
        }
}