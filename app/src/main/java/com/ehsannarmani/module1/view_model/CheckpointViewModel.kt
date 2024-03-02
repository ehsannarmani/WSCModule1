package com.ehsannarmani.module1.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehsannarmani.module1.database.AppDatabase
import com.ehsannarmani.module1.database.CheckpointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckpointViewModel:ViewModel() {

    private val dao = AppDatabase.instance.checkpointDao()

    fun addCheckpoint(){
        viewModelScope.launch(Dispatchers.IO){
            with(RecordViewModel.lastRecordData){
                dao.addCheckpoint(CheckpointEntity(
                    uniqueId = AppDatabase.checkpointUniqueId,
                    lat = lat,
                    lng = lng,
                    image = RecordViewModel.checkpointPicture?.toString(),
                    speed = speed,
                    temperature = temperature,
                    alt = alt,
                    timeElapsed = timeElapsed,
                    steps = steps
                ))
            }
        }
    }
}