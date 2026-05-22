package rpt.tool.pongclock.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.databinding.FragmentSettingsBinding
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import rpt.tool.pongclock.utils.navigation.safeNavController
import kotlin.math.max

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

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

        binding.toolbar.setNavigationOnClickListener { safeNavController?.popBackStack() }

        updateSwitches()

        binding.switchClassicMode.setOnCheckedChangeListener { _, isChecked ->
            if (_binding == null) return@setOnCheckedChangeListener
            if (isChecked) {
                disableAll()
                SharedPreferencesManager.classic = 1
            } else {
                SharedPreferencesManager.classic = 0
            }
            updateSwitches()
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
