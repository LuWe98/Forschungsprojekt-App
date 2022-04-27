package com.serverless.forschungsprojectfaas.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.serverless.forschungsprojectfaas.model.room.entities.PictureEntry
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PictureEntryDao : BaseDao<PictureEntry> {

    @Query("SELECT * FROM PictureTable WHERE title LIKE '%' || :searchQuery || '%' ORDER BY title")
    abstract fun getAllPictureEntries(searchQuery: String): Flow<List<PictureEntry>>

    @Query("SELECT * FROM PictureTable WHERE pictureId =:id LIMIT 1")
    abstract fun getPictureEntryFlowWithId(id: String): Flow<PictureEntry>

}