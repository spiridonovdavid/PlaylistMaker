package com.example.playlistmaker.presentation.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.SettingsInteractor
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsInteractor: SettingsInteractor
    private var isDarkThemeEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsInteractor = Creator.provideSettingsInteractor()
        isDarkThemeEnabled = settingsInteractor.isDarkThemeEnabled()

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.switchtheme)
        themeSwitcher.isChecked = isDarkThemeEnabled

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != isDarkThemeEnabled) {
                isDarkThemeEnabled = isChecked
                settingsInteractor.switchTheme(isChecked)
                switchTheme(isChecked)
            }
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

    private fun switchTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

}