package rpt.tool.pongclock.ui.settings

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import rpt.com.base.BaseFragment
import rpt.com.base.navigation.safeNavController
import rpt.tool.pongclock.R
import rpt.tool.pongclock.databinding.FragmentSettingsBinding
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.extensions.toColor
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import java.util.Calendar
import kotlin.math.max

class SettingsFragment :
    BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate,true) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            
            val left = max(systemBars.left, displayCutout.left)
            val right = max(systemBars.right, displayCutout.right)
            val top = systemBars.top
            val bottom = systemBars.bottom

            v.setPadding(left, top, right, bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            safeNavController(R.id.main_activity_nav_host_fragment)?.popBackStack() }

        updateSwitches()
        applyTheme()

        binding.switchClassicMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.classic = 1
            } else {
                SharedPreferencesManager.classic = 0
            }
            updateSwitches()
            applyTheme()
        }

        binding.switchFuturisticMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.futuristic = 1
            } else {
                SharedPreferencesManager.futuristic = 0
            }
            updateSwitches()
            applyTheme()
        }

        binding.switchMatrixMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.mode = 1
            } else {
                SharedPreferencesManager.mode = 0
            }
            updateSwitches()
            applyTheme()
        }

        binding.switchSeasonMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.season = 1
            } else {
                SharedPreferencesManager.season = 0
            }
            updateSwitches()
            applyTheme()
        }

        binding.switchBreakoutMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.breakOut = 1
            } else {
                SharedPreferencesManager.breakOut = 0
            }
            updateSwitches()
            applyTheme()
        }
    }

    private fun disableAll() {
        SharedPreferencesManager.classic = 0
        SharedPreferencesManager.futuristic = 0
        SharedPreferencesManager.mode = 0
        SharedPreferencesManager.season = 0
        SharedPreferencesManager.breakOut = 0
    }

    private fun updateSwitches() {
        _binding?.let {
            it.switchClassicMode.isChecked = SharedPreferencesManager.classic == 1
            it.switchFuturisticMode.isChecked = SharedPreferencesManager.futuristic == 1
            it.switchMatrixMode.isChecked = SharedPreferencesManager.mode == 1
            it.switchSeasonMode.isChecked = SharedPreferencesManager.season == 1
            it.switchBreakoutMode.isChecked = SharedPreferencesManager.breakOut == 1
        }
    }

    private fun applyTheme() {
        val ctx = context ?: return
        val (mainColor, bgColor) = AppUtils.getModeColors(ctx)
        
        val isFuturistic = SharedPreferencesManager.futuristic == 1
        val accentColor = if (isFuturistic) ctx.getColor(R.color.neon_pink) else mainColor

        _binding?.let {
            it.root.setBackgroundColor(bgColor)
            it.toolbar.setBackgroundColor(bgColor)
            it.toolbar.setTitleTextColor(mainColor)
            it.toolbar.navigationIcon?.setTint(mainColor)

            it.modeCard.setCardBackgroundColor(bgColor)
            it.modeCard.strokeColor = mainColor

            val textViews = listOf(it.lblClassic, it.lblFuturistic, it.lblActive, it.lblFix, it.lblBreakout)
            textViews.forEach { tv -> tv.setTextColor(mainColor) }

            val switches = listOf(
                it.switchClassicMode, it.switchFuturisticMode, it.switchMatrixMode,
                it.switchSeasonMode, it.switchBreakoutMode
            )
            
            switches.forEach { sw ->
                sw.thumbTintList = ColorStateList.valueOf(accentColor)
                sw.trackTintList = ColorStateList.valueOf(accentColor).withAlpha(100)
            }
        }
    }

    override fun onDestroyView() {
        // Clear listeners to avoid callbacks during/after destruction
        _binding?.let {
            it.switchClassicMode.setOnCheckedChangeListener(null)
            it.switchFuturisticMode.setOnCheckedChangeListener(null)
            it.switchMatrixMode.setOnCheckedChangeListener(null)
            it.switchSeasonMode.setOnCheckedChangeListener(null)
            it.switchBreakoutMode.setOnCheckedChangeListener(null)
        }
        super.onDestroyView()
    }
}
