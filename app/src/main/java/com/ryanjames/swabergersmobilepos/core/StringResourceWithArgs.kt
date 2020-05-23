package com.ryanjames.swabergersmobilepos.core

import android.content.Context
import androidx.annotation.StringRes

class StringResourceWithArgs(@StringRes val id: Int, vararg val formatArgs: Any) {

    fun resolve(context: Context): String? {
        return context.getString(id, *formatArgs)
    }
}