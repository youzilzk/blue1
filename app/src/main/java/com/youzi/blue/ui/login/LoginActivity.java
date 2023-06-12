package com.youzi.blue.ui.login;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.youzi.blue.MainActivity;
import com.youzi.blue.R;
import com.youzi.blue.db.DBOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    //定义登录Button 编辑框
    private Button btn_login;
    private EditText et_password, et_userName;
    /*定义数据库所需成员变量 */
    private DBOpenHelper dbOpenHelper;
    //数据库里存储的username,password
    String username;
    String dbpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ListView test_text = (ListView) findViewById(R.id.test_text);

        /*定义数据库对象 */
        dbOpenHelper = new DBOpenHelper(LoginActivity.this, "user.db", null, 1);

        Map<String, String> user = getUser();
        if (user != null && user.get("loginState").equals("1")) {
            //已经登录, 直接跳转到Mainactivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            //关闭登录页面
            LoginActivity.this.finish();
        }

        //初始化
        initView();
        //注册完之后更新

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

        //登录按钮单击事件
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取输入的密码框内容
                String etpassword = et_password.getText().toString();
                /*获取数据库里的数据*/
                //登录按钮获取要查询的账号
                String key = et_userName.getText().toString();
                Map<String, String> user = getUser(key);
                //正则化判断输入的账号是否符合手机号格式
                if (!isTelPhoneNumber(key)) {
                    Toast.makeText(LoginActivity.this, "请输入正确的手机号！", Toast.LENGTH_SHORT).show();
                } else if (user == null) { //如果数据库中没有查询的用户数据
                    //显示提示信息，没有相关记录
                    Toast.makeText(LoginActivity.this,
                            "该用户名未注册，请先注册", Toast.LENGTH_LONG).show();
                } else {
                    username = user.get("username");
                    dbpassword = user.get("password");

                    SimpleAdapter simpleAdapter = new SimpleAdapter(LoginActivity.this, Collections.singletonList(user),
                            R.layout.userdata_main, new String[]{"username", "password"}, new int[]{R.id.result_name, R.id.result_grade});
                    //将适配器和测试的listview关联，我这里的listview叫test_text
                    test_text.setAdapter(simpleAdapter);

                    //查到了用户 对比输入的密码与数据库的密码是否一致 如果相等跳转到主页面去
                    if (etpassword.equals(dbpassword)) {
                        Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                        //登录状态为已登录, 下次直接进入主页
                        updateLoginState(username, "1");
                        //跳转到Mainactivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        //关闭登录页面
                        LoginActivity.this.finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "密码错误！", Toast.LENGTH_SHORT).show();
                    }
                }
                ;
            }
        });
    }

    //获取存储用户信息
    private Map<String, String> getUser() {
        Cursor cursor = dbOpenHelper.getReadableDatabase().query("user", null, null, null, null, null, null);
        //将结果集中的数据存入HashMap

        if (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            //取出查询结果第二列和第三列的值
            //用户名
            map.put("username", cursor.getString(1));
            //状态
            map.put("loginState", cursor.getString(3));
            return map;
        }
        return null;
    }

    //获取存储用户信息
    private Map<String, String> getUser(String username) {
        Cursor cursor = dbOpenHelper.getReadableDatabase().query("user", null, "username = ?", new String[]{username}, null, null, null);
        //将结果集中的数据存入HashMap

        if (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            //取出查询结果第二列和第三列的值
            //用户名
            map.put("username", cursor.getString(1));
            //密码
            map.put("password", cursor.getString(2));
            return map;
        }
        return null;
    }

    private void updateLoginState(String username1, String loginState) {
        ContentValues values = new ContentValues();
        values.put("loginState", loginState);
        dbOpenHelper.getReadableDatabase().update("user", values, "username=?", new String[]{username1});
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

    //定义初始化
    private void initView() {
        btn_login = findViewById(R.id.btn_login);
        et_userName = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
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
