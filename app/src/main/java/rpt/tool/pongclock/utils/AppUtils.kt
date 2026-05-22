package rpt.tool.pongclock.utils

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
            NewYear;
        }

        fun getHoliday(calendar: Calendar): Holiday {
            val month = calendar.get(Calendar.MONTH) // 0-based: 0=Jan, 9=Oct, 11=Dec
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            return when (month) {
                Calendar.OCTOBER if day == 31 -> Holiday.Halloween
                Calendar.DECEMBER if day == 25 -> Holiday.Christmas
                Calendar.JANUARY if day == 1 -> Holiday.NewYear
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
    }
}
