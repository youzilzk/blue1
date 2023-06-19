package com.youzi.blue.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.youzi.blue.R
import com.youzi.blue.net.client.work.Net
import com.youzi.blue.utils.ItemViewTouchListener
import com.youzi.blue.utils.Utils.isNull
import io.netty.channel.Channel


class WorkAccessibilityService : AccessibilityService(), LifecycleOwner {
    private lateinit var windowManager: WindowManager

    var clientChannel: Channel? = null


    private var floatRootView: View? = null//悬浮窗View
    private val mLifecycleRegistry = LifecycleRegistry(this)

    /************************/
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instace: WorkAccessibilityService
        //无障碍服务运行状态
        var isAccessibilityRunning = MutableLiveData<Boolean>()
    }

    private var mediaProjection: MediaProjection? = null


    fun setMediaProject(project: MediaProjection) {
        mediaProjection = project
    }

    fun getMediaProject(): MediaProjection? {
        return mediaProjection
    }

    /*******************************/
    override fun onCreate() {
        instace = this
        super.onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
//        initObserve()

        val username = getSharedPreferences("user", MODE_PRIVATE).getString("username", null)
        //联网
        clientChannel = Net.start(username!!)
    }


    /**
     * 打开关闭的订阅
     */
    private fun initObserve() {
        isAccessibilityRunning.observe(this) {
            if (it) {
                showWindow()
            } else {
                if (!isNull(floatRootView)) {
                    if (!isNull(floatRootView?.windowToken)) {
                        if (!isNull(windowManager)) {
                            windowManager?.removeView(floatRootView)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        // 设置LayoutParam
        // 获取WindowManager服务
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        var layoutParam = WindowManager.LayoutParams()
        layoutParam.apply {
            //显示的位置
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                //刘海屏延伸到刘海里面
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            } else {
                type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            format = PixelFormat.TRANSPARENT
        }
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_float_item, null)
        floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        windowManager.addView(floatRootView, layoutParam)
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        showWindow()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun getLifecycle(): Lifecycle = mLifecycleRegistry
    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}