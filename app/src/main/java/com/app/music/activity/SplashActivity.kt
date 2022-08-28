package com.app.music.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.app.music.R
import com.app.music.databinding.ActivitySlashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var m: ActivitySlashBinding

    ///////////////////////////////////////////////////////////////////////////
    // Override Methods
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m = DataBindingUtil.setContentView(this@SplashActivity, R.layout.activity_slash)
        init()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Custom Methods
    ///////////////////////////////////////////////////////////////////////////

    private fun init() {
        startsAppNormally()
    }

    private fun startsAppNormally() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, MusicActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000.toLong())
    }
}