package com.serverless.forschungsprojectfaas.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.serverless.forschungsprojectfaas.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}