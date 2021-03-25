package com.example.ninelockview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomUnLockView customUnLockView = findViewById(R.id.culv);
        int[] a = new int[5];
        a[0] = 7;
        a[1] = 4;
        a[2] = 1;
        a[3] = 2;
        a[4] = 6;
        customUnLockView.setRightPwdStr(a);
        customUnLockView.setOnUnLockListener(new CustomUnLockView.OnUnLockListener() {
            @Override
            public void doUnLock() {
                Toast.makeText(MainActivity.this, "恭喜你，解锁成功", Toast.LENGTH_LONG).show();
            }
        });
    }

}