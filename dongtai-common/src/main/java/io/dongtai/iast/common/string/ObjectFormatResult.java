package io.dongtai.iast.common.string;

/**
 * 用于表示对 对象格式化的结果
 *
 * @author CC11001100
 * @since 1.13.2
 */
public class ObjectFormatResult {

    // 对象格式化后的字符串，可能不是原始的完整的字符串是被格式化过的，仅作为展示之类的使用
    public String objectFormatString;

    // 原始的字符串长度，对象格式化可以认为有三个步骤：
    //
    // object --> original string --> format string
    //
    // 其中original string通常是调用object的toString()得到的，长度可能比较短，也可能老长老长了
    // format string这一步相当于是对original string进行截断，控制字符串的长度
    public int originalLength;

}
