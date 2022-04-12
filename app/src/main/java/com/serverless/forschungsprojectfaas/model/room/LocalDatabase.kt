package com.serverless.forschungsprojectfaas.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.serverless.forschungsprojectfaas.model.room.dao.PictureEntryDao
import com.serverless.forschungsprojectfaas.model.room.dao.StickEntryDao
import com.serverless.forschungsprojectfaas.model.room.entities.PictureEntry
import com.serverless.forschungsprojectfaas.model.room.entities.StickEntry
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        PictureEntry::class,
        StickEntry::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverter::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun getPictureEntryDao(): PictureEntryDao
    abstract fun getStickEntryDao(): StickEntryDao

    companion object {
        const val LOCAL_ROOM_DATABASE_NAME = "roomDatabase"
    }

}