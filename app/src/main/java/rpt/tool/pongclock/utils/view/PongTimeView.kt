@file:Suppress("DEPRECATION")

package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.graphics.withSave
import rpt.tool.pongclock.R
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.extensions.toColor
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class PongTimeView(context: Context?, attrs: AttributeSet?) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var thread: PongThread? = null

    internal inner class Panel(var x: Int, var y: Int)

    internal inner class Ball(var x: Float, var y: Float) {
        var direction: Float = 0f
        var cosDir: Float = 0f
        var sinDir: Float = 0f
        fun computeDir() {
            cosDir = cos(direction.toDouble()).toFloat()
            sinDir = sin(direction.toDouble()).toFloat()
        }
    }

    internal inner class FxParticle(
        var x: Float, var y: Float,
        var dx: Float, var dy: Float,
        var life: Float, var maxLife: Float,
        val type: Int, val extra: String = "", val color: Int
    )

    inner class PongThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        private var ball: Ball? = null
        private var leftPanel: Panel? = null
        private var rightPanel: Panel? = null

        private var number1X = 0; private var number2X = 0
        private var number3X = 0; private var number4X = 0

        private var currentHours: Int = 0
        private var currentMinutes: Int = 0
        private var waitCount = 0

        private var playFieldY1 = 0; private var playFieldY2 = 0
        private var playFieldX1 = 0; private var playFieldX2 = 0

        @Volatile private var running = false
        private var lastTimeMillis: Long = 0
        private var nextTimeUpdate: Long = 0
        private var gMode = 0

        private var canvasHeight = 0; private var canvasHeight2 = 0
        private var canvasWidth = 0; private var canvasWidth2 = 0
        private var numberScale = 5f
        private var panelLength = 20f
        private var panelXPos = 25f
        private var ballSpeed = 300f
        private var panelSpeed = 250f

        private var isClassic = false
        private var isMatrix = false
        private var isBreakout = false
        private var isSeason = false
        private var holiday = AppUtils.Companion.Holiday.None
        private var currentSeason = AppUtils.Companion.Season.Winter

        private val particles = mutableListOf<FxParticle>()
        private val ballTrail = LinkedList<PointF>()

        private val dashedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign =
            Paint.Align.CENTER }
        private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var bgColor: Int = 0
        private var mainColor: Int = 0

        private val calendar = Calendar.getInstance()
        private val date = Date()

        private val numbers = arrayOf(
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH.toFloat(),
                LW.toFloat(), LH.toFloat(), 0f, LH.toFloat(), 0f, LH.toFloat(), 0f, 0f),
            floatArrayOf(LW.toFloat(), 0f, LW.toFloat(), LH.toFloat()),
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH2.toFloat(),
                LW.toFloat(), LH2.toFloat(), 0f, LH2.toFloat(), 0f, LH2.toFloat(), 0f,
                LH.toFloat(), 0f, LH.toFloat(), LW.toFloat(), LH.toFloat()),
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH2.toFloat(),
                LW.toFloat(), LH2.toFloat(), 0f, LH2.toFloat(), LW.toFloat(), LH2.toFloat(),
                LW.toFloat(), LH.toFloat(), LW.toFloat(), LH.toFloat(), 0f, LH.toFloat()),
            floatArrayOf(0f, 0f, 0f, LH2.toFloat(), 0f, LH2.toFloat(), LW.toFloat(), LH2.toFloat(),
                LW.toFloat(), 0f, LW.toFloat(), LH.toFloat()),
            floatArrayOf(LW.toFloat(), 0f, 0f, 0f, 0f, 0f, 0f, LH2.toFloat(), 0f, LH2.toFloat(),
                LW.toFloat(), LH2.toFloat(), LW.toFloat(), LH2.toFloat(), LW.toFloat(), LH.toFloat(),
                LW.toFloat(), LH.toFloat(), 0f, LH.toFloat()),
            floatArrayOf(LW.toFloat(), 0f, 0f, 0f, 0f, 0f, 0f, LH.toFloat(), 0f, LH.toFloat(),
                LW.toFloat(), LH.toFloat(), LW.toFloat(), LH.toFloat(), LW.toFloat(), LH2.toFloat(),
                LW.toFloat(), LH2.toFloat(), 0f, LH2.toFloat()),
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH.toFloat()),
            floatArrayOf(0f, 0f, LW.toFloat(), 0f, LW.toFloat(), 0f, LW.toFloat(), LH.toFloat(),
                LW.toFloat(), LH.toFloat(), 0f, LH.toFloat(), 0f, LH.toFloat(), 0f, 0f, 0f,
                LH2.toFloat(), LW.toFloat(), LH2.toFloat()),
            floatArrayOf(LW.toFloat(), LH.toFloat(), LW.toFloat(), 0f, LW.toFloat(), 0f, 0f,
                0f, 0f, 0f, 0f, LH2.toFloat(), 0f, LH2.toFloat(), LW.toFloat(), LH2.toFloat())
        )

        init {
            dashedLinePaint.style = Paint.Style.STROKE
            dashedLinePaint.strokeWidth = LINE_WIDTH.toFloat()
            dashedLinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 20f),
                0.0f)
            gridPaint.strokeWidth = 1f

            val now = System.currentTimeMillis()
            date.time = now
            currentHours = date.hours
            currentMinutes = date.minutes
            
            context?.let { ctx ->
                bgColor = ctx.getColor(R.color.black)
                mainColor = ctx.getColor(R.color.white)
            }
            updateThemeAndColors()
        }

        private fun updateThemeAndColors() {
            val ctx = context ?: return
            calendar.timeInMillis = System.currentTimeMillis()

            holiday = AppUtils.getHoliday(calendar)
            currentSeason = AppUtils.getSeason(calendar[Calendar.DAY_OF_YEAR])
            
            isMatrix = SharedPreferencesManager.mode == 1
            isBreakout = SharedPreferencesManager.breakOut == 1
            isSeason = SharedPreferencesManager.season == 1
            val isFuturistic = SharedPreferencesManager.futuristic == 1
            
            // Classic è il default
            isClassic = (SharedPreferencesManager.classic == 1 || (!isMatrix && !isBreakout && !isSeason && !isFuturistic)) && holiday ==
                    AppUtils.Companion.Holiday.None

            mainColor = getColorInternal(ctx, calendar)
            bgColor = if (isClassic || holiday != AppUtils.Companion.Holiday.None)
                ctx.getColor(R.color.black) else ctx.getColor(R.color.modern_background)
            if (holiday == AppUtils.Companion.Holiday.Halloween) bgColor = ctx.getColor(R.color.halloween_bg)

            linePaint.color = mainColor
            linePaint.style = Paint.Style.STROKE
            linePaint.strokeWidth = LINE_WIDTH.toFloat()
            linePaint.strokeCap = Paint.Cap.SQUARE

            panelPaint.color = mainColor
            panelPaint.style = if (isClassic) Paint.Style.STROKE else Paint.Style.FILL_AND_STROKE
            panelPaint.strokeWidth = PANEL_LINE_WIDTH.toFloat()
            panelPaint.strokeCap = Paint.Cap.SQUARE

            if (holiday == AppUtils.Companion.Holiday.Christmas) {
                linePaint.color = ctx.getColor(R.color.christmas_red)
                panelPaint.color = ctx.getColor(R.color.christmas_green)
            } else if (holiday == AppUtils.Companion.Holiday.Halloween) {
                linePaint.color = ctx.getColor(R.color.halloween_orange)
                panelPaint.color = ctx.getColor(R.color.halloween_orange)
            }

            if (!isClassic) {
                linePaint.setShadowLayer(20f, 0f, 0f,
                    linePaint.color)
                panelPaint.setShadowLayer(20f, 0f, 0f,
                    panelPaint.color)
            } else {
                linePaint.clearShadowLayer()
                panelPaint.clearShadowLayer()
            }

            dashedLinePaint.color = mainColor
            dashedLinePaint.alpha = if (isClassic) 255 else 100
            gridPaint.color = mainColor
            gridPaint.alpha = if (isClassic) 0 else 20
        }

        fun setSurfaceSize(width: Int, height: Int) {
            synchronized(surfaceHolder) {
                canvasWidth = width; canvasHeight = height
                canvasWidth2 = width / 2; canvasHeight2 = height / 2
                playFieldY1 = (height * 0.05f).toInt().coerceAtMost(30)
                playFieldY2 = height - playFieldY1
                playFieldX1 = (width * 0.01f).toInt().coerceAtMost(10)
                playFieldX2 = width - playFieldX1
                panelXPos = (width * 0.05f).coerceAtMost(50f)
                panelLength = (height * 0.1f).coerceAtMost(100f)
                ballSpeed = width * 0.4f
                panelSpeed = height * 0.35f

                leftPanel = Panel(panelXPos.toInt(), canvasHeight2)
                rightPanel = Panel((width - panelXPos).toInt(), canvasHeight2)
                ball = Ball(canvasWidth2.toFloat(), canvasHeight2.toFloat())

                numberScale = (width / 200f).coerceIn(3f, 8f)
                val numWidth = LW * numberScale
                val spacing = numWidth * 0.6f
                val centerGap = numWidth * 1.5f
                number2X = (canvasWidth2 - centerGap / 2 - numWidth).toInt()
                number1X = (number2X - spacing - numWidth).toInt()
                number3X = (canvasWidth2 + centerGap / 2).toInt()
                number4X = (number3X + spacing + numWidth).toInt()

                emojiPaint.textSize = panelLength * 1.5f

                lastTimeMillis = System.currentTimeMillis()
                nextTimeUpdate = (lastTimeMillis / 1000) * 1000
                newGame(true)
                setRunning(true)
            }
        }

        fun setRunning(isRunning: Boolean) { running = isRunning }

        private fun newGame(left: Boolean) {
            ball?.y = (Math.random() * (playFieldY2 - playFieldY1) + playFieldY1).toFloat()
            ball?.x = (if (left) canvasWidth2 - 40 else canvasWidth2 + 40).toFloat()
            val d = (Math.random() * 0.8 - 0.4).toFloat()
            ball?.direction = if (left) 0.0f + d else Math.PI.toFloat() + d
            ball?.computeDir()
            ballTrail.clear()
        }

        private fun updatePhysics(deltaMillis: Long) {
            updateTime()
            updateParticles()

            if (gMode == GSTATE_STOPPED || gMode == GSTATE_NONE) return
            val b = ball ?: return

            if (!isClassic && !isBreakout) {
                ballTrail.addFirst(PointF(b.x, b.y))
                if (ballTrail.size > 8) ballTrail.removeLast()
            }

            val distance = ballSpeed / (1000 / deltaMillis)
            val dX = distance * b.cosDir

            if (gMode != GSTATE_HOURSWIN) movePanel(rightPanel ?: return,
                if (dX > 0 && b.x > canvasWidth2) b.y.toInt() else canvasHeight2,
                deltaMillis)
            if (gMode != GSTATE_MINUTESWIN) movePanel(leftPanel ?: return,
                if (dX < 0 && b.x < canvasWidth2) b.y.toInt() else canvasHeight2,
                deltaMillis)

            b.y += distance * b.sinDir

            if (b.y < playFieldY1) {
                b.y = playFieldY1 + (playFieldY1 - b.y); b.direction = -b.direction; b.computeDir()
            } else if (b.y > playFieldY2) {
                b.y = playFieldY2 - (b.y - playFieldY2); b.direction = -b.direction; b.computeDir()
            }

            b.x += dX
            val rp = rightPanel ?: return
            val lp = leftPanel ?: return

            if (b.x > rp.x && b.y > rp.y - panelLength && b.y < rp.y + panelLength) {
                b.x = rp.x + (rp.x - b.x)
                b.direction = (-b.direction + Math.PI + Math.random() * 0.6 - 0.3).toFloat()
                b.direction = b.direction.coerceIn(MIN_RANGLE, MAX_RANGLE)
                b.computeDir()
                onPaddleHit(b.x, b.y)
            } else if (b.x < lp.x && b.y > lp.y - panelLength && b.y < lp.y + panelLength) {
                b.x = lp.x + (lp.x - b.x)
                b.direction = -(b.direction - Math.PI.toFloat() + Math.random() * 0.6 - 0.3)
                    .toFloat()
                b.computeDir()
                onPaddleHit(b.x, b.y)
            }

            // Punto segnato
            if (b.x < playFieldX1 || b.x > playFieldX2) {
                newGame(gMode != GSTATE_MINUTESWIN)
                if (gMode == GSTATE_HOURSWIN || gMode == GSTATE_MINUTESWIN) gMode = GSTATE_STOPPED
            }
        }

        private fun onPaddleHit(x: Float, y: Float) {
            val ctx = context ?: return
            if (holiday == AppUtils.Companion.Holiday.NewYear) {
                val sparkColor = listOf(ctx.getColor(R.color.yellow), ctx.getColor(R.color.white), ctx.getColor(
                    R.color.newyear_gold)).random()
                for (i in 0..15) {
                    particles.add(FxParticle(x, y, (Random.nextFloat() - 0.5f) * 10f,
                        (Random.nextFloat() - 0.5f) * 10f, 1f, 1f, 3,
                        "", sparkColor))
                }
            }
        }

        private fun updateParticles() {
            val ctx = context ?: return
            if (holiday == AppUtils.Companion.Holiday.Halloween && Random.nextFloat() < 0.02f) {
                particles.add(FxParticle(Random.nextFloat() * canvasWidth, canvasHeight +
                        50f, 0f, -Random.nextFloat() * 3 - 1f, 1f, 1f,
                    4, listOf("🦇", "👻").random(), ctx.getColor(R.color.white)))
            } else if (isSeason && currentSeason == AppUtils.Companion.Season.Winter &&
                Random.nextFloat() < 0.1f) {
                particles.add(FxParticle(Random.nextFloat() * canvasWidth, -10f,
                    Random.nextFloat() * 2 - 1f, Random.nextFloat() * 4 + 2f, 1f,
                    1f, 1, "", ctx.getColor(R.color.white)))
            } else if (isMatrix && Random.nextFloat() < 0.05f) {
                particles.add(FxParticle(Random.nextFloat() * canvasWidth, -10f, 0f,
                    Random.nextFloat() * 10 + 5f, 1f, 1f, 2,
                    "01".random().toString(), ctx.getColor(R.color.matrix)))
            }

            // Aggiorna ciclo vitale
            val iter = particles.iterator()
            while (iter.hasNext()) {
                val p = iter.next()
                p.x += p.dx; p.y += p.dy
                if (p.type == 3) { p.dy += 0.2f; p.life -= 0.03f }
                if (p.y < -100 || p.y > canvasHeight + 100 || p.life <= 0) iter.remove()
            }
        }

        private fun movePanel(p: Panel, target: Int, deltaMillis: Long) {
            val dPanel = target - p.y
            if (abs(dPanel) > 6) {
                val dist = (panelSpeed / (1000f / deltaMillis)).toInt()
                p.y += if (dPanel > 0) dist else -dist
            }
        }

        private fun updateTime() {
            val now = System.currentTimeMillis()
            if (now > nextTimeUpdate) {
                nextTimeUpdate += 1000
                date.time = now
                updateThemeAndColors()

                when (gMode) {
                    GSTATE_PLAY -> if (currentHours != date.hours) gMode = GSTATE_HOURSWIN else if
                            (currentMinutes != date.minutes) gMode = GSTATE_MINUTESWIN else
                            { currentHours = date.hours; currentMinutes = date.minutes }
                    GSTATE_STOPPED -> { waitCount++; if (waitCount == 2) { gMode = GSTATE_PLAY
                        waitCount = 0 }; currentHours = date.hours; currentMinutes = date.minutes }
                    GSTATE_NONE -> { waitCount++; if (waitCount >= 3) { gMode = GSTATE_PLAY
                        waitCount = 0 } }
                    else -> gMode = GSTATE_PLAY
                }
            }
        }

        private fun doDraw(canvas: Canvas?) {
            if (canvas == null) return
            val ctx = context ?: return
            canvas.drawColor(bgColor)

            if (!isClassic && !isBreakout) {
                val step = 100
                for (x in 0..canvasWidth step step) canvas.drawLine(x.toFloat(),
                    0f, x.toFloat(), canvasHeight.toFloat(), gridPaint)
                for (y in 0..canvasHeight step step) canvas.drawLine(0f,
                    y.toFloat(), canvasWidth.toFloat(), y.toFloat(), gridPaint)
            }
            particles.forEach { p ->
                when (p.type) {
                    1 -> { particlePaint.color = ctx.getColor(R.color.white); canvas.drawCircle(p.x,
                        p.y, 4f, particlePaint) }
                    2 -> { particlePaint.color = ctx.getColor(R.color.matrix); particlePaint.textSize = 30f; canvas.drawText(p.extra, p.x,
                        p.y, particlePaint) }
                    3 -> { particlePaint.color = p.color; particlePaint.alpha = (p.life * 255)
                        .toInt().coerceIn(0, 255); canvas.drawCircle(p.x, p.y, 5f,
                        particlePaint) }
                    4 -> { emojiPaint.textSize = 60f; emojiPaint.alpha = 150; canvas.
                    drawText(p.extra, p.x, p.y, emojiPaint) }
                }
            }
            particlePaint.alpha = 255

            if (isBreakout) {
                val brickWidth = canvasWidth / 12f
                val brickColors = listOf(
                    ctx.getColor(R.color.red),
                    ctx.getColor(R.color.orange),
                    ctx.getColor(R.color.yellow),
                    ctx.getColor(R.color.green),
                    ctx.getColor(R.color.cyan)
                )
                for (i in 0..12) {
                    panelPaint.color = brickColors[i % brickColors.size]
                    canvas.drawRoundRect(i * brickWidth + 2, 5f, (i + 1) *
                            brickWidth - 2, playFieldY1.toFloat(), 4f, 4f, panelPaint)
                    canvas.drawRoundRect(i * brickWidth + 2, playFieldY2.toFloat(),
                        (i + 1) * brickWidth - 2, canvasHeight - 5f, 4f,
                        4f, panelPaint)
                }
                panelPaint.color = mainColor // Reset
            } else {
                canvas.drawLine(0f, playFieldY1.toFloat(),
                    canvasWidth.toFloat(), playFieldY1.toFloat(), linePaint)
                canvas.drawLine(0f, playFieldY2.toFloat(),
                    canvasWidth.toFloat(), playFieldY2.toFloat(), linePaint)
            }

            canvas.drawLine(canvasWidth2.toFloat(), 0f,
                canvasWidth2.toFloat(), canvasHeight.toFloat(), dashedLinePaint)

            val lp = leftPanel ?: return
            val rp = rightPanel ?: return

            if (isClassic) {
                canvas.drawLine(lp.x.toFloat(), lp.y - panelLength,
                    lp.x.toFloat(), lp.y + panelLength, panelPaint)
                canvas.drawLine(rp.x.toFloat(), rp.y - panelLength,
                    rp.x.toFloat(), rp.y + panelLength, panelPaint)
            } else if (isBreakout) {
                val lColor = ctx.getColor(R.color.brick_blue)
                val rColor = ctx.getColor(R.color.brick_red)
                panelPaint.color = lColor
                canvas.drawRoundRect(RectF(lp.x - 12f, lp.y - panelLength * 1.5f,
                    lp.x + 12f, lp.y + panelLength * 1.5f), 12f, 12f,
                    panelPaint)
                panelPaint.color = rColor
                canvas.drawRoundRect(RectF(rp.x - 12f, rp.y - panelLength * 1.5f,
                    rp.x + 12f, rp.y + panelLength * 1.5f), 12f, 12f,
                    panelPaint)
                panelPaint.color = mainColor
            } else {
                canvas.drawRoundRect(RectF(lp.x - 8f, lp.y - panelLength * 1.2f,
                    lp.x + 8f, lp.y + panelLength * 1.2f), 8f, 8f, panelPaint)
                canvas.drawRoundRect(RectF(rp.x - 8f, rp.y - panelLength * 1.2f,
                    rp.x + 8f, rp.y + panelLength * 1.2f), 8f, 8f, panelPaint)
            }

            // Draw Ball & Trail
            val b = ball ?: return

            if (!isClassic && !isBreakout && holiday == AppUtils.Companion.Holiday.None) {
                // Futuristic Trail Effect
                ballTrail.forEachIndexed { index, point ->
                    panelPaint.alpha = 255 - (index * 30)
                    canvas.drawCircle(point.x, point.y, 10f - (index * 0.8f),
                        panelPaint)
                }
                panelPaint.alpha = 255
            }

            when {
                holiday == AppUtils.Companion.Holiday.Halloween -> {
                    emojiPaint.textSize = panelLength * 1.5f
                    emojiPaint.alpha = 255
                    canvas.drawText("🎃", b.x, b.y + (emojiPaint.textSize / 3), emojiPaint)
                }
                holiday == AppUtils.Companion.Holiday.NewYear -> {
                    panelPaint.color = ctx.getColor(R.color.newyear_gold)
                    canvas.drawCircle(b.x, b.y, 18f, panelPaint)
                    panelPaint.color = mainColor
                }
                isBreakout -> {
                    panelPaint.color = ctx.getColor(R.color.white)
                    canvas.drawCircle(b.x, b.y, 14f, panelPaint)
                    panelPaint.color = mainColor
                }
                isClassic || isMatrix -> canvas.drawRect(b.x - 8, b.y - 8, b.x +
                        8, b.y + 8, panelPaint)
                else -> canvas.drawCircle(b.x, b.y, 12f, panelPaint)
            }

            drawNum(canvas, number1X, currentHours / 10)
            drawNum(canvas, number2X, currentHours % 10)
            drawNum(canvas, number3X, currentMinutes / 10)
            drawNum(canvas, number4X, currentMinutes % 10)
        }

        private fun drawNum(canvas: Canvas, x: Int, n: Int) {
            canvas.withSave {
                val numHeight = LH * numberScale
                translate(x.toFloat(), canvasHeight2 - numHeight / 2)
                scale(numberScale, numberScale)
                drawLines(numbers[n], linePaint)
            }
        }

        override fun run() {
            var lastFrameTime = System.currentTimeMillis()
            while (running) {
                val now = System.currentTimeMillis()
                val delta = now - lastFrameTime
                if (delta >= 16) {
                    lastFrameTime = now
                    var c: Canvas? = null
                    try {
                        c = surfaceHolder.lockCanvas()
                        if (c != null) {
                            updatePhysics(delta)
                            doDraw(c)
                        }
                    } finally { c?.let { surfaceHolder.unlockCanvasAndPost(it) } }
                } else try { sleep(2) } catch (e: InterruptedException) {}
            }
        }
    }

    private fun getColorInternal(ctx: Context, calendar: Calendar): Int {
        val holiday = AppUtils.getHoliday(calendar)
        if (holiday != AppUtils.Companion.Holiday.None) {
            return when (holiday) {
                AppUtils.Companion.Holiday.Halloween -> ctx.getColor(R.color.halloween_orange)
                AppUtils.Companion.Holiday.Christmas -> ctx.getColor(R.color.christmas_red)
                AppUtils.Companion.Holiday.NewYear -> ctx.getColor(R.color.newyear_gold)
                else -> ctx.getColor(R.color.white)
            }
        }
        
        val isMatrix = SharedPreferencesManager.mode == 1
        val isSeason = SharedPreferencesManager.season == 1
        val isBreakout = SharedPreferencesManager.breakOut == 1
        val isFuturistic = SharedPreferencesManager.futuristic == 1
        val isClassic = SharedPreferencesManager.classic == 1 || (!isMatrix && !isSeason && !isBreakout && !isFuturistic)

        var color = ctx.getColor(R.color.white) // Classic default
        if (isClassic) color = ctx.getColor(R.color.white)
        else if (isMatrix) color = ctx.getColor(R.color.matrix)
        else if (isSeason) color = ctx.getColor(AppUtils.getSeason(calendar[Calendar.DAY_OF_YEAR]).toColor())
        else if (isFuturistic) color = ctx.getColor(R.color.modern_cyan)

        return color
    }

    init {
        holder.addCallback(this)
        isFocusable = false
        isClickable = false
    }

    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, height: Int) {
        thread?.setSurfaceSize(w, height) }
    override fun surfaceCreated(h: SurfaceHolder) { thread = PongThread(h)
        thread?.start() }
    override fun surfaceDestroyed(h: SurfaceHolder) {
        thread?.setRunning(false)
        var retry = true
        while (retry) { try { thread?.join(); retry = false } catch (e: InterruptedException) {} }
    }

    companion object {
        const val GSTATE_NONE = 0
        const val GSTATE_HOURSWIN = 1
        const val GSTATE_MINUTESWIN = 2
        const val GSTATE_PLAY = 3
        const val GSTATE_STOPPED = 4
        const val LW = 15
        const val LH = 26
        const val LH2 = LH / 2
        const val LINE_WIDTH = 7
        const val PANEL_LINE_WIDTH = 9
        const val MIN_RANGLE = (3 * (Math.PI / 4)).toFloat()
        const val MAX_RANGLE = (5 * (Math.PI / 4)).toFloat()
    }
}
