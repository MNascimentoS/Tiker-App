package br.com.tiker.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

object AnimationConstants {
    const val DURATION_SHORT = 200
    const val DURATION_LONG = 400
}

/**
 * Fade In Animation
 *
 * R2D2 Animation Function
 *
 * @param duration: you can pass a millisecond value, or leave the 200 default one.
 * @unity you can add a unity after the animation call, that will be executed when it finishes.
 */
fun View.fadeIn(duration: Int = AnimationConstants.DURATION_SHORT, finishCallback: (() -> Unit)? = null) {
    this.visible()
    this.alpha = 0.0f
    this.animate()
        .setDuration(duration.toLong())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finishCallback?.let { it() }
                super.onAnimationEnd(animation)
            }
        })
        .alpha(1.0f)
        .start()
}

/**
 * Fade Out Animation
 *
 * R2D2 Animation Function
 *
 * @param duration: you can pass a millisecond value, or leave the 200 default one.
 * @unity you can add a unity after the animation call, that will be executed when it finishes.
 */
fun View.fadeOut(duration: Int = AnimationConstants.DURATION_SHORT, finishCallback: (() -> Unit)? = null) {
    this.alpha = 1.0f
    this.animate()
        .setDuration(duration.toLong())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finishCallback?.let { it() }
                super.onAnimationEnd(animation)
            }
        })
        .alpha(0.0f)
        .start()
}

fun Activity.alert(title: String, message: String, positive: String, onPositiveClick: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positive
        ) { _, _ ->
            onPositiveClick()
        }
        .show()

}

fun Activity.alert(title: String, message: String) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .show()
}