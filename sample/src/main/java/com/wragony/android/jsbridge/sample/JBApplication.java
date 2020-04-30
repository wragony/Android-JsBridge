package com.wragony.android.jsbridge.sample;

import android.app.Application;
import android.util.Log;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.smtt.sdk.QbSdk;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.JsBridgeConfig;
import com.wragony.android.jsbridge.sample.module.StaticModule;

public class JBApplication extends Application {

    private static JBApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        initX5();

        LeakCanary.install(this);

        JsBridgeConfig.getSetting().setProtocol("window").registerDefaultModule(StaticModule.class).debugMode(true);

    }

    public static JBApplication getInstance() {
        return instance;
    }

    /**
     * init X5
     */
    private void initX5() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback preInitCallback = new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.d(JsBridge.TAG, " onCoreInitFinished");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d(JsBridge.TAG, " onViewInitFinished is " + b);
            }

        };
        //x5 webview init
        QbSdk.initX5Environment(this, preInitCallback);
    }


}
