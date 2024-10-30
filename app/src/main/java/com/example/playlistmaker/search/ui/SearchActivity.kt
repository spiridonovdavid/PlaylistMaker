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
import androidx.core.widget.doOnTextChanged
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.search.model.Track
import com.example.playlistmaker.search.adapters.TrackAdapter
import com.example.playlistmaker.search.model.SearchScreenState
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import com.example.playlistmaker.player.ui.PlayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private val viewModel by viewModel<SearchViewModel>()
    private lateinit var binding: ActivitySearchBinding
    private lateinit var trackAdapter: TrackAdapter
    private var searchText: String = ""
    private val handler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable {
        if (binding.searchInput.text.toString().isNotEmpty()) {
            viewModel.performSearch(binding.searchInput.text.toString())
        }
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

        trackAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) onTrackClick(track)
        }

        binding.trackList.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = trackAdapter
        }

        observeViewModel()

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.searchInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchInput.text.isEmpty()) {
                viewModel.updateSearchHistory()
            }
        }

        binding.clearButton.setOnClickListener {
            binding.searchInput.text.clear()
            hideKeyboard()
            binding.searchInput.clearFocus()
            clearAdapter()
            clearError()
            hideHistory()
            binding.clearButton.isVisible = false
        }

        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            searchDebounce()
            binding.clearButton.isVisible = !text.isNullOrEmpty()
            searchText = text.toString()

            if (text.isNullOrEmpty()) {
                clearError()
                viewModel.updateSearchHistory()
                trackAdapter.updateTracks(emptyList())
            }
        }

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.performSearch(binding.searchInput.text.toString())
                true
            } else {
                false
            }
        }

        binding.buttonUpdate.setOnClickListener {
            binding.errorLayout.isVisible = false
            binding.trackList.isVisible = true
            val query = binding.searchInput.text.toString()
            if (query.isNotEmpty()) {
                viewModel.performSearch(query)
            }
        }

        binding.buttonHistoryClear.setOnClickListener {
            viewModel.clearHistory()
        }

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(KEY_SEARCH_TEXT, "")
            binding.searchInput.setText(searchText)
        } else {
            binding.clearButton.isVisible = false
        }
    }

    private fun observeViewModel() {
        viewModel.getScreenState().observe(this) { state ->
            when (state) {
                is SearchScreenState.Loading -> showLoading()
                is SearchScreenState.ShowSearchResults -> showTracks(state.tracks)
                is SearchScreenState.ShowHistory -> showHistory(state.historyTracks)
                is SearchScreenState.Error -> showError(state.messageResId)
                is SearchScreenState.Empty -> hideHistory()
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.trackList.isVisible = false
        binding.errorLayout.isVisible = false
        binding.buttonHistoryClear.isVisible = false
        binding.textHistory.isVisible = false
    }

    private fun showTracks(tracks: List<Track>) {
        binding.progressBar.isVisible = false
        binding.trackList.isVisible = true
        binding.textHistory.isVisible = false
        binding.buttonHistoryClear.isVisible = false
        trackAdapter.updateTracks(tracks)
    }

    private fun showError(messageResId: Int) {
        binding.progressBar.isVisible = false
        binding.trackList.isVisible = false
        binding.errorLayout.isVisible = true
        binding.errorText.text = getString(messageResId)
        binding.buttonHistoryClear.isVisible = false


        val errorImageResId = when (messageResId) {
            R.string.error_internet -> R.drawable.error_internet
            R.string.error_not_found -> R.drawable.error_notfound
            else -> R.drawable.error_notfound
        }
        binding.errorImage.setImageResource(errorImageResId)
    }

    private fun showHistory(historyTracks: List<Track>) {
        trackAdapter.updateTracks(historyTracks)
        binding.textHistory.isVisible = true
        binding.buttonHistoryClear.isVisible = true
        binding.progressBar.isVisible = false
        binding.trackList.isVisible = true
    }

    private fun hideHistory() {
        trackAdapter.updateTracks(emptyList())
        binding.textHistory.isVisible = false
        binding.buttonHistoryClear.isVisible = false
    }

    private fun clearError() {
        binding.errorLayout.isVisible = false
        binding.trackList.isVisible = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearAdapter() {
        trackAdapter.updateTracks(emptyList())
        trackAdapter.notifyDataSetChanged()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.clearButton.windowToken, 0)
    }

    private fun onTrackClick(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(App.TRACK_DATA, Gson().toJson(track))
        startActivity(intent)
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.saveTrackToHistory(track)
        }, 200)
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

    override fun onResume() {
        super.onResume()
        if (searchText.isNotEmpty()) {
            viewModel.restoreLastSearchResult()
        }
    }

    companion object {
        private const val KEY_SEARCH_TEXT = "SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}