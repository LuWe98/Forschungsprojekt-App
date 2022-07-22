package com.serverless.forschungsprojectfaas.extensions

import androidx.lifecycle.AndroidViewModel
import com.serverless.forschungsprojectfaas.ProjectApplication

val AndroidViewModel.app get() = getApplication<ProjectApplication>()