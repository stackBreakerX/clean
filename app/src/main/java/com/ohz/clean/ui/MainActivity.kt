package com.ohz.clean.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ohz.clean.databinding.ActivityMainBinding
import com.ohz.clean.ui.base.BaseActivity

class MainActivity : BaseActivity() {


    private lateinit var ui: ActivityMainBinding
    var showSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition { showSplashScreen && savedInstanceState == null }

        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)


        showSplashScreen = false

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(B_KEY_SPLASH, showSplashScreen)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val B_KEY_SPLASH = "showSplashScreen"
    }
}