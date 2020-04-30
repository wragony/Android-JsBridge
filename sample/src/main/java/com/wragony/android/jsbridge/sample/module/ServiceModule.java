package com.wragony.android.jsbridge.sample.module;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.module.JSBridgeMethod;
import com.wragony.android.jsbridge.module.JsModule;
import com.wragony.android.jsbridge.module.datatype.JBArray;
import com.wragony.android.jsbridge.module.datatype.JBCallback;
import com.wragony.android.jsbridge.module.datatype.JBMap;
import com.wragony.android.jsbridge.module.datatype.WritableJBArray;
import com.wragony.android.jsbridge.module.datatype.WritableJBMap;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceModule extends JsModule {

    @Override
    public String getModuleName() {
        return "service";
    }

    @JSBridgeMethod(methodName = "hiShare")
    public void share(String msg, final JBCallback success, final JBCallback failure) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(Intent.createChooser(intent, "share"));
            success.apply("success");
        } else {
            failure.apply("failure");
        }
    }

    @JSBridgeMethod
    public void ajax(JBMap dataMap) {
        final String type = dataMap.getString("type");
        final String url = dataMap.getString("url");
        JBMap data = dataMap.getJBMap("data");
        final StringBuilder params = new StringBuilder();
        for (String key : data.keySet()) {
            params.append(key + "=" + data.get(key) + "&");
        }
        final JBCallback successCallback = dataMap.getCallback("success");
        final JBCallback errorCallback = dataMap.getCallback("error");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod(type);
                    conn.setDoOutput(true);
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(params.toString());
                    dos.flush();
                    dos.close();
                    int resultCode = conn.getResponseCode();
                    if (HttpURLConnection.HTTP_OK == resultCode) {
                        StringBuffer sb = new StringBuffer();
                        String readLine;
                        BufferedReader responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        while ((readLine = responseReader.readLine()) != null) {
                            sb.append(readLine).append("\n");
                        }
                        responseReader.close();
                        successCallback.apply(sb.toString());
                    } else {
                        errorCallback.apply("server response error:" + resultCode);
                    }
                } catch (IOException e) {
                    errorCallback.apply(e.getMessage());
                }
            }
        }).start();
    }

    @JSBridgeMethod
    public void test(JBArray array) {
        for (int i = 0; i < array.size(); i++) {
            String output = "" + array.get(i);
            if (array.get(i) != null) {
                output += "##" + array.get(i).getClass();
            }
            Log.d(JsBridge.TAG, output);
        }
        array.getCallback(4).apply("xxx");
        Log.d(JsBridge.TAG, "ret=" + array.getMap(5).getInt("a"));
        array.getMap(5).getCallback("b").apply();
    }

    @JSBridgeMethod
    public void testReturn(JBCallback callback) {
        WritableJBArray jbArray = new WritableJBArray.Create();
        jbArray.pushInt(1);
        jbArray.pushString("34");
        jbArray.pushInt(3);
        WritableJBMap jbMap = new WritableJBMap.Create();
        jbMap.putString("a", "hello");
        jbMap.putString("b", "world");
        callback.apply("\"''haha'\"", jbArray, jbMap);

        String message = jbMap.toString() + "\n" + jbArray.toString();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        Log.d(JsBridge.TAG, message);
        Log.d(JsBridge.TAG, getContext() + "#" + getWebViewObject());
    }
}
