package rpt.tool.pongclock.ui.breakout

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.databinding.FragmentBreakOutClockBinding
import rpt.tool.pongclock.utils.navigation.safeNavController
import rpt.tool.pongclock.utils.navigation.safeNavigate

class BreakOutClockFragment: BaseFragment<FragmentBreakOutClockBinding>(
    FragmentBreakOutClockBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.settings) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            
            val left = Math.max(systemBars.left, displayCutout.left)
            val top = Math.max(systemBars.top, displayCutout.top)
            val density = resources.displayMetrics.density
            
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = left + (16 * density).toInt()
                topMargin = top + (16 * density).toInt()
            }
            insets
        }

        binding.settings.setOnClickListener {
            goToSettingsFragment()
        }
    }

    private fun goToSettingsFragment() {
        try {
            safeNavController?.safeNavigate(BreakOutClockFragmentDirections
                .actionBreakOutClockFragmentToSettingsFragment())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
