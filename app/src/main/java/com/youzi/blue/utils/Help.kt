package com.youzi.blue.utils

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.youzi.blue.service.BlueService
import java.util.*


object Help {

    fun hasBasePermission(context: Activity): Boolean {
        return hasWriteAndRecordPermission(context)
    }

    fun checkBasePermission(context: Activity) {
        if (!hasWriteAndRecordPermission(context)) {
            applyWriteAndRecordPermission(context)
        }
    }

    private fun hasWriteAndRecordPermission(context: Activity): Boolean {
        return !(ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) !== PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) !== PackageManager.PERMISSION_GRANTED)
    }

    private fun applyWriteAndRecordPermission(context: Activity) {
        ActivityCompat.requestPermissions(
            context,
            arrayOf<String>(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ),
            1101
        )
    }

    /**
     * 跳转到设置页面申请打开无障碍辅助功能
     */
    private fun accessibilityToSettingPage(context: Context) {
        //开启辅助功能页面
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            e.printStackTrace()
        }
    }

    /**
     * 判断Service是否开启
     *
     */
    fun isServiceRunning(context: Context, ServiceName: String): Boolean {
        if (TextUtils.isEmpty(ServiceName)) {
            return false
        }
        val myManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningService =
            myManager.getRunningServices(1000) as ArrayList<ActivityManager.RunningServiceInfo>
        for (i in runningService.indices) {
            if (runningService[i].service.className == ServiceName) {
                return true
            }
        }
        return false
    }


    /**
     * 检查无障碍服务权限是否开启
     */
    fun isAccessibilityRunning(context: Activity): Boolean {
        return BlueService::class.java.canonicalName?.let {
            isServiceRunning(
                context,
                it
            )
        } == true
    }


    fun checkAccessibilityPermission(context: Activity) {
        if (!isAccessibilityRunning(context)) {
            accessibilityToSettingPage(context)
        }
    }

    fun isNull(any: Any?): Boolean = any == null

}