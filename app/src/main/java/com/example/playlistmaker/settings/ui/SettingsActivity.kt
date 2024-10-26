package com.example.playlistmaker.settings.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.model.ThemeSettings
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.provideFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isDarkThemeEnabled = settingsViewModel.getThemeSettings().isDarkTheme
        binding.switchtheme.isChecked = isDarkThemeEnabled

        binding.switchtheme.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.updateThemeSettings(ThemeSettings(isChecked))
        }

        settingsViewModel.themeChanged.observe(this) { _ -> }

        binding.shareBtn.setOnClickListener {
            settingsViewModel.shareApp()
        }

        binding.supportBtn.setOnClickListener {
            settingsViewModel.openSupport()
        }

        binding.policyBtn.setOnClickListener {
            settingsViewModel.openTerms()
        }

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}

