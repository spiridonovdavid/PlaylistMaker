package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val appContext = (applicationContext as App)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.switchtheme)

        themeSwitcher.isChecked = appContext.darkTheme
        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            appContext.switchTheme(checked)
            appContext.sharedPrefs?.edit()
                ?.putBoolean(THEME_MODE, checked)
                ?.apply()
        }

        val backButton = findViewById<View>(R.id.back)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val shareButton = findViewById<View>(R.id.share_btn)
        shareButton.setOnClickListener {
            val message = getString(R.string.share_text)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(shareIntent)
        }

        val supportButton = findViewById<View>(R.id.support_btn)
        supportButton.setOnClickListener {
            val email = getString(R.string.email)
            val text = getString(R.string.email_text)
            val subject = getString(R.string.email_subject)

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            intent.putExtra(Intent.EXTRA_TEXT, text)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            startActivity(intent)
        }

        val policyButton = findViewById<View>(R.id.policy_btn)
        policyButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.policy_text))))
        }

    }

}