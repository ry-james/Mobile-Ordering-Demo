package com.ryanjames.swabergersmobilepos.core

import android.content.res.Resources
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


@BindingAdapter("app:imageUrl")
fun loadImage(imageView: ImageView, url: String?) {
    if (url != null) {
        Glide.with(imageView.context)
            .load(url)
            .centerCrop()
            .into(imageView)
    }
}

@BindingAdapter("app:transition")
fun setTransition(motionLayout: MotionLayout, transitionId: Int) {
    if (transitionId != 0) {
        motionLayout.setTransition(transitionId)
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