package me.jfenn.alarmio.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.widget.CompoundButtonCompat
import com.afollestad.aesthetic.Aesthetic
import io.reactivex.disposables.Disposable
import me.jfenn.alarmio.interfaces.Subscribblable

/**
 * An AppCompatCheckBox extension class that
 * implements Aesthetic theming.
 */
class AestheticCheckBoxView : AppCompatCheckBox, Subscribblable {

    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun subscribe() {
        colorAccentSubscription = Aesthetic.get().colorAccent()
                .subscribe { integer ->
                    val colorStateList = ColorStateList(
                            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                            intArrayOf(Color.argb(255, 128, 128, 128), integer)
                    )

                    CompoundButtonCompat.setButtonTintList(this, colorStateList)
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
