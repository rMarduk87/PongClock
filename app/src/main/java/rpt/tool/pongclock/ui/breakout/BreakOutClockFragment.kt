package rpt.tool.pongclock.ui.breakout

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.databinding.FragmentBreakOutClockBinding

class BreakOutClockFragment: BaseFragment<FragmentBreakOutClockBinding>(
    FragmentBreakOutClockBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            
            val left = Math.max(systemBars.left, displayCutout.left)
            val right = Math.max(systemBars.right, displayCutout.right)
            val top = Math.max(systemBars.top, displayCutout.top)
            val bottom = Math.max(systemBars.bottom, displayCutout.bottom)
            
            v.setPadding(left, top, right, bottom)
            insets
        }
    }
}
