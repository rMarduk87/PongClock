package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import rpt.tool.pongclock.R

class BreakoutSimulatedView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bricks = mutableListOf<Brick>()
    private var ballX = 0f; private var ballY = 0f
    private var ballDx = 8f; private var ballDy = -8f
    private var paddleX = 0f
    private var isInit = false
    private val colors by lazy {
        listOf(
            context.getColor(R.color.brick_red),
            context.getColor(R.color.halloween_orange),
            context.getColor(R.color.yellow),
            context.getColor(R.color.green),
            context.getColor(R.color.cyan)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInit) initGame(width.toFloat(), height.toFloat())

        paddleX = ballX - 60f
        paint.color = context.getColor(R.color.white)
        canvas.drawRoundRect(paddleX, height - 100f, paddleX + 120f,
            height - 70f, 10f, 10f, paint)

        canvas.drawCircle(ballX, ballY, 15f, paint)

        ballX += ballDx; ballY += ballDy
        if (ballX < 15 || ballX > width - 15) ballDx *= -1
        if (ballY < 15) ballDy *= -1

        if (ballY > height - 115f && ballX > paddleX && ballX < paddleX + 120f) {
            ballY = height - 115f; ballDy *= -1
        }

        if (ballY > height || bricks.all { !it.active }) initGame(width.toFloat(),
            height.toFloat())

        bricks.forEach { brick ->
            if (brick.active) {
                paint.color = brick.color
                canvas.drawRoundRect(brick.rect, 8f, 8f, paint)
                if (brick.rect.contains(ballX, ballY)) {
                    brick.active = false
                    ballDy *= -1
                }
            }
        }
        postInvalidateDelayed(16)
    }

    private fun initGame(w: Float, h: Float) {
        bricks.clear()
        val cols = 7; val rows = 5; val pad = 10f
        val bw = (w - pad * (cols + 1)) / cols
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = pad + c * (bw + pad)
                val top = pad + r * (60f + pad) + 40f
                bricks.add(Brick(RectF(left, top, left + bw, top + 60f),
                    colors[r % colors.size]))
            }
        }
        ballX = w / 2; ballY = h - 150f
        ballDy = -8f
        isInit = true
    }
    private class Brick(val rect: RectF, val color: Int, var active: Boolean = true)
}
