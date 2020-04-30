package com.wragony.android.jsbridge.module.datatype;

public interface WritableJBArray extends JBArray {

    void pushNull();

    void pushBoolean(boolean value);

    void pushDouble(double value);

    void pushInt(int value);

    void pushLong(long value);

    void pushString(String value);

    void pushArray(WritableJBArray value);

    void pushMap(WritableJBMap value);

    void pushCallback(JBCallback value);

    class Create extends JBArrayImpl {

        public Create() {
        }
    }

}
