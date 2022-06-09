package com.serverless.forschungsprojectfaas.view

import android.graphics.RectF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.minus
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.DispatchEventPublisher
import com.serverless.forschungsprojectfaas.extensions.area
import com.serverless.forschungsprojectfaas.extensions.intersectingArea
import com.serverless.forschungsprojectfaas.extensions.intersectingRect
import com.serverless.forschungsprojectfaas.extensions.log
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

    @Inject
    lateinit var dispatchEventPublisher: DispatchEventPublisher

    override fun onCreate(savedInstanceState: Bundle?) {
        initSplashScreen(savedInstanceState)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initObservers()
    }

    private fun initSplashScreen(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            setTheme(R.style.Theme_Forschungsprojekt)
            return
        }
        installSplashScreen()
    }

    private fun initObservers() {
        dispatchEventPublisher.eventChannelFlow.collectWhenStarted(this) { event ->
            event.execute(this)
        }
    }
}