package com.ohz.clean.ui

import android.app.Application
import com.ohz.clean.ui.coil.CoilTempFiles
import com.ohz.clean.ui.common.memory.MemoryMonitor
import javax.inject.Inject

/**
 * @description
 * @version
 */
class App : Application() {

    @Inject lateinit var coilTempFiles: CoilTempFiles
    @Inject lateinit var memoryMonitor: MemoryMonitor


    override fun onCreate() {
        super.onCreate()
    }

}