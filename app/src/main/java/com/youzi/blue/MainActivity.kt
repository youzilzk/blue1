package com.youzi.blue


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.youzi.blue.ui.HomeFragment
import com.youzi.blue.ui.SettingFragment
import com.youzi.blue.ui.SignActivity
import com.youzi.blue.ui.login.LoginActivity


class MainActivity : AppCompatActivity() {
    private var mTabRadioGroup: RadioGroup? = null
    private var mFragmentSparseArray: SparseArray<Fragment>? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //隐藏自带的标题栏,自己画一个, 为了在标题栏上带"+"号,设置页也需要自己画,因为共用了一个activity
        this.supportActionBar?.hide();

        val username: String =
            getSharedPreferences("user", MODE_PRIVATE).getString("username", "") as String
        if (username == "") {
            //未登录, 直接跳转到LoginActivity
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            //关闭登录页面
            this@MainActivity.finish()
        }

        setContentView(R.layout.activity_main)
        initView()
    }



    private fun initView() {
        mTabRadioGroup = findViewById(R.id.tabs_rg)
        mFragmentSparseArray = SparseArray<Fragment>()
        mFragmentSparseArray!!.append(R.id.home_tab, HomeFragment.newInstance("首页"))
        mFragmentSparseArray!!.append(R.id.settings_tab, SettingFragment.newInstance("设置"))

        // 默认显示第一个
        supportFragmentManager.beginTransaction().add(
            R.id.fragment_container, mFragmentSparseArray!![R.id.home_tab]
        ).commit()

        //具体的fragment切换逻辑可以根据应用调整，例如使用show()/hide()
        mTabRadioGroup!!.setOnCheckedChangeListener { _, checkedId ->
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container, mFragmentSparseArray!![checkedId]
            ).commit()
        }

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