package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*
import kotlin.math.*
import rpt.tool.pongclock.R

class BreakOutTimeView(context: Context?, attrs: AttributeSet?) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var thread: BreakOutThread? = null

    data class Brick(val rect: RectF, val color: Int, var active: Boolean = true)

    init {
        holder.addCallback(this)
        isFocusable = false
        isClickable = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread = BreakOutThread(holder)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        thread?.setSurfaceSize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread?.setRunning(false)
        var retry = true
        while (retry) {
            try { thread?.join(); retry = false } catch (e: InterruptedException) {}
        }
        thread = null
    }

    inner class BreakOutThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        @Volatile
        private var running = true
        private var canvasWidth = 0
        private var canvasHeight = 0

        private var ballRadius = 15f
        private var ballX = 0f
        private var ballY = 0f
        private var ballDX = 15f
        private var ballDY = -15f

        private var paddleWidth = 200f
        private var paddleHeight = 30f
        private var paddleX = 0f
        private val paddleYOffset = 150f

        private val brickRows = 6
        private val brickCols = 10
        private val bricks = mutableListOf<Brick>()

        private val brickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val paddlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.2f
        }

        private var bgColor: Int = Color.BLACK
        private var mainColor: Int = Color.WHITE
        private val calendar = Calendar.getInstance()

        init { updateColors() }

        private fun updateColors() {
            val ctx = context ?: return
            bgColor = ctx.getColor(R.color.modern_background)
            mainColor = ctx.getColor(R.color.white)
            ballPaint.color = mainColor
            ballPaint.setShadowLayer(20f, 0f, 0f, mainColor)
            paddlePaint.color = mainColor
            paddlePaint.setShadowLayer(10f, 0f, 0f, mainColor)
            textPaint.color = mainColor
            textPaint.alpha = 120
        }

        private var currentHours = 0
        private var currentMinutes = 0
        private var currentSeconds = 0
        private var lastProcessedSecond = -1

        fun setRunning(isRunning: Boolean) { running = isRunning }

        fun setSurfaceSize(width: Int, height: Int) {
            synchronized(surfaceHolder) {
                canvasWidth = width
                canvasHeight = height
                ballRadius = width * 0.015f
                paddleWidth = width * 0.2f
                paddleHeight = (height * 0.03f).coerceAtLeast(20f)
                val ctx = context
                if (ctx != null) {
                    textPaint.textSize = ctx.resources.getDimension(R.dimen.breakout_score_text_size)
                }
                resetGame()
            }
        }

        private fun resetGame() {
            createBricks()
            resetBall()
        }

        private fun resetBall() {
            ballX = canvasWidth / 2f
            ballY = canvasHeight / 2f
            val bx = canvasWidth * 0.015f
            val by = canvasHeight * 0.015f
            ballDX = if (Math.random() > 0.5) bx else -bx
            ballDY = -by
            paddleX = canvasWidth / 2f - paddleWidth / 2f
        }

        private fun createBricks() {
            val ctx = context ?: return
            bricks.clear()
            val padding = canvasWidth * 0.01f
            val availableWidth = canvasWidth - (brickCols + 1) * padding
            val brickWidth = availableWidth / brickCols
            val brickHeight = (canvasHeight * 0.04f).coerceAtLeast(20f)
            val topOffset = canvasHeight * 0.25f
            val colors = intArrayOf(
                ctx.getColor(R.color.brick_red),
                ctx.getColor(R.color.brick_amber),
                ctx.getColor(R.color.brick_green),
                ctx.getColor(R.color.brick_blue),
                ctx.getColor(R.color.brick_purple),
                ctx.getColor(R.color.brick_pink)
            )
            for (row in 0 until brickRows) {
                for (col in 0 until brickCols) {
                    val left = padding + col * (brickWidth + padding)
                    val top = topOffset + row * (brickHeight + padding)
                    bricks.add(Brick(RectF(left, top, left + brickWidth, top + brickHeight), colors[row % colors.size]))
                }
            }
        }

        override fun run() {
            var lastFrameTime = System.currentTimeMillis()
            while (running) {
                val now = System.currentTimeMillis()
                val delta = now - lastFrameTime
                if (delta >= 16) {
                    lastFrameTime = now
                    var canvas: Canvas? = null
                    try {
                        canvas = surfaceHolder.lockCanvas()
                        if (canvas != null) {
                            updatePhysics()
                            doDraw(canvas)
                        }
                    } finally {
                        canvas?.let { surfaceHolder.unlockCanvasAndPost(it) }
                    }
                } else try { sleep(2) } catch (e: InterruptedException) {}
            }
        }

        private fun updatePhysics() {
            calendar.timeInMillis = System.currentTimeMillis()
            currentHours = calendar[Calendar.HOUR_OF_DAY]
            currentMinutes = calendar[Calendar.MINUTE]
            currentSeconds = calendar[Calendar.SECOND]
            if (currentSeconds == 0 && lastProcessedSecond == 59) resetGame()
            lastProcessedSecond = currentSeconds

            ballX += ballDX
            ballY += ballDY

            if (ballX - ballRadius < 0) { ballX = ballRadius; ballDX = abs(ballDX) }
            else if (ballX + ballRadius > canvasWidth) { ballX = canvasWidth - ballRadius; ballDX = -abs(ballDX) }
            if (ballY - ballRadius < 0) { ballY = ballRadius; ballDY = abs(ballDY) }

            paddleX += (ballX - paddleWidth / 2f - paddleX) * 0.3f
            paddleX = paddleX.coerceIn(0f, canvasWidth - paddleWidth)

            val pY = canvasHeight - paddleYOffset
            if (ballY + ballRadius > pY && ballY - ballRadius < pY + paddleHeight && ballX > paddleX && ballX < paddleX + paddleWidth) {
                ballY = pY - ballRadius
                ballDY = -abs(ballDY)
                ballDX = (ballX - (paddleX + paddleWidth / 2f)) / (paddleWidth / 2f) * (canvasWidth * 0.02f)
                if (abs(ballDX) < (canvasWidth * 0.003f)) ballDX = if (ballDX < 0) -(canvasWidth * 0.003f) else (canvasWidth * 0.003f)
            }

            for (brick in bricks) {
                if (brick.active && RectF.intersects(brick.rect, RectF(ballX - ballRadius, ballY - ballRadius, ballX + ballRadius, ballY + ballRadius))) {
                    brick.active = false
                    ballDY = -ballDY
                    break
                }
            }
            if (ballY > canvasHeight) resetBall()
        }

        private fun doDraw(canvas: Canvas) {
            val ctx = context ?: return
            canvas.drawColor(bgColor)
            val scoreText = "${ctx.getString(R.string.score)} ${currentHours.toString().padStart(2, '0')}:${currentMinutes.toString().padStart(2, '0')}"
            canvas.drawText(scoreText, canvasWidth / 2f, canvasHeight * 0.18f, textPaint)
            for (brick in bricks) if (brick.active) { brickPaint.color = brick.color; canvas.drawRoundRect(brick.rect, 8f, 8f, brickPaint) }
            canvas.drawRoundRect(RectF(paddleX, canvasHeight - paddleYOffset, paddleX + paddleWidth, canvasHeight - paddleYOffset + paddleHeight), 10f, 10f, paddlePaint)
            canvas.drawCircle(ballX, ballY, ballRadius, ballPaint)
        }
    }
}
