package rpt.tool.pongclock.ui.menu

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.R
import rpt.tool.pongclock.databinding.FragmentMenuBinding
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.extensions.toColor
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import rpt.tool.pongclock.utils.navigation.safeNavController
import rpt.tool.pongclock.utils.navigation.safeNavigate
import java.util.Calendar

class MenuFragment : BaseFragment<FragmentMenuBinding>(FragmentMenuBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        applyTheme()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnClock.setOnClickListener {
            val holiday = AppUtils.getHoliday(Calendar.getInstance())
            val action = if (holiday != AppUtils.Companion.Holiday.None) {
                MenuFragmentDirections.actionMenuFragmentToClockFragment()
            } else if (SharedPreferencesManager.breakOut == 1) {
                MenuFragmentDirections.actionMenuFragmentToBreakOutClockFragment()
            } else {
                MenuFragmentDirections.actionMenuFragmentToClockFragment()
            }
            safeNavController?.safeNavigate(action)
        }

        binding.btnSettings.setOnClickListener {
            safeNavController?.safeNavigate(MenuFragmentDirections.actionMenuFragmentToSettingsFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    private fun applyTheme() {
        val context = requireContext()
        val calendar = Calendar.getInstance()
        val holiday = AppUtils.getHoliday(calendar)
        
        val isClassic = SharedPreferencesManager.classic == 1
        val isMatrix = SharedPreferencesManager.mode == 1
        val isSeason = SharedPreferencesManager.season == 1
        val isBreakout = SharedPreferencesManager.breakOut == 1

        val white = context.getColor(R.color.white)
        val black = context.getColor(R.color.black)

        binding.root.setBackgroundColor(black)
        binding.title.setTextColor(white)
        binding.title.typeface = Typeface.DEFAULT_BOLD
        binding.title.text = context.getString(R.string.app_name)

        when {
            holiday != AppUtils.Companion.Holiday.None -> {
                applyHolidayTheme(holiday)
            }
            isClassic -> {
                binding.root.setBackgroundColor(context.getColor(R.color.classic_bg))
                binding.title.setTextColor(white)
                binding.title.typeface = Typeface.MONOSPACE
                binding.title.text = context.getString(R.string.classic_title)
                styleButton(binding.btnClock, white, true)
                styleButton(binding.btnSettings, white, true)
            }
            isMatrix -> {
                val matrixGreen = context.getColor(R.color.matrix)
                binding.root.setBackgroundColor(context.getColor(R.color.matrix_bg))
                binding.title.setTextColor(matrixGreen)
                binding.title.typeface = Typeface.MONOSPACE
                binding.title.text = context.getString(R.string.matrix_title)
                styleButton(binding.btnClock, matrixGreen, true)
                styleButton(binding.btnSettings, matrixGreen, true)
            }
            isSeason -> {
                val season = AppUtils.getSeason(calendar[Calendar.DAY_OF_YEAR])
                val seasonColor = context.getColor(season.toColor())
                binding.root.setBackgroundColor(context.getColor(R.color.modern_bg))
                binding.title.setTextColor(seasonColor)
                binding.title.text = when(season) {
                    AppUtils.Companion.Season.Spring -> context.getString(R.string.spring_mode)
                    AppUtils.Companion.Season.Summer -> context.getString(R.string.summer_mode)
                    AppUtils.Companion.Season.Fall -> context.getString(R.string.fall_mode)
                    else -> context.getString(R.string.winter_mode)
                }
                styleButton(binding.btnClock, seasonColor, false)
                styleButton(binding.btnSettings, seasonColor, true)
            }
            isBreakout -> {
                binding.root.setBackgroundColor(context.getColor(R.color.breakout_bg))
                binding.title.setTextColor(context.getColor(R.color.brick_amber))
                binding.title.text = context.getString(R.string.breakout_title)
                styleButton(binding.btnClock, context.getColor(R.color.brick_red), false)
                styleButton(binding.btnSettings, context.getColor(R.color.brick_blue), false)
            }
            else -> {
                val cyan = context.getColor(R.color.modern_cyan)
                binding.root.setBackgroundColor(context.getColor(R.color.modern_bg))
                binding.title.setTextColor(cyan)
                styleButton(binding.btnClock, cyan, false)
                styleButton(binding.btnSettings, cyan, true)
            }
        }
    }

    private fun styleButton(button: com.google.android.material.button.MaterialButton, color: Int, isOutlined: Boolean) {
        if (isOutlined) {
            button.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            button.strokeColor = ColorStateList.valueOf(color)
            button.strokeWidth = 4
            button.setTextColor(color)
            button.rippleColor = ColorStateList.valueOf(color).withAlpha(40)
        } else {
            button.backgroundTintList = ColorStateList.valueOf(color)
            button.setTextColor(if (isDarkColor(color)) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
            button.strokeWidth = 0
            button.rippleColor = ColorStateList.valueOf(android.graphics.Color.WHITE).withAlpha(60)
        }
        button.cornerRadius = 4 
    }

    private fun isDarkColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 * android.graphics.Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private fun applyHolidayTheme(holiday: AppUtils.Companion.Holiday) {
        val context = requireContext()
        when (holiday) {
            AppUtils.Companion.Holiday.Halloween -> {
                val orange = context.getColor(R.color.halloween_orange)
                binding.root.setBackgroundColor(context.getColor(R.color.black))
                binding.title.setTextColor(orange)
                binding.title.text = context.getString(R.string.halloween)
                binding.title.typeface = Typeface.create("serif", Typeface.BOLD)
                styleButton(binding.btnClock, orange, true)
                styleButton(binding.btnSettings, orange, true)
            }
            AppUtils.Companion.Holiday.Christmas -> {
                val red = context.getColor(R.color.christmas_red)
                val green = context.getColor(R.color.christmas_green)
                binding.root.setBackgroundColor(context.getColor(R.color.christmas_bg))
                binding.title.setTextColor(red)
                binding.title.text = context.getString(R.string.christmas)
                styleButton(binding.btnClock, red, false)
                styleButton(binding.btnSettings, green, false)
            }
            AppUtils.Companion.Holiday.NewYear -> {
                val gold = context.getColor(R.color.newyear_gold)
                binding.root.setBackgroundColor(context.getColor(R.color.newyear_bg))
                binding.title.setTextColor(gold)
                binding.title.text = context.getString(R.string.new_year)
                styleButton(binding.btnClock, gold, true)
                styleButton(binding.btnSettings, gold, false)
            }
            else -> {}
        }
    }
}
