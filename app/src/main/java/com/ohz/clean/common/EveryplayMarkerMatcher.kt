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
class EveryplayMarkerMatcher @Inject constructor() : NestedPackageMatcher(
    DataArea.Type.SDCARD,
    listOf(".EveryplayCache"),
    setOf(".nomedia", "images", "videos")
) {
    override fun toString(): String = "EveryplayMarkerSource"

    @Module @InstallIn(SingletonComponent::class)
    abstract class DIM {
        @Binds @IntoSet abstract fun source(source: EveryplayMarkerMatcher): MarkerSource
    }
}