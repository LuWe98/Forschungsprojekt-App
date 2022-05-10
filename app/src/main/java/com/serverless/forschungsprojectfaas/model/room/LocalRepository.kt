package com.serverless.forschungsprojectfaas.model.room

import com.serverless.forschungsprojectfaas.model.room.dao.BatchDao
import com.serverless.forschungsprojectfaas.model.room.dao.BaseDao
import com.serverless.forschungsprojectfaas.model.room.dao.PileDao
import com.serverless.forschungsprojectfaas.model.room.dao.BarDao
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBarCount
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBatches
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val localDatabase: LocalDatabase,
    private val pileDao: PileDao,
    private val batchDao: BatchDao,
    private val barDao: BarDao
) {

    @Suppress("UNCHECKED_CAST")
    private fun <T : RoomEntityMarker> getBaseDaoWith(entity: T): BaseDao<T> = when (entity) {
        is Pile -> pileDao
        is Batch -> batchDao
        is Bar -> barDao
        else -> throw IllegalArgumentException("Entity DAO for entity class '${entity::class.simpleName}' not found! Is it added to the 'getBaseDaoWith' method?")
    } as BaseDao<T>

    suspend fun <T : RoomEntityMarker> insert(entity: T) = getBaseDaoWith(entity).insert(entity)

    suspend fun <T : RoomEntityMarker> insert(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.insert(entities)

    suspend fun <T : RoomEntityMarker> update(entity: T) = getBaseDaoWith(entity).update(entity)

    suspend fun <T : RoomEntityMarker> update(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.update(entities)

    suspend fun <T : RoomEntityMarker> delete(entity: T) = getBaseDaoWith(entity).delete(entity)

    suspend fun <T : RoomEntityMarker> delete(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.delete(entities)


    fun getAllPilesFlow(searchQuery: String): Flow<List<Pile>> = pileDao.getAllPilesFlow(searchQuery)

    fun getPileWithBatchesFlow(id: String): Flow<PileWithBatches> = pileDao.getPileWithBatchesFlow(id)

    suspend fun getPileWithBatches(id: String): PileWithBatches = pileDao.getPileWithBatches(id)

    fun getFilteredBatchesFlow(captionToSearch: String): Flow<List<Batch>> = batchDao.getFilteredBatchesFlow(captionToSearch)

    fun findBatchWithCaption(captionToSearch: String): Batch? = batchDao.findBatchWithCaption(captionToSearch)

    fun getPilesWithBarCount(searchQuery: String): Flow<List<PileWithBarCount>> = pileDao.getPilesWithBarCount(searchQuery)

}