package com.serverless.forschungsprojectfaas.view.custom.subsampling

import android.graphics.PointF
import com.serverless.forschungsprojectfaas.view.custom.subsampling.states.ImageEasingStyle

class ImageAnim(
    // Scale at start of anim
    var scaleStart: Float = 0f,
    // Scale at end of anim (target)
    var scaleEnd: Float = 0f,
    // Source center point at start
    var sCenterStart: PointF? = null,
    // Source center point at end, adjusted for pan limits
    var sCenterEnd: PointF? = null,
    // Source center point that was requested, without adjustment
    var sCenterEndRequested: PointF? = null,
    // View point that was double tapped
    var vFocusStart: PointF? = null,
    // Where the view focal point should be moved to during the anim
    var vFocusEnd: PointF? = null,
    // How long the anim takes
    var duration: Long = 500,
    // Whether the anim can be interrupted by a touch
    var interruptible: Boolean = true,
    // Easing style
    var easing: ImageEasingStyle = ImageEasingStyle.EASE_IN_OUT_QUAD,
    // Animation origin (API, double tap or fling)
    var origin: Int = SubsamplingScaleImageView.ORIGIN_ANIM,
    // Start time
    var time: Long = System.currentTimeMillis(),
    var listener: OnAnimationEventListener? = null
)