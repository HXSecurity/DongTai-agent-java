package io.dongtai.iast.core.handler.hookpoint.controller;

import java.io.ByteArrayOutputStream;

public class BodyBuffer {
    private static ThreadLocal<ByteArrayOutputStream> REQUEST_STREAM = new ThreadLocal<ByteArrayOutputStream>() {
        @Override
        public ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(65535);
        }
    };

    private static ThreadLocal<ByteArrayOutputStream> RESPONSE_STREAM = new ThreadLocal<ByteArrayOutputStream>() {
        @Override
        public ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(65535);
        }
    };

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
