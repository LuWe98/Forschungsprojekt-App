package com.serverless.forschungsprojectfaas.di

import android.content.Context
import androidx.room.Room
import com.serverless.forschungsprojectfaas.OwnApplication
import com.serverless.forschungsprojectfaas.model.room.LocalDatabase
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        LocalDatabase::class.java,
        LocalDatabase.LOCAL_ROOM_DATABASE_NAME
    ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideLocalRepository(roomDatabase: LocalDatabase) : LocalRepository = LocalRepository(
        roomDatabase,
        roomDatabase.getPictureEntryDao(),
        roomDatabase.getStickEntryDao()
    )

}