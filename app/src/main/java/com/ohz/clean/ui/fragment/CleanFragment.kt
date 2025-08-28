package com.ohz.clean.ui.fragment

import androidx.fragment.app.viewModels
import com.ohz.clean.R
import com.ohz.clean.databinding.FragmentCleanBinding
import com.ohz.clean.ui.base.Fragment3
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.sdmse.common.viewbinding.viewBinding

/**
 * @description
 * @version
 */

@AndroidEntryPoint
class CleanFragment: Fragment3(R.layout.fragment_clean) {

    override val ui: FragmentCleanBinding by viewBinding()
    override val vm: MainViewModel by viewModels()


}