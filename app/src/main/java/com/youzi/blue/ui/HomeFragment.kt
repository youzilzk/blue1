package com.youzi.blue.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import com.youzi.blue.R
import com.youzi.blue.WatchContect
import com.youzi.blue.service.HelpService
import com.youzi.blue.service.NetService
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_setting.*


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment()/*, View.OnClickListener*/ {

    //列表显示的数据
    private val data: ArrayList<HashMap<String, Any>> = ArrayList()

    companion object {
        private val ARG_SHOW_TEXT = "text"
        private var mContentText: String? = null
        private var fragment: HomeFragment? = null

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment BlankFragment.
         */
        fun newInstance(param: String?): HomeFragment {
            if (fragment == null) {
                fragment = HomeFragment()
                fragment!!.initData()
            }
            val args = Bundle()
            args.putString(ARG_SHOW_TEXT, param)
            fragment!!.arguments = args
            return fragment as HomeFragment
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mContentText = arguments!!.getString(ARG_SHOW_TEXT)
        }
        context?.startService(Intent(activity, NetService::class.java))
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        return rootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showListView()
    }


    fun initData() {
        data.clear()
        for (i in 0 until 5) {
            //定义一个界面与数据的混合体,一个item代表一行记录
            val item: HashMap<String, Any> = HashMap()
            //一行记录，包含多个控件
            item["deviceImage"] = R.drawable.device_ico
            item["deviceName"] = "设备$i"
            item["description"] =
                "俄国人为符合规范鹅嘎王菲和瑞特个人房屋我和如果文特人格奉化人提供服务和如果无法和各位$i"
            item["state"] = R.drawable.state_green
            data.add(item)
        }
    }

    fun showListView() {
        val listView = activity?.findViewById(R.id.listView) as ListView

        val simpleAdapter = SimpleAdapter(
            context,
            data,  //data 不仅仅是数据，而是一个与界面耦合的数据混合体
            R.layout.listviewitems,
            arrayOf<String>("deviceImage", "deviceName", "description", "state"),
            intArrayOf(R.id.device_image, R.id.device_name, R.id.description, R.id.state)
        )
        //foot设置优雅的分割线
        listView.setFooterDividersEnabled(true)

        listView.adapter = simpleAdapter
        listView.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
            val intent = WatchContect.buildIntent(
                Intent(activity, WatchContect::class.java),
                "192.168.1.10"
            )
            activity?.startActivity(intent)
//            通知数据改变
//            simpleAdapter.notifyDataSetChanged()
        }
    }

}