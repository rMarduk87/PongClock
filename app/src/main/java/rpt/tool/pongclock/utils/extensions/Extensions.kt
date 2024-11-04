package rpt.tool.pongclock.utils.extensions

import rpt.tool.pongclock.R
import rpt.tool.pongclock.utils.AppUtils


fun AppUtils.Companion.Season.toColor(): Int {
    return when(this){
        AppUtils.Companion.Season.Spring -> R.color.spring
        AppUtils.Companion.Season.Summer -> R.color.summer
        AppUtils.Companion.Season.Fall -> R.color.fall
        else -> R.color.winter
    }
}