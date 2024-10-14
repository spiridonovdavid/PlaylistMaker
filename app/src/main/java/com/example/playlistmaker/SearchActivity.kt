package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivity : AppCompatActivity() {

    private var searchInput: EditText? = null
    private var inputValue: String = ""
    private var recycler: RecyclerView? = null
    private var errorLayout: LinearLayout? = null
    private var errorImage: ImageView? = null
    private var errorText: TextView? = null
    private var buttonUpdate: Button? = null
    private var historyText: TextView? = null
    private var historyClear: Button? = null
    private var progressBar: ProgressBar? = null
    private val emptyList: MutableList<Track> = mutableListOf()
    private var historyTrack: MutableList<Track>? = mutableListOf()
    private var tracks: MutableList<Track>? = mutableListOf()
    private var baseUrlITunes = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrlITunes)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val iTunesApi = retrofit.create(ItunesApi::class.java)

    private var hasError = false
    private var isNetworkError = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchInput = findViewById(R.id.searchInput)
        recycler = findViewById(R.id.trackList)
        errorLayout = findViewById(R.id.error_layout)
        errorImage = findViewById(R.id.error_image)
        errorText = findViewById(R.id.error_text)
        buttonUpdate = findViewById(R.id.button_update)
        historyText = findViewById(R.id.text_history)
        historyClear = findViewById(R.id.button_history_clear)
        progressBar = findViewById(R.id.progressBar)

        buttonUpdate?.setOnClickListener {
            initSearch()
            clearError()
        }

        historyClear?.setOnClickListener {
            clearHistory((applicationContext as App))
            hideHistoryElements()
        }

        val backButton = findViewById<View>(R.id.back)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val clearButton = findViewById<ImageView>(R.id.clearButton)
        clearButton.isVisible = false
        clearButton.setOnClickListener {
            hideKeyboard()
            searchInput?.setText("")
            searchInput?.clearFocus()
            errorLayout?.isVisible = false
            hasError = false
            clearAdapter()

        }

        searchInput?.setOnFocusChangeListener { view, hasFocus ->
            val historyActive = hasFocus && searchInput!!.text.isEmpty()
            showHistory(historyActive)
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setButtonVisibility(clearButton, s)
                inputValue = s.toString()
                showHistory(searchInput?.hasFocus() == true && s.isNullOrEmpty())
                searchDebounce()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        searchInput?.addTextChangedListener(simpleTextWatcher)

        recycler?.layoutManager = LinearLayoutManager(this)
        searchInput?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                initSearch()
                hideHistoryElements()
                true
            }
            false
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput?.windowToken, 0)
    }

    private fun showNotFoundError() {
        errorLayout?.isVisible = true
        recycler?.isVisible = false
        hasError = true
        buttonUpdate?.isVisible = false
        errorText?.setText(R.string.error_not_found)
        errorImage?.setImageResource(R.drawable.error_notfound)
        isNetworkError = true
    }

    private fun showNetworkError() {
        errorLayout?.isVisible = true
        recycler?.isVisible = false
        hasError = true
        isNetworkError = true
        buttonUpdate?.isVisible = true
        errorText?.setText(R.string.error_internet)
        errorImage?.setImageResource(R.drawable.error_internet)
        isNetworkError = false
    }

    private fun clearError() {
        hasError = false
        isNetworkError = true
        errorLayout?.isVisible = false
        recycler?.isVisible = true
    }

    private fun initSearch() {
        progressBar?.visibility = View.VISIBLE
        clearError()
        iTunesApi.search(inputValue).enqueue(object : retrofit2.Callback<ItunesDataModel> {
            override fun onResponse(
                call: retrofit2.Call<ItunesDataModel>,
                response: retrofit2.Response<ItunesDataModel>
            ) {
                progressBar?.visibility = View.GONE
                if (response.isSuccessful) {
                    tracks = response.body()?.results
                    if (!tracks.isNullOrEmpty()) {
                        recycler?.adapter = TrackAdapter(tracks!!)
                        clearError()
                    } else {
                        showNotFoundError()
                    }
                } else {
                    showNetworkError()
                }

            }

            override fun onFailure(call: retrofit2.Call<ItunesDataModel>, t: Throwable) {
                progressBar?.visibility = View.GONE
                showNetworkError()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearAdapter() {
        recycler?.adapter = TrackAdapter(emptyList)
        recycler?.adapter?.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STRING_VALUE, inputValue)
        outState.putParcelableArrayList(SEARCH_RESULTS, ArrayList(tracks))
        outState.putBoolean(HAS_ERROR, hasError)
        outState.putBoolean(IS_NETWORK_ERROR, isNetworkError)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        inputValue = savedInstanceState.getString(STRING_VALUE, STRING_DEFAULT)
        searchInput?.setText(inputValue)

        tracks = savedInstanceState.getParcelableArrayList(SEARCH_RESULTS)
        hasError = savedInstanceState.getBoolean(HAS_ERROR, false)
        isNetworkError = savedInstanceState.getBoolean(IS_NETWORK_ERROR, true)

        if (hasError && isNetworkError) {
            showNotFoundError()
        } else if (hasError && !isNetworkError) {
            showNetworkError()
        } else {
            recycler?.adapter = TrackAdapter(tracks ?: ArrayList())
            clearError()
        }
    }


    private fun setButtonVisibility(view: View, s: CharSequence?) {
        view.isVisible = !s.isNullOrEmpty()
    }

    private fun adapterInit(adapterListTracks: MutableList<Track>?) {
        clearAdapter()
        tracks = adapterListTracks
        recycler?.adapter = TrackAdapter(tracks!!)
        recycler?.isVisible = true
    }

    private fun showHistory(isActive: Boolean) {

        if (isActive) {

            historyTrack = getHistorySearch((applicationContext as App))

            if (historyTrack?.isNotEmpty() == true) {
                historyText?.isVisible = true
                historyClear?.isVisible = true
                adapterInit(historyTrack)
            } else {
                hideHistoryElements()
            }

        } else {
            hideHistoryElements()
        }

    }

    private fun hideHistoryElements() {
        historyText?.isVisible = false
        historyClear?.isVisible = false
        clearAdapter()
    }

    private val searchRunnable = Runnable { initSearch() }
    private val handler = Handler(Looper.getMainLooper())

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private companion object {
        const val STRING_VALUE = "SEARCH_QUERY"
        const val STRING_DEFAULT = ""
        const val SEARCH_RESULTS = "SEARCH_RESULTS"
        const val HAS_ERROR = "HAS_ERROR"
        const val IS_NETWORK_ERROR = "IS_NETWORK_ERROR"
        const val SEARCH_DEBOUNCE_DELAY = 2000L

    }
}
