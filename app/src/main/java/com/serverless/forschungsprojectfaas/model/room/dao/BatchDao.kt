package com.serverless.forschungsprojectfaas.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BatchDao: BaseDao<Batch> {

    @Query("SELECT * FROM BatchTable WHERE caption LIKE '%' || :captionToSearch || '%' ORDER BY caption")
    abstract fun getFilteredBatchesFlow(captionToSearch: String): Flow<List<Batch>>

    @Query("SELECT * FROM BatchTable WHERE caption = :captionToSearch LIMIT 1")
    abstract suspend fun findBatchWithCaption(captionToSearch: String): Batch?

    @Query("SELECT * FROM BatchTable WHERE caption IN(:captions)")
    abstract suspend fun findBatchesWithCaptions(captions: Collection<String>): List<Batch>

}