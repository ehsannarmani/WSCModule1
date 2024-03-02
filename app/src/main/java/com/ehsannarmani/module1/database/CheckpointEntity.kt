package com.ehsannarmani.module1.database

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity("checkpoints")
data class CheckpointEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
    val uniqueId:String,
    val lat:Double,
    val lng:Double,
    val alt:Double,
    val image:String?,
    val speed:Double,
    val temperature:Double,
    val timeElapsed:Int,
    val steps:Int
){
    companion object{
        val Empty:CheckpointEntity get() = CheckpointEntity(
            id = null,
            uniqueId = UUID.randomUUID().toString(),
            lat = 0.0,
            lng = 0.0,
            alt = 0.0,
            image = null,
            speed = 0.0,
            temperature = 0.0,
            timeElapsed = 0,
            steps = 0
        )
    }

    fun toUiModel():UiCheckpoint{
        return UiCheckpoint(
            id = id ?: (1000..2000).random(),
            lat = lat,
            lng = lng,
            alt = alt,
            image = image,
            speed = speed,
            temperature = temperature,
            timeElapsed = timeElapsed,
            steps = steps,
        )
    }
}

data class UiCheckpoint(
    val id:Int,
    val lat:Double,
    val lng:Double,
    val alt:Double,
    val image:String?,
    val speed:Double,
    val temperature:Double,
    val timeElapsed:Int,
    val steps:Int,
    val imageScale:Animatable<Float,AnimationVector1D> = Animatable(0f),
    val lineProgress:Animatable<Float,AnimationVector1D> = Animatable(0f)
)
