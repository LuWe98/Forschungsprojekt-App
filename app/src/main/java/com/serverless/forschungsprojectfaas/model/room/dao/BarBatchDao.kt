package com.serverless.forschungsprojectfaas.model.room.dao

import androidx.room.Dao
import com.serverless.forschungsprojectfaas.model.room.entities.Bar
import com.serverless.forschungsprojectfaas.model.room.entities.BarBatch

@Dao
abstract class BarBatchDao: BaseDao<BarBatch> {

}