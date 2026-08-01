package rpt.tool.pongclock.ui.menu

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import rpt.com.base.BaseFragment
import rpt.com.base.navigation.safeNavController
import rpt.com.base.navigation.safeNavigate
import rpt.tool.pongclock.R
import rpt.tool.pongclock.databinding.FragmentMenuBinding
import rpt.tool.pongclock.utils.view.*
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.extensions.toColor
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import java.util.Calendar

class MenuFragment : BaseFragment<FragmentMenuBinding>(FragmentMenuBinding::inflate,true) {

    private var pulseAnimator: ObjectAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyTheme()
        startPulseAnimation()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnClock.setOnClickListener {
            val holiday = AppUtils.getHoliday(Calendar.getInstance())
            
            val isBreakout = SharedPreferencesManager.breakOut == 1

            val action = if (holiday != AppUtils.Companion.Holiday.None) {
                MenuFragmentDirections.actionMenuFragmentToClockFragment()
            } else if (isBreakout) {
                MenuFragmentDirections.actionMenuFragmentToBreakOutClockFragment()
            } else {
                MenuFragmentDirections.actionMenuFragmentToClockFragment()
            }
            safeNavController(R.id.main_activity_nav_host_fragment)
                ?.safeNavigate(action)
        }

        binding.btnSettings.setOnClickListener {
            safeNavController(R.id.main_activity_nav_host_fragment)?.safeNavigate(
                MenuFragmentDirections.actionMenuFragmentToSettingsFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        pulseAnimator?.resume()
    }

    override fun onPause() {
        super.onPause()
        pulseAnimator?.pause()
    }

    override fun onDestroyView() {
        pulseAnimator?.cancel()
        super.onDestroyView()
    }

    private fun applyTheme() {
        val context = requireContext()
        val calendar = Calendar.getInstance()
        val holiday = AppUtils.getHoliday(calendar)

        val isMatrix = SharedPreferencesManager.mode == 1
        val isSeason = SharedPreferencesManager.season == 1
        val isBreakout = SharedPreferencesManager.breakOut == 1
        val isFuturistic = SharedPreferencesManager.futuristic == 1

        val isClassic = SharedPreferencesManager.classic == 1 || (!isMatrix && !isSeason && !isBreakout && !isFuturistic)

        val white = context.getColor(R.color.white)

        resetVisualEffects()

        when {
            holiday != AppUtils.Companion.Holiday.None -> {
                applyHolidayTheme(holiday)
            }
            isClassic -> {
                binding.root.setBackgroundColor(context.getColor(R.color.classic_bg))
                binding.title.setTextColor(white)
                binding.title.typeface = Typeface.MONOSPACE
                binding.title.text = context.getString(R.string.classic_title)

                binding.fxContainer.addView(PongEffectView(context))

                styleButton(binding.btnClock, white, true, cornerRadius = 0)
                styleButton(binding.btnSettings, white, true, cornerRadius = 0)
            }
            isMatrix -> {
                val matrixGreen = context.getColor(R.color.matrix)
                binding.root.setBackgroundColor(context.getColor(R.color.matrix_bg))
                binding.title.setTextColor(matrixGreen)
                binding.title.typeface = Typeface.MONOSPACE
                binding.title.text = context.getString(R.string.matrix_title)
                binding.title.setShadowLayer(15f, 0f, 0f, matrixGreen)

                binding.fxContainer.addView(MatrixEffectView(context))

                styleButton(binding.btnClock, matrixGreen, true, cornerRadius = 0)
                styleButton(binding.btnSettings, matrixGreen, true, cornerRadius = 0)
            }
            isBreakout -> {
                binding.root.setBackgroundColor(context.getColor(R.color.breakout_bg))
                binding.title.setTextColor(context.getColor(R.color.brick_amber))
                binding.title.text = context.getString(R.string.breakout_title)
                binding.title.setShadowLayer(5f, 5f, 5f, context.getColor(R.color.black))

                binding.fxContainer.addView(BreakoutSimulatedView(context))

                styleButton(binding.btnClock, context.getColor(R.color.brick_red),
                    false, cornerRadius = 8)
                styleButton(binding.btnSettings, context.getColor(R.color.brick_blue),
                    false, cornerRadius = 8)
            }
            isSeason -> {
                val season = AppUtils.getSeason(calendar[Calendar.DAY_OF_YEAR])
                val seasonColor = context.getColor(season.toColor())
                binding.root.setBackgroundColor(context.getColor(R.color.modern_bg))
                binding.title.setTextColor(seasonColor)

                when(season) {
                    AppUtils.Companion.Season.Spring -> {
                        binding.title.text = context.getString(R.string.spring_mode)
                        binding.fxContainer.addView(ParticleEffectView(context,
                            ParticleShape.LEAF, listOf(context.getColor(R.color.spring_particle)),
                            40, 0.8f))
                    }
                    AppUtils.Companion.Season.Summer -> {
                        binding.title.text = context.getString(R.string.summer_mode)
                        binding.fxContainer.addView(ParticleEffectView(context,
                            ParticleShape.CIRCLE, listOf(context.getColor(R.color.yellow)),
                            30, 0.4f))
                    }
                    AppUtils.Companion.Season.Fall -> {
                        binding.title.text = context.getString(R.string.fall_mode)
                        binding.fxContainer.addView(ParticleEffectView(context,
                            ParticleShape.LEAF, listOf(
                                context.getColor(R.color.fall_leaf_1),
                                context.getColor(R.color.fall_leaf_2)),
                            50, 1.2f))
                    }
                    else -> {
                        binding.title.text = context.getString(R.string.winter_mode)
                        binding.fxContainer.addView(ParticleEffectView(context,
                            ParticleShape.CIRCLE, listOf(context.getColor(R.color.white)),
                            100, 1.0f))
                    }
                }

                styleButton(binding.btnClock, seasonColor, false, cornerRadius = 32)
                styleButton(binding.btnSettings, seasonColor, true, cornerRadius = 32)
            }
            else -> {
                // Stile Moderno/Futuristico
                val cyan = context.getColor(R.color.modern_cyan)
                val pink = context.getColor(R.color.neon_pink)
                binding.root.setBackgroundColor(context.getColor(R.color.modern_bg))
                binding.title.setTextColor(cyan)
                binding.title.setShadowLayer(20f, 0f, 0f, cyan)

                binding.fxContainer.addView(FuturisticEffectView(context))

                styleButton(binding.btnClock, cyan, false, cornerRadius = 24)
                styleButton(binding.btnSettings, pink, true, cornerRadius = 24)
            }
        }
    }

    private fun resetVisualEffects() {
        val context = requireContext()
        binding.root.setBackgroundColor(context.getColor(R.color.black))
        binding.title.typeface = Typeface.DEFAULT_BOLD
        binding.title.setShadowLayer(0f, 0f, 0f, context.getColor(android.R.color.transparent))
        binding.fxContainer.removeAllViews()
    }

    private fun applyHolidayTheme(holiday: AppUtils.Companion.Holiday) {
        val context = requireContext()
        when (holiday) {
            AppUtils.Companion.Holiday.Halloween -> {
                val orange = context.getColor(R.color.halloween_orange)
                binding.root.setBackgroundColor(context.getColor(R.color.halloween_bg))
                binding.title.setTextColor(orange)
                binding.title.text = context.getString(R.string.halloween)
                binding.title.typeface = Typeface.create("serif", Typeface.BOLD)
                binding.title.setShadowLayer(15f, 0f, 0f, context.getColor(R.color.halloween_shadow))

                binding.fxContainer.addView(HalloweenEffectView(context))

                styleButton(binding.btnClock, orange, true, cornerRadius = 16)
                styleButton(binding.btnSettings, orange, true, cornerRadius = 16)
            }
            AppUtils.Companion.Holiday.Christmas -> {
                val red = context.getColor(R.color.christmas_red)
                val green = context.getColor(R.color.christmas_green)
                binding.root.setBackgroundColor(context.getColor(R.color.christmas_bg))
                binding.title.setTextColor(red)
                binding.title.text = context.getString(R.string.christmas)
                binding.title.setShadowLayer(15f, 0f, 0f, context.getColor(R.color.yellow))

                binding.fxContainer.addView(ParticleEffectView(context,
                    ParticleShape.CIRCLE, listOf(context.getColor(R.color.white)),
                    100, 1.0f))

                styleButton(binding.btnClock, red, false, cornerRadius = 24)
                styleButton(binding.btnSettings, green, false, cornerRadius = 24)
            }
            AppUtils.Companion.Holiday.NewYear -> {
                val gold = context.getColor(R.color.newyear_gold)
                binding.root.setBackgroundColor(context.getColor(R.color.newyear_bg))
                binding.title.setTextColor(gold)
                binding.title.text = context.getString(R.string.new_year)
                binding.title.setShadowLayer(10f, 2f, 2f, context.getColor(R.color.black))

                binding.fxContainer.addView(FireworksEffectView(context))

                styleButton(binding.btnClock, gold, true, cornerRadius = 24)
                styleButton(binding.btnSettings, gold, false, cornerRadius = 24)
            }
            AppUtils.Companion.Holiday.WorldCup2026 -> {
                val gold = context.getColor(R.color.worldcup_gold)
                binding.root.setBackgroundColor(context.getColor(R.color.worldcup_bg))
                binding.title.setTextColor(gold)
                binding.title.text = context.getString(R.string.world_cup)
                binding.title.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
                binding.title.setShadowLayer(15f, 0f, 0f, context.getColor(R.color.black))

                styleButton(binding.btnClock, gold, false, cornerRadius = 32)
                styleButton(binding.btnSettings, context.getColor(R.color.white), true, cornerRadius = 32)
            }
            else -> {}
        }
    }

    private fun styleButton(
        button: com.google.android.material.button.MaterialButton,
        color: Int,
        isOutlined: Boolean,
        cornerRadius: Int = 12
    ) {
        val context = requireContext()
        if (isOutlined) {
            button.backgroundTintList = ColorStateList.valueOf(context.getColor(android.R.color.transparent))
            button.strokeColor = ColorStateList.valueOf(color)
            button.strokeWidth = 4
            button.setTextColor(color)
            button.rippleColor = ColorStateList.valueOf(color).withAlpha(40)
        } else {
            button.backgroundTintList = ColorStateList.valueOf(color)
            button.setTextColor(if (isDarkColor(color)) context.getColor(R.color.white) else context.getColor(R.color.black))
            button.strokeWidth = 0
            button.rippleColor = ColorStateList.valueOf(context.getColor(R.color.white)).withAlpha(60)
        }

        val density = resources.displayMetrics.density
        button.cornerRadius = (cornerRadius * density).toInt()
    }

    private fun isDarkColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 *
                android.graphics.Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private fun startPulseAnimation() {
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.btnClock,
            PropertyValuesHolder.ofFloat("scaleX", 1.05f),
            PropertyValuesHolder.ofFloat("scaleY", 1.05f)
        ).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }
}
