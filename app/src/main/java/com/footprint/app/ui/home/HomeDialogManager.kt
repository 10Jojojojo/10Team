package com.footprint.app.ui.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import com.footprint.app.GoogleMapUtil
import com.footprint.app.R
import com.footprint.app.api.model.FlagModel
import com.footprint.app.api.model.WalkModel
import com.footprint.app.databinding.DialogHomeFlagBinding
import com.footprint.app.databinding.DialogHomePolylineBinding
import com.footprint.app.databinding.DialogHomeWalkBinding
import com.footprint.app.databinding.DialogHomeWalkstopBinding
import com.footprint.app.databinding.FragmentHomeBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeDialogManager(
    private val context: Context,
    private val homeViewModel: HomeViewModel,
    private val mGoogleMap: GoogleMap,
    private val layoutInflater: LayoutInflater,
    private val binding: FragmentHomeBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val onWalkStarted: (String, Marker?) -> Unit,
    private val onWalkEnded: (Marker?) -> Unit,
    private val startLocationService: () -> Unit,
    private val captureMapSnapshot: (GoogleMap) -> Unit,
    private val showToast: (String) -> Unit
) {
     fun showDialogWalkstart(currentLatLng: LatLng) {
        val builder = AlertDialog.Builder(context)
        val bindingDialog = DialogHomeWalkBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        bindingDialog.btYes.setOnClickListener {
            dialog.dismiss()
            if (homeViewModel.walkState.value!! == "산책종료") {
                val starttime = SimpleDateFormat("a HH : mm", Locale.KOREA).format(Date())
                startLocationService()
                val startmarker = GoogleMapUtil.addMarker(context,mGoogleMap,currentLatLng,
                    R.drawable.ic_placeholder_start,"산책시작")

                // onWalkStarted 콜백을 호출하여 startTime과 startMarker를 HomeFragment에 전달
                // 콜백 함수의 구현은 다이어로그 매니저 인스턴스 생성시 해준다.
                onWalkStarted(starttime, startmarker)
            }

            homeViewModel.startWalk()
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun showDialogWalkstate(currentLatLng: LatLng,starttime:String) {
        val builder = AlertDialog.Builder(context)
        val bindingDialog = DialogHomeWalkstopBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        // 다이어로그의 사각형 모서리를 둥글게 만들기 위해 콘스트레인트레이아웃의 색깔을 투명으로 만들기 위한 코드
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bindingDialog.btYes.setOnClickListener {
            dialog.dismiss()
            val endMarker = GoogleMapUtil.addMarker(context,mGoogleMap,currentLatLng,
                R.drawable.ic_placeholder_end,"산책종료")
            // onWalkEnded 콜백을 호출하여 endMarker 를 HomeFragment에 전달
            // 콜백 함수의 구현은 다이어로그 매니저 인스턴스 생성시 해준다.
            onWalkEnded(endMarker)
            homeViewModel.walkList.add(
                WalkModel(
                    binding.tvWalkdistancevalue.text.toString(),
                    binding.tvWalktimevalue.text.toString(),
                    homeViewModel.pathPoints.value!!,
                    mGoogleMap.cameraPosition,
                    starttime = starttime,
                    endtime = SimpleDateFormat("a HH : mm", Locale.KOREA).format(Date())
                )
            )
            captureMapSnapshot(mGoogleMap)
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun showDialogPolyline() {
        val builder = AlertDialog.Builder(context)
        val bindingDialog = DialogHomePolylineBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        // 다이어로그의 사각형 모서리를 둥글게 만들기 위해 콘스트레인트레이아웃의 색깔을 투명으로 만들기 위한 코드
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        homeViewModel.colorCodeData.observe(lifecycleOwner) { text ->
            updateShowDialogPolylineColorCode(bindingDialog, text)
        }
        homeViewModel.lineWidthTextData.observe(lifecycleOwner) { number ->
            updateShowDialogPolylineWidth(bindingDialog, number)
        }
        bindingDialog.etColortext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                homeViewModel.updateColorCode(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        bindingDialog.etLinewidthtext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                homeViewModel.updateWidth(s.toString().toFloatOrNull())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        bindingDialog.btYes.setOnClickListener {
            // 색깔 코드 유효성 검사
            val colorCode = bindingDialog.etColortext.text.toString().trim()
            val colorPattern = "^[0-9a-fA-F]{6}$".toRegex()
            if (!colorPattern.matches(colorCode)) {
                showToast("색깔 코드를 올바르게 입력해주세요. (예: FFFFFF)")
                return@setOnClickListener
            }
            // 궤도의 두께 유효성 검사
            val lineWidthText = bindingDialog.etLinewidthtext.text.toString().trim()
            val lineWidth = lineWidthText.toIntOrNull()
            if (lineWidth == null || lineWidth < 0 || lineWidth > 100) {
                showToast("두께는 0부터 100까지의 숫자로 입력해주세요.")
                return@setOnClickListener
            }
            homeViewModel.colorCode = colorCode
            homeViewModel.lineWidthText = lineWidth.toString()
            binding.vExampleline.setBackgroundColor(Color.parseColor("#${homeViewModel.colorCode}"))
//            bindingDialog.vExampleline.layoutParams.width = lineWidth
            dialog.dismiss()
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
        }
    }

     fun showDialogFlag(latLng: LatLng) {
        val builder = AlertDialog.Builder(context)
        val bindingDialog = DialogHomeFlagBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        // 다이어로그의 사각형 모서리를 둥글게 만들기 위해 콘스트레인트레이아웃의 색깔을 투명으로 만들기 위한 코드
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var flag = R.drawable.ic_flag
        bindingDialog.ivFlag1.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag)
            flag = R.drawable.ic_flag
        }
        bindingDialog.ivFlag2.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag_blue)
            flag = R.drawable.ic_flag_blue
        }
        bindingDialog.ivFlag3.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag_green)
            flag = R.drawable.ic_flag_green
        }
        bindingDialog.ivFlag4.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag_purple)
            flag = R.drawable.ic_flag_purple
        }
        bindingDialog.ivFlag5.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag_yellow)
            flag = R.drawable.ic_flag_yellow
        }

        bindingDialog.btYes.setOnClickListener {
            val text = bindingDialog.tvDialogtext.text.toString()
            val marker = GoogleMapUtil.addMarker(context,mGoogleMap,latLng,flag,text,80,80)
            dialog.dismiss()
            homeViewModel.flagList.add(FlagModel(R.drawable.ic_flag, text, latLng, marker))
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun updateShowDialogPolylineColorCode(inflate: DialogHomePolylineBinding, colorCode: String) {
        val colorPattern = "^[0-9a-fA-F]{6}$".toRegex()
        if (!colorPattern.matches(colorCode)) {
            return
        }
        inflate.vExampleline.setBackgroundColor(Color.parseColor("#${colorCode}"))
    }

    private fun updateShowDialogPolylineWidth(inflate: DialogHomePolylineBinding, width: Float?) {
        width?.let {
            if (width > 0.0F && width <= 100F) {
                inflate.vExampleline.layoutParams.height =
                    (width
//                            * (requireContext().resources.displayMetrics.density)
                            )
                        .toInt()
            }
        }
    }
    private fun updateFlagSelection(binding: DialogHomeFlagBinding, selectedFlag: Int) {
        hideAllSelectionIndicators(binding)

        when (selectedFlag) {
            R.drawable.ic_flag -> {
                binding.ivFlag1select.visibility = View.VISIBLE
            }

            R.drawable.ic_flag_blue -> {
                binding.ivFlag2select.visibility = View.VISIBLE
            }

            R.drawable.ic_flag_green -> {
                binding.ivFlag3select.visibility = View.VISIBLE
            }

            R.drawable.ic_flag_purple -> {
                binding.ivFlag4select.visibility = View.VISIBLE
            }

            R.drawable.ic_flag_yellow -> {
                binding.ivFlag5select.visibility = View.VISIBLE
            }
        }
    }
    private fun hideAllSelectionIndicators(binding: DialogHomeFlagBinding) {
        binding.ivFlag1select.visibility = View.GONE
        binding.ivFlag2select.visibility = View.GONE
        binding.ivFlag3select.visibility = View.GONE
        binding.ivFlag4select.visibility = View.GONE
        binding.ivFlag5select.visibility = View.GONE
    }

}