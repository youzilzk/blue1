package com.youzi.blue

import android.os.Bundle
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.youzi.blue.net.client.manager.Manager
import com.youzi.blue.net.common.protocol.Constants
import com.youzi.blue.net.common.protocol.Message
import com.youzi.blue.service.WorkAccessibilityService
import com.youzi.blue.threads.VideoPlayThread
import com.youzi.blue.threads.VoicePlayThread
import java.util.*
import kotlin.concurrent.thread

class WatchContect : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var mSurfaceView: SurfaceView
    lateinit var mSurfaceHolder: SurfaceHolder
    var mdiaPlayThread: VideoPlayThread? = null
    var voicePlayThread: VoicePlayThread? = null


    override fun onResume() {
        super.onResume()
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val actionBar = supportActionBar
        actionBar!!.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_contect)

        val username = savedInstanceState?.get("username") as String
        //请求绑定管道
        val message = Message(Message.TYPE.RELEVANT, username.encodeToByteArray())
        WorkAccessibilityService.instace.clientChannel?.writeAndFlush(message)

        //请求开启命令
        val message1 = Message(Message.TYPE.STARTRECORD, Constants.STATE.REQUEST.value)
        WorkAccessibilityService.instace.clientChannel?.writeAndFlush(message1)

        mSurfaceView = findViewById<SurfaceView>(R.id.surfaceView_watch)
        mSurfaceHolder = mSurfaceView.holder
        mSurfaceHolder.addCallback(this)
    }

    var preTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val currentTime = Date().time
            if (currentTime - preTime > 2000) {
                Toast.makeText(this, getText(R.string.app_back_exit), Toast.LENGTH_SHORT).show()
                preTime = currentTime
                return true
            }
            clear()
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun init() {
        thread(true) {
            mdiaPlayThread =
                VideoPlayThread(mSurfaceHolder.surface, Manager.getDataPackList())
            mdiaPlayThread!!.start()
            voicePlayThread = VoicePlayThread(Manager.getDataPackList())
            voicePlayThread!!.start()
        }
    }

    fun clear() {
        mdiaPlayThread?.exit()
        voicePlayThread?.exit()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {

    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        init()
    }

}