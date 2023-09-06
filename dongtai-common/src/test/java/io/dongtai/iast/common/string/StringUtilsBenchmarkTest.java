package io.dongtai.iast.common.string;

//import org.openjdk.jmh.annotations.*;
//import org.openjdk.jmh.results.format.ResultFormatType;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * @author CC11001100
// */
//@BenchmarkMode(Mode.All)
//@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@State(Scope.Thread)
//@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
//@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
//@Threads(1)
//public class StringUtilsBenchmarkTest {
//
//    @Benchmark
//    public void formatClassNameToSlashDelimiterTest() {
//        String s = StringUtils.formatClassNameToSlashDelimiter("com.foo.bar");
//    }
//
//    @Benchmark
//    public void replace() {
//        String s = "com.foo.bar".replace(".", "/");
//    }
//
//    public static void main(String[] args) throws Exception {
//        Options opts = new OptionsBuilder()
//                .include(StringUtilsBenchmarkTest.class.getSimpleName())
//                .resultFormat(ResultFormatType.JSON)
//                .build();
//        new Runner(opts).run();
//    }
//
//}
