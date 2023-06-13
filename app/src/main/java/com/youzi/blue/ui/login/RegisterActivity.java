package com.youzi.blue.ui.login;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.youzi.blue.R;
import com.youzi.blue.db.DBOpenHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    //存储注册的用户名和密码 把账号密码存储进数据库
                    dbOpenHelper.insertData(et_name, et_password);
                    Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                }
                //关闭注册页面 跳转到登录页面
                RegisterActivity.this.finish();
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
