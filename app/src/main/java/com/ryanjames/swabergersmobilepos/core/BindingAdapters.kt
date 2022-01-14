package com.ryanjames.swabergersmobilepos.core

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButtonToggleGroup
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

@BindingAdapter(value = ["drawableId", "circleCrop", "roundedCorner", "imageUrl", "fallback"], requireAll = false)
fun setSrcCompat(imageView: ImageView, drawableId: Drawable? = null, circleCrop: Boolean = false, roundedCorner: Float? = null, imageUrl: String? = null, fallback: Int? = null) {

    var requestOption = RequestOptions()

    if (roundedCorner != null) {
        requestOption = requestOption.transform(CenterCrop(), RoundedCorners(roundedCorner.toInt()))
    }

    Glide.with(imageView.context)
        .load(imageUrl ?: drawableId)
        .apply {
            if (fallback != null) {
                placeholder(fallback)
                    .error(fallback)
            }
            if (circleCrop) {
                circleCrop()
            }
        }
        .apply(requestOption)
        .into(imageView)
}

@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}

@BindingAdapter("app:transition")
fun setTransition(motionLayout: MotionLayout, transitionId: Int) {
    if (transitionId != 0) {
        motionLayout.setTransition(transitionId)
    }
}

@BindingAdapter("app:textWithArgs")
fun setTextWithArgs(textView: TextView, stringResourceArgs: StringResourceArgs?) {
    if (stringResourceArgs != null) {
        textView.text = stringResourceArgs.resolve(textView.context)
    }
}

@BindingAdapter("app:textBind")
fun setText(textView: TextView, stringBinder: StringBinder?) {
    if (stringBinder is StringResourceArgs) {
        if (stringBinder.id == 0) return
        textView.text = stringBinder.resolve(textView.context)
    } else if (stringBinder is StringWrapper) {
        textView.text = stringBinder.string
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

@BindingAdapter("app:checkedButton")
fun setCheckedButton(materialButtonToggleGroup: MaterialButtonToggleGroup, @IdRes idRes: Int) {
    materialButtonToggleGroup.check(idRes)
}

@BindingAdapter("app:onButtonChecked")
fun setCheckedButton(materialButtonToggleGroup: MaterialButtonToggleGroup, listener: MaterialButtonToggleGroup.OnButtonCheckedListener) {
    materialButtonToggleGroup.addOnButtonCheckedListener(listener)
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