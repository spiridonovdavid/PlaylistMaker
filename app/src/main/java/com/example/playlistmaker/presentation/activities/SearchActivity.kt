package com.example.playlistmaker.presentation.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import com.example.playlistmaker.App
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.ui.player.PlayerActivity
import java.io.IOException

class SearchActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var clearSearchButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var errorImage: ImageView
    private lateinit var errorText: TextView
    private lateinit var errorContainer: LinearLayout
    private lateinit var retrySearchButton: TextView
    private lateinit var historyTitleLabel: TextView
    private lateinit var clearHistoryLabel: TextView
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var progressBar: ProgressBar
    private val tracksInteractor = Creator.provideTracksInteractor()
    private val searchHistoryInteractor = Creator.provideSearchHistoryInteractor()
    private var trackList: MutableList<Track>? = mutableListOf()
    private var searchText: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backButton = findViewById<View>(R.id.back)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        clearSearchButton = findViewById(R.id.clearButton)
        errorImage = findViewById(R.id.error_image)
        errorText = findViewById(R.id.error_text)
        errorContainer = findViewById(R.id.error_layout)
        retrySearchButton = findViewById(R.id.button_update)
        searchInput = findViewById(R.id.searchInput)
        historyTitleLabel = findViewById(R.id.text_history)
        clearHistoryLabel = findViewById(R.id.button_history_clear)
        recyclerView = findViewById(R.id.trackList)
        progressBar = findViewById(R.id.progressBar)

        searchInput.setOnFocusChangeListener { _, hasFocus ->
            val historyState = hasFocus && searchInput.text.isEmpty()
            showHistory(historyState)
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearSearchButton.isVisible = !s.isNullOrEmpty()
                searchText = s.toString()
                if (s != null) {
                    showHistory(searchInput.hasFocus() && s.isEmpty())
                }
                debounce(debounceRunnable, SEARCH_DEBOUNCE_DELAY)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        clearSearchButton.setOnClickListener {
            searchInput.text.clear()
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(clearSearchButton.windowToken, 0)
            searchInput.clearFocus()
            clearAdapter()
            clearError()
            hideHistory(false)
        }

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                initSearch(searchInput.text.toString())
                true
            } else {
                false
            }
        }

        clearSearchButton.isVisible = searchInput.text.isNotEmpty()

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(KEY_SEARCH_TEXT, "")
            searchInput.setText(searchText)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        retrySearchButton.setOnClickListener {
            errorContainer.isVisible = false
            recyclerView.isVisible = true
            val query = searchInput.text.toString()
            if (query.isNotEmpty()) {
                initSearch(query)
            }
        }

        setupRecyclerView()
        updateSearchHistory()
        showHistory(false)

        clearHistoryLabel.setOnClickListener {
            clearHistory()
            updateSearchHistory()
            showHistory(false)
            searchInput.clearFocus()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                onTrackClick(track)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = historyAdapter
    }

    private fun onTrackClick(track: Track) {
        saveTrackToHistory(track)
        updateSearchHistory()
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(App.TRACK_DATA, Gson().toJson(track))
        startActivity(intent)
    }

    private fun initSearch(query: String) {
        if (query.isNotEmpty()) {
            errorContainer.isVisible = false
            recyclerView.isVisible = false
            progressBar.isVisible = true

            tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>) {
                    runOnUiThread {
                        progressBar.isVisible = false
                        if (foundTracks.isNotEmpty()) {
                            recyclerView.isVisible = true
                            updateTracks(foundTracks)
                        } else {
                            showError(false)
                        }
                    }
                }
            }) { throwable ->
                runOnUiThread {
                    progressBar.isVisible = false
                    showError(throwable is IOException)
                }
            }
        }
    }

    private fun updateTracks(foundTracks: List<Track>) {
        trackList = foundTracks.toMutableList()
        recyclerView.adapter = TrackAdapter(trackList!!) { track ->
            onTrackClick(track)
        }
    }

    private fun updateSearchHistory() {
        val history = searchHistoryInteractor.getHistory()
        historyAdapter.updateTracks(history)
    }

    private fun saveTrackToHistory(track: Track) {
        searchHistoryInteractor.saveTrack(track)
        updateSearchHistory()
    }

    private fun clearHistory() {
        searchHistoryInteractor.clearHistory()
        updateSearchHistory()
    }

    private fun showError(isNetworkError: Boolean) {
        errorContainer.isVisible = true
        recyclerView.isVisible = false
        clearAdapter()
        retrySearchButton.isVisible = isNetworkError
        errorText.setText(if (isNetworkError) R.string.error_internet else R.string.error_not_found)
        errorImage.setImageResource(if (isNetworkError) R.drawable.error_internet else R.drawable.error_notfound)
    }

    private fun showHistory(state: Boolean) {
        if (state) {
            hideHistory(true)
            setupRecyclerView()
            updateSearchHistory()
        } else {
            hideHistory(false)
            trackList?.clear()
            recyclerView.adapter = TrackAdapter(trackList ?: emptyList()) { track ->
                onTrackClick(track)
            }
        }
    }

    private fun clearError() {
        errorContainer.isVisible = false
        recyclerView.isVisible = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearAdapter() {
        trackList?.clear()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun hideHistory(state: Boolean) {
        val historyVisible = if (historyAdapter.itemCount <= 0) false else state
        historyTitleLabel.isVisible = historyVisible
        clearHistoryLabel.isVisible = historyVisible
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(KEY_SEARCH_TEXT, "")
        searchInput.setText(searchText)
        initSearch(searchText)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val debounceRunnable = Runnable { initSearch(searchText) }

    private fun debounce(action: Runnable, delay: Long) {
        handler.removeCallbacks(action)
        handler.postDelayed(action, delay)
    }

    private var isClickAllowed = true

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    companion object {
        private const val KEY_SEARCH_TEXT = "SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
