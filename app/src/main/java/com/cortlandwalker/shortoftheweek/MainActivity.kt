package com.cortlandwalker.shortoftheweek

import android.content.res.Configuration
import android.util.Log
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setupWithNavController(navController)
    }

    // ADD THIS: Log to see if Activity survives
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("VideoDebug", "MainActivity: Configuration Changed to ${newConfig.orientation}")
        // We do NOT need to manually reload fragments here because
        // android:configChanges="orientation|screenSize..." in Manifest handles it.
    }

    // ADD THIS: Log to see if Activity is dying
    override fun onDestroy() {
        super.onDestroy()
        Log.d("VideoDebug", "MainActivity: onDestroy")
    }
}
