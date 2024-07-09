package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class SearchActivity : AppCompatActivity() {

    private var inputView: EditText? = null
    private var inputValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backButton = findViewById<View>(R.id.back)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val clearButton = findViewById<ImageView>(R.id.clearButton)

        clearButton.isVisible = false
        inputView = findViewById(R.id.inputView)

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButtonVisibility(clearButton, s)
                inputValue = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        inputView?.addTextChangedListener(simpleTextWatcher)

        clearButton.setOnClickListener {
            inputView?.setText("")
            val hideKeyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideKeyboard.hideSoftInputFromWindow(inputView?.windowToken, 0)
            inputView?.clearFocus()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STRING_VALUE, inputValue)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        inputValue = savedInstanceState.getString(STRING_VALUE, STRING_DEFAULT)
        inputView?.setText(inputValue)
    }

    private fun clearButtonVisibility(view: View, s: CharSequence?) {
        view.isVisible = !s.isNullOrEmpty()
    }

    companion object {
        private const val STRING_VALUE = "PRODUCT_AMOUNT"
        private const val STRING_DEFAULT = ""
    }
}
