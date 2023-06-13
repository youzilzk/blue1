package com.youzi.blue.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class DBOpenHelper extends SQLiteOpenHelper {

    //定义创建用户数据表的SQL语句  主键user数据库表 username和password字段
    final String CREATE_USER_SQL =
            "create table user(_id integer primary  key autoincrement , username, password,loginState )";

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    @Override
    //数据库第一次创建时被调用
    public void onCreate(SQLiteDatabase db) {
        //创建用户列表 execSQL执行修改数据库内容的SQL语句
        db.execSQL(CREATE_USER_SQL);
    }

    @Override
    //版本号发生改变时使用
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //提示版本更新
        System.out.println("---版本更新----" + oldVersion + "--->" + newVersion);
    }


    //获取存储用户信息
    public Map<String, String> getUser() {
        Cursor cursor = getReadableDatabase().query("user", null, null, null, null, null, null);
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
    public Map<String, String> getUser(String username) {
        Cursor cursor = getReadableDatabase().query("user", null, "username = ?", new String[]{username}, null, null, null);
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

    public void updateLoginState(String username1, String loginState) {
        ContentValues values = new ContentValues();
        values.put("loginState", loginState);
        getReadableDatabase().update("user", values, "username=?", new String[]{username1});
    }

    public void updateLoginState(String loginState) {
        ContentValues values = new ContentValues();
        values.put("loginState", loginState);
        getReadableDatabase().update("user", values, null, null);
    }

    //创建数据库的insert方法 插入数据方法
    public void insertData(String username1, String password1) {
        getReadableDatabase().delete("user", null, null);
        ContentValues values = new ContentValues();
        values.put("username", username1);
        values.put("password", password1);
        values.put("loginState", "0");
        getReadableDatabase().insert("user", null, values);
    }
}
