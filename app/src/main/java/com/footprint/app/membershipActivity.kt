package com.footprint.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.footprint.app.Constants.REQUEST_GALLERY
import com.footprint.app.Constants.REQUEST_PERMISSION
import com.footprint.app.api.model.ProfileModel
import com.footprint.app.databinding.ActivityMembershipBinding

class membershipActivity : AppCompatActivity() {

    private val binding by lazy {ActivityMembershipBinding.inflate(layoutInflater)}
    // 멤버 변수로 이미지 Uri 저장
    private var selectedImageUri: Uri? = null
    companion object{
        lateinit var myprofile:ProfileModel
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        initView()
    }
    private fun initView() {
        binding.complete.setOnClickListener {
            // 입력한 데이터를 저장하는 코드 작성해야함
            //MainActivity로 이동
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.profileImage.setOnClickListener {
            openGallery()
        }
        binding.complete.setOnClickListener {
            val nickName = binding.nickName.text.toString()
            val dogName = binding.dogNameword.text.toString()
            val dogAgeText = binding.dogAge.text.toString()
            val dogAge = dogAgeText.toIntOrNull() ?: 0

            //이름 또는 나이 둘다 입력해야 통과
            if (dogName.isEmpty() || dogAge == 0){
                showToast("정보를 모두 입력해주세요")
                return@setOnClickListener
            }

            // 이름 유효성 검사: 한글, 영문, 숫자 조합 2~10자리
            val namePattern = "^[a-zA-Z가-힣0-9]{2,10}$".toRegex()
            if (!namePattern.matches(nickName)) {
                showToast("이름은 한글, 영문, 숫자 조합 2~10자리로 입력해주세요.")
                return@setOnClickListener
            }
            //내새끼 유효성 검사
            val dognamePattern = "^[a-zA-Z가-힣\\s]*$".toRegex()
            if (!dognamePattern.matches(dogName)) {
                showToast("이름에 특수 문자 또는 잘못된 문자가 포함되어 있습니다. 다시 입력하세요.")
                return@setOnClickListener
            }

            //성별 설정하지 않을시 다시 선택하게 함
            val dogSex = when (binding.dogSex.checkedRadioButtonId) {
                R.id.man -> "남아"
                R.id.woman -> "여아"
                else -> {
                    showToast("성별을 선택하세요.")
                    return@setOnClickListener
                }
            }
            if(selectedImageUri==null){
                showToast("사진을 선택하세요.")
                return@setOnClickListener
            }

//            createProfile(nickName,dogName,dogAgeText,dogAge,dogSex,selectedImageUri)
            Log.d("FootprintApp","프로필 모델 : ${myprofile}")
        }
    }

    private fun openGallery() { // 갤러리를 여는 함수.
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES // READ_EXTERNAL_STORAGE 권한이 있는지 확인. 앱이 사용자의 저장소에서 파일을 읽을수 있도록 허용하는 권한. android.Manifest를 import 해와야한다.
            ) != PackageManager.PERMISSION_GRANTED // PackageManager.PERMISSION_GRANTED는 권한 승인상태. 승인상태가 아니라면, 아래의 실행문을 실행하게 된다.
        ) {
            Log.d("FootprintApp","openGallery1")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_PERMISSION)
            // requestPermissions 메서드를 이용해 사용자에게 해당 권한을 요청함.
        } else { // 권한이 없는상태가 아니라면(= 권한이 있는 상태라면)
            Log.d("FootprintApp","openGallery2")
            val intent = Intent(Intent.ACTION_PICK) // 이미지를 선택할 수 있는 액션을 가진 인텐트 객체를 생성, 선택을 하면 intent에 데이터가 담김
            intent.type = "image/*" // 갤러리에서 이미지 파일만 표시하도록 지정
            startActivityForResult(intent, REQUEST_GALLERY)
            // REQUEST_GALLERY = 1이면, 갤러리 액티비티를 시작하면서 이 액티비티에서 결과를 반환받을 것임을 알린다.
        } // 이 함수의 결과로 데이터를 선택하면 intent에 이미지 데이터(자료형은 Uri)가 담김
    }
    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("FootprintApp","onRequestPermissionsResult1")
                    openGallery()
                } else {
                    // 권한 거부 처리
                    Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    Log.d("FootprintApp","onRequestPermissionsResult2")

                }
            }
        }
    }
    // 이미지 선택 결과 처리
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            val imageUri = data?.data
            selectedImageUri = imageUri // 선택된 이미지 Uri 저장
            binding.profileImage.setImageURI(imageUri)
        }
    }
    private fun showToast(message: String) {
        // 토스트 메시지 표시
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
//    private fun createProfile(nickName:String,dogName:String,dogAgeText:String,dogAge:Int,dogSex:String,selectedImageUri:Uri?)
//    {
//        myprofile = ProfileModel(nickName,dogName,dogAgeText,dogAge,dogSex,selectedImageUri)
//    }

}