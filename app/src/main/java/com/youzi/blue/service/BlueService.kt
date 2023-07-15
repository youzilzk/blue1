package com.youzi.blue.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.AlarmManager
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
import com.youzi.blue.net.common.utils.LoggerFactory
import com.youzi.blue.server.ServerThread
import com.youzi.blue.service.ScreenListener.ScreenStateListener
import com.youzi.blue.threads.VideoSender
import io.netty.channel.Channel


class BlueService : AccessibilityService(), LifecycleOwner {
    private val log = LoggerFactory.getLogger()
    private lateinit var username: String

    private lateinit var windowManager: WindowManager

    var clientChannel: Channel? = null

    private var floatRootView: View? = null//悬浮窗View
    private val mLifecycleRegistry = LifecycleRegistry(this)

    /************************/
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instace: BlueService

        //无障碍服务运行状态
        var isAccessibilityRunning = MutableLiveData<Boolean>()
    }

    private var mediaProjection: MediaProjection? = null


    fun setMediaProject(project: MediaProjection) {
        mediaProjection = project
    }

    /*******************************/

    private var recordRunning = false
    private var screen_width = 720
    private var screen_height = 1080

    /******************通知相关,录屏必须要通知***********************/

    private val PID = Process.myPid()
    private var mConnection: ServiceConnection? = null

    /******************************************/

    private lateinit var serverThread: ServerThread
    private lateinit var videoSender: VideoSender
    private val handle: Handler = Handler()

    /***************监听息屏和亮屏****************/
    private lateinit var screenListener: ScreenListener

    override fun onCreate() {
        instace = this

        super.onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        username = getSharedPreferences("user", MODE_PRIVATE).getString("username", null) as String
        //联网
        clientChannel = Net(username).start()

        /******************屏幕监听时钟心跳定时器**********************/
        //刚开启此服务是亮屏, 默认开始时钟网络检测

        screenListener = ScreenListener(this)

        screenListener.register(object : ScreenStateListener {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                this@BlueService,
                0,
                Intent(this@BlueService, AlarmHeartReceiver::class.java),
                0
            )

            override fun onScreenOn() {
                //亮屏
            }

            override fun onScreenOff() {
                //黑屏
                alarmManager.cancel(pendingIntent)
            }

            override fun onUserPresent() {
                //解锁成功
                checkNetwork()
                //时钟检测网络
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    15000,
                    pendingIntent
                )
            }
        })

    }

    fun checkNetwork() {
        log.info("检测网络状态!")
        if (clientChannel == null || !clientChannel!!.isActive) {
            log.warn("网络重连!")
            val channel = Net(username).start()

            updateChannel(channel)
        }

    }

    fun updateChannel(channel: Channel?) {
        clientChannel = channel
        //网络变化, 如果录屏在运行, 则更新发送管道
        if (isRecordRunning()) {
            serverThread.updateChannel(channel)
        }
    }


    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showWindow() {
        // 设置LayoutParam
        // 获取WindowManager服务
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        val layoutParam = WindowManager.LayoutParams()
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
            width = 1
            height = 1
            format = PixelFormat.TRANSPARENT
            x = screen_width
            y = screen_height/10
        }
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_float_item, null)
        //floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        windowManager.addView(floatRootView, layoutParam)
    }

    fun startSendServer() {
        log.info(mediaProjection.toString())
        setNotification()
        serverThread = SendThread()
        serverThread.start()
        try {
            videoSender = VideoSender(
                serverThread, mediaProjection!!,
                screen_width, screen_height,
                2 * 1920 * 1080, 18
            )
            recordRunning = true
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            return
        }
    }

    fun stopRecord() {
        videoSender.exit()
        serverThread.exit()
        recordRunning = false
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
                    this@BlueService.startForeground(PID, getNotification())
                    helpService.startForeground(PID, getNotification())
                    helpService.stopForeground(true)
                    this@BlueService.unbindService(mConnection!!)
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
            val notificationIntent = Intent(this, BlueService::class.java)
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

    fun isRecordRunning(): Boolean {
        return recordRunning
    }

    fun setConfig(width: Int, height: Int) {
        this.screen_width = width
        this.screen_height = height
    }

    private inner class SendThread :
        ServerThread(clientChannel!!) {
        override fun onError(t: Throwable) {
            recordRunning = false
            handle.post {
                Toast.makeText(
                    this@BlueService,
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
        screenListener.unregister()
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}