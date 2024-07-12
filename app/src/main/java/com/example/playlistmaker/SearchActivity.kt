package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.ItunesApi
import com.practicum.playlistmaker.ItunesDataModel
import com.practicum.playlistmaker.Track
import com.practicum.playlistmaker.TrackAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivity : AppCompatActivity() {

    private var inputView: EditText? = null
    private var inputValue: String = ""
    private var recycler: RecyclerView? = null
    private lateinit var errorLayout: LinearLayout
    private lateinit var errorImage: ImageView
    private lateinit var errorText: TextView
    private lateinit var buttonUpdate: Button
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

        inputView = findViewById(R.id.inputView)
        recycler = findViewById(R.id.trackList)
        errorLayout = findViewById(R.id.error_layout)
        errorImage = findViewById(R.id.error_image)
        errorText = findViewById(R.id.error_text)
        buttonUpdate = findViewById(R.id.button_update)

        buttonUpdate.setOnClickListener{
            initSearch()
            showError(false, true)
        }

        val backButton = findViewById<View>(R.id.back)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val clearButton = findViewById<ImageView>(R.id.clearButton)
        clearButton.isVisible = false
        clearButton.setOnClickListener {
            inputView?.setText("")
            val hideKeyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideKeyboard.hideSoftInputFromWindow(inputView?.windowToken, 0)
            inputView?.clearFocus()
            clearAdapter()
            errorLayout.isVisible = false
            hasError = false
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButtonVisibility(clearButton, s)
                inputValue = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        inputView?.addTextChangedListener(simpleTextWatcher)

        recycler?.layoutManager = LinearLayoutManager(this)
        inputView?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                initSearch()
                true
            }
            false
        }
    }

    private fun showError(show: Boolean, network: Boolean) {
        if (show) {
            errorLayout.isVisible = true
            recycler?.isVisible = false
            clearAdapter()
            hasError = true
            isNetworkError = true
            if (network) {
                buttonUpdate.isVisible = false
                errorText.setText(R.string.error_not_found)
                errorImage.setImageResource(R.drawable.error_notfound)
                isNetworkError = true
            } else {
                buttonUpdate.isVisible = true
                errorText.setText(R.string.error_internet)
                errorImage.setImageResource(R.drawable.error_internet)
                isNetworkError = false
            }

        } else {
            hasError = false
            isNetworkError = true
            errorLayout.isVisible = false
            recycler?.isVisible = true
        }
    }

    private fun initSearch() {
        iTunesApi.search(inputValue).enqueue(object : retrofit2.Callback<ItunesDataModel> {
            override fun onResponse(call: retrofit2.Call<ItunesDataModel>, response: retrofit2.Response<ItunesDataModel>) {
                if (response.isSuccessful) {
                    tracks = response.body()?.results
                    if (tracks != null) {
                        if (tracks!!.isNotEmpty()) {
                            recycler?.adapter = TrackAdapter(tracks!!)
                            showError(false, true)
                        } else {
                            showError(true, true)
                        }
                    }
                } else {
                    showError(true, false)
                }
            }

            override fun onFailure(call: retrofit2.Call<ItunesDataModel>, t: Throwable) {
                showError(true, false)
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearAdapter() {
        tracks?.clear()
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
        inputView?.setText(inputValue)

        tracks = savedInstanceState.getParcelableArrayList(SEARCH_RESULTS)
        hasError = savedInstanceState.getBoolean(HAS_ERROR, false)
        isNetworkError = savedInstanceState.getBoolean(IS_NETWORK_ERROR, true)

        if (hasError) {
            showError(true, isNetworkError)
        } else {
            recycler?.adapter = TrackAdapter(tracks ?: mutableListOf())
            showError(false, true)
        }
    }


    private fun clearButtonVisibility(view: View, s: CharSequence?) {
        view.isVisible = !s.isNullOrEmpty()
    }

    companion object {
        private const val STRING_VALUE = "SEARCH_QUERY"
        private const val STRING_DEFAULT = ""
        private const val SEARCH_RESULTS = "SEARCH_RESULTS"
        private const val HAS_ERROR = "HAS_ERROR"
        private const val IS_NETWORK_ERROR = "IS_NETWORK_ERROR"
    }
}
