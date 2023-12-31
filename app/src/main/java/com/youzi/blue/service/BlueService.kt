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
import android.graphics.PixelFormat.*
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.util.DisplayMetrics
import android.view.*
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.youzi.blue.R
import com.youzi.blue.io.SendServerThread
import com.youzi.blue.network.client.work.Net
import com.youzi.blue.network.common.protocol.Constants
import com.youzi.blue.network.common.protocol.Message
import com.youzi.blue.service.ScreenListener.ScreenStateListener
import com.youzi.blue.threads.VideoRecorder
import com.youzi.blue.utils.LoggerFactory
import com.youzi.blue.utils.SingleLiveData
import io.netty.channel.Channel

class BlueService : AccessibilityService(), LifecycleOwner {
    private val log = LoggerFactory.getLogger()
    private lateinit var username: String

    private lateinit var windowManager: WindowManager

    var clientChannel: Channel? = null

    private var floatRootView: View? = null//悬浮窗View

    /************************/
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instace: BlueService

        var netWorkTryTimes: Int = -1

        //无障碍服务运行状态
        var isAccessibilityRunning = SingleLiveData<Boolean>()

        //观察设备在线状态变化
        var deviceStateChange = SingleLiveData<HashMap<String, Any>>()
    }

    private var mediaProjection: MediaProjection? = null


    fun setMediaProject(project: MediaProjection) {
        mediaProjection = project
    }

    fun deviceStateChange(value: HashMap<String, Any>) {
        deviceStateChange.postValue(value)

    }

    /*******************************/

    private var recordRunning = false
    private var screen_width = 720
    private var screen_height = 1080

    /******************通知相关,录屏必须要通知***********************/

    private val PID = Process.myPid()
    private var mConnection: ServiceConnection? = null

    /******************************************/

    private lateinit var sendThread: SendServerThread
    private lateinit var videoRecorder: VideoRecorder

    /***************监听息屏和亮屏****************/
    private lateinit var screenListener: ScreenListener


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        instace = this

        super.onCreate()

        updateUser()
        //联网
        clientChannel = Net(username).start(false)

        /****************************网络监听********************************/
        val networkListener = NetworkListener()
        val netChangeListener = object : NetworkListener.NetChangeListener {
            override fun onWifi() {
                checkNetwork()
            }

            override fun onMobile() {
                checkNetwork()
            }

            override fun onDisconnected() {
                //不尝试重连网络
                netWorkTryTimes = -1
                //断网, 如果录屏在运行, 则置空(不再更新发送管道, 因为之前管道已死亡, 重现绑定管道太繁琐)
                if (isRecordRunning()) {
                    sendThread.setChannelIsNull()
                }
            }
        }
        //服务开启时屏幕肯定是亮屏,所以开启网络状态监听
        networkListener.register(netChangeListener, applicationContext)

        /*************************屏幕监听时钟心跳定时器*****************************/
        //刚开启此服务是亮屏, 默认开始时钟网络检测
        screenListener = ScreenListener(this)
        screenListener.register(object : ScreenStateListener {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                this@BlueService, 0, Intent(this@BlueService, AlarmHeartbeat::class.java), 0
            )

            override fun onScreenOn() {
                //亮屏
            }

            override fun onScreenOff() {
                //黑屏
                //不尝试重连网络
                netWorkTryTimes = -1

                //取消时钟检测
                alarmManager.cancel(pendingIntent)
                networkListener.unRegister()
                //如果没正在录屏, 息屏就断网
                if (!recordRunning) {
                    clientChannel?.writeAndFlush(
                        Message(
                            Message.TYPE.CLOSECHANNEL, Constants.STATE.REQUEST.value
                        )
                    )
                    clientChannel?.close()?.sync()
                }
            }

            override fun onUserPresent() {
                //解锁成功
                //检测网络
                checkNetwork()
                //网络状态监听
                networkListener.register(netChangeListener, applicationContext)

                /*****************************************************************/
                //时钟检测网络
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60000, pendingIntent
                )
            }
        })
    }


    fun updateUser() {
        username = getSharedPreferences("user", MODE_PRIVATE).getString("username", null) as String
    }

    fun tryReConnected() {
        var times = 1
        netWorkTryTimes = 20

        while (times <= netWorkTryTimes) {
            log.info("尝试网络重连,第[{}]次!", times)

            clientChannel = Net(username).start(false)
            //网络变化, 如果录屏在运行, 则置空(不再更新发送管道, 因为之前管道已死亡, 重现绑定管道太繁琐)
            if (isRecordRunning()) {
                sendThread.setChannelIsNull()
            }

            times++
            Thread.sleep(10000)
            if (isNetworkUseful()) {
                log.info("重连网络成功!")
                //如果网络重连次数netWorkTryTimes被置为-1,连接成功也关闭
                if (netWorkTryTimes == -1) {
                    log.info("网络重连次数被置为-1,断开网络!")
                    clientChannel?.close()?.sync()
                }
                break
            }
        }
    }

    fun checkNetwork() {
        log.info("检测网络链路!")
        if (isNetworkUseful()) {
            log.info("网络正常!")
        } else {
            log.warn("网络异常!")
            tryReConnected()
        }

    }

    private fun isNetworkUseful(): Boolean {
        return !(clientChannel == null || !clientChannel!!.isActive)
    }


    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showWindow() {
        // 设置LayoutParam
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        //屏幕宽高
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screen_width = metrics.widthPixels
        screen_height = metrics.heightPixels

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
            format = TRANSPARENT
            x = screen_width
            y = screen_height / 10
        }
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_float_item, null)
        //floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        windowManager.addView(floatRootView, layoutParam)
    }

    fun startSendServer() {
        setNotification()
        sendThread = SendThread()
        sendThread.start()
        try {
            log.info("mediaProjection:  {}", mediaProjection.toString())
            videoRecorder = VideoRecorder(
                sendThread, mediaProjection!!, screen_width, screen_height, 1920 * 1080, 15
            )
            recordRunning = true
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            sendThread.exit
            return
        }
    }

    fun stopRecord() {
        if (recordRunning) {
            videoRecorder.exit()
            sendThread.exit()
            recordRunning = false
        }

    }

    fun sendStopCommand() {
        clientChannel?.writeAndFlush(
            Message(
                Message.TYPE.STOPRECORD, Constants.STATE.REQUEST.value
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
                    log.info("ForegroundService: onServiceDisconnected")
                }

                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    log.info("ForegroundService: onServiceConnected")
                    // sdk >= 18 的，会在通知栏显示service正在运行，这里不要让用户感知，
                    // 所以这里的实现方式是利用2个同进程的service，利用相同的notificationID，
                    // 2个service分别startForeground，然后只在1个service里stopForeground，这样即可去掉通知栏的显示
                    val helpService: Service = (binder as HelpService.LocalBinder).getService()
                    this@BlueService.startForeground(PID, getNotification())
                    helpService.startForeground(PID, getNotification())
                    helpService.stopForeground(true)
                    this@BlueService.unbindService(mConnection!!)
                    mConnection = null
                }
            }
        }
        bindService(
            Intent(this, HelpService::class.java), mConnection!!, BIND_AUTO_CREATE
        )
    }

    private fun getNotification(): Notification? {
        log.info("notification: {}", Build.VERSION.SDK_INT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationIntent = Intent(this, BlueService::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
            val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID").setLargeIcon(
                BitmapFactory.decodeResource(
                    resources, android.R.drawable.btn_default
                )
            ).setSmallIcon(android.R.drawable.bottom_bar).setContentTitle("Starting Service")
                .setContentText("Starting monitoring service").setTicker("Ticker")
                .setContentIntent(pendingIntent)

            return notificationBuilder.build()
        }
        return null
    }

    fun isRecordRunning(): Boolean {
        return recordRunning
    }

    private inner class SendThread : SendServerThread(clientChannel!!) {
        override fun onError(t: Throwable) {
            recordRunning = false
            log.error("服务开启失败: {}", t.message)
        }
    }

    /****************************************************************************/
    override fun onServiceConnected() {
        super.onServiceConnected()
        showWindow()
    }

    override fun getLifecycle(): Lifecycle = LifecycleRegistry(this)

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        screenListener.unregister()
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}