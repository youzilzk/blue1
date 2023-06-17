package com.youzi.blue.service

//noinspection SuspiciousImport
import android.R
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.media.projection.MediaProjection
import android.os.Binder
import android.os.Build
import android.os.Build.VERSION
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.youzi.blue.server.ServerThread
import com.youzi.blue.threads.VideoSender


class ScreenRecordService : LifecycleService() {
    private var mediaProjection: MediaProjection? = null

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

    /********************************************/

    override fun onCreate() {
        super.onCreate()
        val serviceThread = HandlerThread("service_thread", Process.THREAD_PRIORITY_BACKGROUND)
        serviceThread.start()
        running = false
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    fun setMediaProject(project: MediaProjection?) {
        mediaProjection = project
    }

    fun isRunning(): Boolean {
        return running
    }

    fun setConfig(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    /*****************************通知相关********************************/
    private fun getNotification(): Notification? {
        Log.i("blue", "notification: " + Build.VERSION.SDK_INT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Call Start foreground with notification
            val notificationIntent = Intent(this, ScreenRecordService::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
            val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.btn_default
                    )
                )
                .setSmallIcon(R.drawable.bottom_bar)
                .setContentTitle("Starting Service")
                .setContentText("Starting monitoring service")
                .setTicker("Ticker")
                .setContentIntent(pendingIntent)
            val notification = notificationBuilder.build()

            return notification
        }
        return null
    }


    private fun setNotification() {
        // sdk < 18 , 直接调用startForeground即可,不会在通知栏创建通知
        if (VERSION.SDK_INT < 18) {
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
                    this@ScreenRecordService.startForeground(PID, getNotification())
                    helpService.startForeground(PID, getNotification())
                    helpService.stopForeground(true)
                    this@ScreenRecordService.unbindService(mConnection!!)
                    mConnection = null
                }
            }
        }
        bindService(
            Intent(this, HelpService::class.java), mConnection!!,
            BIND_AUTO_CREATE
        )
    }

    /*************************************************************/

    private inner class SendThread :
        ServerThread(WorkAccessibilityService.instace.clientChannel!!) {
        override fun onError(t: Throwable) {
            running = false
            handle.post {
                Toast.makeText(
                    this@ScreenRecordService,
                    "服务开启失败:${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /***************************服务绑定*******************************/
    inner class ScreenRecordBinder : Binder() {
        fun getScreenRecordService(): ScreenRecordService {
            return this@ScreenRecordService
        }
    }


    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return ScreenRecordBinder()
    }

    /******************************启动服务**************************/
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

    /********************************************************/

    override fun onDestroy() {
        super.onDestroy()
        videoSender.exit()
        serverThread.exit()
        running = false
    }
}