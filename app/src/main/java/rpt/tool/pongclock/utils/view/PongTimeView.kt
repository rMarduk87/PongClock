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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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

    inner class PongThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        private var ball: Ball? = null
        private var leftPanel: Panel? = null
        private var rightPanel: Panel? = null

        private var number1X = 0
        private var number2X = 0
        private var number3X = 0
        private var number4X = 0

        private var currentHours: Int = 0
        private var currentMinutes: Int = 0
        private var waitCount = 0

        private var playFieldY1 = 0
        private var playFieldY2 = 0
        private var playFieldX1 = 0
        private var playFieldX2 = 0

        private var running = false
        private var lastTimeMillis: Long = 0
        private var nextTimeUpdate: Long = 0
        private var gMode = 0

        private var canvasHeight = 0
        private var canvasHeight2 = 0
        private var canvasWidth = 0
        private var canvasWidth2 = 0
        private var numberScale = 5f
        private var panelLength = 20f
        private var panelXPos = 25f
        private var ballSpeed = 300f
        private var panelSpeed = 250f

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

        private val dashedLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val panelPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gridPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var bgColor: Int = Color.BLACK
        private var mainColor: Int = Color.WHITE

        init {
            updateColors()
            dashedLinePaint.style = Paint.Style.STROKE
            dashedLinePaint.strokeWidth = LINE_WIDTH.toFloat()
            dashedLinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0.0f)
            gridPaint.strokeWidth = 1f
            
            val now = Date()
            currentHours = now.hours
            currentMinutes = now.minutes
        }

        private fun updateColors() {
            val calendar = Calendar.getInstance()
            val holiday = AppUtils.getHoliday(calendar)
            val isHoliday = holiday != AppUtils.Companion.Holiday.None
            
            mainColor = getColorInternal()
            val isClassic = SharedPreferencesManager.classic == 1 && !isHoliday
            
            val blackColor = context!!.getColor(R.color.black)
            bgColor = if (isClassic) blackColor else context!!.getColor(R.color.modern_background)
            if (isHoliday) {
                bgColor = blackColor
            }

            linePaint.color = mainColor
            linePaint.style = Paint.Style.STROKE
            linePaint.strokeWidth = LINE_WIDTH.toFloat()
            linePaint.strokeCap = if (isClassic) Paint.Cap.SQUARE else Paint.Cap.ROUND
            
            panelPaint.color = mainColor
            panelPaint.style = if (isClassic) Paint.Style.STROKE else Paint.Style.FILL_AND_STROKE
            panelPaint.strokeWidth = PANEL_LINE_WIDTH.toFloat()
            panelPaint.strokeCap = if (isClassic) Paint.Cap.SQUARE else Paint.Cap.ROUND
            
            if (holiday == AppUtils.Companion.Holiday.Christmas) {
                linePaint.color = context!!.getColor(R.color.christmas_red)
                panelPaint.color = context!!.getColor(R.color.christmas_green)
            } else if (holiday == AppUtils.Companion.Holiday.Halloween) {
                linePaint.color = context!!.getColor(R.color.halloween_orange)
                panelPaint.color = context!!.getColor(R.color.halloween_orange)
            }

            if (!isClassic || isHoliday) {
                linePaint.setShadowLayer(25f, 0f, 0f, linePaint.color)
                panelPaint.setShadowLayer(25f, 0f, 0f, panelPaint.color)
            } else {
                linePaint.clearShadowLayer()
                panelPaint.clearShadowLayer()
            }

            dashedLinePaint.color = mainColor
            dashedLinePaint.alpha = if (isClassic) 255 else 100
            gridPaint.color = mainColor
            gridPaint.alpha = if (isClassic) 0 else 30
        }

        fun setSurfaceSize(width: Int, height: Int) {
            synchronized(surfaceHolder) {
                canvasWidth = width
                canvasHeight = height
                canvasWidth2 = width / 2
                canvasHeight2 = height / 2
                playFieldY1 = (height * 0.05f).toInt().coerceAtMost(20)
                playFieldY2 = height - playFieldY1
                playFieldX1 = (width * 0.01f).toInt().coerceAtMost(10)
                playFieldX2 = width - playFieldX1
                panelXPos = (width * 0.05f).coerceAtMost(50f)
                panelLength = (height * 0.1f).coerceAtMost(100f)
                ballSpeed = width * 0.3f
                panelSpeed = height * 0.25f

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
        }

        private fun doDraw(canvas: Canvas?) {
            if (canvas == null) return
            val holiday = AppUtils.getHoliday(Calendar.getInstance())
            updateColors()

            canvas.drawColor(bgColor)
            if (holiday == AppUtils.Companion.Holiday.Christmas) {
                canvas.drawColor(context!!.getColor(R.color.festive_overlay), PorterDuff.Mode.SRC_OVER)
            }

            val step = 100
            for (x in 0..canvasWidth step step) canvas.drawLine(x.toFloat(), 0f, x.toFloat(), canvasHeight.toFloat(), gridPaint)
            for (y in 0..canvasHeight step step) canvas.drawLine(0f, y.toFloat(), canvasWidth.toFloat(), y.toFloat(), gridPaint)
            
            canvas.drawLine(0f, 10f, canvasWidth.toFloat(), 10f, linePaint)
            canvas.drawLine(0f, (canvasHeight - 10).toFloat(), canvasWidth.toFloat(), (canvasHeight - 10).toFloat(), linePaint)
            canvas.drawLine(canvasWidth2.toFloat(), 0f, canvasWidth2.toFloat(), canvasHeight.toFloat(), dashedLinePaint)
            
            val lp = leftPanel!!
            val rp = rightPanel!!
            
            if (holiday == AppUtils.Companion.Holiday.Halloween) {
                val hP = Paint(panelPaint).apply { 
                    style = Paint.Style.FILL
                    color = context!!.getColor(R.color.halloween_orange)
                    setShadowLayer(40f, 0f, 0f, color)
                }
                canvas.drawRect(lp.x - 20f, lp.y - panelLength, lp.x + 20f, lp.y + panelLength, hP)
                canvas.drawRect(rp.x - 20f, rp.y - panelLength, rp.x + 20f, rp.y + panelLength, hP)
            } else if (SharedPreferencesManager.classic == 1 && holiday == AppUtils.Companion.Holiday.None) {
                canvas.drawLine(lp.x.toFloat(), lp.y - panelLength, lp.x.toFloat(), lp.y + panelLength, panelPaint)
                canvas.drawLine(rp.x.toFloat(), rp.y - panelLength, rp.x.toFloat(), rp.y + panelLength, panelPaint)
            } else {
                canvas.drawRoundRect(RectF(lp.x - 10f, lp.y - panelLength * 1.5f, lp.x + 10f, lp.y + panelLength * 1.5f), 10f, 10f, panelPaint)
                canvas.drawRoundRect(RectF(rp.x - 10f, rp.y - panelLength * 1.5f, rp.x + 10f, rp.y + panelLength * 1.5f), 10f, 10f, panelPaint)
            }
            
            val b = ball!!
            when (holiday) {
                AppUtils.Companion.Holiday.Halloween -> {
                    val p = Paint(panelPaint).apply { color = context!!.getColor(R.color.halloween_orange); style = Paint.Style.FILL }
                    canvas.drawCircle(b.x, b.y, 18f, p)
                }
                AppUtils.Companion.Holiday.Christmas -> {
                    val p = Paint(panelPaint).apply { color = context!!.getColor(R.color.christmas_red); style = Paint.Style.FILL }
                    canvas.drawCircle(b.x, b.y, 14f, p)
                }
                AppUtils.Companion.Holiday.NewYear -> {
                    val p = Paint(panelPaint).apply { color = context!!.getColor(R.color.newyear_gold); style = Paint.Style.FILL }
                    canvas.drawCircle(b.x, b.y, 18f, p)
                }
                else -> {
                    if (SharedPreferencesManager.classic == 1) canvas.drawRect(b.x - 5, b.y - 5, b.x + 5, b.y + 5, panelPaint)
                    else canvas.drawCircle(b.x, b.y, 10f, panelPaint)
                }
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

        private fun updatePhysics(deltaMillis: Long) {
            updateTime()
            if (gMode == GSTATE_STOPPED || gMode == GSTATE_NONE) return
            val distance = ballSpeed / (1000 / deltaMillis)
            val dX = distance * ball!!.cosDir
            if (gMode != GSTATE_HOURSWIN) movePanel(rightPanel!!, if (dX > 0 && ball!!.x > canvasWidth2) ball!!.y.toInt() else canvasHeight2, deltaMillis)
            if (gMode != GSTATE_MINUTESWIN) movePanel(leftPanel!!, if (dX < 0 && ball!!.x < canvasWidth2) ball!!.y.toInt() else canvasHeight2, deltaMillis)
            ball!!.y += distance * ball!!.sinDir
            if (ball!!.y < playFieldY1) { ball!!.y = playFieldY1 + (playFieldY1 - ball!!.y); ball!!.direction = -ball!!.direction; ball!!.computeDir() }
            else if (ball!!.y > playFieldY2) { ball!!.y = playFieldY2 - (ball!!.y - playFieldY2); ball!!.direction = -ball!!.direction; ball!!.computeDir() }
            ball!!.x += dX
            val rp = rightPanel!!
            val lp = leftPanel!!
            if (ball!!.x > rp.x && ball!!.y > rp.y - panelLength && ball!!.y < rp.y + panelLength) {
                ball!!.x = rp.x + (rp.x - ball!!.x)
                ball!!.direction = (-ball!!.direction + Math.PI + Math.random() * 0.6 - 0.3).toFloat()
                ball!!.direction = ball!!.direction.coerceIn(MIN_RANGLE, MAX_RANGLE)
                ball!!.computeDir()
            } else if (ball!!.x < lp.x && ball!!.y > lp.y - panelLength && ball!!.y < lp.y + panelLength) {
                ball!!.x = lp.x + (lp.x - ball!!.x)
                ball!!.direction = -(ball!!.direction - Math.PI.toFloat() + Math.random() * 0.6 - 0.3).toFloat()
                ball!!.computeDir()
            }
            if (ball!!.x < playFieldX1 || ball!!.x > playFieldX2) {
                newGame(gMode != GSTATE_MINUTESWIN)
                if (gMode == GSTATE_HOURSWIN || gMode == GSTATE_MINUTESWIN) gMode = GSTATE_STOPPED
            }
        }

        private fun movePanel(p: Panel, target: Int, deltaMillis: Long) {
            val dPanel = target - p.y
            if (abs(dPanel) > 6) {
                val dist = (panelSpeed / (1000 / deltaMillis)).toInt()
                p.y += if (dPanel > 0) dist else -dist
            }
        }

        private fun updateTime() {
            val now = System.currentTimeMillis()
            if (now > nextTimeUpdate) {
                nextTimeUpdate += 1000
                val date = Date(now)
                when (gMode) {
                    GSTATE_PLAY -> if (currentHours != date.hours) gMode = GSTATE_HOURSWIN else if (currentMinutes != date.minutes) gMode = GSTATE_MINUTESWIN else { currentHours = date.hours; currentMinutes = date.minutes }
                    GSTATE_STOPPED -> { waitCount++; if (waitCount == 2) { gMode = GSTATE_PLAY; waitCount = 0 }; currentHours = date.hours; currentMinutes = date.minutes }
                    GSTATE_NONE -> { waitCount++; if (waitCount >= 3) { gMode = GSTATE_PLAY; waitCount = 0 } }
                    else -> gMode = GSTATE_PLAY
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

    private fun getColorInternal(): Int {
        val calendar = Calendar.getInstance()
        val holiday = AppUtils.getHoliday(calendar)
        if (holiday != AppUtils.Companion.Holiday.None) {
            return when (holiday) {
                AppUtils.Companion.Holiday.Halloween -> context.getColor(R.color.halloween_orange)
                AppUtils.Companion.Holiday.Christmas -> context.getColor(R.color.christmas_red)
                AppUtils.Companion.Holiday.NewYear -> context.getColor(R.color.newyear_gold)
                else -> context.getColor(R.color.white)
            }
        }
        var color = context.getColor(R.color.modern_cyan)
        if (SharedPreferencesManager.classic == 1) color = context.getColor(R.color.white)
        else if (SharedPreferencesManager.mode == 1) color = context.getColor(R.color.matrix)
        else if (SharedPreferencesManager.season == 1) color = context.getColor(AppUtils.getSeason(calendar[Calendar.DAY_OF_YEAR]).toColor())
        return color
    }

    init {
        holder.addCallback(this)
        isFocusable = false
        isClickable = false
    }

    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, height: Int) { thread?.setSurfaceSize(w, height) }
    override fun surfaceCreated(h: SurfaceHolder) { thread = PongThread(h); thread?.start() }
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
