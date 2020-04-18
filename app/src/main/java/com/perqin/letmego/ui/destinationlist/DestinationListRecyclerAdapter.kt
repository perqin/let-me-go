package com.perqin.letmego.ui.destinationlist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.perqin.letmego.R
import com.perqin.letmego.data.destination.Destination
import kotlinx.android.synthetic.main.destination_list_item.view.*
import kotlinx.android.synthetic.main.dialog_edit_remark.view.*

/**
 * Created on 7/21/18.
 *
 * @author perqin
 */
class DestinationListRecyclerAdapter : RecyclerView.Adapter<DestinationListRecyclerAdapter.ViewHolder>() {
    var onDestinationClickListener: ((Destination) -> Unit)? = null
    var onEditRemarkListener: ((Destination, String) -> Unit)? = null
    var onDeleteDestinationListener: ((Destination) -> Unit)? = null
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
        val context = holder.itemView.context
        val destination = destinations[position]
        holder.remarkTextView.text = destination.displayName
        holder.addressTextView.text = destination.address
        holder.itemView.setOnClickListener {
            onDestinationClickListener?.invoke(destination)
        }
        holder.menuButton.setOnClickListener { button ->
            PopupMenu(context, button).apply {
                menuInflater.inflate(R.menu.popup_destination, menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.editRemarkItem -> {
                            val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_remark, null)
                            val remarkEditText = view.remarkEditText
                            remarkEditText.setText(destination.displayName)
                            AlertDialog.Builder(context)
                                    .setTitle(R.string.label_editRemark)
                                    .setView(view)
                                    .setPositiveButton(R.string.label_save) { _, _ ->
                                        val newRemark = remarkEditText.text?.toString()?:return@setPositiveButton
                                        onEditRemarkListener?.invoke(destination, newRemark)
                                    }
                                    .setNegativeButton(R.string.label_cancel, null)
                                    .show()
                            true
                        }
                        R.id.copyIdItem -> {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val id = destination.id?.toString().orEmpty()
                            val clip = ClipData.newPlainText(context.getString(R.string.label_destinationId), id)
                            cm.setPrimaryClip(clip)
                            true
                        }
                        R.id.deleteItem -> {
                            onDeleteDestinationListener?.invoke(destination)
                            true
                        }
                        else -> false
                    }
                }
            }.show()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val remarkTextView: TextView = itemView.remarkTextView
        val addressTextView: TextView = itemView.addressTextView
        val menuButton: ImageButton = itemView.menuButton
    }
}
