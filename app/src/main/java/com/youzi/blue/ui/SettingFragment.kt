package com.youzi.blue.ui

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSONObject
import com.youzi.blue.R
import com.youzi.blue.service.BlueService
import com.youzi.blue.ui.login.LoginActivity
import com.youzi.blue.utils.Help
import com.youzi.blue.utils.LoggerFactory
import com.youzi.blue.utils.OkHttp
import com.youzi.blue.utils.Utils
import kotlinx.android.synthetic.main.fragment_setting.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * 设置页
 */
class SettingFragment : Fragment(), View.OnClickListener {
    private val log = LoggerFactory.getLogger()

    private var username: String? = null

    private var accessibilityService: BlueService? = null
    var mediaProjectionManager: MediaProjectionManager? = null

    companion object {
        lateinit var instance: SettingFragment
        private var mContentText: String? = null

        /**
         * 新建设置页的静态方法
         */
        fun newInstance(param1: String?): SettingFragment {
            val fragment = SettingFragment()
            val args = Bundle()
            args.putString("text", param1)
            fragment.arguments = args
            instance = fragment
            return fragment
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
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        username = context?.getSharedPreferences("user", AccessibilityService.MODE_PRIVATE)
            ?.getString("username", null)?.trim()

        //填充验证码
        getToken()

        bt_01.setOnClickListener(this)
        bt_03.setOnClickListener(this)
        bt_04.setOnClickListener(this)
        bt_05.setOnClickListener(this)
        bt_06.setOnClickListener(this)

        bt_05.isEnabled = false

        if (Help.isAccessibilityRunning(activity!!)) {
            bt_01.isEnabled = false
        } else {
            bt_01.text = "启动基础服务"
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            bt_01 -> {
                if (!Help.isAccessibilityRunning(activity!!)) {
                    BlueService.isAccessibilityRunning.observe(activity!!) {
                        if (it) {
                            accessibilityService = BlueService.instace
                            applyCapture()
                        }
                    }

                    //初始化基础服务
                    startBaseService()
                }
            }


            bt_03 -> {
                val sharedPreferences =
                    activity?.getSharedPreferences("user", Context.MODE_PRIVATE)?.edit()
                sharedPreferences?.putString("username", null)?.apply()
                sharedPreferences?.putString("watchToken", null)?.apply()

                //跳转到LoginActivity
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                //关闭主页面
                activity?.finish()
            }

            bt_04 -> {
                tokenBox.isFocusable = true
                tokenBox.isFocusableInTouchMode = true
                bt_05.isEnabled = true
            }

            bt_05 -> {
                tokenBox.isFocusable = false
                tokenBox.isFocusableInTouchMode = false
                val token = tokenBox.text.toString()
                //正则化判断输入的验证码是否符合6位数字
                if (!Utils.isToken(token)) {
                    Toast.makeText(context, "请输入6位数字验证码！", Toast.LENGTH_SHORT).show()
                    return
                }
                updateToken(token)
            }

            bt_06 -> {
                tokenBox.isFocusable = false
                tokenBox.isFocusableInTouchMode = false
                updateToken(null)
            }
        }
    }


    private fun updateToken(token: String?) {
        var url = "http://61.243.3.19:5000/user/updateToken?username=$username"
        if (token != null) {
            url = "$url&token=$token"
        }
        OkHttp.getInstance().httpGet(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(
                    context, "网络错误!", Toast.LENGTH_SHORT
                ).show()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val jo = JSONObject.parseObject(String(response.body().bytes()))
                if ((jo["result"] as Boolean?)!!) {
                    var watchToken = token
                    if (watchToken == null) {
                        //token=null说明是请求服务器随机生成,否则说明是用户自定义的
                        watchToken = jo["data"] as String

                        activity?.runOnUiThread { tokenBox.setText(watchToken) }
                    }
                    //验证码本地存储
                    activity?.getSharedPreferences("user", Context.MODE_PRIVATE)?.edit()
                        ?.putString("watchToken", token)?.apply()

                    activity?.runOnUiThread { bt_05.isEnabled = false }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context, "更新失败!", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun getToken() {
        //禁止输入框
        tokenBox.isFocusable = false
        tokenBox.isFocusableInTouchMode = false
        var watchToken = activity?.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
            ?.getString("watchToken", "") as String
        if (watchToken != "") {
            tokenBox.setText(watchToken)
        } else {
            OkHttp.getInstance()
                .httpGet(
                    "http://61.243.3.19:5000/user/getToken?username=$username",
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            log.info("网络错误!")
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            val jo = JSONObject.parseObject(String(response.body().bytes()))
                            if ((jo["result"] as Boolean?)!!) {
                                watchToken = jo["data"] as String
                                //验证码本地存储
                                activity?.getSharedPreferences("user", Context.MODE_PRIVATE)?.edit()
                                    ?.putString("watchToken", watchToken)?.apply()
                                activity?.runOnUiThread { tokenBox.setText(watchToken) }
                            }
                        }
                    })
        }
    }

    private fun startBaseService() {
        if (!Help.hasBasePermission(activity!!)) {
            Toast.makeText(context, "请先打开麦克风和存储权限！", Toast.LENGTH_SHORT).show()
            return
        }
        Help.checkAccessibilityPermission(activity!!)
    }


    private fun applyCapture() {
        mediaProjectionManager =
            context?.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        //开启录屏请求intent
        val captureIntent = mediaProjectionManager?.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == AppCompatActivity.RESULT_OK) {
            val mediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode, data!!)
            accessibilityService?.setMediaProject(mediaProjection)
            bt_01.isEnabled = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1101) {
            if (grantResults.isNotEmpty() && (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(
                    context, "请设置必须的应用权限，否则将会导致运行异常！", Toast.LENGTH_SHORT
                ).show()
            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                log.info("授权成功")
            }
        }
    }


    /**********************************************************************/
    override fun onResume() {
        super.onResume()
        if (Help.isAccessibilityRunning(activity!!)) {
            if (BlueService.isAccessibilityRunning.value != true) {
                BlueService.isAccessibilityRunning.postValue(true)
            }
        }
    }

}