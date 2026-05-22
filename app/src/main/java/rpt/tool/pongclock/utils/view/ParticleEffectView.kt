package rpt.tool.pongclock.utils.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import kotlin.random.Random
import androidx.core.graphics.withTranslation

enum class ParticleShape { CIRCLE, LEAF, SQUARE }

@SuppressLint("ViewConstructor")
class ParticleEffectView(
    context: Context,
    private val shape: ParticleShape,
    private val colors: List<Int>,
    private val particleCount: Int = 50,
    private val speedMultiplier: Float = 1f
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particles = mutableListOf<Particle>()
    private var isInitialized = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isInitialized) {
            for (i in 0 until particleCount) {
                particles.add(Particle(width.toFloat(), height.toFloat(),
                    colors))
            }
            isInitialized = true
        }

        particles.forEach { p ->
            paint.color = p.color
            paint.alpha = (p.alpha * 255).toInt()

            canvas.withTranslation(p.x, p.y) {
                rotate(p.rotation)

                when (shape) {
                    ParticleShape.CIRCLE -> drawCircle(0f, 0f, p.size, paint)
                    ParticleShape.SQUARE -> drawRect(
                        -p.size, -p.size, p.size,
                        p.size, paint
                    )

                    ParticleShape.LEAF -> drawOval(
                        -p.size, -p.size * 2,
                        p.size, p.size * 2, paint
                    )
                }

            }
            p.update(width.toFloat(), height.toFloat(), speedMultiplier)
        }

        postInvalidateDelayed(16)
    }

    private class Particle(val maxWidth: Float, val maxHeight: Float, colors: List<Int>) {
        var x = Random.nextFloat() * maxWidth
        var y = Random.nextFloat() * maxHeight
        var size = Random.nextFloat() * 10f + 5f
        var speedY = Random.nextFloat() * 4f + 2f
        var speedX = Random.nextFloat() * 2f - 1f
        var alpha = Random.nextFloat() * 0.5f + 0.3f
        var rotation = Random.nextFloat() * 360f
        var rotationSpeed = Random.nextFloat() * 4f - 2f
        val color = colors[Random.nextInt(colors.size)]

        fun update(width: Float, height: Float, speedMult: Float) {
            y += speedY * speedMult
            x += speedX * speedMult
            rotation += rotationSpeed
            if (y > height + size) {
                y = -size
                x = Random.nextFloat() * width
            }
            if (x > width + size) x = -size
            if (x < -size) x = width + size
        }
    }
}