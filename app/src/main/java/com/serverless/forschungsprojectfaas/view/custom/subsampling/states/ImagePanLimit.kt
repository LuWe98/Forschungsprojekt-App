package com.serverless.forschungsprojectfaas.view.custom.subsampling.states

enum class ImagePanLimit {
    /** Don't allow the image to be panned off screen. As much of the image as possible is always displayed, centered in the view when it is smaller. This is the best option for galleries.  */
    PAN_LIMIT_INSIDE,
    /** Allows the image to be panned until it is just off screen, but no further. The edge of the image will stop when it is flush with the screen edge.  */
    PAN_LIMIT_OUTSIDE,
    /** Allows the image to be panned until a corner reaches the center of the screen but no further. Useful when you want to pan any spot on the image to the exact center of the screen.  */
    PAN_LIMIT_CENTER
}