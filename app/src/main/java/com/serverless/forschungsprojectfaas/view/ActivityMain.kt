package com.serverless.forschungsprojectfaas.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.DispatchEventPublisher
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

    @Inject
    lateinit var dispatchEventPublisher: DispatchEventPublisher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        initObservers()
    }

    private fun initObservers() {
        dispatchEventPublisher.eventChannelFlow.collectWhenStarted(this) { event ->
            event.execute(this)
        }
    }

}