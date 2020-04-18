package com.perqin.letmego.ui.destinationlist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.perqin.letmego.R
import com.perqin.letmego.data.destination.Destination
import kotlinx.android.synthetic.main.destination_list_fragment.*
import kotlinx.coroutines.launch

class DestinationListFragment : BottomSheetDialogFragment() {
    private val viewModel: DestinationListViewModel by viewModels()
    private lateinit var recyclerAdapter: DestinationListRecyclerAdapter
    private var callback: Callback? = null

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
        recyclerAdapter = DestinationListRecyclerAdapter()
        recyclerAdapter.onDestinationClickListener = { destination ->
            callback?.onSelectDestination(destination)
            dismiss()
        }
        recyclerAdapter.onEditRemarkListener = { destination, newRemark ->
            lifecycleScope.launch {
                try {
                    viewModel.updateRemarkOfDestination(destination, newRemark)
                } catch (e: Exception) {
                    Log.w(TAG, "onEditRemarkListener: $e")
                    Toast.makeText(context, R.string.text_failToOperate, Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerAdapter.onDeleteDestinationListener = { destination ->
            lifecycleScope.launch {
                try {
                    viewModel.deleteDestination(destination)
                } catch (e: Exception) {
                    Log.w(TAG, "onDeleteDestinationListener: $e")
                    Toast.makeText(context, R.string.text_failToOperate, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                (it as BottomSheetDialog).apply {
                    behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)!!.updateLayoutParams {
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.destination_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.destinations.observe(this, Observer {
            if (it.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                recyclerAdapter.destinations = it
            }
        })
    }

    interface Callback {
        fun onSelectDestination(destination: Destination)
    }

    companion object {
        private const val TAG = "DestinationListFragment"

        fun newInstance() = DestinationListFragment()
    }
}
