package com.serverless.forschungsprojectfaas.di

import com.serverless.forschungsprojectfaas.dispatcher.DispatchEventPublisher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityRetainedModule {

    @Provides
    @ActivityRetainedScoped
    fun provideDispatcherContainer() = DispatchEventPublisher()

    @Provides
    @ActivityRetainedScoped
    fun provideNavigationDispatcher(publisher: DispatchEventPublisher) = NavigationEventDispatcher(publisher)

}