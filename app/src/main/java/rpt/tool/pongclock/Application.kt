package rpt.tool.pongclock

import android.app.Application

class Application  : Application() {

    companion object {

        private lateinit var _instance: Application

        val instance: Application
            get() {
                return _instance
            }
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
    }
}