package com.youzi.blue.ui

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
import com.youzi.blue.R
import com.youzi.blue.service.BlueService
import com.youzi.blue.ui.login.LoginActivity
import com.youzi.blue.utils.LoggerFactory
import com.youzi.blue.utils.Help
import kotlinx.android.synthetic.main.fragment_setting.*

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment(), View.OnClickListener {
    private val log = LoggerFactory.getLogger()

    private var accessibilityService: BlueService? = null
    var mediaProjectionManager: MediaProjectionManager? = null

    companion object {
        lateinit var instance: SettingFragment
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
            instance = fragment
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bt_01.setOnClickListener(this)
        bt_03.setOnClickListener(this)

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
                activity?.getSharedPreferences("user", Context.MODE_PRIVATE)?.edit()!!
                    .putString("username", null).apply()
                //跳转到LoginActivity
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                //关闭主页面
                activity?.finish()
            }
        }
    }


    private fun startBaseService() {
        if (!Help.hasBasePermission(activity!!)) {
            Toast.makeText(context, "请先打开麦克风和存储权限！", Toast.LENGTH_SHORT).show()
            return
        }
        Help.checkAccessibilityPermission(activity!!)
    }


    fun applyCapture() {
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
            if (grantResults.size != 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(
                    context, "请设置必须的应用权限，否则将会导致运行异常！", Toast.LENGTH_SHORT
                ).show()
            } else if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                log.info( "授权成功")
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