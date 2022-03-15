package io.dongtai.plugin;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DongTaiServerCallListener<REQUEST> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<REQUEST> {
    protected DongTaiServerCallListener(ServerCall.Listener<REQUEST> delegate) {
        super(delegate);
    }

    @Override
    public void onMessage(REQUEST message) {
        // todo: 获取请求体，解析请求体的数据，存入污点池，进行后续的污点传播；但是，plugin中如何传播？
        Class<?> messageOfClass = message.getClass();
        Method[] methodsOfClass = messageOfClass.getMethods();
        for (Method getMethod : methodsOfClass) {
            // 如果存在参数，则暂不处理
            String methodName = getMethod.getName();
            Class<?> retClass = getMethod.getReturnType();
            if (methodName.startsWith("get")
                    && !methodName.equals("getClass")
                    && !methodName.equals("getParserForType")
                    && !methodName.equals("getDefaultInstance")
                    && !methodName.equals("getDefaultInstanceForType")
                    && !methodName.equals("getDescriptor")
                    && !methodName.equals("getDescriptorForType")
                    && !methodName.equals("getAllFields")
                    && !methodName.equals("getInitializationErrorString")
                    && getMethod.getParameterCount() == 0
                    && retClass != int.class
                    && retClass != Integer.class
                    && retClass != boolean.class
                    && retClass != Boolean.class
            ) {
                try {
                    Object ret = getMethod.invoke(message);
                    System.out.println(ret);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onMessage(message);
    }

    @Override
    public void onCancel() {
        super.onCancel();
    }

}
