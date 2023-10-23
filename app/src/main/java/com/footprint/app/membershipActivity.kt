package com.footprint.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.footprint.app.databinding.ActivityMembershipBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class membershipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMembershipBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership)

    }
}