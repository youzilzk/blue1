package com.youzi.blue.ui

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.youzi.blue.R
import com.youzi.blue.service.BlueService
import com.youzi.blue.utils.LoggerFactory
import com.youzi.blue.utils.OkHttp
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_setting.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


/**
 * 主页
 */
class HomeFragment : Fragment(), View.OnClickListener {
    private val log = LoggerFactory.getLogger()

    private var username: String? = null

    //列表显示的数据
    private val data: ArrayList<HashMap<String, Any>> = ArrayList()
    private lateinit var simpleAdapter: SimpleAdapter

    companion object {
        private var mContentText: String? = null
        private var fragment: HomeFragment? = null

        /**
         * 新建主页的静态方法
         */
        fun newInstance(param: String?): HomeFragment {
            if (fragment == null) {
                fragment = HomeFragment()
            }
            val args = Bundle()
            args.putString("text", param)
            fragment!!.arguments = args
            return fragment as HomeFragment
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mContentText = arguments!!.getString("text")
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onResume() {
        super.onResume()
        refreshData()

        //监听数据变化
        BlueService.deviceStateChange.observe(activity!!) {
            for (datum in data) {
                if (datum["watcherUser"]?.equals(it["username"]) == true) {
                    when (it["state"] as Int) {
                        1 -> datum["state"] = R.drawable.state_green
                        0 -> datum["state"] = R.drawable.state_gray
                        2 -> datum["state"] = R.drawable.state_yellow
                    }
                    break
                }
            }
            simpleAdapter.notifyDataSetChanged()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        username = context?.getSharedPreferences("user", AccessibilityService.MODE_PRIVATE)
            ?.getString("username", null)?.trim()
        showListView()
        //右上角添加设备按钮
        addDevice.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            addDevice -> {
                val intent = Intent(context, EditDialog::class.java)
                val bundle = Bundle()

                //添加的type为2
                bundle.putInt("type", 1)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        }
    }

    private fun refreshData() {
        data.clear()
        //查询
        OkHttp.getInstance()
            .httpGet("http://61.243.3.19:5000/user/device?username=$username", object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    log.info("refresh data error!")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val jo = JSONObject.parseObject(String(response.body().bytes()))
                    if ((jo["result"] as Boolean?)!!) {
                        val list = jo["data"] as JSONArray
                        val it = list.iterator()
                        while (it.hasNext()) {
                            val i = it.next() as JSONObject
                            //定义一个界面与数据的混合体,一个item代表一行记录
                            val item: HashMap<String, Any> = HashMap()
                            //一行记录，包含多个控件
                            item["deviceImage"] = R.drawable.head_ico
                            item["watcherUser"] = i["username"].toString()
                            item["description"] = "俄国人为符合规范鹅嘎王菲和瑞特个人房屋我和如果文特人格奉化人提供服务和如果无法和各位"

                            val state = i["state"] as Int
                            if (state == 1) item["state"] = R.drawable.state_green
                            else if (state == 0) item["state"] = R.drawable.state_gray
                            else if (state == 2) item["state"] = R.drawable.state_yellow

                            data.add(item)
                        }
                    }
                    activity?.runOnUiThread {
                        simpleAdapter.notifyDataSetChanged()
                    }

                }
            })
    }

    private fun addListItemSlide() {
        //左划操作
        val create = SwipeMenuCreator { menu ->
            val editItem = SwipeMenuItem(activity)
            editItem.background = ColorDrawable(Color.rgb(255, 165, 0))
            editItem.width = dip2px(90f)
            editItem.title = "编辑"
            editItem.titleSize = 18
            editItem.titleColor = Color.WHITE
            menu.addMenuItem(editItem)

            val deleteItem = SwipeMenuItem(activity)
            deleteItem.background = ColorDrawable(Color.RED)
            deleteItem.width = dip2px(90f)
            deleteItem.title = "删除"
            deleteItem.titleSize = 18
            deleteItem.titleColor = Color.WHITE
            menu.addMenuItem(deleteItem)
        }
        listView.setMenuCreator(create)

        //左划菜单事件
        listView.setOnMenuItemClickListener { i, _, menuItem ->
            val watchUser = data[i]["watcherUser"]
            when (menuItem) {
                0 -> {
                    val intent = Intent(context, EditDialog::class.java)
                    val bundle = Bundle()

                    //编辑的type为2
                    bundle.putInt("type", 2)
                    bundle.putString("watchUser", watchUser.toString())

                    intent.putExtras(bundle)
                    startActivity(intent)
                }
                1 -> {
                    OkHttp.getInstance()
                        .httpGet("http://61.243.3.19:5000/user/deleteWatchUser?username=$username&watchUser=$watchUser",
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    Toast.makeText(
                                        context, "网络错误!", Toast.LENGTH_SHORT
                                    ).show()
                                }

                                @Throws(IOException::class)
                                override fun onResponse(call: Call, response: Response) {
                                    val jo = JSONObject.parseObject(String(response.body().bytes()))
                                    val message = jo["message"]

                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context, message.toString(), Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    if ((jo["result"] as Boolean?)!!) {
                                        refreshData()
                                    }
                                }
                            })
                }
            }
            false
        }
    }

    private fun showListView() {
        //列表项左样式
        addListItemSlide()

        simpleAdapter = SimpleAdapter(
            context,
            data,
            R.layout.listview_items,
            arrayOf("deviceImage", "watcherUser", "description", "state"),
            intArrayOf(R.id.device_image, R.id.device_name, R.id.description, R.id.state)
        )
        //foot设置优雅的分割线
        listView.setFooterDividersEnabled(true)

        listView.adapter = simpleAdapter
        listView.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
            if (BlueService.isAccessibilityRunning.value != true) {
                Toast.makeText(context, getText(R.string.base_service_offline), Toast.LENGTH_SHORT)
                    .show()
                return@setOnItemClickListener
            }
            val state = data[i]["state"]
            if (state == R.drawable.state_gray) {
                Toast.makeText(context, getText(R.string.device_offline), Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }
            if (state == R.drawable.state_yellow) {
                Toast.makeText(context, getText(R.string.device_no_permit), Toast.LENGTH_SHORT)
                    .show()
                return@setOnItemClickListener
            }

            val intent = Intent(activity, WatchContent::class.java)
            val username = data[i]["watcherUser"]

            intent.putExtra("username", username.toString())
            activity!!.finish()
            activity?.startActivity(intent)
        }
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}