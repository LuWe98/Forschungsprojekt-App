package com.serverless.forschungsprojectfaas.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.serverless.forschungsprojectfaas.model.room.dao.BatchDao
import com.serverless.forschungsprojectfaas.model.room.dao.PileDao
import com.serverless.forschungsprojectfaas.model.room.dao.BarDao
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        Pile::class,
        Batch::class,
        Bar::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverter::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun getPileDao(): PileDao
    abstract fun getBatchDao(): BatchDao
    abstract fun getBarDao(): BarDao

    companion object {
        const val LOCAL_ROOM_DATABASE_NAME = "roomDatabase"
    }
}