package com.perqin.letmego.pages.main.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.perqin.letmego.R
import com.perqin.letmego.utils.permissionsList
import kotlinx.android.synthetic.main.fragment_permissions.*
import kotlinx.android.synthetic.main.layout_app_bar.*

class PermissionsFragment : Fragment() {
    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as Callback
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_permissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        grantPermissionsButton.setOnClickListener {
            requestPermissions(permissionsList, REQUEST_ALL_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ALL_PERMISSIONS -> {
                if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                    // Failed
                    Toast.makeText(context, R.string.text_permissionsNotAllGranted, Toast.LENGTH_SHORT).show()
                } else {
                    callback?.onAllPermissionsGranted()
                }
            }
        }
    }

    interface Callback {
        fun onAllPermissionsGranted()
    }

    companion object {
        private const val REQUEST_ALL_PERMISSIONS = 1

        fun newInstance(): PermissionsFragment {
            return PermissionsFragment()
        }
    }
}
