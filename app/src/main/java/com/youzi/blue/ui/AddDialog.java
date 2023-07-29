package com.youzi.blue.ui;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.youzi.blue.R;
import com.youzi.blue.utils.OkHttp;
import com.youzi.blue.utils.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddDialog extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device_dialog);
        //设置对话框activity的宽度等于屏幕宽度，一定要设置，不然对话框会显示不全
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//需要添加的语句

        Button add = (Button) findViewById(R.id.add);
        Button cancel = (Button) findViewById(R.id.cancel);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText watchUserB = findViewById(R.id.watchUser);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText tokenB = findViewById(R.id.token);

        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String watchUser = watchUserB.getText().toString();
                String token = tokenB.getText().toString();

                //正则化判断输入的账号是否符合手机号格式
                if (!Utils.isTelPhoneNumber(watchUser)) {
                    Toast.makeText(AddDialog.this, "请输入正确的手机号！", Toast.LENGTH_SHORT).show();
                    return;
                }
                //正则化判断输入的验证码是否符合6位数字
                if (!Utils.isToken(token)) {
                    Toast.makeText(AddDialog.this, "请输入6位数字验证码！", Toast.LENGTH_SHORT).show();
                    return;
                }

                String username = getSharedPreferences("user", AccessibilityService.MODE_PRIVATE).getString("username", null);
                OkHttp.getInstance().httpGet("http://61.243.3.19:5000/user/addDevice?username=" + username + "&watchUser=" + watchUser + "&token=" + token, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        AddDialog.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddDialog.this, "网络错误!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        JSONObject jo = JSONObject.parseObject(new String(response.body().bytes()));
                        Boolean result = (Boolean) jo.get("result");

                        String showText = jo.getString("message");

                        AddDialog.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddDialog.this, showText, Toast.LENGTH_SHORT).show();
                            }
                        });
                        //添加成功, 关闭对话框
                        if (Boolean.TRUE.equals(result)) {
                            finish();
                        }
                    }
                });
            }
        });
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
