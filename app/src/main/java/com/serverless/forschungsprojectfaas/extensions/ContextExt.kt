package com.serverless.forschungsprojectfaas.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.util.TypedValue
import androidx.annotation.AttrRes

fun Context.isPermissionGranted(permission: String) = checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

fun Context.getThemeColor(@AttrRes themeAttrId: Int) = TypedValue().let {
    theme.resolveAttribute(themeAttrId, it, true)
    it.data
}