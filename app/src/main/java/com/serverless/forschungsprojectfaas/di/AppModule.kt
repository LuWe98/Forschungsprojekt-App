package com.serverless.forschungsprojectfaas.di

import android.content.Context
import androidx.room.Room
import com.serverless.forschungsprojectfaas.OwnApplication
import com.serverless.forschungsprojectfaas.model.ktor.RemoteRepository
import com.serverless.forschungsprojectfaas.model.room.LocalDatabase
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideApplication(@ApplicationContext context: Context) = context as OwnApplication

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
        roomDatabase.getPileDao(),
        roomDatabase.getBatchDao(),
        roomDatabase.getBarDao()
    )

    @Provides
    @Singleton
    fun provideKtorClient() = HttpClient(Android) {
        install(DefaultRequest) {
            url.takeFrom(URLBuilder().takeFrom(RemoteRepository.REMOTE_URL).apply {
                encodedPath += url.encodedPath
                protocol = URLProtocol.HTTP
            })
            contentType(ContentType.Application.Json)
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        engine {
            connectTimeout = Int.MAX_VALUE
            socketTimeout = Int.MAX_VALUE
        }
    }

    @Provides
    @Singleton
    fun provideRemoteRepository(client: HttpClient) = RemoteRepository(client)

}