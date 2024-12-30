package com.example.playlistmaker.search.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.search.model.Track
import com.example.playlistmaker.search.adapters.TrackAdapter
import com.example.playlistmaker.search.model.SearchScreenState
import com.example.playlistmaker.search.view_model.SearchViewModel
import com.example.playlistmaker.utils.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<SearchViewModel>()

    private lateinit var trackAdapter: TrackAdapter
    private var searchText: String = ""

    private lateinit var trackClickDebounce: (Track) -> Unit
    private lateinit var trackSearchDebounce: (Unit) -> Unit


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackClickDebounce = debounce(SEARCH_DEBOUNCE_DELAY, requireActivity().lifecycleScope, false) {
            viewModel.saveTrackToHistory(it)
        }

        trackSearchDebounce = debounce(CLICK_DEBOUNCE_DELAY, requireActivity().lifecycleScope, false) {
            viewModel.performSearch(binding.searchInput.text.toString())
        }

        trackAdapter = TrackAdapter(
            emptyList(),
            onTrackClick = { track -> onTrackClick(track) }
        )

        binding.trackList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }

        observeViewModel()

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

            trackSearchDebounce(Unit)

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
            viewModel.restoreLastState()
        } else {
            binding.clearButton.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewModel.getScreenState().observe(viewLifecycleOwner) { state ->
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
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.clearButton.windowToken, 0)
    }

    private fun onTrackClick(track: Track) {
        trackClickDebounce(track)
        val action = SearchFragmentDirections.actionSearchFragmentToPlayerFragment(track)
        findNavController().navigate(action)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }

    override fun onResume() {
        super.onResume()
        viewModel.restoreLastState()
    }

    companion object {
        private const val KEY_SEARCH_TEXT = "SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
