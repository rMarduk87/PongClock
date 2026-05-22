package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import rpt.tool.pongclock.R
import java.util.*

class SnowEffectView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.white)
    }
    private val random = Random()
    private val flakes = mutableListOf<Flake>()

    data class Flake(var x: Float, var y: Float, val speed: Float, val size: Float)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (flakes.size < 100) {
            flakes.add(Flake(random.nextFloat() * width, -10f, random.nextFloat() * 5 + 2, random.nextFloat() * 8 + 4))
        }

        val iterator = flakes.iterator()
        while (iterator.hasNext()) {
            val f = iterator.next()
            canvas.drawCircle(f.x, f.y, f.size, paint)
            f.y += f.speed
            f.x += (random.nextFloat() - 0.5f) * 2
            if (f.y > height) iterator.remove()
        }
        invalidate()
    }
}
