package com.ehsannarmani.module1

import com.ehsannarmani.module1.database.CheckpointEntity
import com.ehsannarmani.module1.database.UiCheckpoint

fun getYForCheckpoints(checkpoint1:UiCheckpoint?,checkpoint2:UiCheckpoint,default:Int = 10): Int {


    var diffInMovement:Double? = null
    var percentageOfDiff:Double? =null

    val checkpointMovement = checkpoint2.lat*checkpoint2.lng

    checkpoint1?.let { previousCheckpoint->
        val previousMovement = previousCheckpoint.lat*previousCheckpoint.lng

        val minDiff = -1
        val maxDiff = 1

        val diffRange = maxDiff-minDiff
        diffInMovement = checkpointMovement-previousMovement

        percentageOfDiff = ((diffInMovement!!-minDiff)/diffRange)*100

    }

    var pointY = default
    if (percentageOfDiff != null){
        if (diffInMovement!! < 0.0){
            // move up
            pointY += percentageOfDiff!!.toInt()*2
        }else{
            // move down
            pointY -= percentageOfDiff!!.toInt()*2
        }
    }
    return pointY
}