package io.dongtai.iast.common.string;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 用于把对象格式化为字符串
 *
 * @author CC11001100
 * @since 1.13.2
 */
public class ObjectFormatter {

    /**
     * 把对象格式化为字符串，高频调用要尽可能快
     *
     * @param value     要转换为字符串的对象
     * @param charLimit 转换时的字符长度限制，超过此长度将被格式化为一个祖传下来的表示字符串省略的格式 :)
     * @return 比如"aaa"，如果超长可能会发生省略： "aaaaaaaaaaaaaaaaaa...aaaaaaaaaaaaaa"
     * @see ObjectFormatResult
     */
    public static ObjectFormatResult formatObject(Object value, int charLimit) {

        ObjectFormatResult r = new ObjectFormatResult();

        if (null == value) {
            return r;
        }

        try {
            if (value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
                // 判断是否是基本类型的数组，基本类型的数组无法类型转换为Object[]，导致java.lang.ClassCastException异常
                Object[] taints = (Object[]) value;
                return objArray2StringV2(taints, charLimit);
            } else if (value instanceof StringWriter) {
                String s = ((StringWriter) value).getBuffer().toString();
                r.originalLength = s.length();
                r.objectFormatString = StringUtils.normalize(s, charLimit);
                return r;
            } else {
                String s = value.toString();
                r.originalLength = s.length();
                r.objectFormatString = StringUtils.normalize(s, charLimit);
                return r;
            }
        } catch (Throwable e) {
            // org.jruby.RubyBasicObject.hashCode() may cause NullPointerException when RubyBasicObject.metaClass is null
            String typeName = value.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(value));
            r.originalLength = typeName.length();
            r.objectFormatString = StringUtils.normalize(typeName, charLimit);
            return r;
        }
    }

    /**
     * 对象数组转为字符串，会往下穿透到第二层
     *
     * @param objArray  要转换为字符串的对象数组
     * @param charLimit 同 {{@link #formatObject(Object, int)}}
     * @return
     * @see #formatObject
     */
    private static ObjectFormatResult objArray2StringV2(Object[] objArray, int charLimit) {

        ObjectFormatResult r = new ObjectFormatResult();

        // 第一步，先把对象都收集一下，把要处理的对象打平
        List<Object> objList = new ArrayList<>();
        for (Object taint : objArray) {
            if (taint != null) {
                if (taint.getClass().isArray() && !taint.getClass().getComponentType().isPrimitive()) {
                    Object[] subTaints = (Object[]) taint;
                    for (Object subTaint : subTaints) {
                        if (subTaint == null) {
                            continue;
                        }
                        objList.add(subTaint);
                    }
                } else {
                    objList.add(taint);
                }
            }
        }

        // 从前往后开始读取
        StringBuilder header = new StringBuilder();
        int headIndex = 0;
        while (headIndex < objList.size()) {

            String s = objList.get(headIndex).toString();
            headIndex++;
            // 如果这个地方的字符串比较长怎么办？是不是应该截断一下？还是有优化空间的
            header.append(s);
            r.originalLength += s.length();

            // 如果进来的话，说明长度是超了，这个时候应该做的是从尾部读取一部分进来，然后等会儿做截断用
            if (header.length() > charLimit) {

                // 然后就从尾部开始向前读取
                int readCount = 0;
                LinkedList<String> tailStringList = new LinkedList<>();
                int needReadChar = charLimit / 2 + charLimit % 2;
                for (int tailIndex = objList.size() - 1; tailIndex >= headIndex; tailIndex--) {
                    s = objList.get(tailIndex).toString();
                    // 仅读取需要的字符数，超过的话则不再读取
                    if (readCount < needReadChar) {
                        readCount += s.length();
                        tailStringList.addFirst(s);
                    }
                    // 但是长度是要整个计算的
                    r.originalLength += s.length();
                }

                // 然后开始拼接处理
                tailStringList.forEach(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        // 是不是应该避免一下不必要的拼接拷贝？某些特殊数据下还是可能会发生占用较长时间
                        header.append(s);
                    }
                });

                break;
            }
        }

        r.objectFormatString = StringUtils.normalize(header.toString(), charLimit);
        return r;
    }

}
