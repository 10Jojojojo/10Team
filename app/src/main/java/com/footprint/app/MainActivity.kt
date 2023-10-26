package com.footprint.app


import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.footprint.app.databinding.ActivityMainBinding
import com.footprint.app.services.MyService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    companion object {
        lateinit var apiKey: String
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        apiKey = getString(R.string.api_key)
//        val intent = Intent(this, MyService::class.java)
//        startService(intent)
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {
                    // 선택된 프래그먼트에 따라 다르게 UI 구성 코드 작성하기
                }
                R.id.navigation_community -> {
                    // 선택된 프래그먼트에 따라 다르게 UI 구성 코드 작성하기
                }
                R.id.navigation_mypage -> {
                    // 선택된 프래그먼트에 따라 다르게 UI 구성 코드 작성하기
                }
            }
        }
    }
}