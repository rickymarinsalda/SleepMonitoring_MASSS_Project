package com.unipi.sleepmonitoring_masss_project

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.unipi.sleepmonitoring_masss_project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main);
    }

    fun openDrawer(view: View) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.openDrawer(GravityCompat.START)
    }

}