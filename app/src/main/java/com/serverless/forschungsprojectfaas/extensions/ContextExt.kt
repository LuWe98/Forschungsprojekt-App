package com.serverless.forschungsprojectfaas.extensions

import android.content.Context
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.serverless.forschungsprojectfaas.R
import java.io.File

fun Context.getThemeColor(@AttrRes themeAttrId: Int) = TypedValue().let {
    theme.resolveAttribute(themeAttrId, it, true)
    it.data
}

val Context.appFilesDirectory get() : File? = getExternalStoragePublicDirectory(
    Environment.DIRECTORY_DOCUMENTS + File.separator + getString(R.string.app_name)
).also {
    if(!it.exists()){
        it.mkdir()
    }
}