package com.secnium.iast.core;

import org.junit.Test;

public class ArrayListTest {
    @Test
    public void testArray() {
        // int型，数据量不定；
        // 限制最大为5000如何？超过5000则忽略
        int[] hashCodes = new int[5000];
        String tempStr = new String("123");
        int hashCode = tempStr.hashCode();
        System.out.println(hashCode);
        int index = 0;
        hashCodes[index] = hashCode;

        for (int i = 0; i <= index; i++) {
            if (hashCode == hashCodes[i]) {
                System.out.println("found taint value");
                break;
            }
        }

        System.out.println(hashCodes.length);
    }
}
