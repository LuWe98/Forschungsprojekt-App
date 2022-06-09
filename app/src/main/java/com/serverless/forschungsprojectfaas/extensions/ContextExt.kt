package com.serverless.forschungsprojectfaas.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.serverless.forschungsprojectfaas.R
import java.io.File

fun Context.isPermissionGranted(permission: String) = checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

fun Context.getThemeColor(@AttrRes themeAttrId: Int) = TypedValue().let {
    theme.resolveAttribute(themeAttrId, it, true)
    it.data
}

val Context.appFilesDirectory get() : File? = getExternalFilesDir(
    Environment.DIRECTORY_DOCUMENTS
            + File.separator.toString()
            + getString(R.string.app_name)
)

val Context.appFilesDirectory2 get() : File? = getExternalStoragePublicDirectory(
    Environment.DIRECTORY_DOCUMENTS + File.separator + getString(R.string.app_name)
).also {
    if(!it.exists()){
        it.mkdir()
    }
}