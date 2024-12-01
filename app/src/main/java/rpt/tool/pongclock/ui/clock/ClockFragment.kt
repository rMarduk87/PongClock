package rpt.tool.pongclock.ui.clock

import android.graphics.Color
import android.os.Bundle
import android.view.View
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.R
import rpt.tool.pongclock.databinding.ClockFragmentBinding
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.extensions.toColor
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import rpt.tool.pongclock.utils.navigation.safeNavController
import rpt.tool.pongclock.utils.navigation.safeNavigate
import rpt.tool.pongclock.utils.view.PongTimeView.PongThread
import java.util.Calendar


@Suppress("DEPRECATION")
class ClockFragment : BaseFragment<ClockFragmentBinding>(ClockFragmentBinding::inflate) {

    private var pongThread: PongThread? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pongThread = binding.pongview.thread

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