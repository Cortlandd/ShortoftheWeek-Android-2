package com.cortlandwalker.shortoftheweek

import android.content.res.Configuration
import android.util.Log
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cortlandwalker.shortoftheweek.ui.theme.ShortOfTheWeekTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val navHost =
//            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
//        val navController = navHost.navController
//
//        findViewById<BottomNavigationView>(R.id.bottom_nav)
//            .setupWithNavController(navController)

        setContent {
            ShortOfTheWeekTheme {
                SharedTransitionLayout {
                    AppNavigation(sharedTransitionScope = this)
                }
            }
        }
    }
}
