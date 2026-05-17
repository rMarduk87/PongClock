package rpt.tool.pongclock.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import rpt.tool.pongclock.BaseFragment
import kotlin.math.max
import rpt.tool.pongclock.databinding.FragmentSettingsBinding
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import rpt.tool.pongclock.utils.navigation.safeNavController
import rpt.tool.pongclock.utils.navigation.safeNavigate


@Suppress("DEPRECATION")
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            
            // Combine system bars and display cutout insets
            val left = max(systemBars.left, displayCutout.left)
            val right = max(systemBars.right, displayCutout.right)
            val top = systemBars.top
            val bottom = systemBars.bottom

            v.setPadding(left, top, right, bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        updateSwitches()

        binding.switchClassicMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.classic = 1
            } else {
                SharedPreferencesManager.classic = 0
            }
            updateSwitches()
        }

        binding.switchFuturisticMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.futuristic = 1
            } else {
                SharedPreferencesManager.futuristic = 0
            }
            updateSwitches()
        }

        binding.switchMatrixMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.mode = 1
            } else {
                SharedPreferencesManager.mode = 0
            }
            updateSwitches()
        }

        binding.switchSeasonMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.season = 1
            } else {
                SharedPreferencesManager.season = 0
            }
            updateSwitches()
        }

        binding.switchBreakoutMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.breakOut = 1
            } else {
                SharedPreferencesManager.breakOut = 0
            }
            updateSwitches()
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
        binding.switchClassicMode.isChecked = SharedPreferencesManager.classic == 1
        binding.switchFuturisticMode.isChecked = SharedPreferencesManager.futuristic == 1
        binding.switchMatrixMode.isChecked = SharedPreferencesManager.mode == 1
        binding.switchSeasonMode.isChecked = SharedPreferencesManager.season == 1
        binding.switchBreakoutMode.isChecked = SharedPreferencesManager.breakOut == 1
    }

    private fun finish() {
        val action = if (SharedPreferencesManager.breakOut == 1) {
            SettingsFragmentDirections.actionSettingsFragmentToBreakOutClockFragment()
        } else {
            SettingsFragmentDirections.actionSettingsFragmentToClockFragment()
        }
        safeNavController?.safeNavigate(action)
    }
}
