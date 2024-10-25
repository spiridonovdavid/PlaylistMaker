package com.example.playlistmaker.search.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.example.playlistmaker.App
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.search.model.Track
import com.example.playlistmaker.search.adapters.TrackAdapter
import com.example.playlistmaker.search.model.HistoryState
import com.example.playlistmaker.search.model.SearchState
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import com.example.playlistmaker.player.ui.PlayerActivity


class SearchActivity : AppCompatActivity() {

    private val searchHistoryInteractor = Creator.provideSearchHistoryInteractor()

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.provideFactory(
            Creator.provideTracksInteractor()
        )
    }

    private lateinit var searchHistoryAdapter: TrackAdapter
    private var tracks: MutableList<Track> = mutableListOf()

    private var searchText: String = ""

    private val handler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable {
        if (binding.searchInput.text.toString().isNotEmpty())
            viewModel.performSearch(binding.searchInput.text.toString())
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private var isClickAllowed = true

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setuptrackList()
        observeSearchState()
        observeHistoryState()
        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.searchInput.setOnFocusChangeListener { _, hasFocus ->
            val historyState = hasFocus && binding.searchInput.text.isEmpty()
            showHistory(historyState)
        }


        binding.clearButton.setOnClickListener {
            binding.searchInput.text.clear()
            hideKeyboard()
            binding.searchInput.clearFocus()
            clearAdapter()
            clearError()
            hideHistory(false)
        }

        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            val historyState = binding.searchInput.hasFocus() && text.isNullOrEmpty()
            clearError()
            showHistory(historyState)
            searchDebounce()

            binding.clearButton.isVisible = !text.isNullOrEmpty()
            searchText = text.toString()
        }

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.performSearch(binding.searchInput.text.toString())
                true
            } else {
                false
            }
        }

        binding.clearButton.isVisible = binding.searchInput.text.isNotEmpty()

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(KEY_SEARCH_TEXT, "")
            binding.searchInput.setText(searchText)
        }

        binding.trackList.layoutManager = LinearLayoutManager(this)

        binding.buttonUpdate.setOnClickListener {

            binding.errorLayout.isVisible = false
            binding.trackList.isVisible = true


            val query = binding.searchInput.text.toString()
            if (query.isNotEmpty()) {
                viewModel.performSearch(query)
            }
        }

        setuptrackList()
        updateSearchHistory()
        showHistory(false)

        binding.buttonHistoryClear.setOnClickListener {

            clearHistory()

            updateSearchHistory()
            showHistory(false)
            binding.searchInput.clearFocus()
        }

    }

    private fun setuptrackList() {
        searchHistoryAdapter = TrackAdapter(emptyList()) { track ->
            clickDebounce()
            onTrackClick(track)
        }
        binding.trackList.layoutManager = LinearLayoutManager(this)
        binding.trackList.adapter = searchHistoryAdapter
    }

    private fun observeSearchState() {
        viewModel.getSearchState().observe(this) { searchState ->
            hideErrors()
            when (searchState) {

                is SearchState.Loading -> {
                    binding.trackList.isVisible = false
                    binding.progressBar.isVisible = true
                    binding.textHistory.isVisible = false
                }

                is SearchState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.trackList.isVisible = true
                    updateTracks(searchState.tracks)
                }

                is SearchState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.trackList.isVisible = false
                    binding.errorLayout.isVisible = true
                    binding.errorText.text = getString(searchState.messageResId)
                }

                is SearchState.History -> {
                    showHistory(true)
                }
            }
        }
    }

    private fun observeHistoryState() {
        viewModel.getHistoryState().observe(this) { historyState ->
            when (historyState) {
                is HistoryState.ShowHistory -> {
                    searchHistoryAdapter.updateTracks(historyState.historyTracks)
                    binding.textHistory.isVisible = true
                }
                is HistoryState.HideHistory -> {
                    searchHistoryAdapter.updateTracks(emptyList())
                    binding.textHistory.isVisible = false
                }
            }
        }
    }


    private fun onTrackClick(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(App.TRACK_DATA, Gson().toJson(track))
        startActivity(intent)
        Handler(Looper.getMainLooper()).postDelayed({
            saveTrackToHistory(track)
            updateSearchHistory()
        }, 200)

    }

    private fun updateTracks(foundTracks: List<Track>) {
        tracks = foundTracks.toMutableList()
        binding.trackList.adapter = TrackAdapter(tracks) { track ->
            onTrackClick(track)
        }
    }

    private fun updateSearchHistory() {
        val history = searchHistoryInteractor.getHistory()
        searchHistoryAdapter.updateTracks(history)
    }

    private fun saveTrackToHistory(track: Track) {
        searchHistoryInteractor.saveTrack(track)
        updateSearchHistory()
    }

    private fun clearHistory() {
        searchHistoryInteractor.clearHistory()
        updateSearchHistory()
    }

    private fun showHistory(state: Boolean) {
        if (state) {
            hideHistory(true)
            setuptrackList()
            updateSearchHistory()
        } else {
            hideHistory(false)
            tracks.clear()
            binding.trackList.adapter = TrackAdapter(tracks){ track ->
                onTrackClick(track)
            }
        }
    }

    private fun clearError() {
        binding.errorLayout.isVisible = false
        binding.trackList.isVisible = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearAdapter() {
        tracks.clear()
        binding.trackList.adapter?.notifyDataSetChanged()
    }

    private fun hideHistory(state: Boolean) {
        var stateCount = state
        if (searchHistoryAdapter.itemCount <= 0) {
            stateCount = false
        }

        binding.textHistory.isVisible = stateCount
        binding.clearButton.isVisible = stateCount

    }

    private fun hideErrors() {
        binding.errorLayout.isVisible = false
        binding.trackList.isVisible = false
    }
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.clearButton.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(KEY_SEARCH_TEXT, "")
        binding.searchInput.setText(searchText)
        viewModel.performSearch(searchText)
    }

    companion object {
        private const val KEY_SEARCH_TEXT = "SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L

    }
}
