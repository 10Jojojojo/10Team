package com.footprint.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.footprint.app.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.footprint.app.databinding.ActivityLoginBinding
import com.google.android.gms.common.api.ApiException

class
LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnGoogleSignIn: SignInButton
    private val binding by lazy{ActivityLoginBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        btnGoogleSignIn = binding.btngoogle

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("AIzaSyCm5x95U8m1ayqqEtztoG5HVnYOSFiXkj8")
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // 이벤트 추가하기
        btnGoogleSignIn.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                            showDogProfilePage()
                } else {
                    showToast("Firebase 로그인 실패")
                }
            }
    }
    private fun showDogProfilePage() {
        val intent = Intent(this, MainActivity::class.java) // 강아지프로필 페이지로 수정필요
        startActivity(intent)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Google 로그인성공시 Firebase에 인증
                firebaseAuthWithGoogle(account?.idToken!!)
            } catch (e: ApiException) {
                e.printStackTrace()
                showToast("Google 로그인 실패: "+e.statusCode)
            }

        }
    }
    companion object {
        const val RC_SIGN_IN = 9001
    }
}

//package com.footprint.app.ui.login
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.footprint.app.MainActivity
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.SignInButton
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//import com.footprint.app.R
//import com.footprint.app.databinding.ActivityLoginBinding
//import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
//import com.google.android.gms.common.api.ApiException
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase

//class
//LoginActivity : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var btnGoogleSignIn: SignInButton
//    private val binding by lazy{ActivityLoginBinding.inflate(layoutInflater)}
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
////        auth = Firebase.auth
//        btnGoogleSignIn = binding.btngoogle
//
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken("796616173651-tuijqr0er165et5ln12lgud85gc39d8h.apps.googleusercontent.com")
////            .requestIdToken("796616173651-ll4lp11j213p2atnv1eh9ppurldkasee.apps.googleusercontent.com"  )
//            .requestEmail()
//            .build()
//        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
//
//        // 이벤트 추가하기
//        btnGoogleSignIn.setOnClickListener {
//            val signInIntent = mGoogleSignInClient.signInIntent
//            startActivityForResult(signInIntent, RC_SIGN_IN)
//        }
//    }
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    showDogProfilePage()
//                } else {
//                    showToast("Firebase 로그인 실패")
//                }
//            }
//    }
//    private fun showDogProfilePage() {
//        val intent = Intent(this, MainActivity::class.java) // 강아지프로필 페이지로 수정필요
//        startActivity(intent)
//    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)
//                // Google 로그인성공시 Firebase에 인증
//                firebaseAuthWithGoogle(account?.idToken!!)
//            } catch (e: ApiException) {
//                Log.e("LoginActivity", "signInResult:failed code=" + e.statusCode)
//                showToast("Google 로그인 실패: " + GoogleSignInStatusCodes.getStatusCodeString(e.statusCode))
//
////                showToast("Google 로그인 실패")
//
//            }
//        }
//    }
//    companion object {
//        const val RC_SIGN_IN = 9001
//    }
//}