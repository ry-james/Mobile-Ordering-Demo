package com.ryanjames.swabergersmobilepos.core

import android.content.Context
import androidx.annotation.StringRes

interface StringBinder

class StringResourceArgs(@StringRes val id: Int, vararg val formatArgs: Any) : StringBinder {
    fun resolve(context: Context): String? {
        return context.getString(id, *formatArgs)
    }
}

class StringWrapper(val string: String) : StringBinder