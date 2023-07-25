package com.youzi.blue.ui.login;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.youzi.blue.R;
import com.youzi.blue.network.client.entity.User;
import com.youzi.blue.network.common.FormData;
import com.youzi.blue.utils.OkHttp;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText et_register_username, et_register_password, et_again_password;

    SharedPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userPreferences = getSharedPreferences("user", MODE_PRIVATE);

        //注册按钮
        Button btn_register = (Button) findViewById(R.id.btn_register);
        //用户名编辑框
        et_register_username = findViewById(R.id.et_register_username);
        //密码编辑框
        et_register_password = findViewById(R.id.et_register_password);
        //再次输入密码编辑框
        et_again_password = findViewById(R.id.et_again_password);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取三个编辑框的内容
                String et_name = et_register_username.getText().toString();
                String et_password = et_register_password.getText().toString();
                String et_confirm = et_again_password.getText().toString();

                //判断异常情况弹窗
                //编辑框为空
                if (TextUtils.isEmpty(et_name)) {
                    Toast.makeText(RegisterActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
                    //对用户名进行手机号正则化验证，调用下面写的idTelPhoneNumber方法
                } else if (!isTelPhoneNumber(et_name)) {
                    Toast.makeText(RegisterActivity.this, "请输入正确的手机号码！", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(et_password)) {
                    Toast.makeText(RegisterActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
                    //两次密码框内容不一致
                } else if (!TextUtils.equals(et_password, et_confirm)) {
                    Toast.makeText(RegisterActivity.this, "密码不一致！", Toast.LENGTH_SHORT).show();
                } else {
                    //注册
                    User user = new User(null, et_name, et_password, null, null, null);
                    FormBody formBody = null;
                    try {
                        formBody = new FormData<User>().get(user);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    OkHttp.getInstance().httpPost("http://61.243.3.19:5000/user/register", formBody, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("blue", "error");
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            JSONObject jo = JSONObject.parseObject(new String(response.body().bytes()));

                            TextView show_message = (TextView) findViewById(R.id.show_message);
                            String showText;

                            if ((Boolean) jo.get("result")) {
                                //修改本地存储为已登录
                                userPreferences.edit().putString("username", et_name).apply();
                                showText = "注册成功!";
                                //关闭注册页面 跳转到登录页面
                                RegisterActivity.this.finish();
                            } else {
                                showText = "注册失败!" + jo.get("message");
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    show_message.setText(showText);
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    show_message.setText("");
                                }
                            }).start();
                        }
                    });
                }
            }
        });
    }

    /*正则化验证手机号码方法*/
    public static boolean isTelPhoneNumber(String mobile) {
        if (mobile != null && mobile.length() == 11) {
            Pattern pattern = Pattern.compile("^1[3|4|5|6|7|8|9][0-9]\\d{8}$");
            Matcher matcher = pattern.matcher(mobile);
            return matcher.matches();
        } else {
            return false;
        }
    }
}
