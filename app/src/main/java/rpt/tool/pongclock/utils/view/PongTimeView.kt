@file:Suppress("DEPRECATION")

package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import androidx.core.graphics.withSave
import rpt.tool.pongclock.R
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.extensions.toColor
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class PongTimeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private var thread: PongThread? = null

    private fun colorCompat(resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    internal inner class Panel(var x: Int, var y: Int)

    internal inner class Ball(var x: Float, var y: Float) {
        var direction: Float = 0f
        var cosDir: Float = 0f
        var sinDir: Float = 0f

        var speedMultiplier: Float = 1f
        var alpha: Int = 255
        var anomalyTimer: Int = 0
        var lastHitBy: Int = 0

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
        @Volatile private var pendingWidth = -1
        @Volatile private var pendingHeight = -1

        private var canvasHeight = 0; private var canvasHeight2 = 0
        private var canvasWidth = 0; private var canvasWidth2 = 0
        private var numberScale = 5f
        private var goalHeightPx = 0f
        private var panelLength = 20f
        private var panelXPos = 25f
        private var ballSpeed = 300f
        private var panelSpeed = 250f

        private var gMode = GSTATE_PLAY
        private var nextTimeUpdate: Long = 0
        private var lastTimeMillis: Long = 0

        private var isClassic = false
        private var isMatrix = false
        private var isBreakout = false
        private var isSeason = false
        private var isFuturistic = false
        private var isGlitch = false
        private var isNoGravity = false
        private var isBlueprint = false
        private var glitchTimer = 0

        private var holiday: Any? = null
        private var currentSeason: Any? = null

        internal inner class Star(var x: Float, var y: Float, var speed: Float, var size: Float, var alpha: Int)
        internal inner class Planet(var x: Float, var y: Float, var speed: Float, var size: Float, var color: Int, var hasRing: Boolean)

        private val stars = mutableListOf<Star>()
        private val planets = mutableListOf<Planet>()
        private val particles = mutableListOf<FxParticle>()

        private val trailX = FloatArray(8)
        private val trailY = FloatArray(8)
        private var trailSize = 0

        private var crtBarY = -100f

        // --- OGGETTI GRAFICI OTTIMIZZATI ---
        private val dashedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val pinkLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private val scanlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCompat(R.color.scanline)
            strokeWidth = 4f
        }
        private val ghostPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 100 }

        // Elementi Blueprint
        private val blueprintGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCompat(R.color.blueprint_grid)
            strokeWidth = 1f
        }
        private val blueprintGridMajorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCompat(R.color.blueprint_grid_major)
            strokeWidth = 2f
        }
        private val blueprintVectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCompat(R.color.blueprint_vector)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        private val blueprintPredictionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCompat(R.color.blueprint_prediction)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)
        }
        private val blueprintTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCompat(R.color.blueprint_text)
            typeface = Typeface.MONOSPACE
            textSize = 24f
        }
        private val blueprintPath = Path()

        // Elementi Glitch Pre-Allocati
        private val glitchCyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCompat(R.color.cyan)
            style = Paint.Style.STROKE
            strokeWidth = LINE_WIDTH.toFloat()
            strokeCap = Paint.Cap.SQUARE
            alpha = 150
        }
        private val glitchMagentaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCompat(R.color.magenta)
            style = Paint.Style.STROKE
            strokeWidth = LINE_WIDTH.toFloat()
            strokeCap = Paint.Cap.SQUARE
            alpha = 150
        }
        private val glitchBlockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // Elementi Buco Nero Pre-Allocati
        private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorCompat(R.color.white) }
        private val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 3f }
        private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val diskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 14f; alpha = 180 }
        private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = colorCompat(R.color.black) }
        private val photonRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f; color = colorCompat(R.color.photon_ring) }

        private val planetRingRect = RectF()
        private val diskRect = RectF()

        // Colori Cacheati
        private var bgColor = colorCompat(R.color.black)
        private var mainColor = colorCompat(R.color.white)
        private var cWhite = colorCompat(R.color.white)
        private var cMatrix = colorCompat(R.color.green)
        private var cNeonPink = colorCompat(R.color.magenta)
        private var cModernCyan = colorCompat(R.color.cyan)
        private var cBrickBlue = colorCompat(R.color.blue)
        private var cBrickRed = colorCompat(R.color.red)
        private var cAcidPurple = colorCompat(R.color.magenta)
        private var cBlackHoleCyan = colorCompat(R.color.cyan)
        private var cNewYearGold = colorCompat(R.color.yellow)
        private val cBrickColors = IntArray(5)

        private val matrixChars = arrayOf("0", "1")
        private val halloweenEmojis = arrayOf("🦇", "👻")
        private val newYearColors = IntArray(3)
        private val worldCupColors = IntArray(2)

        private val calendar = Calendar.getInstance()
        private val date = Date()

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
            dashedLinePaint.style = Paint.Style.STROKE
            dashedLinePaint.strokeWidth = LINE_WIDTH.toFloat()
            dashedLinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0.0f)
            gridPaint.strokeWidth = 1f

            numberPaint.style = Paint.Style.STROKE
            numberPaint.strokeWidth = LINE_WIDTH.toFloat()
            numberPaint.strokeCap = Paint.Cap.SQUARE

            pinkLinePaint.style = Paint.Style.STROKE
            pinkLinePaint.strokeWidth = LINE_WIDTH.toFloat()
            pinkLinePaint.strokeCap = Paint.Cap.SQUARE

            val now = System.currentTimeMillis()
            date.time = now
            currentHours = date.hours
            currentMinutes = date.minutes

            updateThemeAndColors()
        }

        private fun updateThemeAndColors() {
            calendar.timeInMillis = System.currentTimeMillis()

            holiday = AppUtils.getHoliday(calendar)
            currentSeason = AppUtils.getSeason(calendar[Calendar.DAY_OF_YEAR])

            isMatrix = SharedPreferencesManager.mode == 1
            isBreakout = SharedPreferencesManager.breakOut == 1
            isSeason = SharedPreferencesManager.season == 1
            isFuturistic = SharedPreferencesManager.futuristic == 1
            isGlitch = SharedPreferencesManager.glitch == 1
            isNoGravity = SharedPreferencesManager.noGravity == 1
            isBlueprint = SharedPreferencesManager.bluePrint == 1

            isClassic = (SharedPreferencesManager.classic == 1 || (!isMatrix && !isBreakout && !isSeason && !isFuturistic && !isGlitch && !isNoGravity && !isBlueprint))

            cWhite = colorCompat(R.color.white)
            cMatrix = colorCompat(R.color.matrix)
            cNeonPink = colorCompat(R.color.neon_pink)
            cModernCyan = colorCompat(R.color.modern_cyan)
            cBrickBlue = colorCompat(R.color.brick_blue)
            cBrickRed = colorCompat(R.color.brick_red)
            cAcidPurple = colorCompat(R.color.acid_purple)
            cBlackHoleCyan = colorCompat(R.color.black_hole_cyan)
            cNewYearGold = colorCompat(R.color.newyear_gold)

            cBrickColors[0] = colorCompat(R.color.red)
            cBrickColors[1] = colorCompat(R.color.orange)
            cBrickColors[2] = colorCompat(R.color.yellow)
            cBrickColors[3] = colorCompat(R.color.green)
            cBrickColors[4] = colorCompat(R.color.cyan)

            newYearColors[0] = colorCompat(R.color.yellow)
            newYearColors[1] = cWhite
            newYearColors[2] = cNewYearGold

            worldCupColors[0] = colorCompat(R.color.green)
            worldCupColors[1] = colorCompat(R.color.worldcup_pitch)

            ghostPaint.color = colorCompat(R.color.shocking_pink)
            mainColor = getColorInternal(calendar)

            bgColor = if (isClassic) colorCompat(R.color.black) else colorCompat(R.color.modern_background)

            linePaint.color = mainColor
            linePaint.style = Paint.Style.STROKE
            linePaint.strokeWidth = LINE_WIDTH.toFloat()
            linePaint.strokeCap = Paint.Cap.SQUARE

            panelPaint.color = mainColor
            panelPaint.style = if (isClassic) Paint.Style.STROKE else Paint.Style.FILL_AND_STROKE
            panelPaint.strokeWidth = PANEL_LINE_WIDTH.toFloat()
            panelPaint.strokeCap = Paint.Cap.SQUARE

            dashedLinePaint.color = mainColor
            dashedLinePaint.alpha = if (isClassic) 255 else 100
            gridPaint.color = mainColor
            gridPaint.alpha = if (isClassic) 0 else 20
            numberPaint.color = mainColor
            numberPaint.strokeWidth = LINE_WIDTH.toFloat()

            if (isFuturistic) {
                linePaint.color = cModernCyan
                panelPaint.color = cModernCyan
                dashedLinePaint.color = cModernCyan
                numberPaint.color = cNeonPink
                pinkLinePaint.color = cNeonPink
            }

            if (isGlitch) {
                mainColor = colorCompat(R.color.cyberpunk_green)
                bgColor = colorCompat(R.color.glitch_bg)
                linePaint.color = cAcidPurple
                panelPaint.color = colorCompat(R.color.shocking_pink)
                dashedLinePaint.color = colorCompat(R.color.cyberpunk_green)
                numberPaint.color = colorCompat(R.color.cyberpunk_green)
                pinkLinePaint.color = colorCompat(R.color.shocking_pink)
            }

            if (isBlueprint) {
                bgColor = colorCompat(R.color.blueprint_background)
                mainColor = colorCompat(R.color.white)
                linePaint.color = mainColor
                linePaint.strokeWidth = 2f
                panelPaint.color = mainColor
                panelPaint.strokeWidth = 2f
                panelPaint.style = Paint.Style.STROKE
                dashedLinePaint.color = mainColor
                dashedLinePaint.strokeWidth = 2f
                dashedLinePaint.alpha = 200
                numberPaint.color = mainColor
                numberPaint.strokeWidth = 2f
            }
        }

        fun setSurfaceSize(width: Int, height: Int) {
            pendingWidth = width
            pendingHeight = height
        }

        private fun applySurfaceSize() {
            val w = pendingWidth
            val h = pendingHeight
            if (w <= 0 || h <= 0) return

            pendingWidth = -1
            pendingHeight = -1

            canvasWidth = w; canvasHeight = h
            canvasWidth2 = w / 2; canvasHeight2 = h / 2
            playFieldY1 = (h * 0.05f).toInt().coerceAtMost(30)
            playFieldY2 = h - playFieldY1
            playFieldX1 = (w * 0.01f).toInt().coerceAtMost(10)
            playFieldX2 = w - playFieldX1
            panelXPos = (w * 0.05f).coerceAtMost(50f)
            panelLength = (h * 0.1f).coerceAtMost(100f)

            // Adatta la dimensione del testo HUD in base all'altezza dello schermo
            blueprintTextPaint.textSize = (h * 0.02f).coerceIn(16f, 32f)

            goalHeightPx = context.resources.displayMetrics.density * 180f
            ballSpeed = w * 0.4f
            panelSpeed = h * 0.35f

            leftPanel = Panel(panelXPos.toInt(), canvasHeight2)
            rightPanel = Panel((w - panelXPos).toInt(), canvasHeight2)
            ball = Ball(canvasWidth2.toFloat(), canvasHeight2.toFloat())

            stars.clear()
            for (i in 0 until 80) {
                stars.add(Star(Random.nextFloat() * w, Random.nextFloat() * h,
                    Random.nextFloat() * 2f + 0.5f, Random.nextFloat() * 3f + 1f, Random.nextInt(150) + 100))
            }

            planets.clear()
            val pColors = intArrayOf(
                colorCompat(R.color.planet_orange),
                colorCompat(R.color.planet_blue),
                colorCompat(R.color.planet_purple),
                colorCompat(R.color.planet_cyan)
            )
            for(i in 0 until 4) {
                planets.add(Planet(
                    Random.nextFloat() * w,
                    Random.nextFloat() * h,
                    Random.nextFloat() * 1.5f + 0.5f,
                    Random.nextFloat() * 15f + 10f,
                    pColors[Random.nextInt(pColors.size)],
                    Random.nextBoolean()
                ))
            }

            val cx = canvasWidth2.toFloat()
            val cy = canvasHeight2.toFloat()
            val bhRadius = 45f
            haloPaint.shader = RadialGradient(
                cx, cy, bhRadius * 2.8f,
                intArrayOf(colorCompat(R.color.transparent), cAcidPurple, cBlackHoleCyan, colorCompat(R.color.transparent)),
                floatArrayOf(0f, 0.4f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
            diskRect.set(cx - bhRadius * 2.6f, cy - bhRadius * 0.8f, cx + bhRadius * 2.6f, cy + bhRadius * 0.8f)

            numberScale = (w / 200f).coerceIn(3f, 8f)
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
        }

        fun setRunning(isRunning: Boolean) { running = isRunning }

        private fun newGame(left: Boolean) {
            ball?.y = (Math.random() * (playFieldY2 - playFieldY1) + playFieldY1).toFloat()
            ball?.x = (if (left) canvasWidth2 - 40 else canvasWidth2 + 40).toFloat()
            val d = (Math.random() * 0.8 - 0.4).toFloat()
            ball?.direction = if (left) 0.0f + d else Math.PI.toFloat() + d
            ball?.computeDir()

            ball?.speedMultiplier = 1f
            ball?.alpha = 255
            ball?.anomalyTimer = 0
            ball?.lastHitBy = 0

            trailSize = 0
        }

        private fun updatePhysics(deltaMillis: Long) {
            updateTime()
            updateParticles()

            if (isGlitch) {
                crtBarY += (deltaMillis * 0.2f)
                if (crtBarY > canvasHeight + 100f) crtBarY = -150f
            }

            if (gMode == GSTATE_STOPPED || gMode == GSTATE_NONE) return
            val b = ball ?: return
            val lp = leftPanel ?: return
            val rp = rightPanel ?: return

            if (!isClassic && !isBreakout) {
                for (i in 7 downTo 1) {
                    trailX[i] = trailX[i - 1]
                    trailY[i] = trailY[i - 1]
                }
                trailX[0] = b.x
                trailY[0] = b.y
                if (trailSize < 8) trailSize++
            }

            if (isNoGravity) {
                // (Codice NoGravity non modificato...)
                stars.forEach { s ->
                    s.x -= s.speed
                    if (s.x < 0) {
                        s.x = canvasWidth.toFloat()
                        s.y = Random.nextFloat() * canvasHeight
                    }
                }
                planets.forEach { p ->
                    p.x -= p.speed
                    if (p.x + p.size * 2 < 0) {
                        p.x = canvasWidth.toFloat() + p.size * 2
                        p.y = Random.nextFloat() * canvasHeight
                    }
                }

                val cx = canvasWidth2.toFloat()
                val cy = canvasHeight2.toFloat()
                val dx = cx - b.x
                val dy = cy - b.y
                val distCenterSq = dx * dx + dy * dy
                val anomalyZoneRadius = 150f
                val anomalyZoneSq = anomalyZoneRadius * anomalyZoneRadius

                if (gMode == GSTATE_PLAY) {
                    if (b.anomalyTimer > 0) {
                        b.anomalyTimer -= deltaMillis.toInt()
                        if (b.anomalyTimer <= 0) {
                            b.speedMultiplier = 1f
                            b.alpha = 255
                            b.anomalyTimer = 0
                        } else if (b.alpha < 255) {
                            b.alpha = if (Random.nextFloat() > 0.6f) 80 else 0
                        }
                    } else if (distCenterSq < anomalyZoneSq && Random.nextFloat() < 0.05f) {
                        val effect = Random.nextInt(3)
                        b.anomalyTimer = Random.nextInt(1000) + 800

                        when (effect) {
                            0 -> b.speedMultiplier = if (Random.nextBoolean()) 0.3f else 1.8f
                            1 -> b.alpha = 0
                            2 -> {
                                if (b.lastHitBy == 1) {
                                    b.direction = atan2(lp.y - b.y, lp.x - b.x).toFloat()
                                    b.computeDir()
                                    b.speedMultiplier = 2.0f
                                    b.anomalyTimer = 600
                                    for (i in 0..6) particles.add(FxParticle(b.x, b.y, (Random.nextFloat() - 0.5f) * 8f, (Random.nextFloat() - 0.5f) * 8f, 1f, 1f, 3, "", cModernCyan))
                                } else if (b.lastHitBy == 2) {
                                    b.direction = atan2(rp.y - b.y, rp.x - b.x).toFloat()
                                    b.computeDir()
                                    b.speedMultiplier = 2.0f
                                    b.anomalyTimer = 600
                                    for (i in 0..6) particles.add(FxParticle(b.x, b.y, (Random.nextFloat() - 0.5f) * 8f, (Random.nextFloat() - 0.5f) * 8f, 1f, 1f, 3, "", cModernCyan))
                                } else {
                                    b.anomalyTimer = 0
                                }
                            }
                        }
                    }
                }

                if (distCenterSq > 100f) {
                    val targetAngle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
                    var angleDiff = (targetAngle - b.direction) % (2 * Math.PI.toFloat())
                    if (angleDiff > Math.PI.toFloat()) angleDiff -= 2 * Math.PI.toFloat()
                    if (angleDiff < -Math.PI.toFloat()) angleDiff += 2 * Math.PI.toFloat()

                    val gravityStrength = if (gMode == GSTATE_HOURSWIN || gMode == GSTATE_MINUTESWIN) 0.000f else 0.008f
                    if (gravityStrength > 0f) b.direction += angleDiff * gravityStrength
                    b.computeDir()
                }
            }

            val distance = (ballSpeed * b.speedMultiplier) / (1000f / deltaMillis)
            val dX = distance * b.cosDir

            if (gMode != GSTATE_HOURSWIN) movePanel(rp, if (dX > 0 && b.x > canvasWidth2) b.y.toInt() else canvasHeight2, deltaMillis)
            if (gMode != GSTATE_MINUTESWIN) movePanel(lp, if (dX < 0 && b.x < canvasWidth2) b.y.toInt() else canvasHeight2, deltaMillis)

            b.y += distance * b.sinDir

            if (b.y < playFieldY1) {
                b.y = playFieldY1 + (playFieldY1 - b.y); b.direction = -b.direction; b.computeDir()
            } else if (b.y > playFieldY2) {
                b.y = playFieldY2 - (b.y - playFieldY2); b.direction = -b.direction; b.computeDir()
            }

            b.x += dX

            if (dX > 0 && b.x > rp.x && b.y > rp.y - panelLength && b.y < rp.y + panelLength) {
                b.x = rp.x + (rp.x - b.x)
                b.direction = (-b.direction + Math.PI + Math.random() * 0.6 - 0.3).toFloat()
                b.direction = b.direction.coerceIn(MIN_RANGLE, MAX_RANGLE)
                b.computeDir()
                b.lastHitBy = 2
                onPaddleHit(b.x, b.y)
            } else if (dX < 0 && b.x < lp.x && b.y > lp.y - panelLength && b.y < lp.y + panelLength) {
                b.x = lp.x + (lp.x - b.x)
                b.direction = (-b.direction + Math.PI + Math.random() * 0.6 - 0.3).toFloat()
                b.computeDir()
                b.lastHitBy = 1
                onPaddleHit(b.x, b.y)
            }

            if (b.x < playFieldX1 || b.x > playFieldX2) {
                var isGoal = true
                if (holiday.toString().contains("WorldCup2026")) {
                    isGoal = b.y > (canvasHeight2 - goalHeightPx / 2) && b.y < (canvasHeight2 + goalHeightPx / 2)
                }

                if (isGoal) {
                    newGame(gMode != GSTATE_MINUTESWIN)
                    if (gMode == GSTATE_HOURSWIN || gMode == GSTATE_MINUTESWIN) gMode = GSTATE_STOPPED
                } else {
                    if (b.x < playFieldX1) {
                        b.x = playFieldX1 + (playFieldX1 - b.x)
                        b.direction = (Math.PI.toFloat() - b.direction)
                    } else {
                        b.x = playFieldX2 - (b.x - playFieldX2)
                        b.direction = (Math.PI.toFloat() - b.direction)
                    }
                    b.computeDir()
                }
            }
        }

        private fun onPaddleHit(x: Float, y: Float) {
            if (isGlitch) {
                glitchTimer = Random.nextInt(8, 16)
                for (i in 0..8) {
                    particles.add(FxParticle(x, y, (Random.nextFloat() - 0.5f) * 16f, (Random.nextFloat() - 0.5f) * 6f, 1f, 1f, 5, "", if(Random.nextBoolean()) colorCompat(R.color.cyan) else colorCompat(R.color.magenta)))
                }
            }
            if (holiday.toString().contains("NewYear")) {
                val sparkColor = newYearColors[Random.nextInt(newYearColors.size)]
                for (i in 0..15) {
                    particles.add(FxParticle(x, y, (Random.nextFloat() - 0.5f) * 10f, (Random.nextFloat() - 0.5f) * 10f, 1f, 1f, 3, "", sparkColor))
                }
            } else if (holiday.toString().contains("WorldCup2026")) {
                val grassColor = worldCupColors[Random.nextInt(worldCupColors.size)]
                for (i in 0..10) {
                    particles.add(FxParticle(x, y, (Random.nextFloat() - 0.5f) * 8f, (Random.nextFloat() - 0.5f) * 8f, 1f, 1f, 3, "", grassColor))
                }
            }
        }

        private fun updateParticles() {
            if (holiday.toString().contains("Halloween") && Random.nextFloat() < 0.02f) {
                particles.add(FxParticle(Random.nextFloat() * canvasWidth, canvasHeight + 50f, 0f, -Random.nextFloat() * 3 - 1f, 1f, 1f, 4, halloweenEmojis[Random.nextInt(halloweenEmojis.size)], cWhite))
            } else if (isSeason && currentSeason.toString().contains("Winter") && Random.nextFloat() < 0.1f) {
                particles.add(FxParticle(Random.nextFloat() * canvasWidth, -10f, Random.nextFloat() * 2 - 1f, Random.nextFloat() * 4 + 2f, 1f, 1f, 1, "", cWhite))
            } else if (isMatrix && Random.nextFloat() < 0.05f) {
                particles.add(FxParticle(Random.nextFloat() * canvasWidth, -10f, 0f, Random.nextFloat() * 10 + 5f, 1f, 1f, 2, matrixChars[Random.nextInt(matrixChars.size)], cMatrix))
            }

            val iter = particles.iterator()
            while (iter.hasNext()) {
                val p = iter.next()
                p.x += p.dx; p.y += p.dy
                if (p.type == 3) { p.dy += 0.2f; p.life -= 0.03f }
                if (p.type == 5) { p.life -= 0.05f }
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
                    GSTATE_STOPPED -> { waitCount++; if (waitCount == 2) { gMode = GSTATE_PLAY; waitCount = 0 }
                        currentHours = date.hours; currentMinutes = date.minutes }
                    GSTATE_NONE -> { waitCount++; if (waitCount >= 3) { gMode = GSTATE_PLAY; waitCount = 0 } }
                    else -> gMode = GSTATE_PLAY
                }
            }
        }

        private fun doDraw(canvas: Canvas) {
            val lp = leftPanel ?: return
            val rp = rightPanel ?: return
            val b = ball ?: return

            var canvasSaved = false
            val isGlitchSpike = isGlitch && (glitchTimer > 0 || Math.random() < 0.05)

            if (isGlitch) {
                if (glitchTimer > 0) glitchTimer--
                val shakeX = if (isGlitchSpike) (Math.random() * 16 - 8).toFloat() else 0f
                val shakeY = if (isGlitchSpike) (Math.random() * 10 - 5).toFloat() else 0f
                canvas.save()
                canvas.translate(shakeX, shakeY)
                canvasSaved = true
            }

            try {
                if (isNoGravity) {
                    // (Codice draw NoGravity non modificato...)
                    canvas.drawColor(colorCompat(R.color.black))
                    stars.forEach { s ->
                        starPaint.alpha = s.alpha
                        canvas.drawCircle(s.x, s.y, s.size, starPaint)
                    }

                    planets.forEach { p ->
                        planetPaint.color = p.color
                        canvas.drawCircle(p.x, p.y, p.size, planetPaint)
                        planetPaint.color = colorCompat(R.color.planet_shadow)
                        planetRingRect.set(p.x - p.size, p.y - p.size, p.x + p.size, p.y + p.size)
                        canvas.drawArc(planetRingRect, 90f, 180f, false, planetPaint)

                        if (p.hasRing) {
                            ringPaint.color = colorCompat(R.color.planet_ring)
                            planetRingRect.set(p.x - p.size * 1.8f, p.y - p.size * 0.4f, p.x + p.size * 1.8f, p.y + p.size * 0.4f)
                            canvas.drawOval(planetRingRect, ringPaint)
                        }
                    }
                    val cx = canvasWidth2.toFloat()
                    val cy = canvasHeight2.toFloat()
                    val bhRadius = 45f

                    canvas.drawCircle(cx, cy, bhRadius * 2.8f, haloPaint)
                    diskPaint.strokeWidth = 14f
                    diskPaint.color = cNewYearGold
                    diskPaint.alpha = 180
                    canvas.drawOval(diskRect, diskPaint)
                    diskPaint.strokeWidth = 4f
                    diskPaint.color = colorCompat(R.color.white)
                    canvas.drawOval(diskRect, diskPaint)
                    canvas.drawCircle(cx, cy, bhRadius, corePaint)
                    canvas.drawCircle(cx, cy, bhRadius, photonRingPaint)

                } else {
                    canvas.drawColor(bgColor)
                }

                if (!isClassic && !isBreakout) {
                    val step = if (isBlueprint) 50 else 100
                    for (x in 0..canvasWidth step step) {
                        val activeGridPaint = if (isBlueprint && (x % (step * 4) == 0)) blueprintGridMajorPaint else if (isBlueprint) blueprintGridPaint else gridPaint
                        canvas.drawLine(x.toFloat(), 0f, x.toFloat(), canvasHeight.toFloat(), activeGridPaint)
                    }
                    for (y in 0..canvasHeight step step) {
                        val activeGridPaint = if (isBlueprint && (y % (step * 4) == 0)) blueprintGridMajorPaint else if (isBlueprint) blueprintGridPaint else gridPaint
                        canvas.drawLine(0f, y.toFloat(), canvasWidth.toFloat(), y.toFloat(), activeGridPaint)
                    }
                }

                if (isGlitch) {
                    glitchBlockPaint.color = colorCompat(R.color.glitch_block)
                    canvas.drawRect(0f, crtBarY, canvasWidth.toFloat(), crtBarY + 40f, glitchBlockPaint)
                    if (isGlitchSpike) {
                        val blockCount = Random.nextInt(3, 8)
                        for (i in 0 until blockCount) {
                            glitchBlockPaint.color = if (Random.nextBoolean()) colorCompat(R.color.cyan) else colorCompat(R.color.magenta)
                            glitchBlockPaint.alpha = Random.nextInt(40, 100)
                            val rx = Random.nextFloat() * canvasWidth
                            val ry = Random.nextFloat() * canvasHeight
                            val rw = Random.nextFloat() * 150f + 50f
                            val rh = Random.nextFloat() * 15f + 5f
                            canvas.drawRect(rx, ry, rx + rw, ry + rh, glitchBlockPaint)
                        }
                    }
                }

                particles.forEach { p ->
                    when (p.type) {
                        1 -> { particlePaint.color = cWhite; canvas.drawCircle(p.x, p.y, 4f, particlePaint) }
                        2 -> { particlePaint.color = cMatrix; particlePaint.textSize = 30f; canvas.drawText(p.extra, p.x, p.y, particlePaint) }
                        3 -> { particlePaint.color = p.color; particlePaint.alpha = (p.life * 255).toInt().coerceIn(0, 255); canvas.drawCircle(p.x, p.y, 5f, particlePaint) }
                        4 -> { emojiPaint.textSize = 60f; emojiPaint.alpha = 150; canvas.drawText(p.extra, p.x, p.y, emojiPaint) }
                        5 -> {
                            particlePaint.color = p.color
                            particlePaint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
                            val w = Random.nextFloat() * 15f + 5f
                            canvas.drawRect(p.x - w, p.y - 2f, p.x + w, p.y + 2f, particlePaint)
                        }
                    }
                }
                particlePaint.alpha = 255

                if (isBreakout) {
                    val brickWidth = canvasWidth / 12f
                    for (i in 0..12) {
                        panelPaint.color = cBrickColors[i % cBrickColors.size]
                        canvas.drawRoundRect(i * brickWidth + 2, 5f, (i + 1) * brickWidth - 2, playFieldY1.toFloat(), 4f, 4f, panelPaint)
                        canvas.drawRoundRect(i * brickWidth + 2, playFieldY2.toFloat(), (i + 1) * brickWidth - 2, canvasHeight - 5f, 4f, 4f, panelPaint)
                    }
                    panelPaint.color = if (isFuturistic) cModernCyan else mainColor
                } else {
                    if (isFuturistic) {
                        canvas.drawLine(0f, playFieldY1.toFloat(), canvasWidth.toFloat(), playFieldY1.toFloat(), linePaint)
                        canvas.drawLine(0f, playFieldY2.toFloat(), canvasWidth.toFloat(), playFieldY2.toFloat(), pinkLinePaint)
                    } else {
                        canvas.drawLine(0f, playFieldY1.toFloat(), canvasWidth.toFloat(), playFieldY1.toFloat(), linePaint)
                        canvas.drawLine(0f, playFieldY2.toFloat(), canvasWidth.toFloat(), playFieldY2.toFloat(), linePaint)
                    }
                }

                canvas.drawLine(canvasWidth2.toFloat(), 0f, canvasWidth2.toFloat(), canvasHeight.toFloat(), dashedLinePaint)

                // Renderizzazione tracciati per modalità dinamiche
                if (!isClassic && !isBreakout) {
                    if (isBlueprint && trailSize > 1) {
                        blueprintPath.reset()
                        blueprintPath.moveTo(b.x, b.y)
                        for (index in 0 until trailSize) {
                            blueprintPath.lineTo(trailX[index], trailY[index])
                        }
                        canvas.drawPath(blueprintPath, blueprintVectorPaint)
                    } else if (isFuturistic) {
                        for (index in 0 until trailSize) {
                            panelPaint.color = if (index < 3) cNeonPink else cModernCyan
                            panelPaint.alpha = 255 - (index * 30)
                            canvas.drawRect(trailX[index] - 8, trailY[index] - 8, trailX[index] + 8, trailY[index] + 8, panelPaint)
                        }
                        panelPaint.color = cModernCyan
                    } else if (!isBlueprint) {
                        for (index in 0 until trailSize) {
                            panelPaint.alpha = 255 - (index * 30)
                            canvas.drawCircle(trailX[index], trailY[index], 10f - (index * 0.8f), panelPaint)
                        }
                    }
                    panelPaint.alpha = 255
                }

                if (isBlueprint) {
                    // Cartigli / Technical Overlay negli angoli
                    val padding = 30f
                    canvas.drawText(context.getString(R.string.blueprint_system_mode), padding, padding + blueprintTextPaint.textSize, blueprintTextPaint)
                    canvas.drawText(context.getString(R.string.blueprint_project_scale), padding, canvasHeight - padding, blueprintTextPaint)

                    val rightAlignText = context.getString(R.string.blueprint_engine_status)
                    val textWidth = blueprintTextPaint.measureText(rightAlignText)
                    canvas.drawText(rightAlignText, canvasWidth - textWidth - padding, canvasHeight - padding, blueprintTextPaint)

                    // Assi di traslazione racchette per effetto "tecnico"
                    canvas.drawLine(lp.x.toFloat(), 0f, lp.x.toFloat(), canvasHeight.toFloat(), blueprintGridPaint)
                    canvas.drawLine(rp.x.toFloat(), 0f, rp.x.toFloat(), canvasHeight.toFloat(), blueprintGridPaint)

                    // Telemetria (HUD) ancorata alla racchetta sinistra (Player 1/AI)
                    canvas.drawText(context.getString(R.string.blueprint_y_distance, abs(canvasHeight2 - lp.y)), lp.x + 15f, lp.y.toFloat(), blueprintTextPaint)

                    // CPU Trajectory Raycasting (Predizione 3 Rimbalzi)
                    var pX = b.x
                    var pY = b.y
                    var pDir = b.direction
                    var bounces = 0
                    blueprintPath.reset()
                    blueprintPath.moveTo(pX, pY)

                    while (bounces < 3) {
                        val pCos = cos(pDir.toDouble()).toFloat()
                        val pSin = sin(pDir.toDouble()).toFloat()

                        var distToX = Float.MAX_VALUE
                        var nextX = pX
                        if (pCos > 0.0001f) {
                            distToX = (playFieldX2 - pX) / pCos
                            nextX = playFieldX2.toFloat()
                        } else if (pCos < -0.0001f) {
                            distToX = (playFieldX1 - pX) / pCos
                            nextX = playFieldX1.toFloat()
                        }

                        var distToY = Float.MAX_VALUE
                        var nextY = pY
                        if (pSin > 0.0001f) {
                            distToY = (playFieldY2 - pY) / pSin
                            nextY = playFieldY2.toFloat()
                        } else if (pSin < -0.0001f) {
                            distToY = (playFieldY1 - pY) / pSin
                            nextY = playFieldY1.toFloat()
                        }

                        if (distToX < distToY) {
                            pY += distToX * pSin
                            pX = nextX
                            pDir = (Math.PI.toFloat() - pDir)
                            bounces++
                        } else {
                            pX += distToY * pCos
                            pY = nextY
                            pDir = -pDir
                            bounces++
                        }
                        blueprintPath.lineTo(pX, pY)

                        // Disegna un marker (nodo vettoriale) sul punto di impatto stimato
                        canvas.drawCircle(pX, pY, 4f, blueprintVectorPaint)
                    }
                    canvas.drawPath(blueprintPath, blueprintPredictionPaint)
                }

                if (isClassic || isBlueprint) {
                    canvas.drawLine(lp.x.toFloat(), lp.y - panelLength, lp.x.toFloat(), lp.y + panelLength, panelPaint)
                    canvas.drawLine(rp.x.toFloat(), rp.y - panelLength, rp.x.toFloat(), rp.y + panelLength, panelPaint)
                } else if (isBreakout) {
                    panelPaint.color = cBrickBlue
                    canvas.drawRoundRect(RectF(lp.x - 12f, lp.y - panelLength * 1.5f, lp.x + 12f, lp.y + panelLength * 1.5f), 12f, 12f, panelPaint)
                    panelPaint.color = cBrickRed
                    canvas.drawRoundRect(RectF(rp.x - 12f, rp.y - panelLength * 1.5f, rp.x + 12f, rp.y + panelLength * 1.5f), 12f, 12f, panelPaint)
                    panelPaint.color = if (isFuturistic) cModernCyan else mainColor
                } else if (isFuturistic) {
                    panelPaint.color = cModernCyan
                    canvas.drawRoundRect(RectF(lp.x - 8f, lp.y - panelLength * 1.2f, lp.x + 8f, lp.y + panelLength * 1.2f), 8f, 8f, panelPaint)
                    panelPaint.color = cNeonPink
                    canvas.drawRoundRect(RectF(rp.x - 8f, rp.y - panelLength * 1.2f, rp.x + 8f, rp.y + panelLength * 1.2f), 8f, 8f, panelPaint)
                    panelPaint.color = cModernCyan
                } else {
                    canvas.drawRoundRect(RectF(lp.x - 8f, lp.y - panelLength * 1.2f, lp.x + 8f, lp.y + panelLength * 1.2f), 8f, 8f, panelPaint)
                    canvas.drawRoundRect(RectF(rp.x - 8f, rp.y - panelLength * 1.2f, rp.x + 8f, rp.y + panelLength * 1.2f), 8f, 8f, panelPaint)
                }

                val prevAlpha = panelPaint.alpha
                if (isNoGravity) panelPaint.alpha = b.alpha

                when {
                    holiday.toString().contains("Halloween") -> {
                        emojiPaint.textSize = panelLength * 1.5f
                        emojiPaint.alpha = if (isNoGravity) b.alpha else 255
                        canvas.drawText("🎃", b.x, b.y + (emojiPaint.textSize / 3), emojiPaint)
                    }
                    holiday.toString().contains("NewYear") -> {
                        panelPaint.color = cNewYearGold
                        canvas.drawCircle(b.x, b.y, 18f, panelPaint)
                        panelPaint.color = mainColor
                    }
                    holiday.toString().contains("WorldCup2026") -> {
                        emojiPaint.textSize = panelLength * 1.5f
                        emojiPaint.alpha = if (isNoGravity) b.alpha else 255
                        canvas.drawText("⚽", b.x, b.y + (emojiPaint.textSize / 3), emojiPaint)
                    }
                    isBreakout -> {
                        panelPaint.color = cWhite
                        canvas.drawCircle(b.x, b.y, 14f, panelPaint)
                        panelPaint.color = if (isFuturistic) cModernCyan else mainColor
                    }
                    isFuturistic -> {
                        panelPaint.color = cNeonPink
                        canvas.drawRect(b.x - 10, b.y - 10, b.x + 10, b.y + 10, panelPaint)
                        panelPaint.color = cModernCyan
                    }
                    isBlueprint -> {
                        // Disegna il mirino (Crosshair vettoriale) anzichè la pallina solida
                        val crossSize = 20f
                        canvas.drawLine(b.x - crossSize, b.y, b.x + crossSize, b.y, blueprintVectorPaint)
                        canvas.drawLine(b.x, b.y - crossSize, b.x, b.y + crossSize, blueprintVectorPaint)

                        // HUD Ball (Testo flottante con coordinate e angolo)
                        val txtX = b.x + 15f
                        val txtY = b.y - 15f
                        val lineH = blueprintTextPaint.textSize
                        val angleDeg = ((b.direction * 180) / Math.PI).toInt()
                        canvas.drawText(context.getString(R.string.blueprint_coordinates, b.x.toInt(), b.y.toInt()), txtX, txtY, blueprintTextPaint)
                        canvas.drawText(context.getString(R.string.blueprint_angle, angleDeg), txtX, txtY + lineH, blueprintTextPaint)
                    }
                    isClassic || isMatrix -> canvas.drawRect(b.x - 8, b.y - 8, b.x + 8, b.y + 8, panelPaint)
                    else -> canvas.drawCircle(b.x, b.y, 12f, panelPaint)
                }

                panelPaint.alpha = prevAlpha

                drawNum(canvas, number1X, currentHours / 10, isGlitchSpike)
                drawNum(canvas, number2X, currentHours % 10, isGlitchSpike)
                drawNum(canvas, number3X, currentMinutes / 10, isGlitchSpike)
                drawNum(canvas, number4X, currentMinutes % 10, isGlitchSpike)

                if (isGlitch) {
                    for (y in 0..canvasHeight step 12) {
                        canvas.drawLine(0f, y.toFloat(), canvasWidth.toFloat(), y.toFloat(), scanlinePaint)
                    }

                    if (trailSize > 0) {
                        val gx = trailX[trailSize - 1]
                        val gy = trailY[trailSize - 1]
                        canvas.drawCircle(gx + (Math.random() * 30 - 15).toFloat(), gy + (Math.random() * 30 - 15).toFloat(), 15f, ghostPaint)
                    }
                }
            } catch (e: Exception) {
            } finally {
                if (canvasSaved) {
                    canvas.restore()
                }
            }
        }

        private fun drawNum(canvas: Canvas, x: Int, n: Int, isGlitchSpike: Boolean = false) {
            canvas.withSave {
                val numHeight = LH * numberScale
                translate(x.toFloat(), canvasHeight2 - numHeight / 2)
                scale(numberScale, numberScale)

                if (isGlitchSpike && Math.random() > 0.4) {
                    val shiftX = (Math.random() * 2 - 1).toFloat()
                    canvas.withSave {
                        translate(shiftX * 2, 0f)
                        drawLines(numbers[n], glitchCyanPaint)
                    }
                    canvas.withSave {
                        translate(-shiftX * 2, 0f)
                        drawLines(numbers[n], glitchMagentaPaint)
                    }
                }
                drawLines(numbers[n], numberPaint)
            }
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
                            } catch (t: Throwable) {
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

    private fun getColorInternal(calendar: Calendar): Int {
        val holidayVal = AppUtils.getHoliday(calendar)
        if (holidayVal.toString() != "None" && holidayVal.toString() != "0") {
            return when {
                holidayVal.toString().contains("Halloween") -> colorCompat(R.color.halloween_orange)
                holidayVal.toString().contains("Christmas") -> colorCompat(R.color.christmas_red)
                holidayVal.toString().contains("NewYear") -> colorCompat(R.color.newyear_gold)
                holidayVal.toString().contains("WorldCup2026") -> colorCompat(R.color.white)
                else -> colorCompat(R.color.white)
            }
        }

        val isMatrix = SharedPreferencesManager.mode == 1
        val isSeason = SharedPreferencesManager.season == 1
        val isBreakout = SharedPreferencesManager.breakOut == 1
        val isFuturistic = SharedPreferencesManager.futuristic == 1
        val isGlitch = SharedPreferencesManager.glitch == 1
        val isNoGravity = SharedPreferencesManager.noGravity == 1
        val isBlueprint = SharedPreferencesManager.bluePrint == 1

        val isClassic = SharedPreferencesManager.classic == 1 || (!isMatrix &&
                !isSeason && !isBreakout && !isFuturistic && !isGlitch && !isNoGravity && !isBlueprint)

        var color = colorCompat(R.color.white)
        if (isClassic || isBlueprint) color = colorCompat(R.color.white)
        else if (isMatrix) color = colorCompat(R.color.matrix)
        else if (isSeason) color = colorCompat(AppUtils.getSeason(calendar[Calendar.DAY_OF_YEAR]).toColor())
        else if (isFuturistic) color = colorCompat(R.color.modern_cyan)

        return color
    }

    init {
        holder.addCallback(this)
        isFocusable = false
        isClickable = false
    }

    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, height: Int) {
        thread?.setSurfaceSize(w, height)
    }

    override fun surfaceCreated(h: SurfaceHolder) {
        thread = PongThread(h)
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