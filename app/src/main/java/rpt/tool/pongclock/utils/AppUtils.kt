package rpt.tool.pongclock.utils

import java.util.Calendar
import java.util.Date


class AppUtils {
    companion object {

        const val USERS_SHARED_PREF : String = "user_pref"
        const val FIRST_RUN_KEY : String = "firstrun"
        const val MATRIX_MODE : String = "matrix_mode"
        const val SEASON_MODE : String = "season_mode"
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