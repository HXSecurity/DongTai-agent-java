package io.dongtai.plugin;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;

public class DongTaiServerCallListener<REQUEST> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<REQUEST> {
    protected DongTaiServerCallListener(ServerCall.Listener<REQUEST> delegate) {
        super(delegate);
    }
}
