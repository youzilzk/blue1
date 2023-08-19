package com.youzi.blue.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.youzi.blue.MainActivity
import com.youzi.blue.R
import com.youzi.blue.network.client.manager.Manager
import com.youzi.blue.network.common.protocol.Constants
import com.youzi.blue.network.common.protocol.Message
import com.youzi.blue.service.BlueService
import com.youzi.blue.threads.VideoPlayThread
import java.util.*
import kotlin.concurrent.thread


class WatchContent : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var mSurfaceView: SurfaceView
    lateinit var mSurfaceHolder: SurfaceHolder
    var mdiaPlayThread: VideoPlayThread? = null
//    var voicePlayThread: VoicePlayThread? = null


    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.finish()
    }

    override fun onResume() {
        super.onResume()
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val actionBar = supportActionBar
        actionBar!!.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_contect)

        val username = intent.getStringExtra("username") as String

        //请求绑定管道
        val message = Message(Message.TYPE.RELEVANT, username.encodeToByteArray())
        BlueService.instace.clientChannel?.writeAndFlush(message)

        //请求开启命令
        val message1 = Message(Message.TYPE.STARTRECORD, Constants.STATE.REQUEST.value)
        BlueService.instace.clientChannel?.writeAndFlush(message1)

        mSurfaceView = findViewById<SurfaceView>(R.id.surfaceView_watch)
        mSurfaceHolder = mSurfaceView.holder
        mSurfaceHolder.addCallback(this)
    }

    var preTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val currentTime = Date().time
            if (currentTime - preTime > 2000) {
                Toast.makeText(this, getText(R.string.app_back_last), Toast.LENGTH_SHORT).show()
                preTime = currentTime
                return true
            }
            clear()
            sendStopCommand()
            finish()
        }
        startActivity(Intent(this, MainActivity::class.java))
        return super.onKeyDown(keyCode, event)
    }

    private fun init() {
        thread(true) {
            mdiaPlayThread =
                VideoPlayThread(mSurfaceHolder.surface, Manager.getDataPackList())
            mdiaPlayThread!!.start()
//            voicePlayThread = VoicePlayThread(Manager.getDataPackList())
//            voicePlayThread!!.start()
        }
    }

    fun clear() {
        mdiaPlayThread?.exit()
//        voicePlayThread?.exit()
    }

    private fun sendStopCommand() {
        BlueService.instace.sendStopCommand()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {

    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        init()
    }
}