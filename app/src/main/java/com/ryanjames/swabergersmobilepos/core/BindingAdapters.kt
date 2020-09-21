package com.ryanjames.swabergersmobilepos.core

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


@BindingAdapter("app:imageUrl")
fun loadImage(imageView: ImageView, url: String?) {
    if(url !=  null) {
        Glide.with(imageView.context)
            .load(url)
            .centerCrop()
            .into(imageView)
    }
}