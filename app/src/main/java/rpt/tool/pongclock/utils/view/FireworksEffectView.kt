package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import rpt.tool.pongclock.R
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class FireworksEffectView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random()
    private val sparks = mutableListOf<Spark>()

    data class Spark(var x: Float, var y: Float, var vx: Float, var vy: Float, val color: Int, var alpha: Int = 255)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (random.nextFloat() > 0.95) {
            val cx = random.nextFloat() * width
            val cy = random.nextFloat() * height
            val color = listOf(
                context.getColor(R.color.yellow),
                context.getColor(R.color.cyan),
                context.getColor(R.color.magenta),
                context.getColor(R.color.newyear_gold)
            ).random()
            for (i in 0..30) {
                val angle = random.nextFloat() * Math.PI * 2
                val speed = random.nextFloat() * 10 + 5
                sparks.add(Spark(cx, cy, (cos(angle) * speed).toFloat(), (sin(angle) * speed).toFloat(), color))
            }
        }

        val iterator = sparks.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            paint.color = s.color
            paint.alpha = s.alpha
            canvas.drawCircle(s.x, s.y, 4f, paint)
            s.x += s.vx
            s.y += s.vy
            s.vy += 0.2f // Gravità
            s.alpha -= 5
            if (s.alpha <= 0) iterator.remove()
        }
        postInvalidateDelayed(16)
    }
}
