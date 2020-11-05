package com.wragony.android.jsbridge;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.JsPromptResult;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wragony.android.jsbridge.common.IPromptResult;
import com.wragony.android.jsbridge.common.IWebView;
import com.wragony.android.jsbridge.common.OnValueCallback;
import com.wragony.android.jsbridge.exception.JBArgumentErrorException;
import com.wragony.android.jsbridge.module.JsModule;
import com.wragony.android.jsbridge.module.JsStaticModule;
import com.wragony.android.jsbridge.module.datatype.JSArgumentType;
import com.wragony.android.jsbridge.module.datatype.JsObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

class JsBridgeImpl extends JsBridge {

    private Object mWebView;
    private final JsBridgeConfigImpl config;
    private final List<JsModule> loadModule;
    private final Map<JsModule, HashMap<String, JsMethod>> exposedMethods;
    private final String className;
    private String preLoad;
    private final Handler handler;
    private final Set<String> moduleLayers;
    private String newProtocol;
    private String newLoadReadyMethod;

    JsBridgeImpl(JsModule... modules) {
        this(null, null, modules);
    }

    JsBridgeImpl(String protocol, String readyMethod, JsModule... modules) {
        config = JsBridgeConfigImpl.getInstance();
        className = "JB_" + Integer.toHexString(hashCode());
        loadModule = new ArrayList<>();
        exposedMethods = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());
        moduleLayers = new HashSet<>();
        this.newProtocol = protocol;
        this.newLoadReadyMethod = readyMethod;
        if (TextUtils.isEmpty(newProtocol)) {
            newProtocol = config.getProtocol();
        }
        if (TextUtils.isEmpty(newLoadReadyMethod)) {
            newLoadReadyMethod = config.getReadyFuncName();
        }
        loadingModule(modules);
        JBLog.d(String.format("Protocol:%s, LoadReadyMethod:%s, moduleSize:%s",
                newProtocol, newLoadReadyMethod, loadModule.size()));
    }

    /**
     * load module
     */
    private void loadingModule(JsModule... modules) {
        try {
            for (Class<? extends JsModule> moduleCls : config.getDefaultModule()) {
                JsModule module = moduleCls.newInstance();
                if (module != null && !TextUtils.isEmpty(module.getModuleName())) {
                    loadModule.add(module);
                }
            }
            if (modules != null) {
                for (JsModule module : modules) {
                    if (module != null && !TextUtils.isEmpty(module.getModuleName())) {
                        loadModule.add(module);
                    }
                }
            }
            if (!loadModule.isEmpty()) {
                Collections.sort(loadModule, new ModuleComparator());
                for (JsModule module : loadModule) {
                    HashMap<String, JsMethod> methodsMap = JBUtils.getAllMethod(
                            module, module.getClass(), newProtocol);
                    exposedMethods.put(module, methodsMap);
                }
            }
        } catch (Exception e) {
            JBLog.e("loadingModule error", e);
        }
    }

    @Override
    public final void injectJs(@NonNull WebView webView) {
        onInjectJs(webView.getContext(), webView);
    }

    @Override
    public final void injectJs(@NonNull IWebView webView) {
        onInjectJs(webView.getContext(), webView);
    }

    private void onInjectJs(final Context context, final Object webView) {
        this.mWebView = webView;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (preLoad == null) {
                    if (TextUtils.isEmpty(newProtocol) || JBUtils.PROTOCOL_WINDOW.equals(newProtocol)) {
                        preLoad = getInjectJsGlobalString();
                    } else {
                        preLoad = getInjectJsString();
                    }
                }
                for (JsModule module : loadModule) {
                    // 监听方法不需要注入 Context
                    if (exposedMethods.get(module) == null || exposedMethods.get(module).isEmpty()) {
                        continue;
                    }
                    // 为JsModule设置context 和 WebView
                    if (module.mContext != null && module.mContext.getClass().equals(context.getClass())) {
                        break;
                    }
                    try {
                        Field contextField = module.getClass().getField("mContext");
                        if (contextField != null) {
                            contextField.set(module, context);
                        }
                        Field webViewField = module.getClass().getField("mWebView");
                        if (webViewField != null) {
                            webViewField.set(module, webView);
                        }
                    } catch (Exception e) {
                        JBLog.e("JsModule set Context Error", e);
                    }
                }
                evaluateJavascript(preLoad);
                JBLog.d("onInjectJs finish");
            }
        }, "JsBridgeThread").start();
    }

    @Override
    public final boolean callJsPrompt(@NonNull String methodArgs, @NonNull JsPromptResult result) {
        return onCallJsPrompt(methodArgs, result);
    }

    @Override
    public final boolean callJsPrompt(@NonNull String methodArgs, @NonNull IPromptResult result) {
        return onCallJsPrompt(methodArgs, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return onCallJsPrompt(message, result);
    }

    @Override
    public final void clean() {
        evaluateJavascript(newProtocol + "=undefined;");
    }

    @Override
    public final void release() {
        for (JsModule module : exposedMethods.keySet()) {
            module.mWebView = null;
            module.mContext = null;
        }
        exposedMethods.clear();
        JBLog.d("JsBridge destroy");
    }

    @Override
    public void evaluateJavascript(@NonNull final String jsCode) {
        if (mWebView == null) {
            JBLog.d("Please call injectJs first");
            return;
        }
        handler.post(new Runnable() {

            @Override
            public void run() {
                JBUtils.evalJs(mWebView, jsCode, null);
            }
        });
    }

    @Override
    public void callJs(final String functionName, final String params, final OnValueCallback mCallback) {
        if (mWebView == null) {
            JBLog.d("Please call injectJs first");
            return;
        }
        handler.post(new Runnable() {

            @Override
            public void run() {
                StringBuilder builder = new StringBuilder();
                builder.append(functionName);
                builder.append("(");
                if (!TextUtils.isEmpty(params)) {
                    builder.append("'");
                    builder.append(params);
                    builder.append("'");
                }
                builder.append(");");
                String jsCode = builder.toString();

                JBUtils.evalJs(mWebView, jsCode, mCallback);
            }
        });
    }

    @Override
    public void callJs(final String functionName, final JsObject params, final OnValueCallback mCallback) {
        if (mWebView == null) {
            JBLog.d("Please call injectJs first");
            return;
        }
        handler.post(new Runnable() {

            @Override
            public void run() {
                StringBuilder builder = new StringBuilder();
                builder.append(functionName);
                builder.append("(");
                if (null != params) {
                    builder.append(params.convertJS());
                }
                builder.append(");");
                String jsCode = builder.toString();

                JBUtils.evalJs(mWebView, jsCode, mCallback);
            }
        });
    }

    @Nullable
    private JsModule getModule(String moduleName) {
        if (TextUtils.isEmpty(moduleName)) {
            return null;
        }
        for (JsModule module : exposedMethods.keySet()) {
            if (moduleName.equals(module.getModuleName())) {
                return module;
            }
        }
        return null;
    }

    /**
     * 注入到全局window对象上
     *
     * @return
     * @author wragony
     */
    private String getInjectJsGlobalString() {
        StringBuilder builder = new StringBuilder();
        // 注入通用方法
        builder.append(JBUtilMethodFactory.getUtilMethods(newLoadReadyMethod));
        // 注入默认方法
        for (JsModule module : loadModule) {
            HashMap<String, JsMethod> methods = exposedMethods.get(module);
            if (methods == null) {
                continue;
            }
            if (module instanceof JsStaticModule) {
                for (String method : methods.keySet()) {
                    JsMethod jsMethod = methods.get(method);
                    builder.append(jsMethod.getInjectJs());
                }
            } else {
                List<String> moduleGroup = JBUtils.moduleSplit(module.getModuleName());
                if (moduleGroup.isEmpty()) {
                    continue;
                } else {
                    for (int i = 0; i < moduleGroup.size() - 1; ++i) {
                        if (!moduleLayers.contains(moduleGroup.get(i))) {
                            for (int k = i; k < moduleGroup.size() - 1; ++k) {
                                String moduleName = moduleGroup.get(k);
                                if (moduleName.contains(".")) {
                                    builder.append(moduleGroup.get(k) + " = {};");
                                } else {
                                    builder.append("var " + moduleGroup.get(k) + " = {};");
                                }
                                moduleLayers.add(moduleGroup.get(k));
                            }
                            break;
                        }
                    }
                    String moduleName = module.getModuleName();
                    if (moduleName.contains(".")) {
                        builder.append(module.getModuleName() + " = {");
                    } else {
                        builder.append("var " + module.getModuleName() + " = {");
                    }
                    moduleLayers.add(module.getModuleName());
                }
                for (String method : methods.keySet()) {
                    JsMethod jsMethod = methods.get(method);
                    builder.append(jsMethod.getInjectJs());
                }
                builder.append("};");
            }
        }
        builder.append("window.OnJsBridgeReady();");
        return builder.toString();
    }

    private String getInjectJsString() {
        StringBuilder builder = new StringBuilder();
        builder.append("var ").append(className).append("=function(){");
        // 注入通用方法
        builder.append(JBUtilMethodFactory.getUtilMethods(newLoadReadyMethod));
        // 注入默认方法
        for (JsModule module : loadModule) {
            HashMap<String, JsMethod> methods = exposedMethods.get(module);
            if (methods == null) {
                continue;
            }
            if (module instanceof JsStaticModule) {
                for (String method : methods.keySet()) {
                    JsMethod jsMethod = methods.get(method);
                    builder.append(jsMethod.getInjectJs());
                }
            } else {
                List<String> moduleGroup = JBUtils.moduleSplit(module.getModuleName());
                if (moduleGroup.isEmpty()) {
                    continue;
                } else {
                    for (int i = 0; i < moduleGroup.size() - 1; ++i) {
                        if (!moduleLayers.contains(moduleGroup.get(i))) {
                            for (int k = i; k < moduleGroup.size() - 1; ++k) {
                                builder.append(className + ".prototype." + moduleGroup.get(k) + " = {};");
                                moduleLayers.add(moduleGroup.get(k));
                            }
                            break;
                        }
                    }
                    builder.append(className + ".prototype." + module.getModuleName() + " = {");
                    moduleLayers.add(module.getModuleName());
                }
                for (String method : methods.keySet()) {
                    JsMethod jsMethod = methods.get(method);
                    builder.append(jsMethod.getInjectJs());
                }
                builder.append("};");
            }
        }
        builder.append("};");
        builder.append("window." + newProtocol + "=new " + className + "();");
        builder.append(newProtocol + ".OnJsBridgeReady();");
        return builder.toString();
    }

    /**
     * onJsPrompt 处理
     *
     * @param methodArgs
     * @param result
     */
    private boolean onCallJsPrompt(String methodArgs, Object result) {
        JBLog.d("callJsPrompt: " + methodArgs);
        if (TextUtils.isEmpty(methodArgs) || result == null) {
            return false;
        }
        JBArgumentParser argumentParser = JBArgumentParser.parse(methodArgs);
        if (argumentParser.isSuccess() && !TextUtils.isEmpty(argumentParser.getModule())
                && !TextUtils.isEmpty(argumentParser.getMethod())) {
            JsModule findModule = getModule(argumentParser.getModule());
            if (findModule != null) {
                HashMap<String, JsMethod> methodHashMap = exposedMethods.get(findModule);
                if (methodHashMap != null && !methodHashMap.isEmpty() && methodHashMap.containsKey(
                        argumentParser.getMethod())) {
                    JsMethod method = methodHashMap.get(argumentParser.getMethod());
                    List<JBArgumentParser.Parameter> parameters = argumentParser.getParameters();
                    int length = method.getParameterType().size();
                    Object[] invokeArgs = new Object[length];
                    for (int i = 0; i < length; ++i) {
                        @JSArgumentType.Type int type = method.getParameterType().get(i);
                        if (parameters != null && parameters.size() >= i + 1) {
                            JBArgumentParser.Parameter param = parameters.get(i);
                            Object parseObject = JBUtils.parseToObject(type, param, method);
                            if (parseObject instanceof JBArgumentErrorException) {
                                setJsPromptResult(result, false, parseObject.toString());
                                return true;
                            }
                            invokeArgs[i] = parseObject;
                        }
                        if (invokeArgs[i] == null) {
                            switch (type) {
                                case JSArgumentType.TYPE_NUMBER:
                                    invokeArgs[i] = 0;
                                    break;
                                case JSArgumentType.TYPE_BOOL:
                                    invokeArgs[i] = false;
                                    break;
                                case JSArgumentType.TYPE_ARRAY:
                                case JSArgumentType.TYPE_DOUBLE:
                                case JSArgumentType.TYPE_FLOAT:
                                case JSArgumentType.TYPE_FUNCTION:
                                case JSArgumentType.TYPE_INT:
                                case JSArgumentType.TYPE_LONG:
                                case JSArgumentType.TYPE_OBJECT:
                                case JSArgumentType.TYPE_STRING:
                                case JSArgumentType.TYPE_UNDEFINE:
                                default:
                                    break;
                            }
                        }
                    }
                    try {
                        Object ret = method.invoke(invokeArgs);
                        setJsPromptResult(result, true, ret == null ? "" : ret);
                    } catch (Exception e) {
                        Throwable throwable = e;
                        if (e instanceof InvocationTargetException) {
                            throwable = ((InvocationTargetException) e).getTargetException();
                        }
                        setJsPromptResult(result, false, "Error: " + throwable.toString());
                        JBLog.e("Call JsMethod <" + method.getMethodName() + "> Error", e);
                    }
                    return true;
                }
            }
            setJsPromptResult(result, false, "JBArgument Parse error");
            return true;
        }
        JBLog.e("JBArgument error", argumentParser.getThrowable());
        setJsPromptResult(result, false, argumentParser.getErrorMsg());
        return false;
    }

    /**
     * 设置 prompt 回调结果
     *
     * @param promptResult JsPromptResult
     * @param success bool 是否执行成功
     * @param msg 返回结果
     */
    private void setJsPromptResult(Object promptResult, boolean success, Object msg) {
        JSONObject ret = new JSONObject();
        try {
            ret.put("success", success);
            ret.put("msg", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (promptResult instanceof JsPromptResult) {
            ((JsPromptResult) promptResult).confirm(ret.toString());
        } else if (promptResult instanceof IPromptResult) {
            ((IPromptResult) promptResult).confirm(ret.toString());
        } else {
            throw new IllegalArgumentException("JsPromptResult Type Error: " + msg);
        }
    }

    /**
     * sort by module
     */
    private static class ModuleComparator implements Comparator<JsModule> {

        @Override
        public int compare(JsModule lhs, JsModule rhs) {
            return lhs.getModuleName().split("\\.").length - rhs.getModuleName().split("\\.").length;
        }
    }
}
