package me.jfenn.alarmio.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.CompoundButtonCompat
import com.afollestad.aesthetic.Aesthetic
import io.reactivex.disposables.Disposable
import me.jfenn.alarmio.interfaces.Subscribblable

/**
 * A SwitchCompat extension class that implements
 * Aesthetic theming.
 */
class AestheticSwitchView : SwitchCompat, Subscribblable {

    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun subscribe() {
        colorAccentSubscription = Aesthetic.get().colorAccent()
                .subscribe { integer ->
                    val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))

                    CompoundButtonCompat.setButtonTintList(this, ColorStateList(
                            states,
                            intArrayOf(Color.argb(100, 128, 128, 128), integer)
                    ))

                    thumbDrawable?.let { drawable ->
                        DrawableCompat.setTintList(DrawableCompat.wrap(drawable), ColorStateList(
                                states,
                                intArrayOf(Color.argb(255, 128, 128, 128), integer)
                        ))
                    }

                    trackDrawable?.let { drawable ->
                        DrawableCompat.setTintList(DrawableCompat.wrap(drawable), ColorStateList(
                                states,
                                intArrayOf(Color.argb(100, 128, 128, 128), Color.argb(100, Color.red(integer), Color.green(integer), Color.blue(integer)))
                        ))
                    }
                }

        textColorPrimarySubscription = Aesthetic.get().textColorPrimary()
                .subscribe { integer -> setTextColor(integer) }
    }

    override fun unsubscribe() {
        colorAccentSubscription?.dispose()
        textColorPrimarySubscription?.dispose()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        subscribe()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unsubscribe()
    }
}
