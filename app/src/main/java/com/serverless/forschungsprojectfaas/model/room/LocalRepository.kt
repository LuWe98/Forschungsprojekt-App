package com.serverless.forschungsprojectfaas.model.room

import com.serverless.forschungsprojectfaas.model.room.dao.BarBatchDao
import com.serverless.forschungsprojectfaas.model.room.dao.BaseDao
import com.serverless.forschungsprojectfaas.model.room.dao.CapturedPictureDao
import com.serverless.forschungsprojectfaas.model.room.dao.BarDao
import com.serverless.forschungsprojectfaas.model.room.entities.CapturedPicture
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class LocalRepository @Inject constructor(
    private val localDatabase: LocalDatabase,
    private val capturedPictureDao: CapturedPictureDao,
    private val barBatchDao: BarBatchDao,
    private val barDao: BarDao
) {

    @Suppress("UNCHECKED_CAST")
    private fun <T : RoomEntityMarker> getBaseDaoWith(entity: T): BaseDao<T> = when (entity::class as KClass<T>) {
        CapturedPicture::class -> capturedPictureDao
        Bar::class -> barDao
        else -> throw IllegalArgumentException("Entity DAO for entity class '${entity::class.simpleName}' not found! Is it added to the 'getBaseDaoWith' method?")
    } as BaseDao<T>

    suspend fun <T : RoomEntityMarker> insert(entity: T) = getBaseDaoWith(entity).insert(entity)

    suspend fun <T : RoomEntityMarker> insert(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.insert(entities)

    suspend fun <T : RoomEntityMarker> update(entity: T) = getBaseDaoWith(entity).update(entity)

    suspend fun <T : RoomEntityMarker> update(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.update(entities)

    suspend fun <T : RoomEntityMarker> delete(entity: T) = getBaseDaoWith(entity).delete(entity)

    suspend fun <T : RoomEntityMarker> delete(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.delete(entities)


    fun getAllPictureEntries(searchQuery: String) = capturedPictureDao.getAllPictureEntries(searchQuery)

    fun getPictureEntryFlowWithId(id: String): Flow<CapturedPicture> = capturedPictureDao.getPictureEntryFlowWithId(id)

}