package com.ehsannarmani.module1.view_model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehsannarmani.module1.database.AppDatabase
import com.ehsannarmani.module1.database.CheckpointEntity
import com.ehsannarmani.module1.database.UiCheckpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel:ViewModel() {
    private val dao = AppDatabase.instance.checkpointDao()

    private val _histories = MutableStateFlow(emptyList<List<UiCheckpoint>>())
    val histories = _histories.asStateFlow()

    init {
        getHistories()
    }

    fun resetCheckpointAnimations(scope:CoroutineScope){
        scope.launch {
            _histories.update {
                it.map { it.map {
                    val instance = it
                    it.imageScale.animateTo(0f, animationSpec = tween(0))
                    instance
                } }
            }
        }
    }

    fun getHistories(){
        viewModelScope.launch(Dispatchers.IO) {
            dao.getCheckpoints().collect{checkpoints->
                checkpoints.groupBy { it.uniqueId }.also { groupedCheckpoints->
                    _histories.update {
                        groupedCheckpoints.values.map { it.map { it.toUiModel() } }.toList()
                    }
                }
            }
        }
    }
}