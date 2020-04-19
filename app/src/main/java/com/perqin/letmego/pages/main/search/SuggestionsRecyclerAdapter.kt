package com.perqin.letmego.pages.main.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.perqin.letmego.R
import kotlinx.android.synthetic.main.item_suggestion.view.*

class SuggestionsRecyclerAdapter : RecyclerView.Adapter<SuggestionsRecyclerAdapter.ViewHolder>() {
    var onSuggestionSelectedListener: ((Suggestion) -> Unit)? = null
    var suggestions = emptyList<Suggestion>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false))
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.suggestionTextView.text = suggestion.title
        holder.itemView.setOnClickListener {
            onSuggestionSelectedListener?.invoke(suggestion)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val suggestionTextView: TextView = itemView.suggestionTextView
    }
}

data class Suggestion(
        val title: String,
        val latitude: Double,
        val longitude: Double,
)
