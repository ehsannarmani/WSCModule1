package com.ehsannarmani.module1.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.UUID


@Database(entities = [CheckpointEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    companion object{
        lateinit var instance:AppDatabase
        lateinit var checkpointUniqueId:String

        fun refreshCheckpointUniqueId(){
            checkpointUniqueId = UUID.randomUUID().toString()
        }

        fun setup(context: Context){
            instance = Room.databaseBuilder(context,AppDatabase::class.java,"db")
                .build()
        }
    }

    abstract fun checkpointDao():CheckpointDao
}