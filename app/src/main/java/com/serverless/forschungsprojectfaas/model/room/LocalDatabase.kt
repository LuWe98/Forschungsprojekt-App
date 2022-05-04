package com.serverless.forschungsprojectfaas.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.serverless.forschungsprojectfaas.model.room.dao.BarBatchDao
import com.serverless.forschungsprojectfaas.model.room.dao.CapturedPictureDao
import com.serverless.forschungsprojectfaas.model.room.dao.BarDao
import com.serverless.forschungsprojectfaas.model.room.entities.CapturedPicture
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.BarBatch
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        CapturedPicture::class,
        BarBatch::class,
        Bar::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverter::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun getCapturedPictureDao(): CapturedPictureDao
    abstract fun getBarBatchDao(): BarBatchDao
    abstract fun getBarDao(): BarDao

    companion object {
        const val LOCAL_ROOM_DATABASE_NAME = "roomDatabase"
    }

}