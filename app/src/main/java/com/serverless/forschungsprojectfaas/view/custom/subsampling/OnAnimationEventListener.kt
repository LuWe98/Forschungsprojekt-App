package com.serverless.forschungsprojectfaas.view.custom.subsampling


/**
 * An event listener for animations, allows events to be triggered when an animation completes,
 * is aborted by another animation starting, or is aborted by a touch event. Note that none of
 * these events are triggered if the activity is paused, the image is swapped, or in other cases
 * where the view's internal state gets wiped or draw events stop.
 */
interface OnAnimationEventListener {
    /**
     * The animation has completed, having reached its endpoint.
     */
    fun onComplete()

    /**
     * The animation has been aborted before reaching its endpoint because the user touched the screen.
     */
    fun onInterruptedByUser()

    /**
     * The animation has been aborted before reaching its endpoint because a new animation has been started.
     */
    fun onInterruptedByNewAnim()
}