package rpt.tool.pongclock.utils

import rpt.tool.pongclock.utils.extensions.toColor
import java.util.Calendar

class AppUtils {
    companion object {

        const val USERS_SHARED_PREF : String = "user_pref"
        const val FIRST_RUN_KEY : String = "firstrun"
        const val MATRIX_MODE : String = "matrix_mode"
        const val SEASON_MODE : String = "season_mode"
        const val BREAKOUT_MODE : String = "breakout_mode"
        const val CLASSIC_MODE : String = "classic_mode"
        const val FUTURISTIC_MODE : String = "futuristic_mode"
        const val DAY_SPRING_MIN = 80
        const val DAY_SPRING_MAX = 172
        const val DAY_SUMMER_MIN = DAY_SPRING_MAX
        const val DAY_SUMMER_MAX = 264
        const val DAY_FALL_MIN = DAY_SUMMER_MAX
        const val DAY_FALL_MAX = 355


        enum class Season {
            Winter,
            Spring,
            Summer,
            Fall;
        }

        enum class Holiday {
            None,
            Halloween,
            Christmas,
            NewYear,
            WorldCup2026;
        }

        fun getHoliday(calendar: Calendar): Holiday {
            val month = calendar.get(Calendar.MONTH) // 0-based: 0=Jan, 9=Oct, 11=Dec
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val year = calendar.get(Calendar.YEAR)

            if (year == 2026) {
                if ((month == Calendar.JUNE && day >= 11) || (month == Calendar.JULY && day <= 20)) {
                    return Holiday.WorldCup2026
                }
            }

            return when {
                month == Calendar.OCTOBER && day == 31 -> Holiday.Halloween
                month == Calendar.DECEMBER && day == 25 -> Holiday.Christmas
                month == Calendar.JANUARY && day == 1 -> Holiday.NewYear
                else -> Holiday.None
            }
        }

        fun getSeason(dayOfYear: Int): Season {
            var result = Season.Winter
            when (dayOfYear) {
                in (DAY_SPRING_MIN + 1)..DAY_SPRING_MAX -> {
                    result = Season.Spring
                }
                in (DAY_SUMMER_MIN + 1)..DAY_SUMMER_MAX -> {
                    result = Season.Summer
                }
                in (DAY_FALL_MIN + 1)..DAY_FALL_MAX -> {
                    result = Season.Fall
                }
            }
            return result
        }

        fun getModeColors(ctx: android.content.Context): Pair<Int, Int> {
            val calendar = Calendar.getInstance()
            val holiday = getHoliday(calendar)
            
            val isMatrix = rpt.tool.pongclock.utils.manager.SharedPreferencesManager.mode == 1
            val isSeason = rpt.tool.pongclock.utils.manager.SharedPreferencesManager.season == 1
            val isBreakout = rpt.tool.pongclock.utils.manager.SharedPreferencesManager.breakOut == 1
            val isFuturistic = rpt.tool.pongclock.utils.manager.SharedPreferencesManager.futuristic == 1

            var mainColor = ctx.getColor(rpt.tool.pongclock.R.color.white)
            var bgColor = ctx.getColor(rpt.tool.pongclock.R.color.black)

            if (holiday != Holiday.None) {
                when (holiday) {
                    Holiday.Halloween -> {
                        mainColor = ctx.getColor(rpt.tool.pongclock.R.color.halloween_orange)
                        bgColor = ctx.getColor(rpt.tool.pongclock.R.color.halloween_bg)
                    }
                    Holiday.Christmas -> {
                        mainColor = ctx.getColor(rpt.tool.pongclock.R.color.christmas_red)
                        bgColor = ctx.getColor(rpt.tool.pongclock.R.color.christmas_bg)
                    }
                    Holiday.NewYear -> {
                        mainColor = ctx.getColor(rpt.tool.pongclock.R.color.newyear_gold)
                        bgColor = ctx.getColor(rpt.tool.pongclock.R.color.newyear_bg)
                    }
                    Holiday.WorldCup2026 -> {
                        mainColor = ctx.getColor(rpt.tool.pongclock.R.color.white)
                        bgColor = ctx.getColor(rpt.tool.pongclock.R.color.worldcup_bg)
                    }
                    else -> {}
                }
            } else {
                if (isMatrix) {
                    mainColor = ctx.getColor(rpt.tool.pongclock.R.color.matrix)
                    bgColor = ctx.getColor(rpt.tool.pongclock.R.color.black)
                } else if (isSeason) {
                    val season = getSeason(calendar.get(Calendar.DAY_OF_YEAR))
                    mainColor = ctx.getColor(season.toColor())
                    bgColor = ctx.getColor(rpt.tool.pongclock.R.color.modern_background)
                } else if (isBreakout) {
                    mainColor = ctx.getColor(rpt.tool.pongclock.R.color.white)
                    bgColor = ctx.getColor(rpt.tool.pongclock.R.color.breakout_bg)
                } else if (isFuturistic) {
                    mainColor = ctx.getColor(rpt.tool.pongclock.R.color.modern_cyan)
                    bgColor = ctx.getColor(rpt.tool.pongclock.R.color.modern_background)
                } else {
                    mainColor = ctx.getColor(rpt.tool.pongclock.R.color.white)
                    bgColor = ctx.getColor(rpt.tool.pongclock.R.color.black)
                }
            }
            
            return Pair(mainColor, bgColor)
        }
    }
}
