package com.ehsannarmani.module1.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.ehsannarmani.module1.LocalAppState
import com.ehsannarmani.module1.view_model.CheckpointViewModel
import com.ehsannarmani.module1.view_model.RecordViewModel
import java.io.File
import java.io.OutputStream
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ehsannarmani.module1.to2Decimal
import com.ehsannarmani.module1.to5Decimal

@Composable
fun CheckpointScreen(
    viewModel: CheckpointViewModel = viewModel()
) {

    val appState = LocalAppState.current

    var image = remember {
        mutableStateOf<ByteArray?>(null)
    }
    LaunchedEffect(Unit) {
        RecordViewModel.checkpointPicture?.let {
            appState.context.contentResolver.openInputStream(it).use {
                image.value = it?.readBytes()
                it?.close()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = {
            appState.navController.popBackStack()
        }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "New Checkpoint", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(22.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            image.value?.let {
                Image(
                    modifier = Modifier
                        .size(250.dp)
                        .rotate(90f),
                    contentScale = ContentScale.Crop,
                    bitmap = BitmapFactory
                        .decodeByteArray(it, 0, it.size)
                        .asImageBitmap(),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            with(RecordViewModel.lastRecordData) {
                Text(text = "Speed: ${speed.to2Decimal()} m/s")
                Text(text = "Temperature: ${temperature} C")
                Text(text = "Location: ${lat.to5Decimal()}, ${lng.to5Decimal()}, ${alt.to5Decimal()}", textAlign = TextAlign.Center)
                Text(text = "Steps: $steps")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )
        Button(onClick = {
            viewModel.addCheckpoint()
            appState.navController.popBackStack()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Save")
        }
    }
}