package rpt.tool.pongclock.ui.settings

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import rpt.tool.pongclock.BaseFragment
import rpt.tool.pongclock.databinding.SettingsFragmentBinding
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import rpt.tool.pongclock.R
import rpt.tool.pongclock.utils.navigation.safeNavController
import rpt.tool.pongclock.utils.navigation.safeNavigate


@Suppress("DEPRECATION")
class SettingsFragment : BaseFragment<SettingsFragmentBinding>(SettingsFragmentBinding::inflate) {

    private var mDropdown: PopupWindow? = null

    @SuppressLint("ObsoleteSdkInt", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.leftIconBlock.setOnClickListener{ finish() }

        binding.switchMatrixMode.setChecked(SharedPreferencesManager.mode == 1)
        binding.switchSeasonMode.setChecked(SharedPreferencesManager.season == 1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.navigationBarColor = requireContext().resources.getColor(R.color.black)
        }
        binding.switchMatrixMode.setOnCheckedChangeListener{ _, isChecked ->
            SharedPreferencesManager.mode = if (isChecked) 1 else 0
            SharedPreferencesManager.season = if (isChecked) 0 else
                if(SharedPreferencesManager.season == 1) 1 else 0
            binding.switchMatrixMode.setChecked(SharedPreferencesManager.mode == 1)
            binding.switchSeasonMode.setChecked(SharedPreferencesManager.season == 1)
        }

        binding.switchSeasonMode.setOnCheckedChangeListener{ _, isChecked ->
            SharedPreferencesManager.season = if (isChecked) 1 else 0
            SharedPreferencesManager.mode = if (isChecked) 0 else
                if(SharedPreferencesManager.mode == 1) 1 else 0
            binding.switchMatrixMode.setChecked(SharedPreferencesManager.mode == 1)
            binding.switchSeasonMode.setChecked(SharedPreferencesManager.season == 1)
        }
    }

    private fun finish() {
        safeNavController?.safeNavigate(SettingsFragmentDirections
            .actionSettingsFragmentToClockFragment())
    }


}