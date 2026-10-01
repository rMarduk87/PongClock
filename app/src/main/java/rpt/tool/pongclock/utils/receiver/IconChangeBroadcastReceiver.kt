package rpt.tool.pongclock.utils.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import rpt.tool.pongclock.utils.AppUtils

class IconChangeBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {

            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                AppUtils.updateAppIcon(context)
            }
        }
    }
}