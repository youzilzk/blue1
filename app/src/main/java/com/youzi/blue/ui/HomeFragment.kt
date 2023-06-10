package com.youzi.blue.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.youzi.blue.R
import com.youzi.blue.WatchContect
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_setting.*

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), View.OnClickListener {


    companion object {
        private val ARG_SHOW_TEXT = "text"
        private var mContentText: String? = null

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment BlankFragment.
         */
        fun newInstance(param1: String?): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(ARG_SHOW_TEXT, param1)
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mContentText = arguments!!.getString(ARG_SHOW_TEXT)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val contentTv = rootView.findViewById<TextView>(R.id.watch_01)
        contentTv.text = mContentText
        return rootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        watch_01.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            watch_01 -> {
                val intent = WatchContect.buildIntent(
                    Intent(activity, WatchContect::class.java),
                    "192.168.0.105"
                )
                activity?.startActivity(intent)
            }
        }
    }
fun listView(){}
}