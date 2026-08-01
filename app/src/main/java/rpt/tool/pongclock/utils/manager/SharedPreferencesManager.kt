package rpt.tool.pongclock.utils.manager

import android.content.Context
import android.content.SharedPreferences
import rpt.tool.pongclock.Application
import rpt.tool.pongclock.utils.AppUtils
import androidx.core.content.edit


object SharedPreferencesManager {
    private val ctx: Context
        get() = Application.instance

    private fun createSharedPreferences(): SharedPreferences {
        return ctx.getSharedPreferences(AppUtils.USERS_SHARED_PREF, Context.MODE_PRIVATE)
    }

    private val sharedPreferences by lazy { createSharedPreferences() }

    var firstRun: Boolean
        get() = sharedPreferences.getBoolean(AppUtils.FIRST_RUN_KEY, true)
        set(value) = sharedPreferences.edit { putBoolean(AppUtils.FIRST_RUN_KEY, value) }
    var mode: Int
        get() = sharedPreferences.getInt(AppUtils.MATRIX_MODE, 0)
        set(value) = sharedPreferences.edit { putInt(AppUtils.MATRIX_MODE, value) }
    var season: Int
        get() = sharedPreferences.getInt(AppUtils.SEASON_MODE, 0)
        set(value) = sharedPreferences.edit { putInt(AppUtils.SEASON_MODE, value) }
    var breakOut: Int
        get() = sharedPreferences.getInt(AppUtils.BREAKOUT_MODE, 0)
        set(value) = sharedPreferences.edit { putInt(AppUtils.BREAKOUT_MODE, value) }
    var classic: Int
        get() = sharedPreferences.getInt(AppUtils.CLASSIC_MODE, 0)
        set(value) = sharedPreferences.edit { putInt(AppUtils.CLASSIC_MODE, value) }
    var futuristic: Int
        get() = sharedPreferences.getInt(AppUtils.FUTURISTIC_MODE, 1) // Default to 1
        set(value) = sharedPreferences.edit { putInt(AppUtils.FUTURISTIC_MODE, value) }
}