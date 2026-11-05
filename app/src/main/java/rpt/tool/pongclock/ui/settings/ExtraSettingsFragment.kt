package rpt.tool.pongclock.ui.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import rpt.com.base.BaseFragment
import rpt.com.base.navigation.safeNavController
import rpt.com.base.navigation.safeNavigate
import rpt.tool.pongclock.R
import rpt.tool.pongclock.databinding.FragmentExtraSettingsBinding
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import kotlin.math.max

class ExtraSettingsFragment : BaseFragment<FragmentExtraSettingsBinding>(
    FragmentExtraSettingsBinding::inflate,true) {

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
            safeNavController(R.id.main_activity_nav_host_fragment)?.safeNavigate(
                ExtraSettingsFragmentDirections.actionExtraSettingsFragmentToSettingsFragment()
            )
        }

        updateSwitches()
        applyTheme()

        binding.switchGlitchMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.glitch = 1
            } else {
                SharedPreferencesManager.glitch = 0
            }
            updateSwitches()
            applyTheme()
        }

        binding.switchNoGravityMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.noGravity = 1
            } else {
                SharedPreferencesManager.noGravity = 0
            }
            updateSwitches()
            applyTheme()
        }

        binding.switchBluePrintMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.bluePrint = 1
            } else {
                SharedPreferencesManager.bluePrint = 0
            }
            updateSwitches()
            applyTheme()
        }

        binding.switchPacManMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.pacManMode = 1
            } else {
                SharedPreferencesManager.pacManMode = 0
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
        SharedPreferencesManager.glitch = 0
        SharedPreferencesManager.noGravity = 0
        SharedPreferencesManager.bluePrint = 0
        SharedPreferencesManager.pacManMode = 0
    }

    private fun updateSwitches() {
        _binding?.let {
            it.switchGlitchMode.isChecked = SharedPreferencesManager.glitch == 1
            it.switchNoGravityMode.isChecked = SharedPreferencesManager.noGravity == 1
            it.switchBluePrintMode.isChecked = SharedPreferencesManager.bluePrint == 1
            it.switchPacManMode.isChecked = SharedPreferencesManager.pacManMode == 1
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

            val textViews = listOf(it.lblGlitch, it.lblNoGravity, it.lblBluePrint, it.lblPacMan)
            textViews.forEach { tv -> tv.setTextColor(mainColor) }

            val switches = listOf(
                it.switchGlitchMode, it.switchNoGravityMode,
                it.switchBluePrintMode, it.switchPacManMode
            )

            switches.forEach { sw ->
                sw.thumbTintList = ColorStateList.valueOf(accentColor)
                sw.trackTintList = ColorStateList.valueOf(accentColor).withAlpha(100)
            }
        }
    }

    override fun onDestroyView() {
        _binding?.let {
            it.switchGlitchMode.setOnCheckedChangeListener(null)
            it.switchNoGravityMode.setOnCheckedChangeListener(null)
            it.switchBluePrintMode.setOnCheckedChangeListener(null)
            it.switchPacManMode.setOnCheckedChangeListener(null)
        }
        super.onDestroyView()
    }
}