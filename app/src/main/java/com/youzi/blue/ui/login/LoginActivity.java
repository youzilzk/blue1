package com.youzi.blue.ui.login;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.youzi.blue.MainActivity;
import com.youzi.blue.R;
import com.youzi.blue.service.BlueService;
import com.youzi.blue.utils.Help;
import com.youzi.blue.utils.OkHttp;
import com.youzi.blue.utils.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //权限检查
        Help.INSTANCE.checkBasePermission(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ignoreBatteryOptimization(this);
        }

        Button btn_login = findViewById(R.id.btn_login);
        EditText et_userName = findViewById(R.id.et_username);
        EditText et_password = findViewById(R.id.et_password);

        /*点击跳转至注册页面 【还没有账号？点击注册】按钮*/
        Button btn_register1 = findViewById(R.id.btn_register1);
        btn_register1.setOnClickListener(view -> {
            //点击按钮跳转到注册页面
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            //注册返回代码为0时，跳转
            startActivity(intent);
        });

        btn_login.setOnClickListener(view -> {
            //获取输入的密码框内容
            String etPassword = et_password.getText().toString().trim();
            //登录按钮获取要查询的账号
            String etUsername = et_userName.getText().toString().trim();

            //正则化判断输入的账号是否符合手机号格式
            if (!Utils.isTelPhoneNumber(etUsername)) {
                Toast.makeText(LoginActivity.this, "请输入正确的手机号！", Toast.LENGTH_SHORT).show();
            } else {
                //登录
                OkHttp.getInstance().httpGet("http://61.243.3.19:5000/user/auth?username=" + etUsername + "&password=" + etPassword, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("blue", "error");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        JSONObject jo = JSONObject.parseObject(new String(response.body().bytes()));

                        TextView login_show = findViewById(R.id.login_show);
                        String showText;

                        if ((Boolean) jo.get("result")) {
                            showText = "登录成功!";
                            //修改本地存储为已登录
                            getSharedPreferences("user", MODE_PRIVATE).edit().putString("username", etUsername).apply();

                            //退出登录并重新登录时, blue服务可能在运行, 就更新用户信息, 重新联网
                            if (Help.INSTANCE.isAccessibilityRunning(LoginActivity.this)) {
                                BlueService blueService = BlueService.instace;
                                blueService.updateUser();
                                try {
                                    blueService.getClientChannel().close().sync();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                blueService.tryReConnected();
                            }

                            //跳转到Mainactivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            //关闭登录页面
                            LoginActivity.this.finish();
                        } else {
                            showText = "登陆失败!" + jo.get("message");
                        }
                        new Thread(() -> {
                            login_show.setText(showText);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            login_show.setText("");
                        }).start();
                    }
                });
            }
        });
    }

    /**
     * 忽略电池优化
     */
    @SuppressLint("BatteryLife")
    @RequiresApi(Build.VERSION_CODES.M)
    private void ignoreBatteryOptimization(Activity activity) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            startActivity(intent);
        }
    }
}
