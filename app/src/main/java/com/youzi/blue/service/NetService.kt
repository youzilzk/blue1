package com.youzi.blue.service


import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.youzi.blue.net.client.work.Clienter
import io.netty.channel.Channel


class NetService : Service() {
     var clientChannel: Channel? = null

    /********************************************/
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instace: NetService
    }

    override fun onCreate() {
        super.onCreate()
        Clienter.connect()
        clientChannel = Clienter.connect()
        instace = this
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}