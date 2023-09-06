package io.dongtai.iast.core.handler.hookpoint.models;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author CC11001100
 */
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(1)
public class MethodEventBenchmarkTest {

    /**
     * 生成给定范围内的随机长度的字符串
     *
     * @param begin
     * @param end
     * @return
     */
    private String random(int begin, int end) {
        Random r = new Random();
        int length = (r.nextInt() % (end - begin)) + begin;
        char[] chars = new char[length];
        for (int i = 0; i < chars.length; i++) {
            char c = (char) ((r.nextInt() % 26) + 'A');
            chars[i] = c;
        }
        return new String(chars);
    }

    private String[] randomArray(int ArrayLengthBegin, int ArrayLengthEnd, int begin, int end) {
        Random r = new Random();
//        int length = (r.nextInt() % (ArrayLengthEnd - ArrayLengthBegin)) + ArrayLengthBegin;
        int length = 100;
        String[] ss = new String[length];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = random(begin, end);
        }
        return ss;
    }

//    @Benchmark
//    public void formatObjTest() {
//        MethodEvent.formatObj(random(1000, 2000), 1024);
//    }
//
//    @Benchmark
//    public void obj2StringTest() {
//        MethodEvent.obj2String(random(1000, 2000));
//    }
//
//    @Benchmark
//    public void formatObjLongStringTest() {
//        MethodEvent.formatObj(random(100000, 200000), 1024);
//    }
//
//    @Benchmark
//    public void obj2StringLongStringTest() {
//        MethodEvent.obj2String(random(100000, 200000));
//    }

//    @Benchmark
//    public void formatObjArrayTest() {
//        MethodEvent.formatObject(randomArray(10, 100, 1000, 2000), 1024);
//    }
//
//    @Benchmark
//    public void obj2StringArrayTest() {
//        MethodEvent.obj2String(randomArray(10, 100, 1000, 2000));
//    }
//
//    @Benchmark
//    public void formatObjArrayLongStringTest() {
//        MethodEvent.formatObject(randomArray(10, 100, 100000, 200000), 1024);
//    }
//
//    @Benchmark
//    public void obj2StringArrayLongStringTest() {
//        MethodEvent.obj2String(randomArray(10, 100, 100000, 200000));
//    }
//
//    public static void main(String[] args) throws Exception {
//        Options opts = new OptionsBuilder()
//                .include(MethodEventBenchmarkTest.class.getSimpleName())
//                .resultFormat(ResultFormatType.JSON)
//                .build();
//        new Runner(opts).run();
//    }

}
