package io.dongtai.iast.common.utils.serialize;


import java.io.*;
import java.util.List;

/**
 * 反序列化安全的ObjectInputStream
 *
 * @author chenyi
 * @date 2021/9/22
 */
public class DeserializeSafeObjectInputStream extends ObjectInputStream {
    /**
     * 白名单类型列表
     */
    private final List<Class<?>> targetClazzWhiteList;
    /**
     * 黑名单类名称列表
     */
    private final List<String> targetClazzBlackList;

    /**
     * 实例化反序列化安全的ObjectInputStream，需要指定黑名单或者白名单
     *
     * @param in             InputStream
     * @param clazzWhiteList 类型白名单
     * @param clazzBlackList 类名前缀黑名单
     * @throws IOException IO异常
     */
    public DeserializeSafeObjectInputStream(InputStream in, List<Class<?>> clazzWhiteList, List<String> clazzBlackList) throws IOException {
        super(in);
        targetClazzWhiteList = clazzWhiteList;
        targetClazzBlackList = clazzBlackList;
        if (isWhiteListEmpty() && isBlackListEmpty()) {
            throw new IllegalArgumentException("反序列化黑白名单配置错误");
        }
    }

    /**
     * resolveClass方法hook,依据黑白名单进行反序列化阻断
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        checkWhiteList(desc);
        checkBlackList(desc);
        return super.resolveClass(desc);
    }

    /**
     * 校验类型是否在白名单列表中
     *
     * @param desc 反序列化目标类
     * @throws InvalidClassException 类型非法异常
     */
    private void checkWhiteList(ObjectStreamClass desc) throws InvalidClassException {
        if (isWhiteListEmpty()) {
            return;
        }
        String descName = desc.getName();
        boolean isInWhiteList = false;
        for (Class<?> whiteClazz : this.targetClazzWhiteList) {
            if (whiteClazz != null && whiteClazz.getName().equals(descName)) {
                isInWhiteList = true;
                break;
            }
        }
        if (!isInWhiteList) {
            throw new InvalidClassException(desc.getName(), "不安全的反序列化,class类型不合法");
        }
    }

    /**
     * 校验类型名称前缀是否在黑名单列表中
     *
     * @param desc 反序列化目标类
     * @throws InvalidClassException 类型非法异常
     */
    private void checkBlackList(ObjectStreamClass desc) throws InvalidClassException {
        if (isBlackListEmpty()) {
            return;
        }
        String descName = desc.getName();
        if (descName == null) {
            return;
        }
        for (String blackClazz : this.targetClazzBlackList) {
            // 检查类型namespace前缀
            if (blackClazz != null && descName.startsWith(blackClazz)) {
                throw new InvalidClassException(desc.getName(), "不安全的反序列化,class类型不合法!");
            }
        }
    }

    private boolean isWhiteListEmpty() {
        return targetClazzWhiteList == null || targetClazzWhiteList.isEmpty();
    }

    private boolean isBlackListEmpty() {
        return targetClazzBlackList == null || targetClazzBlackList.isEmpty();
    }
}
