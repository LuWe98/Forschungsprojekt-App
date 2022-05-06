package com.serverless.forschungsprojectfaas.extensions

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.view.ActivityMain

val NavHostFragment.currentFragment get() : Fragment = childFragmentManager.fragments.last()

val ActivityMain.navHostFragment get() : NavHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

val ActivityMain.navController get() = navHostFragment.navController

val ActivityMain.currentNavHostFragment get() = navHostFragment.currentFragment

val NavController.currentDestinationId get(): Int? = currentDestination?.id