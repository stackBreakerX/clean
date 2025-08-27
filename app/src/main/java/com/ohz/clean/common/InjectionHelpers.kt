package com.ohz.clean.common

import android.app.Service
import com.ohz.clean.App
import dagger.hilt.internal.GeneratedComponentManager

fun Service.isValidAndroidEntryPoint(): Boolean {
    return application is GeneratedComponentManager<*> || application is App
}