package com.serverless.forschungsprojectfaas.extensions

import android.content.res.ColorStateList
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

inline fun View.onClick(crossinline action: () -> (Unit)) {
    setOnClickListener {
        action.invoke()
    }
}

inline fun View.onLongClick(crossinline action: () -> (Unit)) {
    setOnLongClickListener {
        action.invoke()
        return@setOnLongClickListener true
    }
}

inline fun SeekBar.onProgressChanged(crossinline action: (Int, Boolean) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            action.invoke(progress, fromUser)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}

inline fun EditText.onTextChanged(crossinline action: (String) -> (Unit)) {
    doOnTextChanged { text, _, _, _ -> action.invoke(text.toString()) }
}

fun RecyclerView.disableChangeAnimation() {
    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
}

fun View.setBackgroundTint(@ColorInt colorInt: Int) {
    backgroundTintList = ColorStateList.valueOf(colorInt)
}

fun ImageView.setImageDrawable(@DrawableRes drawableRes: Int) {
    setImageDrawable(ContextCompat.getDrawable(context, drawableRes))
}