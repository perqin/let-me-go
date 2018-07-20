package com.perqin.letmego.ui.destinationlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.perqin.letmego.R
import com.perqin.letmego.data.destination.Destination

/**
 * Created on 7/21/18.
 *
 * @author perqin
 */
class DestinationListRecyclerAdapter : RecyclerView.Adapter<DestinationListRecyclerAdapter.ViewHolder>() {
    var destinations: List<Destination> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.destination_list_item, parent, false))
    }

    override fun getItemCount() = destinations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
