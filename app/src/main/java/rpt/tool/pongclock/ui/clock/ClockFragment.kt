package rpt.tool.pongclock.ui.clock

import android.os.Bundle
import android.view.View
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.databinding.ClockFragmentBinding
import rpt.tool.pongclock.utils.navigation.safeNavController
import rpt.tool.pongclock.utils.navigation.safeNavigate
import rpt.tool.pongclock.utils.view.PongTimeView.PongThread


@Suppress("DEPRECATION")
class ClockFragment : BaseFragment<ClockFragmentBinding>(ClockFragmentBinding::inflate) {

    private var pongThread: PongThread? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pongThread = binding.pongview.thread

        binding.settings.setOnClickListener {
            goToSettingsFragment()
        }

    }

    private fun goToSettingsFragment() {
        safeNavController?.safeNavigate(ClockFragmentDirections.
        actionClockFragmentToSettingsFragment())
    }
}