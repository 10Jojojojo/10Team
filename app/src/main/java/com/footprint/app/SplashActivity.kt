package com.footprint.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.footprint.app.databinding.ActivitySplashBinding
import com.footprint.app.ui.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    // ViewBinding 인스턴스 생성
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding을 초기화하고 뷰를 설정
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 애니메이션 로드 및 시작
        val slideAndBounce1 = AnimationUtils.loadAnimation(this, R.anim.anim_splash)
        val slideAndBounce2 = AnimationUtils.loadAnimation(this, R.anim.anim_splash2)
        val slideAndBounce3 = AnimationUtils.loadAnimation(this, R.anim.anim_splash3)
        val slideAndBounce4 = AnimationUtils.loadAnimation(this, R.anim.anim_splash4)
        val slideAndBounce5 = AnimationUtils.loadAnimation(this, R.anim.anim_splash5)
        val slideAndBounce6 = AnimationUtils.loadAnimation(this, R.anim.anim_splash6)
        val slideAndBounce7 = AnimationUtils.loadAnimation(this, R.anim.anim_splash7)
        val slideAndBounce8 = AnimationUtils.loadAnimation(this, R.anim.anim_splash8)
        val slideAndBounce9 = AnimationUtils.loadAnimation(this, R.anim.anim_splash9)
        val slideAndBounce10 = AnimationUtils.loadAnimation(this, R.anim.anim_splash10)
        binding.splashImage1.startAnimation(slideAndBounce1)
        binding.splashImage2.startAnimation(slideAndBounce2)
        binding.splashImage3.startAnimation(slideAndBounce3)
        binding.splashImage4.startAnimation(slideAndBounce4)
        binding.splashImage5.startAnimation(slideAndBounce5)
        binding.splashImage6.startAnimation(slideAndBounce6)
        binding.splashImage7.startAnimation(slideAndBounce7)
        binding.splashImage8.startAnimation(slideAndBounce8)
        binding.splashImage9.startAnimation(slideAndBounce9)
        binding.splashImage10.startAnimation(slideAndBounce10)


        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, 4000) // 애니메이션이 끝난 후, 5초 후에 MainActivity로 이동
    }
}