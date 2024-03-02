package com.ehsannarmani.module1.screens

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ehsannarmani.module1.view_model.RecordViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ehsannarmani.module1.LocalAppState
import com.ehsannarmani.module1.Navigation
import com.ehsannarmani.module1.R
import com.ehsannarmani.module1.to2Decimal
import com.ehsannarmani.module1.to5Decimal
import com.ehsannarmani.module1.view_model.RecordData
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun RecordScreen(
    viewModel: RecordViewModel = viewModel()
) {

    val data by viewModel.recordData.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val startCheckpointAdded by viewModel.startCheckpointAdded.collectAsState()

    val appState = LocalAppState.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val sensorManager = appState.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val locationManager = appState.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    val speedHistory = remember {
        mutableStateListOf<Double>()
    }
    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { result->
                    when (event.sensor?.type) {
                        Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                            viewModel.updateRecordData { it.copy(
                                temperature = result.firstOrNull()?.toDouble() ?: 0.0
                            ) }
                        }
                    }
                }

            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

        }
    }

    val handleLocationUpdate = {location:Location->
        viewModel.updateRecordData {
            it.copy(
                lat = location.latitude,
                lng = location.longitude,
                alt = location.altitude,
            )
        }
        if (locations.isEmpty()){
            // add initial checkpoint
            if (location.latitude != 0.0){
                viewModel.addInitialCheckpoint(
                    initialLat = location.latitude,
                    initialLng = location.longitude
                )
            }

        }
        viewModel.addLocation(location)
    }
    val locationListener = remember {
        LocationListener { location->
//           handleLocationUpdate(location)
        }
    }
    LaunchedEffect(locations.count()){
        if (locations.count() >=2){
            val last = locations.last()
            val oneToLast = locations[locations.size-2]
            val results = FloatArray(1)
            Location.distanceBetween(oneToLast.latitude,oneToLast.longitude,last.latitude,last.longitude,results)

            val diffInTime = (last.time - oneToLast.time)/1000.0
            val metersPassed = results.first()
            val metersPerSecond = metersPassed/diffInTime
            speedHistory.add(metersPerSecond)

            if (speedHistory.reversed().take(5).all { it  <= 1 }){
                // last 5 speeds is zero, so person stopped
                viewModel.updateRecordData { it.copy(speed = 0.0) }

            }else{
                if (metersPerSecond > 0){
                    viewModel.updateRecordData { it.copy(speed = metersPerSecond) }
                }
            }

        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            if (it.values.all { it }){
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    locationListener
                )
            }
        }
    )

    val registerSensors = {
        sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
            SensorManager.SENSOR_DELAY_UI
        )
        permissionLauncher.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
    val unregisterSensor = {
        sensorManager.unregisterListener(
            sensorListener
        )
        locationManager.removeUpdates(locationListener)
    }

    LaunchedEffect(data.state == RecordData.RecordingState.Recording) {
        val condition = data.state == RecordData.RecordingState.Recording
        if (condition) {
            registerSensors()
        } else {
            unregisterSensor()
        }
        while (condition) {
            delay(1000)
            viewModel.elapseTime()
            runCatching {
                locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
                .onSuccess {
                    it?.let {
                        handleLocationUpdate(it)
                    }
                }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { owner, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (data.state == RecordData.RecordingState.Paused) {
                        viewModel.startRecording()
                    }
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Record", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.timeElapsed.toString(),
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedVisibility(visible = data.state != RecordData.RecordingState.NotStarted) {
            Column {
                Text(text = "Speed: ${data.speed.to2Decimal()} m/s")
                Text(text = "Temperature: ${data.temperature} C")
                Text(text = "Location: ${data.lat.to5Decimal()}, ${data.lng.to5Decimal()}, ${data.alt.to5Decimal()}")
                Text(text = "Steps: ${data.steps}")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )
        AnimatedVisibility(visible = data.state != RecordData.RecordingState.NotStarted) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    if (startCheckpointAdded){
                        viewModel.stopRecording()
                        appState.navController.navigate(Navigation.Camera.route)
                    }else{
                        Toast.makeText(appState.context, "Wait for location and initial checkpoint!", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Button(onClick = {
                    if (data.state == RecordData.RecordingState.Paused) {
                        viewModel.startRecording()
                    } else {
                        viewModel.endRecording()
                        appState.navController.popBackStack()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = if (data.state == RecordData.RecordingState.Paused) R.drawable.ic_play else R.drawable.ic_pause),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
        AnimatedVisibility(visible = data.state == RecordData.RecordingState.NotStarted) {
            Button(onClick = viewModel::startRecording) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}