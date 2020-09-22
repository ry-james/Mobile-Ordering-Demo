package com.ryanjames.swabergersmobilepos.core

import android.widget.ImageView
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