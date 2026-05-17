package rpt.tool.pongclock.ui.clock

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.R
import rpt.tool.pongclock.databinding.FragmentClockBinding
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.extensions.toColor
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import rpt.tool.pongclock.utils.navigation.safeNavController
import rpt.tool.pongclock.utils.navigation.safeNavigate
import rpt.tool.pongclock.utils.view.PongTimeView.PongThread
import java.util.Calendar


@Suppress("DEPRECATION")
class ClockFragment : BaseFragment<FragmentClockBinding>(FragmentClockBinding::inflate) {

    private var pongThread: PongThread? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (SharedPreferencesManager.breakOut == 1) {
            safeNavController?.safeNavigate(ClockFragmentDirections
                .actionClockFragmentToBreakOutClockFragment())
            return
        }

        pongThread = binding.pongview.thread

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

        binding.settings.setColorFilter(getColor())

        binding.settings.setOnClickListener {
            goToSettingsFragment()
        }

    }

    private fun goToSettingsFragment() {
        safeNavController?.safeNavigate(ClockFragmentDirections.
        actionClockFragmentToSettingsFragment())
    }

    private fun getColor(): Int {
        var color = Color.WHITE
        if(SharedPreferencesManager.mode == 1 && SharedPreferencesManager.season == 0){
            color = requireContext().getColor(R.color.matrix)
        }
        else if((SharedPreferencesManager.mode == 1 || SharedPreferencesManager.mode ==0)
            && SharedPreferencesManager.season == 1){
            val calendar = Calendar.getInstance()
            val currentDayOfYear = calendar[Calendar.DAY_OF_YEAR]
            color = requireContext().getColor(AppUtils.getSeason(currentDayOfYear).toColor())
        }
        return color
    }
}