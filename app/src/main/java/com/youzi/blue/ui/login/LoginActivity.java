package com.youzi.blue.ui.login;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.youzi.blue.MainActivity;
import com.youzi.blue.R;
import com.youzi.blue.db.DBOpenHelper;
import com.youzi.blue.utils.OkHttp;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    /*定义数据库所需成员变量 */
    private DBOpenHelper dbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*定义数据库对象 */
        dbOpenHelper = new DBOpenHelper(LoginActivity.this, "user.db", null, 1);

        Map<String, String> user = dbOpenHelper.getUser();
        if (user != null) {
            //已经登录, 直接跳转到Mainactivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            //关闭登录页面
            LoginActivity.this.finish();
        }

        Button btn_login = findViewById(R.id.btn_login);
        EditText et_userName = findViewById(R.id.et_username);
        EditText et_password = findViewById(R.id.et_password);

        /*点击跳转至注册页面 【还没有账号？点击注册】按钮*/
        Button btn_register1 = (Button) findViewById(R.id.btn_register1);
        btn_register1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击按钮跳转到注册页面
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                //注册返回代码为0时，跳转
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取输入的密码框内容
                String etPassword = et_password.getText().toString();
                //登录按钮获取要查询的账号
                String etUsername = et_userName.getText().toString();

                //正则化判断输入的账号是否符合手机号格式
                if (!isTelPhoneNumber(etUsername)) {
                    Toast.makeText(LoginActivity.this, "请输入正确的手机号！", Toast.LENGTH_SHORT).show();
                } else {
                    //登录
                    OkHttp.getInstance().httpGet("http://192.168.11.208:8008/user/auth?username=" + etUsername + "&password=" + etPassword, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("blue", "error");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            JSONObject jo = JSONObject.parseObject(new String(response.body().bytes()));

                            TextView login_show = (TextView) findViewById(R.id.login_show);
                            String showText;

                            if ((Boolean) jo.get("result")) {
                                showText = "登录成功!";
                                //修改本地存储为已登录
                                dbOpenHelper.updateUserInfo(etUsername, etPassword);
                                //跳转到Mainactivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                //关闭登录页面
                                LoginActivity.this.finish();
                            } else {
                                showText = "登陆失败!" + jo.get("message");
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    login_show.setText(showText);
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    login_show.setText("");
                                }
                            }).start();
                        }
                    });
                }
            }
        });
    }


    /*正则化验证手机号码*/
    public static boolean isTelPhoneNumber(String mobile) {
        if (mobile != null && mobile.length() == 11) {
            Pattern pattern = Pattern.compile("^1[3|4|5|6|7|8|9][0-9]\\d{8}$");
            Matcher matcher = pattern.matcher(mobile);
            return matcher.matches();
        } else {
            return false;
        }
    }

    //    //重写onDestroy()方法
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbOpenHelper != null) {
            dbOpenHelper.close();
        }
    }
}
