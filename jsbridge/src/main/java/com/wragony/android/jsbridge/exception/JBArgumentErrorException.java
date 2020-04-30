package com.wragony.android.jsbridge.exception;

public class JBArgumentErrorException extends RuntimeException {

    public JBArgumentErrorException(String detailMessage) {
        super(detailMessage);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
