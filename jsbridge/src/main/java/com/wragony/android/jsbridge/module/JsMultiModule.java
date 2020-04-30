package com.wragony.android.jsbridge.module;

public abstract class JsMultiModule extends JsModule {

    @Override
    public final String getModuleName() {
        StringBuilder builder = new StringBuilder();
        if (getMultiModule() != null) {
            for (int i = 0; i < getMultiModule().length; i++) {
                builder.append(getMultiModule()[i]);
                if (i != getMultiModule().length - 1) {
                    builder.append(".");
                }
            }
        }
        return builder.toString();
    }

    public abstract String[] getMultiModule();

}
