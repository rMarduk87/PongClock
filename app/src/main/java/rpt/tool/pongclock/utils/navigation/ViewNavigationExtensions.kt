package rpt.tool.pongclock.utils.navigation

import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import rpt.tool.pongclock.utils.log.w


val View.safeNavController: NavController?
    get() {
        runCatching {
            findNavController()
        }.onSuccess {
            return it
        }.onFailure {
            w(it)
        }

        return null
    }