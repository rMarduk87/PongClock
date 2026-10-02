package rpt.tool.pongclock.utils.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import rpt.tool.pongclock.utils.AppUtils

class IconChangeBroadcastReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {

        AppUtils.updateAppIcon(context)
    }
}