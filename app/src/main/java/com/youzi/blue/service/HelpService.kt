package com.youzi.blue.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log


class HelpService : Service() {
    inner class LocalBinder : Binder() {
        fun getService() = this@HelpService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "HelpService: onBind()")
        return LocalBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "HelpService: onDestroy()")
    }

    companion object {
        private const val TAG = "HelpService"
    }
}
