package com.youzi.blue.ui;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.youzi.blue.R;
import com.youzi.blue.utils.OkHttp;
import com.youzi.blue.utils.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EditDialog extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_dialog);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText watchUserB = findViewById(R.id.watchUser);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText tokenB = findViewById(R.id.token);

        TextView upBoxTitle = findViewById(R.id.upBoxTitle);
        Bundle bundle = getIntent().getExtras();

        //编辑框: 1-添加,2-编辑
        assert bundle != null;
        int editDialogType = bundle.getInt("type");

        if (editDialogType == 1) {
            upBoxTitle.setText("添加");
        } else if (editDialogType == 2) {
            upBoxTitle.setText("编辑");
            //回显, 禁止输入
            String watchUser = bundle.getString("watchUser");
            watchUserB.setText(watchUser);
            watchUserB.setBackgroundResource(R.drawable.textview_border);
            watchUserB.setFocusable(false);
            watchUserB.setFocusableInTouchMode(false);
        }

        //设置对话框activity的宽度等于屏幕宽度，一定要设置，不然对话框会显示不全
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button add = findViewById(R.id.add);
        Button cancel = findViewById(R.id.cancel);


        add.setOnClickListener(v -> {
            String watchUser = watchUserB.getText().toString();
            String token = tokenB.getText().toString();

            //正则化判断输入的账号是否符合手机号格式
            if (!Utils.isTelPhoneNumber(watchUser)) {
                Toast.makeText(EditDialog.this, "请输入正确的手机号！", Toast.LENGTH_SHORT).show();
                return;
            }
            //正则化判断输入的验证码是否符合6位数字
            if (!Utils.isToken(token)) {
                Toast.makeText(EditDialog.this, "请输入6位数字验证码！", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = getSharedPreferences("user", AccessibilityService.MODE_PRIVATE).getString("username", null);
            OkHttp.getInstance().httpGet("http://61.243.3.19:5000/user/updateDevice?username=" + username + "&watchUser=" + watchUser + "&token=" + token, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    EditDialog.this.runOnUiThread(() -> Toast.makeText(EditDialog.this, "网络错误!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    JSONObject jo = JSONObject.parseObject(new String(response.body().bytes()));
                    Boolean result = (Boolean) jo.get("result");

                    String showText = jo.getString("message");

                    EditDialog.this.runOnUiThread(() -> Toast.makeText(EditDialog.this, showText, Toast.LENGTH_SHORT).show());
                    //添加成功, 关闭对话框
                    if (Boolean.TRUE.equals(result)) {
                        finish();
                    }
                }
            });
        });
        cancel.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
