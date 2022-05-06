package com.serverless.forschungsprojectfaas.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBatches
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PileDao : BaseDao<Pile> {

    @Query("SELECT * FROM PileTable WHERE title LIKE '%' || :searchQuery || '%' ORDER BY title")
    abstract fun getAllPilesFlow(searchQuery: String): Flow<List<Pile>>

    @Transaction
    @Query("SELECT * FROM PileTable WHERE pileId =:id LIMIT 1")
    abstract fun getPileWithBatchesFlow(id: String): Flow<PileWithBatches>

}