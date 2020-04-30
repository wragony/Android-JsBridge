package com.wragony.android.jsbridge.sample.module;

import android.util.Log;
import android.widget.Toast;
import com.wragony.android.jsbridge.JsBridge;
import com.wragony.android.jsbridge.module.JSBridgeMethod;
import com.wragony.android.jsbridge.module.JsStaticModule;
import com.wragony.android.jsbridge.module.datatype.JBCallback;
import com.wragony.android.jsbridge.module.datatype.JBMap;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StaticModule extends JsStaticModule {

    @JSBridgeMethod
    public void alert(String msg, JBCallback callback) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @JSBridgeMethod
    public String mapToString(JBMap map) {
        Log.e(JsBridge.TAG, map.getString("data"));
        return map.getString("data");
    }

    @JSBridgeMethod
    public int getVersion() {
        String number = new SimpleDateFormat("YYYYMMdd").format(Calendar.getInstance().getTime());
        Toast.makeText(getContext(), number, Toast.LENGTH_SHORT).show();
        return Integer.parseInt(number);
    }
}
