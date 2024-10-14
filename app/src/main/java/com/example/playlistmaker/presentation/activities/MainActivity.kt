package com.example.playlistmaker.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSearch = findViewById<Button>(R.id.button_search)
        val buttonMedia = findViewById<Button>(R.id.button_media)
        val buttonSettings = findViewById<Button>(R.id.button_settings)

        buttonSearch.setOnClickListener {
            val intentSearch = Intent(this, SearchActivity::class.java)
            startActivity(intentSearch)
        }

        buttonMedia.setOnClickListener {
            val intentMusic = Intent(this, MediaActivity::class.java)
            startActivity(intentMusic)
        }

        buttonSettings.setOnClickListener {
            val intentSetting = Intent(this, SettingsActivity::class.java)
            startActivity(intentSetting)
        }
    }
}