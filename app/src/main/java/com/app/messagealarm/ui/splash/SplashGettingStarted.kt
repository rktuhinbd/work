package com.app.messagealarm.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.app.messagealarm.R
import com.app.messagealarm.ui.terms_privacy.TermsPrivacyActivity

class SplashGettingStarted : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_getting_started)

        val button = findViewById<Button>(R.id.btn_get_started)

        button?.setOnClickListener()
        {
            // displaying a toast message
            val intent = Intent(this, TermsPrivacyActivity::class.java)

            startActivity(intent)
    }

    }
}