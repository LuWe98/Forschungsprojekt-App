package com.serverless.forschungsprojectfaas.model

import androidx.annotation.DrawableRes
import com.serverless.forschungsprojectfaas.R

enum class PileStatus(@DrawableRes val iconRes: Int) {
    FAILED(R.drawable.ic_error),
    NOT_EVALUATED(R.drawable.ic_cloud_upload),
    EVALUATING(R.drawable.ic_sync),
    LOCALLY_CHANGED(R.drawable.ic_refresh),
    UPLOADED(R.drawable.ic_check_circle);
}