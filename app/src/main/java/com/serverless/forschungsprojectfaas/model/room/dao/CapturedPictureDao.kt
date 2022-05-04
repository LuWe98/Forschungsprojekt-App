package com.serverless.forschungsprojectfaas.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.serverless.forschungsprojectfaas.model.room.entities.CapturedPicture
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CapturedPictureDao : BaseDao<CapturedPicture> {

    @Query("SELECT * FROM PictureTable WHERE title LIKE '%' || :searchQuery || '%' ORDER BY title")
    abstract fun getAllPictureEntries(searchQuery: String): Flow<List<CapturedPicture>>

    @Query("SELECT * FROM PictureTable WHERE pictureId =:id LIMIT 1")
    abstract fun getPictureEntryFlowWithId(id: String): Flow<CapturedPicture>

}