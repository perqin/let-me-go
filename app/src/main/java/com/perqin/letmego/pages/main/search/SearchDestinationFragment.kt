package com.perqin.letmego.pages.main.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.perqin.letmego.R
import com.perqin.letmego.data.api.TencentLbsApi
import com.perqin.letmego.data.location.TencentLocator
import com.perqin.letmego.data.place.Place
import kotlinx.android.synthetic.main.fragment_search_destination.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchDestinationFragment : Fragment() {
    private lateinit var suggestionsRecyclerAdapter: SuggestionsRecyclerAdapter
    private var callback: Callback? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private var city = ""
    private val updateSuggestionsRunnable = Runnable {
        isSuggestionsUpdatePending = false
        val query = searchEditText.text.toString()
        lifecycleScope.launch {
            if (query.isEmpty()) {
                suggestionsRecyclerAdapter.suggestions = emptyList()
                return@launch
            }
            try {
                if (city == "") {
                    val location = TencentLocator.lastLocation?:return@launch
                    city = withContext(Dispatchers.IO) {
                        TencentLbsApi.getCurrentCity(location.latitude, location.longitude)
                    }
                }
                suggestionsRecyclerAdapter.suggestions = withContext(Dispatchers.IO) {
                    TencentLbsApi.getSuggestions(query, city)
                }
            } catch (e: Exception) {
                Log.w(TAG, "updateSuggestionsRunnable: $e")
                Toast.makeText(context, R.string.text_failToOperate, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private var isSuggestionsUpdatePending = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as Callback
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        suggestionsRecyclerAdapter = SuggestionsRecyclerAdapter()
        suggestionsRecyclerAdapter.onSuggestionSelectedListener = { suggestion ->
            callback?.locatePlaceAndClose(Place(suggestion.latitude, suggestion.longitude, suggestion.title))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_destination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton.setOnClickListener {
            callback?.closeSearch()
        }
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    fireSearchAndClose(searchEditText.text.toString())
                    true
                }
                else -> false
            }
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    clearButton.visibility = View.GONE
                } else {
                    clearButton.visibility = View.VISIBLE
                    updateSuggestionsAfterDelay()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        searchEditText.requestFocus()
        clearButton.setOnClickListener {
            searchEditText.text.clear()
        }
        suggestionsRecyclerView.adapter = suggestionsRecyclerAdapter
        suggestionsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        uiHandler.removeCallbacks(updateSuggestionsRunnable)
    }

    private fun updateSuggestionsAfterDelay() {
        if (isSuggestionsUpdatePending) {
            uiHandler.removeCallbacks(updateSuggestionsRunnable)
            uiHandler.postDelayed(updateSuggestionsRunnable, UPDATE_SUGGESTIONS_DELAY)
            return
        }
        isSuggestionsUpdatePending = true
        uiHandler.postDelayed(updateSuggestionsRunnable, UPDATE_SUGGESTIONS_DELAY)
    }

    private fun fireSearchAndClose(query: String) {
        lifecycleScope.launch {
            try {
                val location = TencentLocator.lastLocation?:return@launch
                val place = withContext(Dispatchers.IO) {
                    TencentLbsApi.searchPlace(query, location.latitude, location.longitude)
                }?:return@launch
                callback?.locatePlaceAndClose(place)
            } catch (e: Exception) {
                Log.w(TAG, "fireSearchAndClose: $e")
                Toast.makeText(context, R.string.text_failToOperate, Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface Callback {
        fun closeSearch()
        fun locatePlaceAndClose(place: Place)
    }

    companion object {
        private const val TAG = "SearchDestinationFragme"
        private const val UPDATE_SUGGESTIONS_DELAY = 300L
        fun newInstance(): SearchDestinationFragment = SearchDestinationFragment()
    }
}
