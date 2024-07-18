package rpt.tool.pongclock.ui.clock

import android.R
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.databinding.ClockFragmentBinding
import rpt.tool.pongclock.utils.view.PongTimeView
import rpt.tool.pongclock.utils.view.PongTimeView.PongThread


class ClockFragment : BaseFragment<ClockFragmentBinding>(ClockFragmentBinding::inflate) {

    val MENU_TOGGLEFPS: Int = 1
    val MENU_ABOUT: Int = 2
    val DIALOG_ABOUT: Int = 3

    private var pongThread: PongThread? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pongThread = binding.pongview.thread


        // turn off the window's title bar and switch to fullscreen
    }
}