package com.youzi.blue.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Process
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.youzi.blue.R
import com.youzi.blue.net.client.work.Net
import com.youzi.blue.net.common.protocol.Constants
import com.youzi.blue.net.common.protocol.Message
import com.youzi.blue.server.ServerThread
import com.youzi.blue.threads.VideoSender
import com.youzi.blue.utils.ItemViewTouchListener
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

    /*******************************/

    private var running = false
    private var width = 720
    private var height = 1080

    /******************通知相关,录屏必须要通知***********************/

    private val PID = Process.myPid()
    private var mConnection: ServiceConnection? = null

    /******************************************/

    private lateinit var serverThread: ServerThread
    private lateinit var videoSender: VideoSender
    private val handle: Handler = Handler()

    /*******************************/


    override fun onCreate() {
        instace = this
        super.onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        val username = getSharedPreferences("user", MODE_PRIVATE).getString("username", null)
        //联网
        clientChannel = Net.start(username!!)
    }


    @SuppressLint("ClickableViewAccessibility", "InflateParams")
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

    fun startSendServer() {
        setNotification()
        serverThread = SendThread()
        serverThread.start()
        try {
            videoSender = VideoSender(
                serverThread, mediaProjection!!,
                width, height,
                2 * 1920 * 1080, 18
            )
            running = true
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            return
        }
    }

    fun stopRecord() {
        videoSender.exit()
        serverThread.exit()
        running = false
    }

    fun sendStopCommand() {
        clientChannel?.writeAndFlush(
            Message(
                Message.TYPE.STOPRECORD,
                Constants.STATE.REQUEST.value
            )
        )
    }

    private fun setNotification() {
        // sdk < 18 , 直接调用startForeground即可,不会在通知栏创建通知
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(PID, getNotification())
            return
        }
        if (null == mConnection) {
            mConnection = object : ServiceConnection {
                override fun onServiceDisconnected(name: ComponentName) {
                    Log.d("blue", "ForegroundService: onServiceDisconnected")
                }

                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    Log.d("blue", "ForegroundService: onServiceConnected")
                    // sdk >= 18 的，会在通知栏显示service正在运行，这里不要让用户感知，所以这里的实现方式是利用2个同进程的service，利用相同的notificationID，
                    // 2个service分别startForeground，然后只在1个service里stopForeground，这样即可去掉通知栏的显示
                    val helpService: Service = (binder as HelpService.LocalBinder)
                        .getService()
                    this@WorkAccessibilityService.startForeground(PID, getNotification())
                    helpService.startForeground(PID, getNotification())
                    helpService.stopForeground(true)
                    this@WorkAccessibilityService.unbindService(mConnection!!)
                    mConnection = null
                }
            }
        }
        bindService(
            Intent(this, HelpService::class.java), mConnection!!,
            BIND_AUTO_CREATE
        )
    }

    private fun getNotification(): Notification? {
        Log.i("blue", "notification: " + Build.VERSION.SDK_INT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Call Start foreground with notification
            val notificationIntent = Intent(this, WorkAccessibilityService::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
            val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        android.R.drawable.btn_default
                    )
                )
                .setSmallIcon(android.R.drawable.bottom_bar)
                .setContentTitle("Starting Service")
                .setContentText("Starting monitoring service")
                .setTicker("Ticker")
                .setContentIntent(pendingIntent)
            val notification = notificationBuilder.build()

            return notification
        }
        return null
    }

    fun isRunning(): Boolean {
        return running
    }

    fun setConfig(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    private inner class SendThread :
        ServerThread(clientChannel!!) {
        override fun onError(t: Throwable) {
            running = false
            handle.post {
                Toast.makeText(
                    this@WorkAccessibilityService,
                    "服务开启失败:${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /****************************************************************************/
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