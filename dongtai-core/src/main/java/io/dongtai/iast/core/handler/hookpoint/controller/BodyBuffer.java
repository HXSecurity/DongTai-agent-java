package io.dongtai.iast.core.handler.hookpoint.controller;

import java.io.ByteArrayOutputStream;

public class BodyBuffer {
    private static final ThreadLocal<ByteArrayOutputStream> REQUEST_STREAM = ThreadLocal.withInitial(() -> new ByteArrayOutputStream(65535));

    private static final ThreadLocal<ByteArrayOutputStream> RESPONSE_STREAM = ThreadLocal.withInitial(() -> new ByteArrayOutputStream(65535));

    public ByteArrayOutputStream getRequest() {
        return REQUEST_STREAM.get();
    }

    public ByteArrayOutputStream getResponse() {
        return RESPONSE_STREAM.get();
    }

    public void remove() {
        REQUEST_STREAM.remove();
        RESPONSE_STREAM.remove();
    }
}
