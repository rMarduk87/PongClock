package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import rpt.tool.pongclock.R
import kotlin.math.hypot
import kotlin.random.Random

class FuturisticEffectView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        color = context.getColor(R.color.cyan) 
    }
    private val nodes = List(40) { Node() }
    private var isInit = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInit) { 
            nodes.forEach { it.init(width.toFloat(), height.toFloat()) }
            isInit = true 
        }

        nodes.forEach { node ->
            node.update(width.toFloat(), height.toFloat())
            paint.alpha = 255
            canvas.drawCircle(node.x, node.y, 4f, paint)

            // Disegna linee se vicini
            nodes.forEach { other ->
                val dist = hypot(node.x - other.x, node.y - other.y)
                if (dist < 150f) {
                    paint.alpha = (255 * (1 - dist / 150f)).toInt()
                    paint.strokeWidth = 2f
                    canvas.drawLine(node.x, node.y, other.x,
                        other.y, paint)
                }
            }
        }
        postInvalidateDelayed(16)
    }

    private class Node {
        var x = 0f; var y = 0f; var dx = 0f; var dy = 0f
        fun init(w: Float, h: Float) {
            x = Random.nextFloat() * w; y = Random.nextFloat() * h
            dx = Random.nextFloat() * 2 - 1; dy = Random.nextFloat() * 2 - 1
        }
        fun update(w: Float, h: Float) {
            x += dx; y += dy
            if (x !in 0.0..w.toDouble()) dx *= -1
            if (y !in 0.0..h.toDouble()) dy *= -1
        }
    }
}
