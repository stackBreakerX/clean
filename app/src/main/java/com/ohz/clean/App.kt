package com.ohz.clean

import android.app.Application
import com.ohz.clean.coil.CoilTempFiles
import com.ohz.clean.common.memory.MemoryMonitor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * @description
 * @version
 */

@HiltAndroidApp
class App : Application() {

//    @Inject lateinit var coilTempFiles: CoilTempFiles
//    @Inject lateinit var memoryMonitor: MemoryMonitor


    override fun onCreate() {
        super.onCreate()

//        memoryMonitor.register()

    }

}