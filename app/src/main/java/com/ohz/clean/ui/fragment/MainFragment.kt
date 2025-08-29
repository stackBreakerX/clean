package com.ohz.clean.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.ohz.clean.MainDirections
import com.ohz.clean.R
import com.ohz.clean.common.EdgeToEdgeHelper
import com.ohz.clean.databinding.FragmentMainBinding
import com.ohz.clean.ui.base.Fragment3
import com.ohz.clean.utils.StorageUtil
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.sdmse.common.viewbinding.viewBinding
import kotlin.getValue

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class MainFragment : Fragment3(R.layout.fragment_main) {
    private var param1: String? = null
    private var param2: String? = null

    override val ui: FragmentMainBinding by viewBinding()
    override val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EdgeToEdgeHelper(requireActivity()).apply {
            insetsPadding(ui.root, left = true, right = true, bottom = true, top = true)
        }

        val info = StorageUtil.getTotalStorageInfo(requireActivity())
        Log.d("$tag Storage", "总容量: ${info.totalSize / (1024*1024*1024)} GB")
        Log.d("$tag", "已使用: ${info.usedSize / (1024*1024*1024)} GB")
        Log.d("$tag", "可用: ${info.freeSize / (1024*1024*1024)} GB")

        val progress = (info.usedSize.toFloat().div(info.totalSize) * 100).toInt()
        ui.graphCaption.text = progress.toString() + "%"

        val totalSize = "${info.totalSize / (1024*1024*1024)} GB"
        val usedSize = "${info.usedSize / (1024*1024*1024)} GB"
        val freeSize = "${info.freeSize / (1024*1024*1024)} GB"

        ui.available.text = "$freeSize 可用空间"
        ui.capacity.text = "$usedSize / $totalSize"

        ui.progress.progress = progress

        vm.state.observe2(ui) { state ->
            Log.d(tag, "onViewCreated() called with: state = $state")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

}