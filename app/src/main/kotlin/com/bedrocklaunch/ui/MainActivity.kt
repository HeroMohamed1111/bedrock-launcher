package com.bedrocklaunch.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bedrocklaunch.R
import com.bedrocklaunch.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. Navigation Component manages all fragment destinations.
 * Material BottomNavigationView provides top-level navigation between the five main
 * sections: Launcher, Mods, Servers, Downloads, News.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Material3 splash screen before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Top-level destinations — these get no "back" arrow in the AppBar
        appBarConfig = AppBarConfiguration(
            setOf(
                R.id.launcherFragment,
                R.id.modsFragment,
                R.id.serversFragment,
                R.id.downloadsFragment,
                R.id.newsFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNavigation.setupWithNavController(navController)

        // Hide/show bottom nav when drilling into sub-destinations
        navController.addOnDestinationChangedListener { _, dest, _ ->
            when (dest.id) {
                R.id.launcherFragment,
                R.id.modsFragment,
                R.id.serversFragment,
                R.id.downloadsFragment,
                R.id.newsFragment -> binding.bottomNavigation.visibility =
                    android.view.View.VISIBLE

                else -> binding.bottomNavigation.visibility = android.view.View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
}
