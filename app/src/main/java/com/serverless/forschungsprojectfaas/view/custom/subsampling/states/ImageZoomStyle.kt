package com.serverless.forschungsprojectfaas.view.custom.subsampling.states

enum class ImageZoomStyle {
    /** During zoom animation, keep the point of the image that was tapped in the same place, and scale the image around it.  */
    ZOOM_FOCUS_FIXED,
    /** During zoom animation, move the point of the image that was tapped to the center of the screen.  */
    ZOOM_FOCUS_CENTER,
    /** Zoom in to and center the tapped point immediately without animating.  */
    ZOOM_FOCUS_CENTER_IMMEDIATE
}