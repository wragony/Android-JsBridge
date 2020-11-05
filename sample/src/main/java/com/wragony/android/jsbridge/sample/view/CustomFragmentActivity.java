package com.wragony.android.jsbridge.sample.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wragony.android.jsbridge.sample.R;

public class CustomFragmentActivity extends AppCompatActivity {

    public static final String TAG = "CustomFragmentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        getSupportFragmentManager().beginTransaction().add(R.id.content, new CustomFragment(), TAG).commit();
    }

}
