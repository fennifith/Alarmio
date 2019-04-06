package me.jfenn.alarmio.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import com.afollestad.aesthetic.Aesthetic
import io.reactivex.disposables.Disposable
import me.jfenn.alarmio.interfaces.Subscribblable

/**
 * Display a progress line, with a given foreground/background
 * color set.
 */
class ProgressLineView : View, Subscribblable {

    private var backgroundPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.LTGRAY
    }

    private var linePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.DKGRAY
    }

    private var progress: Float = 0f
    private var drawnProgress: Float = 0f

    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun subscribe() {
        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe { integer ->
                    linePaint.color = integer
                    linePaint.alpha = 100
                    postInvalidate()
                }

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe { integer ->
                    backgroundPaint.color = integer
                    backgroundPaint.alpha = 30
                    postInvalidate()
                }
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

    fun update(progress: Float) {
        this.progress = progress
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (drawnProgress != progress)
            drawnProgress = (drawnProgress * 4 + progress) / 5

        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)
        canvas.drawRect(0f, 0f, canvas.width * drawnProgress, canvas.height.toFloat(), linePaint)

        if ((drawnProgress - progress) * canvas.width != 0f)
            postInvalidate()
    }
}
