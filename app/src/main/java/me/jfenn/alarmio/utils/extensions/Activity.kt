package me.jfenn.alarmio.utils.extensions

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

internal inline fun <reified T> ignore(what: () -> T?) =
        try {
            what()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

internal inline fun <reified T : View> Activity.bind(@IdRes res: Int): Lazy<T?> = lazy { findView<T>(res) }

internal inline fun <reified T : View> Fragment.bind(@IdRes res: Int): Lazy<T?>? = lazy { findView<T>(res) }

internal inline fun <reified T : View> View.bind(@IdRes res: Int): Lazy<T?> = lazy { findView<T>(res) }

internal inline fun <reified T : View> Activity.findView(@IdRes res: Int): T? = ignore { findViewById(res) }

internal inline fun <reified T : View> Fragment.findView(@IdRes res: Int): T? = ignore { view?.findView(res) }

internal inline fun <reified T : View> View.findView(@IdRes res: Int): T? = ignore { findViewById(res) }

