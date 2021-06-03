package br.com.tiker.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun Context.toastLong(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

object AnimationConstants {
    const val DURATION_SHORT = 200
    const val DURATION_LONG = 400
}

// chops a list into non-view sublists of length L
fun <T> chopped(list: List<T>, L: Int): ArrayList<List<T>>? {
    val parts: ArrayList<List<T>> = ArrayList()
    val N = list.size
    var i = 0
    while (i < N) {
        parts.add(ArrayList(
                list.subList(i, N.coerceAtMost(i + L)))
        )
        i += L
    }
    return parts
}

inline fun <T> LifecycleOwner.observe(liveData: LiveData<T>, crossinline onChanged: (T) -> Unit) {
    liveData.observe(this, androidx.lifecycle.Observer { onChanged(it) })
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

fun Activity.alert(title: String, message: String, positive: String, allowDismiss: Boolean, onDismissClick: (() -> Unit)? = null, onPositiveClick: () -> Unit) {
    var dismiss = allowDismiss
    AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positive
            ) { _, _ ->
                dismiss = true
                onPositiveClick()
            }
            .setOnDismissListener {
                if (!dismiss) alert(title, message, positive, dismiss, onDismissClick, onPositiveClick)
                else onDismissClick?.invoke()
            }
            .show()

}

fun Activity.alert(title: String, message: String) {
    AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .show()
}

fun random(): String? {
    val generator = java.util.Random()
    val randomStringBuilder = StringBuilder()
    val randomLength = generator.nextInt(20)
    var tempChar: Char
    for (i in 0 until randomLength) {
        tempChar = (generator.nextInt(42) + 48).toChar()
        randomStringBuilder.append(tempChar)
    }
    var string = randomStringBuilder.toString()

    string = string.replace("/", "")
    string = string.replace(":", "")
    string = string.replace("-", "")
    string = string.replace(";", "")
    string = string.replace(">", "")
    string = string.replace("<", "")
    string = string.replace("=", "")
    string = string.replace("@", "")
    string = string.replace("?", "")
    return string
}