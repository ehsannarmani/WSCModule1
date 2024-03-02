package com.ehsannarmani.module1.view_model

import android.location.Location
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehsannarmani.module1.database.AppDatabase
import com.ehsannarmani.module1.database.CheckpointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordViewModel:ViewModel() {

    companion object{
        lateinit var lastRecordData: RecordData
        var checkpointPicture:Uri? = null
    }

    private val dao = AppDatabase.instance.checkpointDao()

    private val _recordData = MutableStateFlow(RecordData(
        state = RecordData.RecordingState.NotStarted
    ))
    val recordData = _recordData.asStateFlow()

    private val _locations = MutableStateFlow(emptyList<Location>())
    val locations = _locations.asStateFlow()

    private val _startCheckpointAdded = MutableStateFlow(false)
    val startCheckpointAdded = _startCheckpointAdded.asStateFlow()

    fun addLocation(location: Location){
        _locations.update { it+location }
    }

    fun updateRecordData(updater:(RecordData)->RecordData){
        _recordData.update { updater(it) }
    }
    fun elapseTime() = updateRecordData { it.copy(timeElapsed = it.timeElapsed+1) }

    fun startRecording() = updateRecordData { it.copy(state = RecordData.RecordingState.Recording) }
    fun stopRecording() {
        lastRecordData = recordData.value
        updateRecordData { it.copy(state = RecordData.RecordingState.Paused) }
    }

    fun addInitialCheckpoint(
        initialLat:Double,
        initialLng:Double,
    ){
        _startCheckpointAdded.update { true }
        viewModelScope.launch(Dispatchers.IO){
            dao.addCheckpoint(CheckpointEntity.Empty.copy(
                lat = initialLat,
                lng = initialLng,
                uniqueId = AppDatabase.checkpointUniqueId
            ))
        }
    }

    fun endRecording(){
        stopRecording()
        if (startCheckpointAdded.value){
            viewModelScope.launch(Dispatchers.IO){
                with(recordData.value){
                    dao.addCheckpoint(CheckpointEntity(
                        uniqueId = AppDatabase.checkpointUniqueId,
                        lat = lat,
                        lng = lng,
                        alt = alt,
                        speed = speed,
                        temperature = temperature,
                        timeElapsed = timeElapsed,
                        image = null,
                        steps = steps
                    ))
                }
            }
        }
    }
}

data class RecordData(
    val state:RecordingState,
    val speed:Double = 0.0,
    val temperature:Double = 0.0,
    val lat:Double = 0.0,
    val lng:Double = 0.0,
    val alt:Double = 0.0,
    val steps:Int = 0,
    val timeElapsed:Int = 0
){
    enum class RecordingState{
        NotStarted,Paused,Recording
    }
}