package com.youzi.blue.ui.login;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import com.youzi.blue.MainActivity;
import com.youzi.blue.R;
import com.youzi.blue.db.DBOpenHelper;
import com.youzi.blue.net.client.entity.User;
import com.youzi.blue.net.common.FormData;
import com.youzi.blue.utils.OkHttp;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private Button btn_register;
    private EditText et_register_username, et_register_password, et_again_password;
    /*数据库成员变量*/
    private DBOpenHelper dbOpenHelper;

    String et_name;
    String et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //注册按钮
        btn_register = (Button) findViewById(R.id.btn_register);
        //用户名编辑框
        et_register_username = findViewById(R.id.et_register_username);
        //密码编辑框
        et_register_password = findViewById(R.id.et_register_password);
        //再次输入密码编辑框
        et_again_password = findViewById(R.id.et_again_password);

        /*实例化数据库变量dbOpenHelper*/
        dbOpenHelper = new DBOpenHelper(RegisterActivity.this, "user.db", null, 1);

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
                    OkHttp.getInstance().httpPost("http://192.168.1.12:8008/user/register", formBody, new Callback() {
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
                                //存储注册的用户名和密码 把账号密码存储进数据库
                                dbOpenHelper.updateUserInfo(et_name, et_password);
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


    //重写onDestroy()方法
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbOpenHelper != null) {
            dbOpenHelper.close();
        }
    }
}
