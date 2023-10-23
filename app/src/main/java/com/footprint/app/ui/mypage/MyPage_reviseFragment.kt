package com.footprint.app.ui.mypage

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMyPageReviseBinding

class MyPage_reviseFragment : Fragment(R.layout.fragment_my_page_revise) {
    private lateinit var viewModel: MyPageViewModel
    private var _binding: FragmentMyPageReviseBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyPageReviseBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(MyPageViewModel::class.java)

        reUesrInfo()
        revisepage()
        cance()
        dog()
    }

    private fun reUesrInfo() {
        binding.save.setOnClickListener {
            val newName = binding.mypageReviseName.text.toString()
            val newIntroduction = binding.mypageReviseIntroduction.text.toString()
            val newTown = binding.town.text.toString()

            viewModel.name = newName
            viewModel.introduction = newIntroduction
            viewModel.town = newTown

            findNavController().navigate(R.id.mypage)
        }
    }

    private fun revisepage() {
        binding.mypageReviseName.setText(viewModel.name)
        binding.mypageReviseIntroduction.setText(viewModel.introduction)
        binding.town.setText(viewModel.town)
    }

    private fun cance() {
        binding.Cance.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }

    //리사이클러뷰에 아이템을 뛰어우고 추가 혹은 삭제 될때마다 리사이클러뷰 갱신,
     fun dog() {
        val recyclerView = binding.dogCard
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        //리사이클러뷰 가로 스크롤
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        val adapter = DogAdapter(viewModel.dogList) { position ->
            showDeleteDialog(requireContext(), position)
        }
        recyclerView.adapter = adapter
    }

    //삭제 다이얼로그
    private fun showDeleteDialog(context: Context, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("삭제 확인")
        builder.setMessage("이 항목을 삭제하시겠습니까?")
        builder.setPositiveButton("확인") { _: DialogInterface, _: Int ->
            val adapter = binding.dogCard.adapter as DogAdapter
            adapter.removeItem(position)
        }
        builder.setNegativeButton("취소") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        //다이얼로그 취소,확인 텍스트 색상변경
        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.setTextColor(context.resources.getColor(R.color.black))
            negativeButton.setTextColor(context.resources.getColor(R.color.red))
        }
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
