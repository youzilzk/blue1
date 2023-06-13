package com.youzi.blue.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.youzi.blue.MainActivity
import com.youzi.blue.R
import com.youzi.blue.db.DBOpenHelper
import com.youzi.blue.net.client.work.Clienter
import com.youzi.blue.service.ScreenRecordService
import com.youzi.blue.service.WorkAccessibilityService
import com.youzi.blue.ui.login.LoginActivity
import com.youzi.blue.utils.Utils
import kotlinx.android.synthetic.main.fragment_setting.*

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment(), View.OnClickListener {
    private var accessibilityService: WorkAccessibilityService? = null
    private var screenRecordService: ScreenRecordService? = null
    var mediaProjectionManager: MediaProjectionManager? = null

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
        fun newInstance(param1: String?): SettingFragment {
            val fragment = SettingFragment()
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
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bt_01.setOnClickListener(this)
        bt_02.setOnClickListener(this)
        bt_03.setOnClickListener(this)

        if (Utils.isAccessibilityRunning(activity!!)) {
            bt_01.isEnabled = false
        } else {
            bt_01.text = "启动基础服务"
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            bt_01 -> {
                if (!Utils.isAccessibilityRunning(activity!!)) {
                    WorkAccessibilityService.isAccessibilityRunning.observe(activity!!) {
                        if (it) {
                            accessibilityService = WorkAccessibilityService.instace
                            applyCapture()
                            accessibilityService!!.channel = Clienter.getChannel()
                        }
                    }

                    //初始化基础服务
                    startBaseService()
                }
            }

            bt_02 -> {
                if (!Utils.isAccessibilityRunning(activity!!)) {
                    Toast.makeText(
                        activity,
                        "基础服务未开启!\n基础服务有时会开启失败, 请关闭后再开启!",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                if (screenRecordService == null) {
                    connectService()
                    Toast.makeText(context, "开始录屏", Toast.LENGTH_SHORT).show()
                    bt_02.text = "结束录屏"
                } else if (screenRecordService?.isRunning() == false) {
                    //开始录屏
                    screenRecordService!!.startSendServer()
                    Toast.makeText(context, "开始录屏", Toast.LENGTH_SHORT).show()
                    bt_02.text = "结束录屏"
                } else if (screenRecordService!!.isRunning()) {
                    screenRecordService = null
                    context?.unbindService(serviceConnection)
                    bt_02.text = "开始录屏"
                }
            }

            bt_03 -> {
                DBOpenHelper(context, "user.db", null, 1).updateLoginState("0")
                //跳转到LoginActivity
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                //关闭主页面
                activity?.finish()
            }
        }
    }

    private fun startBaseService() {
        if (!Utils.hasBasePermission(activity!!)) {
            Toast.makeText(context, "请先打开麦克风和存储权限！", Toast.LENGTH_SHORT)
                .show()
            return
        }
        Utils.checkAccessibilityPermission(activity!!)
    }


    fun applyCapture() {
        mediaProjectionManager =
            context?.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        //开启录屏请求intent
        val captureIntent = mediaProjectionManager?.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 101)
    }

    private fun connectService() {
        val intent = Intent(context, ScreenRecordService::class.java)
        context?.bindService(intent, serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: ScreenRecordService.ScreenRecordBinder =
                service as ScreenRecordService.ScreenRecordBinder
            screenRecordService = binder.getScreenRecordService()
            screenRecordService?.setMediaProject(accessibilityService?.getMediaProject())
            //初始化录屏窗口配置
            initWindowsConfig(screenRecordService!!)
            if (!screenRecordService!!.isRunning()) {
                screenRecordService!!.startSendServer()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Toast.makeText(context, "录屏服务断开！", Toast.LENGTH_SHORT).show()
        }
    }

    fun initWindowsConfig(screenRecordService: ScreenRecordService) {
        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
        screenRecordService.setConfig(
            metrics.widthPixels,
            metrics.heightPixels
        )
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
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1101) {
            if (grantResults.size != 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(
                    context,
                    "请设置必须的应用权限，否则将会导致运行异常！",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i("", "授权成功")
            }
        }
    }


    /**********************************************************************/
    override fun onResume() {
        super.onResume()
        if (Utils.isAccessibilityRunning(activity!!)) {
            if (WorkAccessibilityService.isAccessibilityRunning.value != true) {
                WorkAccessibilityService.isAccessibilityRunning.postValue(true)
            }
        }
    }

}