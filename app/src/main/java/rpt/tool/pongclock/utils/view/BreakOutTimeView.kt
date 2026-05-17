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
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread = BreakOutThread(holder)
        thread?.setRunning(true)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        thread?.setSurfaceSize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread?.setRunning(false)
        try {
            thread?.join()
        } catch (e: InterruptedException) {
        }
        thread = null
    }

    inner class BreakOutThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        private var running = false
        private var canvasWidth = 0
        private var canvasHeight = 0

        private val ballRadius = 15f
        private var ballX = 0f
        private var ballY = 0f
        private var ballDX = 15f
        private var ballDY = -15f

        private val paddleWidth = 200f
        private val paddleHeight = 30f
        private var paddleX = 0f
        private val paddleYOffset = 150f

        private val brickRows = 6
        private val brickCols = 10
        private val bricks = mutableListOf<Brick>()

        private val brickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.FILL
            setShadowLayer(20f, 0f, 0f, context.getColor(R.color.white))
        }
        private val paddlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.FILL
            setShadowLayer(10f, 0f, 0f, context.getColor(R.color.white))
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.white)
            textSize = 120f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            alpha = 120
        }

        private var currentHours = 0
        private var currentMinutes = 0
        private var currentSeconds = 0
        private var lastProcessedSecond = -1

        fun setRunning(isRunning: Boolean) {
            running = isRunning
        }

        fun setSurfaceSize(width: Int, height: Int) {
            synchronized(surfaceHolder) {
                canvasWidth = width
                canvasHeight = height
                resetGame()
            }
        }

        private fun resetGame() {
            ballX = canvasWidth / 2f
            ballY = canvasHeight / 2f

            ballDX = if (Math.random() > 0.5) 15f else -15f
            ballDY = -15f

            paddleX = canvasWidth / 2f - paddleWidth / 2f
            createBricks()
        }

        private fun createBricks() {
            bricks.clear()
            val padding = 12f
            val availableWidth = canvasWidth - (brickCols + 1) * padding
            val brickWidth = availableWidth / brickCols
            val brickHeight = 45f
            val topOffset = 250f

            val colors = intArrayOf(
                context.getColor(R.color.brick_red),
                context.getColor(R.color.brick_amber),
                context.getColor(R.color.brick_green),
                context.getColor(R.color.brick_blue),
                context.getColor(R.color.brick_purple),
                context.getColor(R.color.brick_pink)
            )

            for (row in 0 until brickRows) {
                for (col in 0 until brickCols) {
                    val left = padding + col * (brickWidth + padding)
                    val top = topOffset + row * (brickHeight + padding)
                    bricks.add(Brick(RectF(left, top, left + brickWidth,
                        top + brickHeight), colors[row % colors.size]))
                }
            }
        }

        override fun run() {
            var lastFrameTime = System.currentTimeMillis()
            while (running) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - lastFrameTime

                if (deltaTime >= 16) {
                    lastFrameTime = currentTime
                    val canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        synchronized(surfaceHolder) {
                            updatePhysics()
                            doDraw(canvas)
                        }
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                } else {
                    try {
                        sleep(2)
                    } catch (e: InterruptedException) {}
                }
            }
        }

        private fun updatePhysics() {
            val calendar = Calendar.getInstance()
            currentHours = calendar.get(Calendar.HOUR_OF_DAY)
            currentMinutes = calendar.get(Calendar.MINUTE)
            currentSeconds = calendar.get(Calendar.SECOND)

            if (currentSeconds == 0 && lastProcessedSecond == 59) {
                resetGame()
            }
            lastProcessedSecond = currentSeconds

            ballX += ballDX
            ballY += ballDY

            if (ballX - ballRadius < 0) {
                ballX = ballRadius
                ballDX = abs(ballDX)
            } else if (ballX + ballRadius > canvasWidth) {
                ballX = canvasWidth - ballRadius
                ballDX = -abs(ballDX)
            }

            if (ballY - ballRadius < 0) {
                ballY = ballRadius
                ballDY = abs(ballDY)
            }

            val targetPaddleX = ballX - paddleWidth / 2f
            paddleX += (targetPaddleX - paddleX) * 0.3f
            paddleX = paddleX.coerceIn(0f, canvasWidth - paddleWidth)

            val paddleY = canvasHeight - paddleYOffset

            if (ballY + ballRadius > paddleY && ballY - ballRadius < paddleY + paddleHeight &&
                ballX > paddleX && ballX < paddleX + paddleWidth) {

                ballY = paddleY - ballRadius
                ballDY = -abs(ballDY)

                val hitPoint = (ballX - (paddleX + paddleWidth / 2f)) / (paddleWidth / 2f)
                ballDX = hitPoint * 20f
                if (abs(ballDX) < 3f) {
                    ballDX = if (ballDX < 0) -3f else 3f
                }
            }

            for (brick in bricks) {
                if (brick.active && RectF.intersects(brick.rect, RectF(ballX -
                            ballRadius, ballY - ballRadius, ballX + ballRadius, ballY + ballRadius))) {
                    brick.active = false
                    ballDY = -ballDY
                    break
                }
            }

            if (ballY > canvasHeight) {
                ballX = canvasWidth / 2f
                ballY = canvasHeight / 2f
                ballDY = -15f
                ballDX = if (Math.random() > 0.5) 15f else -15f
            }
        }

        private fun doDraw(canvas: Canvas) {
            canvas.drawColor(context.getColor(R.color.modern_background))

            val scoreText = buildString {
                append(context.getString(R.string.score))
                append(" ")
                append(currentHours.toString().padStart(2, '0'))
                append(":")
                append(currentMinutes.toString().padStart(2, '0'))
            }
            canvas.drawText(scoreText, canvasWidth / 2f, 180f, textPaint)

            for (brick in bricks) {
                if (brick.active) {
                    brickPaint.color = brick.color
                    canvas.drawRoundRect(brick.rect, 8f, 8f, brickPaint)
                }
            }

            val paddleRect = RectF(paddleX, canvasHeight - paddleYOffset,
                paddleX + paddleWidth, canvasHeight - paddleYOffset + paddleHeight)
            canvas.drawRoundRect(paddleRect, 10f, 10f, paddlePaint)

            canvas.drawCircle(ballX, ballY, ballRadius, ballPaint)
        }
    }
}