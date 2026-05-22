package rpt.tool.pongclock.ui.clock

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.R
import rpt.tool.pongclock.databinding.FragmentClockBinding
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import java.util.Calendar

@Suppress("DEPRECATION")
class ClockFragment : BaseFragment<FragmentClockBinding>(FragmentClockBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val holiday = AppUtils.getHoliday(Calendar.getInstance())
        if (SharedPreferencesManager.breakOut == 1 && holiday == AppUtils.Companion.Holiday.None) {
            findNavController().navigate(R.id.breakOutClockFragment)
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val left = systemBars.left.coerceAtLeast(displayCutout.left)
            val right = systemBars.right.coerceAtLeast(displayCutout.right)
            val top = systemBars.top.coerceAtLeast(displayCutout.top)
            val bottom = systemBars.bottom.coerceAtLeast(displayCutout.bottom)

            v.setPadding(left, top, right, bottom)
            insets
        }
    }
}
