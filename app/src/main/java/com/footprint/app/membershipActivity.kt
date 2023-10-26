package com.footprint.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.footprint.app.databinding.ActivityMembershipBinding

class membershipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMembershipBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership)

        binding = ActivityMembershipBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.complete.setOnClickListener {

// 입력한 데이터를 저장하는 코드 작성해야함

//MainActivity로 이동

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}