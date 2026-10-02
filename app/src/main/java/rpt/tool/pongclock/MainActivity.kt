package rpt.tool.pongclock

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import rpt.com.base.BaseActivity
import rpt.tool.pongclock.databinding.ActivityMainBinding
import rpt.tool.pongclock.utils.AppUtils

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppUtils.updateAppIcon(this)
    }

    override fun getSystemService(name: String): Any? {
        if (name == "ethernet") {
            return null
        }
        return super.getSystemService(name)
    }


    override fun onNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_activity_nav_host_fragment) as
                    NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}