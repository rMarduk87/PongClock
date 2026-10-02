package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class HalloweenEffectView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 80f }
    private val entities = List(15) { SpookyEntity() }
    private var isInit = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        if (!isInit) {
            entities.forEach { it.init(width.toFloat(), height.toFloat()) }
            isInit = true
        }

        entities.forEach { e ->
            e.update(width.toFloat(), height.toFloat())
            paint.alpha = (e.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawText(e.emoji, e.x + sin(e.y / 50f) * 50f, e.y, paint)
        }
        postInvalidateDelayed(16)
    }

    private class SpookyEntity {
        var x = 0f; var y = 0f; var speedY = 0f; var alpha = 1f; var emoji = ""
        fun init(w: Float, h: Float) {
            val emojis = listOf("🦇", "👻", "🎃", "🕸️")
            emoji = emojis.random()
            x = if (w > 0) Random.nextFloat() * w else 0f
            y = if (h > 0) Random.nextFloat() * h + h else 100f
            speedY = Random.nextFloat() * 4 + 2
            alpha = Random.nextFloat() * 0.5f + 0.3f
        }
        fun update(w: Float, h: Float) {
            y -= speedY
            if (y < -100f) init(w, h)
        }
    }
}
