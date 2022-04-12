package com.serverless.forschungsprojectfaas.model.room.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.serverless.forschungsprojectfaas.model.room.RoomEntityMarker

interface BaseDao <T: RoomEntityMarker> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entities: Collection<T>): LongArray?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entities: Collection<T>): Int?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: T): Int?

    @Delete
    suspend fun delete(entities: Collection<T>?)

    @Delete
    suspend fun delete(entity: T)

}