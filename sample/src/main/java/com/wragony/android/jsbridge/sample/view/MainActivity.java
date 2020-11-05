package com.wragony.android.jsbridge.sample.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wragony.android.jsbridge.sample.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_system_webView).setOnClickListener(this);
        findViewById(R.id.btn_custom_webwiew).setOnClickListener(this);
        findViewById(R.id.btn_fragment_webwiew).setOnClickListener(this);
        findViewById(R.id.btn_x5_webwiew).setOnClickListener(this);
        findViewById(R.id.btn_uc_webwiew).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_system_webView:
                startActivity(new Intent(this, SystemWebViewActivity.class));
                break;
            case R.id.btn_custom_webwiew:
                startActivity(new Intent(this, CustomWebViewActivity.class));
                break;
            case R.id.btn_fragment_webwiew:
                startActivity(new Intent(this, CustomFragmentActivity.class));
                break;
            case R.id.btn_x5_webwiew:
                startActivity(new Intent(this, X5WebViewActivity.class));
                break;
            case R.id.btn_uc_webwiew:
                startActivity(new Intent(this, UcWebViewActivity.class));
                break;
            default:
                break;
        }
    }
}
