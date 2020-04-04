package com.thegergo02.minkreta.misc

import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.StringRes

@SuppressLint("Registered")
class App : Application() {
    companion object {
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

object Strings {
    fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
        return App.instance.getString(stringRes, *formatArgs)
    }
}
