@file:Suppress("DEPRECATION")

package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import rpt.tool.pongclock.R
import androidx.core.graphics.withSave
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sin

class PacmanClockTimeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private var thread: PacmanThread? = null

    private fun colorCompat(resId: Int): Int = ContextCompat.getColor(context, resId)

    inner class PacmanThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        @Volatile private var running = false
        @Volatile private var pendingWidth = -1
        @Volatile private var pendingHeight = -1

        private var canvasHeight = 0
        private var canvasHeight2 = 0
        private var canvasWidth = 0
        private var canvasWidth2 = 0

        private var numberScale = 5f
        private var number1X = 0; private var number2X = 0
        private var number3X = 0; private var number4X = 0

        private var currentHours: Int = 0
        private var currentMinutes: Int = 0

        private val calendar = Calendar.getInstance()
        private val date = Date()

        // Colori classici Pac-Man
        private val COLOR_PACMAN = colorCompat(R.color.pacman_yellow)
        private val COLOR_BLINKY = colorCompat(R.color.red)
        private val COLOR_PINKY = colorCompat(R.color.pacman_pink)
        private val COLOR_INKY = colorCompat(R.color.cyan)
        private val COLOR_CLYDE = colorCompat(R.color.pacman_orange)
        private val COLOR_MAZE = colorCompat(R.color.pacman_maze)
        private val COLOR_PELLET = colorCompat(R.color.pacman_pellet)
        private val COLOR_SCARED = colorCompat(R.color.pacman_scared)

        // Strumenti di disegno
        private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val colonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private val mazePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bgMazePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val pacmanPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val ghostPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val redScorePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private val ghostPath = Path()
        private val mouthPath = Path()

        // Stati e attori dell'animazione inferiore
        private var trackY = 0f
        private var charRadius = 30f

        private val STATE_CHASE_RIGHT = 0
        private val STATE_CHASE_LEFT = 1
        private var animState = STATE_CHASE_RIGHT

        inner class Actor(var x: Float, val color: Int) {
            var isDead = false
        }

        private var pacmanX = 0f
        private val ghosts = arrayOf(
            Actor(0f, COLOR_BLINKY),
            Actor(0f, COLOR_PINKY),
            Actor(0f, COLOR_INKY),
            Actor(0f, COLOR_CLYDE)
        )

        private val numbers = arrayOf(
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH.toFloat(), LW.toFloat(), LH.toFloat(), 0f, LH.toFloat(), 0f, LH.toFloat(), 0f, 0f),
            floatArrayOf(LW.toFloat(), 0f, LW.toFloat(), LH.toFloat()),
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH2.toFloat(), LW.toFloat(), LH2.toFloat(), 0f, LH2.toFloat(), 0f, LH2.toFloat(), 0f, LH.toFloat(), 0f, LH.toFloat(), LW.toFloat(), LH.toFloat()),
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH2.toFloat(), LW.toFloat(), LH2.toFloat(), 0f, LH2.toFloat(), LW.toFloat(), LH2.toFloat(), LW.toFloat(), LH.toFloat(), LW.toFloat(), LH.toFloat(), 0f, LH.toFloat()),
            floatArrayOf(0f, 0f, 0f, LH2.toFloat(), 0f, LH2.toFloat(), LW.toFloat(), LH2.toFloat(), LW.toFloat(), 0f, LW.toFloat(), LH.toFloat()),
            floatArrayOf(LW.toFloat(), 0f, 0f, 0f, 0f, 0f, 0f, LH2.toFloat(), 0f, LH2.toFloat(), LW.toFloat(), LH2.toFloat(), LW.toFloat(), LH2.toFloat(), LW.toFloat(), LH.toFloat(), LW.toFloat(), LH.toFloat(), 0f, LH.toFloat()),
            floatArrayOf(LW.toFloat(), 0f, 0f, 0f, 0f, 0f, 0f, LH.toFloat(), 0f, LH.toFloat(), LW.toFloat(), LH.toFloat(), LW.toFloat(), LH.toFloat(), LW.toFloat(), LH2.toFloat(), LW.toFloat(), LH2.toFloat(), 0f, LH2.toFloat()),
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH.toFloat()),
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH.toFloat(), LW.toFloat(), LH.toFloat(), 0f, LH.toFloat(), 0f, LH.toFloat(), 0f, 0f, 0f, LH2.toFloat(), LW.toFloat(), LH2.toFloat()),
            floatArrayOf(LW.toFloat(), LH.toFloat(), LW.toFloat(), 0f, LW.toFloat(), 0f, 0f, 0f, 0f, 0f, 0f, LH2.toFloat(), 0f, LH2.toFloat(), LW.toFloat(), LH2.toFloat())
        )

        init {
            // Numeri dell'orologio (Linee pulite basate sulla griglia 15x26)
            numberPaint.color = COLOR_PELLET
            numberPaint.style = Paint.Style.STROKE
            numberPaint.strokeCap = Paint.Cap.ROUND
            numberPaint.strokeJoin = Paint.Join.ROUND
            numberPaint.strokeWidth = 2f // Spessore relativo alla griglia base, perfetto per lo scale!

            // I due punti lampeggianti (Power Pellets centrali)
            colonPaint.color = COLOR_PELLET
            colonPaint.style = Paint.Style.FILL

            mazePaint.color = COLOR_MAZE
            mazePaint.style = Paint.Style.STROKE
            mazePaint.strokeWidth = 6f

            bgMazePaint.color = COLOR_MAZE
            bgMazePaint.style = Paint.Style.STROKE
            bgMazePaint.strokeWidth = 4f
            bgMazePaint.alpha = 130

            pacmanPaint.color = COLOR_PACMAN
            pacmanPaint.style = Paint.Style.FILL

            ghostPaint.style = Paint.Style.FILL
            eyePaint.color = colorCompat(R.color.white)
            eyePaint.style = Paint.Style.FILL
            pupilPaint.color = colorCompat(R.color.pacman_maze)
            pupilPaint.style = Paint.Style.FILL

            scorePaint.color = colorCompat(R.color.white)
            scorePaint.typeface = Typeface.MONOSPACE
            scorePaint.textAlign = Paint.Align.CENTER

            redScorePaint.color = colorCompat(R.color.red)
            redScorePaint.typeface = Typeface.MONOSPACE
            redScorePaint.textAlign = Paint.Align.CENTER

            val now = System.currentTimeMillis()
            date.time = now
            currentHours = date.hours
            currentMinutes = date.minutes
        }

        fun setSurfaceSize(width: Int, height: Int) {
            pendingWidth = width
            pendingHeight = height
        }

        private fun applySurfaceSize() {
            val w = pendingWidth
            val h = pendingHeight
            if (w <= 0 || h <= 0) return

            pendingWidth = -1; pendingHeight = -1

            canvasWidth = w; canvasHeight = h
            canvasWidth2 = w / 2; canvasHeight2 = h / 2

            // Il calcolo dello scale per la griglia
            numberScale = (w / 180f).coerceIn(3f, 8f)

            val numWidth = LW * numberScale
            val spacing = numWidth * 0.6f
            val centerGap = numWidth * 1.5f

            number2X = (canvasWidth2 - centerGap / 2 - numWidth).toInt()
            number1X = (number2X - spacing - numWidth).toInt()
            number3X = (canvasWidth2 + centerGap / 2).toInt()
            number4X = (number3X + spacing + numWidth).toInt()

            scorePaint.textSize = h * 0.04f
            redScorePaint.textSize = h * 0.04f

            trackY = h * 0.82f
            charRadius = (h * 0.04f).coerceIn(20f, 60f)

            resetRunnersRight()
        }

        private fun resetRunnersRight() {
            pacmanX = -charRadius * 2
            for (i in ghosts.indices) {
                ghosts[i].x = pacmanX - (charRadius * 4) - (i * charRadius * 3)
                ghosts[i].isDead = false
            }
        }

        private fun resetRunnersLeftScared() {
            pacmanX = canvasWidth + (charRadius * 12)
            for (i in ghosts.indices) {
                ghosts[i].x = canvasWidth + charRadius + (i * charRadius * 3)
                ghosts[i].isDead = false
            }
        }

        fun setRunning(isRunning: Boolean) { running = isRunning }

        private fun updatePhysics(deltaMillis: Long) {
            val now = System.currentTimeMillis()
            date.time = now

            if (currentMinutes != date.minutes) {
                currentHours = date.hours
                currentMinutes = date.minutes
                animState = STATE_CHASE_LEFT
                resetRunnersLeftScared()
            }

            val secDelta = deltaMillis / 1000f

            if (animState == STATE_CHASE_RIGHT) {
                val speed = canvasWidth * 0.25f
                pacmanX += speed * secDelta
                ghosts.forEach { it.x += speed * secDelta }

                if (ghosts.last().x > canvasWidth + charRadius * 2) {
                    resetRunnersRight()
                }

            } else if (animState == STATE_CHASE_LEFT) {
                val pacSpeed = canvasWidth * 0.30f
                pacmanX -= pacSpeed * secDelta

                ghosts.forEach {
                    if (it.isDead) {
                        it.x -= (canvasWidth * 0.5f) * secDelta
                    } else {
                        it.x -= (canvasWidth * 0.18f) * secDelta
                        if (abs(pacmanX - it.x) < charRadius) {
                            it.isDead = true
                        }
                    }
                }

                if (pacmanX < -charRadius * 4 && ghosts.all { it.x < -charRadius * 2 }) {
                    animState = STATE_CHASE_RIGHT
                    resetRunnersRight()
                }
            }
        }

        private fun drawArcadeBackground(canvas: Canvas) {
            val topTextY = canvasHeight * 0.08f
            val scoreY = canvasHeight * 0.14f

            canvas.drawText(context.getString(R.string.pacman_one_up), canvasWidth * 0.15f, topTextY, redScorePaint)
            canvas.drawText("00", canvasWidth * 0.15f, scoreY, scorePaint)

            canvas.drawText(context.getString(R.string.pacman_high_score), canvasWidth * 0.5f, topTextY, redScorePaint)
            canvas.drawText("10000", canvasWidth * 0.5f, scoreY, scorePaint)

            val margin = 30f
            val topY = canvasHeight * 0.20f
            val bottomY = trackY - charRadius * 2.2f

            canvas.drawRoundRect(margin, topY, canvasWidth - margin, bottomY, 15f, 15f, bgMazePaint)
            canvas.drawRoundRect(margin + 12f, topY + 12f, canvasWidth - margin - 12f, bottomY - 12f, 10f, 10f, bgMazePaint)

            val clockTop = canvasHeight2 * 0.65f - (LH * numberScale / 2) - 30f
            val clockBottom = canvasHeight2 * 0.65f + (LH * numberScale / 2) + 30f
            val cx = canvasWidth2.toFloat()
            val innerMargin = margin + 50f

            canvas.drawRoundRect(innerMargin, clockTop, cx - (LW * numberScale * 2) - 40f, clockBottom, 15f, 15f, bgMazePaint)
            canvas.drawRoundRect(cx + (LW * numberScale * 2) + 40f, clockTop, canvasWidth - innerMargin, clockBottom, 15f, 15f, bgMazePaint)

            drawCherry(canvas, innerMargin + 30f, bottomY - 40f)
            drawCherry(canvas, canvasWidth - innerMargin - 30f, bottomY - 40f)
        }

        private fun drawCherry(canvas: Canvas, x: Float, y: Float) {
            val cherryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
            val stemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE; strokeWidth = 3f; color = colorCompat(R.color.pacman_stem)
            }
            canvas.drawLine(x, y, x + 10f, y - 20f, stemPaint)
            canvas.drawLine(x + 20f, y, x + 10f, y - 20f, stemPaint)
            canvas.drawLine(x + 10f, y - 20f, x + 25f, y - 25f, stemPaint)
            stemPaint.color = colorCompat(R.color.green)
            canvas.drawLine(x + 10f, y - 20f, x, y - 25f, stemPaint)

            cherryPaint.color = colorCompat(R.color.red)
            canvas.drawCircle(x, y, 10f, cherryPaint)
            canvas.drawCircle(x + 20f, y, 10f, cherryPaint)

            cherryPaint.color = colorCompat(R.color.white)
            canvas.drawCircle(x - 3f, y - 3f, 2f, cherryPaint)
            canvas.drawCircle(x + 17f, y - 3f, 2f, cherryPaint)
        }

        private fun doDraw(canvas: Canvas) {
            canvas.drawColor(colorCompat(R.color.black))

            val tick = System.currentTimeMillis()

            // 1. Sfondo Labirinto e Scritte
            drawArcadeBackground(canvas)

            // 2. Disegno Orologio Principale (Ora i numeri saranno leggibili e nitidi)
            val clockCenterY = canvasHeight2 * 0.65f
            drawNum(canvas, number1X, currentHours / 10, clockCenterY)
            drawNum(canvas, number2X, currentHours % 10, clockCenterY)

            // Due punti centrali (Power Pellets lampeggianti)
            val alphaPulse = ((sin(tick * 0.005) + 1) * 127).toInt()
            colonPaint.alpha = alphaPulse
            val colonRadius = numberScale * 1.2f
            canvas.drawCircle(canvasWidth2.toFloat(), clockCenterY - LH * numberScale * 0.15f, colonRadius, colonPaint)
            canvas.drawCircle(canvasWidth2.toFloat(), clockCenterY + LH * numberScale * 0.15f, colonRadius, colonPaint)

            drawNum(canvas, number3X, currentMinutes / 10, clockCenterY)
            drawNum(canvas, number4X, currentMinutes % 10, clockCenterY)

            // 3. Disegno Pista (Corsia bassa animata)
            val trackH = charRadius * 1.5f
            canvas.drawLine(0f, trackY - trackH, canvasWidth.toFloat(), trackY - trackH, mazePaint)
            canvas.drawLine(0f, trackY + trackH, canvasWidth.toFloat(), trackY + trackH, mazePaint)
            canvas.drawRect(-10f, trackY - trackH - 12f, canvasWidth + 10f, trackY - trackH, mazePaint)
            canvas.drawRect(-10f, trackY + trackH, canvasWidth + 10f, trackY + trackH + 12f, mazePaint)

            // 4. Disegno Personaggi
            val pacmanDir = if (animState == STATE_CHASE_RIGHT) 0 else 2
            drawPacman(canvas, pacmanX, trackY, pacmanDir, tick)

            val ghostDir = if (animState == STATE_CHASE_RIGHT) 0 else 2
            val isScared = (animState == STATE_CHASE_LEFT)

            ghosts.forEach { ghost ->
                drawGhost(canvas, ghost.x, trackY, ghost.color, ghostDir, isScared, ghost.isDead, tick)
            }
        }

        private fun drawNum(canvas: Canvas, x: Int, n: Int, centerY: Float) {
            canvas.withSave {
                val numHeight = LH * numberScale
                translate(x.toFloat(), centerY - numHeight / 2)

                // Lo scaling trasforma la griglia a dimensioni fisiche corrette per lo schermo
                // Lo strokeWidth di 2f viene anch'esso scalato diventando proporzionato!
                scale(numberScale, numberScale)

                drawLines(numbers[n], numberPaint)
            }
        }

        private fun drawPacman(canvas: Canvas, x: Float, y: Float, dir: Int, tick: Long) {
            val mouthAngle = (sin(tick * 0.015) * 45).toFloat().absoluteValue
            var start = mouthAngle
            val sweep = 360f - (2 * mouthAngle)

            if (dir == 2) {
                start = 180f + mouthAngle
            }

            val rect = RectF(x - charRadius, y - charRadius, x + charRadius, y + charRadius)
            canvas.drawArc(rect, start, sweep, true, pacmanPaint)
        }

        private fun drawGhost(canvas: Canvas, x: Float, y: Float, color: Int, dir: Int, isScared: Boolean, isDead: Boolean, tick: Long) {
            if (!isDead) {
                ghostPaint.color = if (isScared) COLOR_SCARED else color
                ghostPath.reset()

                ghostPath.moveTo(x - charRadius, y + charRadius)
                ghostPath.lineTo(x - charRadius, y)
                ghostPath.arcTo(RectF(x - charRadius, y - charRadius, x + charRadius, y + charRadius), 180f, 180f)
                ghostPath.lineTo(x + charRadius, y + charRadius)

                val wOff = if ((tick / 200) % 2 == 0L) 0f else charRadius / 3f
                val base = y + charRadius
                ghostPath.lineTo(x + charRadius - charRadius/3, base - 5f + wOff)
                ghostPath.lineTo(x + charRadius - 2*charRadius/3, base - wOff)
                ghostPath.lineTo(x, base - 5f + wOff)
                ghostPath.lineTo(x - 2*charRadius/3, base - wOff)
                ghostPath.lineTo(x - charRadius/3, base - 5f + wOff)
                ghostPath.lineTo(x - charRadius, base)

                canvas.drawPath(ghostPath, ghostPaint)

                if (isScared) {
                    eyePaint.color = COLOR_PINKY
                    canvas.drawCircle(x - charRadius/3, y - charRadius/5, charRadius/6, eyePaint)
                    canvas.drawCircle(x + charRadius/3, y - charRadius/5, charRadius/6, eyePaint)

                    mouthPath.reset()
                    val my = y + charRadius/3
                    val mw = charRadius/2
                    mouthPath.moveTo(x - mw, my + 4f)
                    mouthPath.lineTo(x - mw/2, my - 2f)
                    mouthPath.lineTo(x, my + 4f)
                    mouthPath.lineTo(x + mw/2, my - 2f)
                    mouthPath.lineTo(x + mw, my + 4f)

                    val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        this.color = COLOR_PINKY
                        this.style = Paint.Style.STROKE
                        this.strokeWidth = 3f
                        this.strokeJoin = Paint.Join.ROUND
                    }
                    canvas.drawPath(mouthPath, mouthPaint)
                    return
                }
            }

            eyePaint.color = colorCompat(R.color.white)
            val eyeRad = charRadius / 3f
            val pupilRad = eyeRad / 2f

            var dx = 0f
            if (dir == 0) dx = eyeRad / 2f
            if (dir == 2) dx = -eyeRad / 2f

            canvas.drawCircle(x - eyeRad + dx/2, y - eyeRad, eyeRad, eyePaint)
            canvas.drawCircle(x + eyeRad + dx/2, y - eyeRad, eyeRad, eyePaint)

            canvas.drawCircle(x - eyeRad + dx, y - eyeRad, pupilRad, pupilPaint)
            canvas.drawCircle(x + eyeRad + dx, y - eyeRad, pupilRad, pupilPaint)
        }

        override fun run() {
            var lastFrameTime = System.currentTimeMillis()
            while (running && !isInterrupted) {
                try {
                    val now = System.currentTimeMillis()
                    val delta = now - lastFrameTime
                    if (delta >= 16) {
                        lastFrameTime = now
                        applySurfaceSize()
                        if (canvasWidth > 0 && canvasHeight > 0 && running) {
                            var c: Canvas? = null
                            try {
                                c = surfaceHolder.lockCanvas()
                                if (c != null && running) {
                                    updatePhysics(delta)
                                    doDraw(c)
                                }
                            } finally {
                                if (c != null) {
                                    try { surfaceHolder.unlockCanvasAndPost(c) } catch (t: Throwable) {}
                                }
                            }
                        }
                    } else {
                        sleep(2)
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    init {
        holder.addCallback(this)
        isFocusable = false
        isClickable = false
    }

    override fun surfaceChanged(h: SurfaceHolder, format: Int, w: Int, height: Int) {
        thread?.setSurfaceSize(w, height)
    }

    override fun surfaceCreated(h: SurfaceHolder) {
        thread = PacmanThread(h)
        thread?.setRunning(true)
        thread?.start()
    }

    override fun surfaceDestroyed(h: SurfaceHolder) {
        var retry = true
        thread?.setRunning(false)
        thread?.interrupt()

        while (retry) {
            try {
                thread?.join(100)
                retry = false
            } catch (e: InterruptedException) {
                retry = false
            }
        }
        thread = null
    }

    companion object {
        const val LW = 15
        const val LH = 26
        const val LH2 = LH / 2
    }
}