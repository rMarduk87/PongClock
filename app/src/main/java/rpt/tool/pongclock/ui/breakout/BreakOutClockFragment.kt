package rpt.tool.pongclock.ui.breakout

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import rpt.com.base.BaseFragment
import rpt.tool.pongclock.databinding.FragmentBreakOutClockBinding

class BreakOutClockFragment: BaseFragment<FragmentBreakOutClockBinding>(
    FragmentBreakOutClockBinding::inflate,true) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
