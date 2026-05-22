package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import rpt.tool.pongclock.R
import java.util.*

class MatrixEffectView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.matrix)
        textSize = 40f
    }
    private val random = Random()
    private var columns = 0
    private var drops = IntArray(0)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        columns = w / 40
        drops = IntArray(columns)
        for (i in 0 until columns) drops[i] = random.nextInt(h / 40)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Fondo semitrasparente per l'effetto scia
        canvas.drawColor(context.getColor(R.color.matrix_dim_bg))

        paint.color = context.getColor(R.color.matrix)
        for (i in drops.indices) {
            val char = (random.nextInt(95) + 33).toChar().toString()
            canvas.drawText(char, (i * 40).toFloat(), (drops[i] * 40).toFloat(), paint)

            if (drops[i] * 40 > height && random.nextFloat() > 0.975) {
                drops[i] = 0
            }
            drops[i]++
        }
        postInvalidateDelayed(50)
    }
}
