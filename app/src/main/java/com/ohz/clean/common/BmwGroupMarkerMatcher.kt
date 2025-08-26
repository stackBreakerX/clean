package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject

@Reusable
class BmwGroupMarkerMatcher @Inject constructor() : NestedPackageMatcher(
    DataArea.Type.SDCARD,
    listOf("bmwgroup"),
    setOf(".nomedia")
) {
    override fun toString(): String = "BmwGroupMarkerSource"

    @Module @InstallIn(SingletonComponent::class)
    abstract class DIM {
        @Binds @IntoSet abstract fun source(source: BmwGroupMarkerMatcher): MarkerSource
    }
}