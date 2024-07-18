package rpt.tool.pongclock.utils.navigation

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import rpt.tool.pongclock.utils.log.w

val Fragment.safeNavController: NavController?
    get() {
        runCatching {
            findNavController()
        }.onSuccess {
            return it
        }

        runCatching {
            requireActivity().safeNavController
        }.onSuccess {
            return it
        }

        runCatching {
            requireView().safeNavController
        }.onSuccess {
            return it
        }.onFailure {
            w(it)
        }

        return null
    }