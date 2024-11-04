package rpt.tool.pongclock.utils.manager

import android.content.Context
import android.content.SharedPreferences
import rpt.tool.pongclock.Application
import rpt.tool.pongclock.utils.AppUtils


object SharedPreferencesManager {
    private val ctx: Context
        get() = Application.instance

    private fun createSharedPreferences(): SharedPreferences {
        return ctx.getSharedPreferences(AppUtils.USERS_SHARED_PREF, Context.MODE_PRIVATE)
    }

    private val sharedPreferences by lazy { createSharedPreferences() }

    var firstRun: Boolean
        get() = sharedPreferences.getBoolean(AppUtils.FIRST_RUN_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(AppUtils.FIRST_RUN_KEY, value).apply()
    var mode: Int
        get() = sharedPreferences.getInt(AppUtils.MATRIX_MODE, 0)
        set(value) = sharedPreferences.edit().putInt(AppUtils.MATRIX_MODE, value).apply()
    var season: Int
        get() = sharedPreferences.getInt(AppUtils.SEASON_MODE, 0)
        set(value) = sharedPreferences.edit().putInt(AppUtils.SEASON_MODE, value).apply()
}