package rpt.tool.pongclock.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.PathEffect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import rpt.tool.pongclock.utils.log.i
import java.util.Date
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class PongTimeView(context: Context?, attrs: AttributeSet?) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {
    val thread: PongThread

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

        private var number1X = 0 // xpos of number 1 (hours)
        private var number2X = 0 // xpos of number 2 (hours)
        private var number3X = 0 // xpos of number 3 (minutes)
        private var number4X = 0 // xpos of number 4 (minutes)

        private var currentHours: Int
        private var currentMinutes: Int
        private var currentFPS = 0
        private var waitCount = 0

        private var playFieldY1 = 0 // playfield frame
        private var playFieldY2 = 0
        private var playFieldX1 = 0
        private var playFieldX2 = 0

        private var running = false
        private var lastTimeMillis: Long = 0
        private var nextTimeUpdate: Long = 0
        private val lastTime: Date
        private var mode: Int
        private var gMode = 0
        private var showFPS = false

        private var canvasHeight = 0
        private var canvasHeight2 = 0 // height / 2
        private var canvasWidth = 0
        private var canvasWidth2 = 0 // width / 2;

        private val dashedLinePaint: Paint
        private val linePaint = Paint()
        private val panelPaint: Paint
        private val textPaint: Paint

        private val currentDate: Date = Date()

        private val numbers = arrayOf( // lines to draw numbers
            floatArrayOf(
                0f, 0f, Companion.LW.toFloat(), 0f,  // null
                Companion.LW.toFloat(), 0f, Companion.LW.toFloat(), Companion.LH.toFloat(),
                Companion.LW.toFloat(), Companion.LH.toFloat(), 0f, Companion.LH.toFloat(),
                0f, Companion.LH.toFloat(), 0f, 0f
            ),
            floatArrayOf(
                Companion.LW.toFloat(),
                0f,
                Companion.LW.toFloat(),
                Companion.LH.toFloat()
            ),  // one
            floatArrayOf(
                0f, 0f, Companion.LW.toFloat(), 0f,  // two
                Companion.LW.toFloat(), 0f, Companion.LW.toFloat(), Companion.LH2.toFloat(),
                Companion.LW.toFloat(), Companion.LH2.toFloat(), 0f, Companion.LH2.toFloat(),
                0f, Companion.LH2.toFloat(), 0f, Companion.LH.toFloat(),
                0f, Companion.LH.toFloat(), Companion.LW.toFloat(), Companion.LH.toFloat()
            ),
            floatArrayOf(
                0f,
                0f,
                Companion.LW.toFloat(),
                0f,  // three
                Companion.LW.toFloat(),
                0f,
                Companion.LW.toFloat(),
                Companion.LH2.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH2.toFloat(),
                0f,
                Companion.LH2.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH2.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH.toFloat(),
                0f,
                Companion.LH.toFloat()
            ),
            floatArrayOf(
                0f, 0f, 0f, Companion.LH2.toFloat(),  // four
                0f, Companion.LH2.toFloat(), Companion.LW.toFloat(), Companion.LH2.toFloat(),
                Companion.LW.toFloat(), 0f, Companion.LW.toFloat(), Companion.LH.toFloat()
            ),
            floatArrayOf(
                Companion.LW.toFloat(),
                0f,
                0f,
                0f,  // five
                0f,
                0f,
                0f,
                Companion.LH2.toFloat(),
                0f,
                Companion.LH2.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH2.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH2.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH.toFloat(),
                0f,
                Companion.LH.toFloat()
            ),
            floatArrayOf(
                Companion.LW.toFloat(),
                0f,
                0f,
                0f,  // six
                0f,
                0f,
                0f,
                Companion.LH.toFloat(),
                0f,
                Companion.LH.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH2.toFloat(),
                Companion.LW.toFloat(),
                Companion.LH2.toFloat(),
                0f,
                Companion.LH2.toFloat()
            ),
            floatArrayOf(
                0f, 0f, Companion.LW.toFloat(), 0f,  // seven
                Companion.LW.toFloat(), 0f, Companion.LW.toFloat(), Companion.LH.toFloat()
            ),
            floatArrayOf(
                0f, 0f, Companion.LW.toFloat(), 0f,  // eight
                Companion.LW.toFloat(), 0f, Companion.LW.toFloat(), Companion.LH.toFloat(),
                Companion.LW.toFloat(), Companion.LH.toFloat(), 0f, Companion.LH.toFloat(),
                0f, Companion.LH.toFloat(), 0f, 0f,
                0f, Companion.LH2.toFloat(), Companion.LW.toFloat(), Companion.LH2.toFloat()
            ),
            floatArrayOf(
                Companion.LW.toFloat(), Companion.LH.toFloat(), Companion.LW.toFloat(), 0f,  // nine
                Companion.LW.toFloat(), 0f, 0f, 0f,
                0f, 0f, 0f, Companion.LH2.toFloat(),
                0f, Companion.LH2.toFloat(), Companion.LW.toFloat(), Companion.LH2.toFloat()
            )
        )

        init {
            linePaint.color = Color.WHITE
            linePaint.style = Paint.Style.STROKE
            linePaint.strokeWidth = Companion.LINE_WIDTH.toFloat()
            linePaint.strokeCap = Cap.SQUARE

            panelPaint = Paint()
            panelPaint.color = Color.WHITE
            panelPaint.style = Paint.Style.STROKE
            panelPaint.strokeWidth = Companion.PANEL_LINE_WIDTH.toFloat()
            panelPaint.strokeCap = Cap.SQUARE

            dashedLinePaint = Paint()
            dashedLinePaint.color = Color.WHITE
            dashedLinePaint.style = Paint.Style.STROKE
            dashedLinePaint.strokeWidth = Companion.LINE_WIDTH.toFloat()
            val pe: PathEffect = DashPathEffect(floatArrayOf(20f, 16f), 0.0f)
            dashedLinePaint.setPathEffect(pe)

            textPaint = Paint()
            textPaint.isAntiAlias = true
            textPaint.textSize = 15f
            textPaint.color = Color.GRAY
            textPaint.setTypeface(Typeface.MONOSPACE)

            lastTime = Date()
            currentHours = lastTime.hours
            currentMinutes = lastTime.minutes

            mode = Companion.STATE_PAUSE
            if (DEBUG) {
                i(this.javaClass.name, "PongThread")
            }
        }

        fun setSurfaceSize(width: Int, height: Int) {
            // synchronized to make sure these all change atomically
            synchronized(surfaceHolder) {
                canvasWidth = width
                canvasHeight = height
                canvasWidth2 = canvasWidth / 2
                canvasHeight2 = canvasHeight / 2
                playFieldY1 = 8
                playFieldY2 = canvasHeight - 9
                playFieldX1 = 5
                playFieldX2 = canvasWidth - 5

                leftPanel =
                    Panel(Companion.PANEL_XPOS, canvasHeight2)
                rightPanel = Panel(
                    canvasWidth - Companion.PANEL_XPOS,
                    canvasHeight2
                )
                ball = Ball(100f, 100f)

                number1X = canvasWidth2 - 100
                number2X = canvasWidth2 - (100 - Companion.SP)
                number3X =
                    canvasWidth2 + (100 - Companion.SP - Companion.LW)
                number4X = canvasWidth2 + (100 - Companion.LW)

                gMode = Companion.GSTATE_NONE
                lastTimeMillis = System.currentTimeMillis()
                nextTimeUpdate = (lastTimeMillis / 1000) * 1000

                newGame(true)
                setRunning(true)
                mode = Companion.STATE_RUNNING
                if (DEBUG) {
                    i(this.javaClass.name, "setSurfaceSize")
                }
            }
        }

        fun toggleFPS() {
            showFPS = !showFPS
        }

        fun setRunning(running: Boolean) {
            this.running = running
        }

        private fun newGame(left: Boolean) {
            if (DEBUG) {
                i(
                    this.javaClass.name,
                    "new game, left wins: $left"
                )
            }
            ball!!.y = canvasHeight2.toFloat()
            ball!!.x = (if ((left)) canvasWidth2 - 40 else canvasWidth2 + 40).toFloat()
            val d = (Math.random() * 0.8 - 0.4).toFloat()
            ball!!.direction = if ((left)) 0.0f + d else Math.PI.toFloat() + d
            ball!!.computeDir()

            leftPanel!!.y = canvasHeight2
            rightPanel!!.y = canvasHeight2
            if (DEBUG) {
                i(
                    this.javaClass.name,
                    "--> " + (if (left) "left" else "right") + " wins, new game: " + ball!!.x + " " + ball!!.y
                )
            }
        }

        private fun doDraw(canvas: Canvas?) {
            canvas!!.drawColor(Color.BLACK)
            if (showFPS) {
                canvas.drawText("FPS:$currentFPS", 10f, 25f, textPaint)
            }
            drawBall(canvas)
            drawFieldAndPanels(canvas)
            drawTime(canvas, currentHours, currentMinutes)
        }

        private fun drawBall(canvas: Canvas?) {
            canvas!!.drawPoint(ball!!.x, ball!!.y, panelPaint)
        }

        private fun drawTime(canvas: Canvas?, hours: Int, minutes: Int) {
            drawNumber(canvas, number1X, Companion.NUMBER_Y, hours / 10)
            drawNumber(canvas, number2X, Companion.NUMBER_Y, hours % 10)
            drawNumber(canvas, number3X, Companion.NUMBER_Y, minutes / 10)
            drawNumber(canvas, number4X, Companion.NUMBER_Y, minutes % 10)
        }

        private fun drawNumber(canvas: Canvas?, x: Int, y: Int, n: Int) {
            canvas!!.save()
            canvas.translate(x.toFloat(), y.toFloat())
            canvas.drawLines(numbers[n], linePaint)
            canvas.restore()
        }

        private fun drawFieldAndPanels(c: Canvas?) {
            c!!.drawLine(0f, 3f, canvasWidth.toFloat(), 3f, linePaint)
            c.drawLine(
                0f,
                (canvasHeight - 4).toFloat(),
                canvasWidth.toFloat(),
                (canvasHeight - 4).toFloat(),
                linePaint
            )
            c.drawLine(
                (canvasWidth / 2).toFloat(),
                0f,
                (canvasWidth / 2).toFloat(),
                canvasHeight.toFloat(),
                dashedLinePaint
            )
            c.drawLine(
                leftPanel!!.x.toFloat(),
                (leftPanel!!.y - Companion.PANEL_LENGTH).toFloat(),
                leftPanel!!.x.toFloat(),
                (leftPanel!!.y + Companion.PANEL_LENGTH).toFloat(),
                panelPaint
            )
            c.drawLine(
                rightPanel!!.x.toFloat(),
                (rightPanel!!.y - Companion.PANEL_LENGTH).toFloat(),
                rightPanel!!.x.toFloat(),
                (rightPanel!!.y + Companion.PANEL_LENGTH).toFloat(),
                panelPaint
            )
        }

        private fun updatePhysics(now: Long, deltaMillis: Long) {
            // update time

            updateTime(now)

            if (gMode == Companion.GSTATE_STOPPED || gMode == Companion.GSTATE_NONE) {
                return
            }

            val distance = Companion.BALL_SPEED / (1000 / deltaMillis)
            val dX = (distance * ball!!.cosDir)


            // move panels only if neither is about to win
            if (gMode != Companion.GSTATE_HOURSWIN) {
                if ((dX > 0) && (ball!!.x > canvasWidth2)) {
                    movePanel(rightPanel, ball!!.y.toInt(), deltaMillis)
                } else {
                    movePanel(rightPanel, canvasHeight2, deltaMillis)
                }
            }
            if (gMode != Companion.GSTATE_MINUTESWIN) {
                if ((dX < 0) && (ball!!.x < canvasWidth2)) {
                    movePanel(leftPanel, ball!!.y.toInt(), deltaMillis)
                } else {
                    movePanel(leftPanel, canvasHeight2, deltaMillis)
                }
            }


            // check if ball bounces at playfield (top and bottom)
            ball!!.y += (distance * ball!!.sinDir)
            if (ball!!.y < playFieldY1) {
                ball!!.y = playFieldY1 + (playFieldY1 - ball!!.y)
                ball!!.direction = -ball!!.direction
                ball!!.computeDir()
            } else if (ball!!.y > playFieldY2) {
                ball!!.y = playFieldY2 - (ball!!.y - playFieldY2)
                ball!!.direction = -ball!!.direction
                ball!!.computeDir()
            }


            // check if ball bounces at panels
            ball!!.x += dX
            if ((ball!!.x > rightPanel!!.x) &&
                (ball!!.y > rightPanel!!.y - Companion.PANEL_LENGTH) &&
                (ball!!.y < rightPanel!!.y + Companion.PANEL_LENGTH)
            ) {
                ball!!.x = rightPanel!!.x + (rightPanel!!.x - ball!!.x)
                // try to keep the ball direction in between +/- 45 degrees
                ball!!.direction =
                    (-ball!!.direction + Math.PI + Math.random() * 0.6 - 0.3).toFloat()
                ball!!.direction =
                    if ((ball!!.direction > Companion.TWOPI)) ball!!.direction - Companion.TWOPI else if ((ball!!.direction < 0)) ball!!.direction + Companion.TWOPI else ball!!.direction
                if (ball!!.direction < Companion.MIN_RANGLE) {
                    ball!!.direction = Companion.MIN_RANGLE
                } else if (ball!!.direction > Companion.MAX_RANGLE) {
                    ball!!.direction = Companion.MAX_RANGLE
                }
                ball!!.computeDir()
            } else if ((ball!!.x < leftPanel!!.x) &&
                (ball!!.y > leftPanel!!.y - Companion.PANEL_LENGTH) &&
                (ball!!.y < leftPanel!!.y + Companion.PANEL_LENGTH)
            ) {
                ball!!.x = leftPanel!!.x + (leftPanel!!.x - (ball!!.x))
                ball!!.direction =
                    -(ball!!.direction - Math.PI + Math.random() * 0.6 - 0.3).toFloat()
                ball!!.computeDir()
            }


            // check if ball leaves the playfield.
            if ((ball!!.x < playFieldX1) || (ball!!.x > playFieldX2)) {
                newGame(gMode != Companion.GSTATE_MINUTESWIN)
                if ((gMode == Companion.GSTATE_HOURSWIN) || (gMode == Companion.GSTATE_MINUTESWIN)) {
                    if (DEBUG) {
                        i(this.javaClass.name, "stopped")
                    }
                    gMode = Companion.GSTATE_STOPPED
                } else if (DEBUG) {
                    i(this.javaClass.name, "oops, we lose! " + ball!!.x)
                }
            }
        }

        private fun movePanel(p: Panel?, target: Int, deltaMillis: Long) {
            val dPanel = target - p!!.y
            if (abs(dPanel.toDouble()) > 6) {
                val distance = (Companion.PANEL_SPEED / (1000 / deltaMillis)).toInt()
                p.y += if ((dPanel > 0)) distance else -distance
            }
        }

        private fun updateTime(now: Long) {
            var now = now
            now = System.currentTimeMillis()
            if (now > nextTimeUpdate) {
                nextTimeUpdate += 1000
                currentDate.time = now
                when (gMode) {
                    Companion.GSTATE_MINUTESWIN -> {}
                    Companion.GSTATE_HOURSWIN -> {}
                    Companion.GSTATE_PLAY -> if (currentHours != currentDate.hours) {
                        gMode = Companion.GSTATE_HOURSWIN
                        if (DEBUG) {
                            i(this.javaClass.name, "hours!")
                        }
                    } else if (currentMinutes != currentDate.minutes) {
                        gMode = Companion.GSTATE_MINUTESWIN
                        if (DEBUG) {
                            i(this.javaClass.name, "minutes!")
                        }
                    } else {
                        currentHours = currentDate.hours
                        currentMinutes = currentDate.minutes
                    }

                    Companion.GSTATE_STOPPED -> {
                        waitCount++
                        if (waitCount == 2) {
                            gMode = Companion.GSTATE_PLAY
                            waitCount = 0
                        }
                        currentHours = currentDate.hours
                        currentMinutes = currentDate.minutes
                        if (DEBUG) {
                            i(this.javaClass.name, "play on")
                        }
                    }

                    Companion.GSTATE_NONE -> {
                        if (DEBUG) {
                            i(this.javaClass.name, "waiting ...")
                        }
                        waitCount++
                        if (waitCount >= 3) {
                            gMode = Companion.GSTATE_PLAY
                            waitCount = 0
                        }
                    }

                    else -> {
                        if (DEBUG) {
                            i(
                                this.javaClass.name,
                                "ooops! $gMode"
                            )
                        }
                        gMode = Companion.GSTATE_PLAY
                    }
                }
            }
        }

        override fun run() {
            var count = 0
            var t1 = System.currentTimeMillis()
            if (DEBUG) {
                i(this.javaClass.name, "entering run")
            }
            while (running) {
                var c: Canvas? = null
                try {
                    c = surfaceHolder.lockCanvas(null)
                    if (mode == Companion.STATE_RUNNING) {
                        count++
                        val now = System.currentTimeMillis()
                        val delta = now - lastTimeMillis
                        updatePhysics(now, delta)
                        lastTimeMillis = now

                        if (now - t1 > 5000) {
                            currentFPS = count / 5
                            t1 = now
                            count = 0
                        }
                        synchronized(surfaceHolder) {
                            doDraw(c)
                        }
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c)
                    }
                }
            }
        }


    } // PongThread


    init {
        // register our interest in hearing about changes to our surface
        val holder = holder
        holder.addCallback(this)

        // create thread only; it's started in surfaceCreated()
        thread = PongThread(holder)
        isFocusable = true // make sure we get key events
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        thread.setSurfaceSize(width, height)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        if (DEBUG) {
            i(this.javaClass.name, "surfaceCreated")
        }
        thread.setRunning(true)
        thread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        var retry = true
        thread.setRunning(false)
        while (retry) {
            try {
                thread.join()
                retry = false
            } catch (e: InterruptedException) {
            }
        }
    }

    companion object {
        const val STATE_PAUSE: Int = 1
        const val STATE_RUNNING: Int = 2
        const val DEBUG: Boolean = false

        const val GSTATE_NONE: Int = 0
        const val GSTATE_HOURSWIN: Int = 1
        const val GSTATE_MINUTESWIN: Int = 2
        const val GSTATE_PLAY: Int = 3
        const val GSTATE_STOPPED: Int = 4

        private const val NUMBER_Y = 50

        private const val BALL_SPEED = 300.0f // pixels per second
        private const val PANEL_SPEED = 250.0f // pixels per second
        private const val PANEL_LENGTH = 20
        private const val PANEL_XPOS = 25
        private const val TWOPI = (Math.PI * 2).toFloat()
        private const val MIN_RANGLE = (3 * (Math.PI / 4)).toFloat()
        private const val MAX_RANGLE = (5 * (Math.PI / 4)).toFloat()

        private const val LINE_WIDTH = 7 // line width to draw the playfield
        private const val PANEL_LINE_WIDTH = 9 // line width to draw the panels
        private const val LW = 15 // letter width
        private const val LH = 26 // letter height
        private const val LH2 = LH / 2
        private const val SP = LW + 15 // letter width + space
    }
}