package com.footprint.app.ui.mypage
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMyPostBinding
import com.footprint.app.databinding.FragmentMypageBinding

class MyPostFragment : Fragment(R.layout.fragment_my_post) {

    private var _binding: FragmentMyPostBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMyPostBinding.bind(view)

        backPage()

    }

    private fun backPage(){
        binding.postPageBack.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }
}
