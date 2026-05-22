package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import rpt.tool.pongclock.R

class PongEffectView(context: Context) : View(context) {
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.white)
        strokeWidth = 12f
        style = Paint.Style.STROKE
    }
    private val paddlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.white)
    }

    private var ballX = 100f
    private var ballY = 100f
    private var ballDx = 12f
    private var ballDy = 12f
    private var paddle1Y = 300f
    private var paddle2Y = 300f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Background lines
        canvas.drawLine(0f, 20f, width.toFloat(), 20f, linePaint)
        canvas.drawLine(0f, height - 20f, width.toFloat(), height - 20f, linePaint)
        
        // Ball
        canvas.drawRect(ballX - 10, ballY - 10, ballX + 10, ballY + 10, paddlePaint)
        
        // Dynamic Paddles (AI follow)
        paddle1Y += (ballY - (paddle1Y + 60f)) * 0.15f
        paddle2Y += (ballY - (paddle2Y + 60f)) * 0.15f
        
        canvas.drawRect(50f, paddle1Y, 70f, paddle1Y + 120f, paddlePaint)
        canvas.drawRect(width - 70f, paddle2Y, width - 50f, paddle2Y + 120f, paddlePaint)
        
        // Update physics
        ballX += ballDx
        ballY += ballDy
        
        if (ballY < 40f || ballY > height - 40f) ballDy *= -1f
        if (ballX < 85f || ballX > width - 85f) ballDx *= -1f
        
        postInvalidateDelayed(16)
    }
}
