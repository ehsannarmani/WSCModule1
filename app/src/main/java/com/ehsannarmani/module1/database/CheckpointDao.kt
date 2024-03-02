package com.ehsannarmani.module1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface CheckpointDao {
    @Insert
    fun addCheckpoint(checkpointEntity: CheckpointEntity)

    @Query("SELECT * FROM checkpoints")
    fun getCheckpoints():Flow<List<CheckpointEntity>>
}