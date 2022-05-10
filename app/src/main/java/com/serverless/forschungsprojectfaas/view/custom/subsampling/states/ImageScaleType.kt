package com.serverless.forschungsprojectfaas.view.custom.subsampling.states

enum class ImageScaleType {
    /** Scale the image so that both dimensions of the image will be equal to or less than the corresponding dimension of the view. The image is then centered in the view. This is the default behaviour and best for galleries.  */
    SCALE_TYPE_CENTER_INSIDE,
    /** Scale the image uniformly so that both dimensions of the image will be equal to or larger than the corresponding dimension of the view. The image is then centered in the view.  */
    SCALE_TYPE_CENTER_CROP,
    /** Scale the image so that both dimensions of the image will be equal to or less than the maxScale and equal to or larger than minScale. The image is then centered in the view.  */
    SCALE_TYPE_CUSTOM,
    /** Scale the image so that both dimensions of the image will be equal to or larger than the corresponding dimension of the view. The top left is shown.  */
    SCALE_TYPE_START
}