package com.youzi.blue

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.SparseArray
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.youzi.blue.network.client.manager.Manager
import com.youzi.blue.ui.HomeFragment
import com.youzi.blue.ui.SettingFragment
import com.youzi.blue.ui.SignActivity
import com.youzi.blue.utils.Utils


class MainActivity : AppCompatActivity() {
    private var mTabRadioGroup: RadioGroup? = null
    private var mFragmentSparseArray: SparseArray<Fragment>? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Manager.initContext(applicationContext)

        setContentView(R.layout.activity_main)
        initView()

        //权限检查
        Utils.checkBasePermission(this)
        ignoreBatteryOptimization(this)
    }

    /**
     * 忽略电池优化
     */
    @SuppressLint("BatteryLife")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun ignoreBatteryOptimization(activity: Activity) {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.packageName)
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + activity.packageName)
            startActivity(intent)
        }
    }

    private fun initView() {
        mTabRadioGroup = findViewById(R.id.tabs_rg)
        mFragmentSparseArray = SparseArray<Fragment>()
        mFragmentSparseArray!!.append(R.id.home_tab, HomeFragment.newInstance("首页"))
        mFragmentSparseArray!!.append(R.id.settings_tab, SettingFragment.newInstance("设置"))
        mTabRadioGroup!!.setOnCheckedChangeListener { group, checkedId -> // 具体的fragment切换逻辑可以根据应用调整，例如使用show()/hide()
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container, mFragmentSparseArray!![checkedId]
            ).commit()
        }
        // 默认显示第一个
        supportFragmentManager.beginTransaction().add(
            R.id.fragment_container, mFragmentSparseArray!![R.id.home_tab]
        ).commit()

        findViewById<ImageView>(R.id.sign_iv).setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    SignActivity::class.java
                )
            )
        }
    }
}