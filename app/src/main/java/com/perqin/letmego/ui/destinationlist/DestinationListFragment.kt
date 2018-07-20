package com.perqin.letmego.ui.destinationlist

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.perqin.letmego.R

class DestinationListFragment : Fragment() {

    companion object {
        fun newInstance() = DestinationListFragment()
    }

    private lateinit var viewModel: DestinationListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.destination_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DestinationListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
