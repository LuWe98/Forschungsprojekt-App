package com.serverless.forschungsprojectfaas.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBarCount
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBatches
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PileDao : BaseDao<Pile> {

    @Query("SELECT * FROM PileTable WHERE title LIKE '%' || :searchQuery || '%' ORDER BY title")
    abstract fun getAllPilesFlow(searchQuery: String): Flow<List<Pile>>

    @Transaction
    @Query("SELECT * FROM PileTable WHERE pileId =:id LIMIT 1")
    abstract fun getPileWithBatchesFlow(id: String): Flow<PileWithBatches>

    @Transaction
    @Query("SELECT DISTINCT p.*, COUNT(*) as count FROM PileTable as p JOIN BarTable as b ON(p.pileId = b.pileId) WHERE p.title LIKE '%' || :searchQuery || '%' GROUP BY b.pileId ORDER BY p.title")
    abstract fun getPilesWithBarCount(searchQuery: String): Flow<List<PileWithBarCount>>

    @Transaction
    @Query("SELECT * FROM PileTable WHERE pileId =:id LIMIT 1")
    abstract suspend fun getPileWithBatches(id: String): PileWithBatches

}