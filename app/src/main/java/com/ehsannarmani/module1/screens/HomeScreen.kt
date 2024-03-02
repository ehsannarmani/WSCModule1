package com.ehsannarmani.module1.screens

import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ehsannarmani.module1.view_model.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ehsannarmani.module1.LocalAppState
import com.ehsannarmani.module1.Navigation
import com.ehsannarmani.module1.database.AppDatabase
import com.ehsannarmani.module1.database.CheckpointEntity
import com.ehsannarmani.module1.database.UiCheckpoint
import com.ehsannarmani.module1.getYForCheckpoints
import com.ehsannarmani.module1.to2Decimal
import com.ehsannarmani.module1.to5Decimal
import com.ehsannarmani.module1.view_model.RecordViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.TextStyle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val appState = LocalAppState.current
    val histories by viewModel.histories.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    val selectedCheckpoint = remember {
        mutableStateOf<UiCheckpoint?>(null)
    }

    if (selectedCheckpoint.value != null){
        Dialog(onDismissRequest = { selectedCheckpoint.value = null }) {
            Column(modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                with(selectedCheckpoint.value!!) {
                    image?.let { 
                        AsyncImage(modifier = Modifier.size(250.dp).clip(RoundedCornerShape(8.dp)), contentDescription = null, contentScale = ContentScale.Crop,model = it)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Speed: ${speed.to2Decimal()} m/s")
                    Text(text = "Temperature: $temperature C")
                    Text(text = "Location: ${lat.to5Decimal()}, ${lng.to5Decimal()}, ${alt.to5Decimal()}", textAlign = TextAlign.Center)
                    Text(text = "Steps: $steps")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hiking History", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(histories) {
                HistoryItem(checkpoints = it, onCheckpointClick = {
                    selectedCheckpoint.value = it
                },viewModel)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.BottomCenter
    ) {
        Button(onClick = {
            AppDatabase.refreshCheckpointUniqueId()
            viewModel.resetCheckpointAnimations(appState.coroutineScope)
            appState.navController.navigate(Navigation.Record.route)
        }) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun HistoryItem(checkpoints: List<UiCheckpoint>,onCheckpointClick:(UiCheckpoint)->Unit,viewModel: HomeViewModel) {
    val appState = LocalAppState.current

    val drawableAreaWidth = remember {
        mutableFloatStateOf(0f)
    }
    val checkpointX = (drawableAreaWidth.floatValue /
            (checkpoints.count())).toInt() + 10

    val lineProgress = remember {
        Animatable(0f)
    }
    LaunchedEffect(Unit){
        launch {
            delay(50)
            lineProgress.animateTo(1f, animationSpec = tween(
                350*checkpoints.count(),
                easing = LinearEasing
            ))
        }

    }



    LaunchedEffect(Unit) {
        launch {
            checkpoints.forEachIndexed { index, uiCheckpoint ->
                delay((index * (if (index == checkpoints.lastIndex) 50 else 100)).toLong())
                uiCheckpoint.imageScale.animateTo(1f, animationSpec = tween(300))
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp)
                ) {

                    val path = Path()

                    checkpoints.forEachIndexed { index, checkpoint ->
                        if (index + 1 <= checkpoints.lastIndex) {
                            val previousCheckpoint = checkpoints.getOrNull(index - 1)
                            val nextCheckpoint = checkpoints.getOrNull(index + 1)

                            val lineY =
                                getYForCheckpoints(previousCheckpoint, checkpoint, 50).toFloat()
                            val lineX = (index * checkpointX).toFloat()

                            val endLineY =
                                getYForCheckpoints(checkpoint, nextCheckpoint!!, 50).toFloat()
                            val endLineX = ((index + 1) * checkpointX).toFloat()

                            if (index == 0){
                                path.moveTo(lineX,lineY)

                            }

                            path.lineTo(endLineX,endLineY)
                        }
                    }

                    val segment = Path()

                    val pathMeasure = PathMeasure().apply {
                        setPath(path, false)
                    }
                    pathMeasure.getSegment(
                        0f,
                        pathMeasure.length*lineProgress.value,
                        segment,
                    )
                    drawPath(
                        path = segment,
                        color = Color.Black,
                        style = Stroke(5f)
                    )


                }
                Box(modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        drawableAreaWidth.value = it.boundsInWindow().width
                    }
                    .padding(top = 64.dp)) {
                    checkpoints.forEachIndexed { index, checkpoint ->
                        val previousCheckpoint = checkpoints.getOrNull(index - 1)
                        val pointY = getYForCheckpoints(previousCheckpoint, checkpoint)
                        if (index == 0) {
                            Box(modifier = Modifier
                                .size(30.dp)
                                .offset { IntOffset(0, pointY) }
                                .scale(checkpoint.imageScale.value)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color.Black, CircleShape)
                                .clickable { onCheckpointClick(checkpoint) },
                                contentAlignment = Alignment.Center) {
                                Text(text = "S")
                            }
                        }
                        if (index == checkpoints.lastIndex) {
                            Box(modifier = Modifier
                                .size(30.dp)
                                .offset { IntOffset(checkpointX * index, pointY) }
                                .scale(checkpoint.imageScale.value)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color.Black, CircleShape)
                                .clickable { onCheckpointClick(checkpoint) },
                                contentAlignment = Alignment.Center) {
                                Text(text = "E")
                            }
                        }
                        checkpoint.image?.let {
                            AsyncImage(
                                modifier = Modifier
                                    .size(30.dp)
                                    .offset { IntOffset(checkpointX * index, pointY) }
                                    .rotate(90f)
                                    .scale(checkpoint.imageScale.value)
                                    .clip(CircleShape)
                                    .clickable { onCheckpointClick(checkpoint) },
                                model = it,
                                contentDescription = null,
                                contentScale = ContentScale.Crop

                            )
                        }
                    }
                }

            }
            Text(
                text = checkpoints.last().timeElapsed.toString() + "s", modifier = Modifier.align(
                    Alignment.BottomEnd
                )
            )
        }
    }
}