package com.serverless.forschungsprojectfaas.model.room

import androidx.room.withTransaction
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.model.PileStatus
import com.serverless.forschungsprojectfaas.model.ktor.PotentialBox
import com.serverless.forschungsprojectfaas.model.room.dao.BatchDao
import com.serverless.forschungsprojectfaas.model.room.dao.BaseDao
import com.serverless.forschungsprojectfaas.model.room.dao.PileDao
import com.serverless.forschungsprojectfaas.model.room.dao.BarDao
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBarCount
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBatches
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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

    suspend fun findBatchWithCaption(captionToSearch: String): Batch? = batchDao.findBatchWithCaption(captionToSearch)

    suspend fun findBatchesWithCaptions(captions: Collection<String>): List<Batch> = batchDao.findBatchesWithCaptions(captions)

    fun getPilesWithBarCount(searchQuery: String): Flow<List<PileWithBarCount>> = pileDao.getPilesWithBarCount(searchQuery)


    suspend fun updatePileStatus(pileId: String, status: PileStatus) = pileDao.updatePileStatus(pileId, status)

    suspend fun resetCurrentlyEvaluatingPiles() = pileDao.resetCurrentlyEvaluatingPiles()


    suspend fun insertBatchesAndBars(batches: List<Batch>, bars: List<Bar>) = withContext(Dispatchers.IO) {
        localDatabase.withTransaction {
            insert(batches)
            insert(bars)
        }
    }

    suspend fun insertBarOfPile(bar: Bar) = withContext(Dispatchers.IO) {
        localDatabase.withTransaction {
            updatePileStatus(bar.pileId, PileStatus.LOCALLY_CHANGED)
            insert(bar)
        }
    }

    suspend fun updateBarOfPile(bar: Bar) = withContext(Dispatchers.IO) {
        localDatabase.withTransaction {
            updatePileStatus(bar.pileId, PileStatus.LOCALLY_CHANGED)
            update(bar)
        }
    }

    suspend fun updateBarsOfPile(bars: List<Bar>) = withContext(Dispatchers.IO) {
        localDatabase.withTransaction {
            updatePileStatus(bars.firstOrNull()?.pileId ?: "", PileStatus.LOCALLY_CHANGED)
            update(bars)
        }
    }

    suspend fun deleteBarsOfPile(bars: List<Bar>) = withContext(Dispatchers.IO) {
        localDatabase.withTransaction {
            updatePileStatus(bars.firstOrNull()?.pileId ?: "", PileStatus.LOCALLY_CHANGED)
            delete(bars)
        }
    }


    suspend fun insertBatchesAndBarsOfResponse(pileId: String, results: List<PotentialBox>) = withContext(Dispatchers.IO) {
        val groupedByCaption = results.groupBy(PotentialBox::caption)
        val localBatches = findBatchesWithCaptions(groupedByCaption.keys)

        val batchesToInsert = mutableListOf<Batch>()
        val barsToInsert = mutableListOf<Bar>()

        groupedByCaption.forEach { (caption, boxes) ->
            val batchId: String? = if (caption.length != 2) {
                null
            }
            else {
                localBatches.firstOrNull { it.caption == caption } ?: Batch(caption = caption).also(batchesToInsert::add)
            }?.batchId

            boxes.map { box ->
                Bar(
                    batchId = batchId,
                    pileId = pileId,
                    rect = box.rect
                )
            }.also(barsToInsert::addAll)
        }

        insertBatchesAndBars(
            batches = batchesToInsert,
            bars = runValidityChecks(barsToInsert, localBatches + batchesToInsert)
        )

        updatePileStatus(pileId, PileStatus.UPLOADED)
    }

    private fun runValidityChecks(bars: List<Bar>, batches: List<Batch>): List<Bar> {
        val averageBarDimensions = bars.averageBarDimensions
        val batchMap = batches.associateBy(Batch::batchId)
        return bars.filterOverlappingBars(0.85f)
            .fixBarDimensions(averageBarDimensions)
            .filterIsolatedBars(averageBarDimensions)
            .adjustBatchIdsIfPossible(2, 1f)
            .adjustBatchIdsIfPossible(2, 0.5f)
            .adjustBatchIdsIfPossible(3, 0.5f)
            .adjustSpacesBetweenBatchGroups(5)
            .adjustSpacesBetweenBatchGroups(4)
            .adjustSpacesBetweenBatchGroups(3)
            .adjustSpacesBetweenBatchGroups(2)
            .adjustLonelyBarsBetween(3, 1f, batchMap)
            .adjustBatchIdsIfPossible(1, 1f)
            .adjustBatchIdsIfPossible(2, 0.5f)
            .adjustBatchIdsIfPossible(3, 0.5f)
            .adjustSpacesBetweenBatchGroups(3)
            .adjustSpacesBetweenBatchGroups(2)
            .adjustLonelyBarsBetween(3, 0.75f, batchMap)
            .adjustBatchIdsIfPossible(1, 1f)
            .adjustBatchIdsIfPossible(2, 0.5f)
            .adjustBatchIdsIfPossible(3, 0.5f)
            .adjustSpacesBetweenBatchGroups(3)
            .adjustSpacesBetweenBatchGroups(2)
            .adjustLonelyBarsBetween(3, 0.5f, batchMap)
            .adjustBatchIdsIfPossible(1, 1f)
            .adjustBatchIdsIfPossible(2, 0.5f)
            .adjustBatchIdsIfPossible(3, 0.5f)
            .adjustSpacesBetweenBatchGroups(3)
            .adjustSpacesBetweenBatchGroups(2)
    }
}