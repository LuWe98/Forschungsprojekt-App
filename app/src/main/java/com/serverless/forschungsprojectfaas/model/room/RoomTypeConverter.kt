package com.serverless.forschungsprojectfaas.model.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class RoomTypeConverter {

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val os = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        return os.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0 , bytes.size)
    }

    @TypeConverter
    fun fromUri(uri: Uri) : String {
        return uri.toString()
    }

    @TypeConverter
    fun toUri(uriString: String) : Uri {
        return Uri.parse(uriString)
    }

}