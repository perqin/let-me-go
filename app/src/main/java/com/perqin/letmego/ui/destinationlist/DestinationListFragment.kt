package com.perqin.letmego.ui.destinationlist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.perqin.letmego.R
import kotlinx.android.synthetic.main.destination_list_fragment.*

class DestinationListFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = DestinationListFragment()
    }

    private val viewModel: DestinationListViewModel by viewModels()
    private lateinit var recyclerAdapter: DestinationListRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerAdapter = DestinationListRecyclerAdapter()
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.destinations.observe(this, Observer {
            recyclerAdapter.destinations = it
        })
    }

}
