package com.ryanjames.swabergersmobilepos.core

import android.content.res.Resources
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout


@BindingAdapter("app:imageUrl")
fun loadImage(imageView: ImageView, url: String?) {
    if (url != null) {
        Glide.with(imageView.context)
            .load(url)
            .centerCrop()
            .into(imageView)
    }
}

@BindingAdapter("app:srcCompat")
fun setSrcCompat(imageView: ImageView, @DrawableRes drawableId: Int) {
    imageView.setImageResource(drawableId)
}

@BindingAdapter("app:transition")
fun setTransition(motionLayout: MotionLayout, transitionId: Int) {
    if (transitionId != 0) {
        motionLayout.setTransition(transitionId)
    }
}

@BindingAdapter("app:textWithArgs")
fun setTextWithArgs(textView: TextView, stringResourceWithArgs: StringResourceWithArgs?) {
    if (stringResourceWithArgs != null) {
        textView.text = stringResourceWithArgs.resolve(textView.context)
    }
}

@BindingAdapter("android:textColor")
fun setTextColor(textView: TextView, colorId: Int) {
    try {
        val color = textView.context.getColor(colorId)
        textView.setTextColor(color)
    } catch (resourceException: Resources.NotFoundException) {
        Log.e("android:textColor", "Can't find color")
    }

}

@BindingAdapter("layoutBackground")
fun loadImage(layout: ConstraintLayout, @DrawableRes resource: Int) {
    layout.background = layout.context.getDrawable(resource)
}

@BindingAdapter("error")
fun setError(textInputLayout: TextInputLayout, @StringRes resource: Int?) {
    textInputLayout.isErrorEnabled = resource != null
    textInputLayout.error = resource?.let { textInputLayout.context.getString(resource) }
}