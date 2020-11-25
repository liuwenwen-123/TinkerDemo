package com.lww.tinkerfixdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("eee", getClassLoader().getClass().getName());
        requestReadExternalPermission();
    }

    public void shoxwbug(View view) {
        Test test = new Test();
        test.test();
    }

    public void fix(View view) {

//        将 dex 放到稀有目录
//        得到私有目录下的 odex 的文件夹
        File odex = this.getDir("odex", Context.MODE_PRIVATE);
//        获取到补丁的名字（真实开发 由服务器告诉我们）
        String name = "fix.dex";
//        获取复制过去的路径的file 对象
        File file = new File(odex.getAbsoluteFile(), name);
        String fileParh = file.getAbsolutePath();
//        判断 这个补丁是否存在
        if (file.exists()) {
            file.delete();
        }

//         创建输入输出流
        FileOutputStream outputStream = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(Environment.getExternalStorageDirectory(), name));
            outputStream = new FileOutputStream(fileParh);

            byte[] bytes = new byte[1024];
            int num = 0;
            while ((num = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, num);
                outputStream.flush();
            }
            File f = new File(fileParh);
//             去加载dex  然后合并到当前app的dex 数组中
//           这行 代码最好放在 application 中
            FixManager.loadDex(MainActivity.this);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @SuppressLint("NewApi")
    private void requestReadExternalPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        } else {
        }
    }

    @SuppressLint("NewApi")
    private void requestWriteExternalPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
        }
    }
}