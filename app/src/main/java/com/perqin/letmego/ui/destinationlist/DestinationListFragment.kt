package com.perqin.letmego.ui.destinationlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.perqin.letmego.R
import kotlinx.android.synthetic.main.destination_list_fragment.*

class DestinationListFragment : Fragment() {
    companion object {
        fun newInstance() = DestinationListFragment()
    }

    private lateinit var viewModel: DestinationListViewModel
    private lateinit var recyclerAdapter: DestinationListRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerAdapter = DestinationListRecyclerAdapter()
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
        viewModel = ViewModelProviders.of(this).get(DestinationListViewModel::class.java)
        viewModel.destinations.observe(this, Observer { it!!
            recyclerAdapter.destinations = it
        })
    }

}
