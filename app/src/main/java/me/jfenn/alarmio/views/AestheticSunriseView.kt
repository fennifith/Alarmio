package me.jfenn.alarmio.views

import android.content.Context
import android.util.AttributeSet

import com.afollestad.aesthetic.Aesthetic
import io.reactivex.disposables.Disposable
import me.jfenn.alarmio.interfaces.Subscribblable
import me.jfenn.sunrisesunsetview.SunriseSunsetView

/**
 * A SunriseView extension class that implements
 * Aesthetic theming.
 */
class AestheticSunriseView : SunriseSunsetView, Subscribblable {

    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        isClickable = false
        isFocusable = false
    }

    override fun subscribe() {
        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe { integer ->
                    sunsetColor = (200 shl 24) or (integer and 0x00FFFFFF)
                    futureColor = (20 shl 24) or (integer and 0x00FFFFFF)
                    postInvalidate()
                }

        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe { integer -> sunriseColor = (200 shl 24) or (integer and 0x00FFFFFF) }
    }

    override fun unsubscribe() {
        textColorPrimarySubscription?.dispose()
        colorAccentSubscription?.dispose()
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
