package io.dongtai.iast.core.handler.hookpoint.models.policy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.*;

public class TaintPositionTest {
    @Test
    public void testConstructor() {
        IllegalArgumentException exception;

        Map<String, String> tests = new HashMap<String, String>() {{
            put(null, TaintPosition.ERR_POSITION_EMPTY);
            put("", TaintPosition.ERR_POSITION_EMPTY);
            put("  ", TaintPosition.ERR_POSITION_EMPTY);
            put("X", TaintPosition.ERR_POSITION_INVALID);
            put("P", TaintPosition.ERR_POSITION_PARAMETER_INDEX_INVALID);
            put("PA", TaintPosition.ERR_POSITION_PARAMETER_INDEX_INVALID);
            put("P0", TaintPosition.ERR_POSITION_PARAMETER_INDEX_INVALID);
            put("P-1", TaintPosition.ERR_POSITION_PARAMETER_INDEX_INVALID);
            put("P1.2", TaintPosition.ERR_POSITION_PARAMETER_INDEX_INVALID);
        }};

        for (Map.Entry<String, String> entry : tests.entrySet()) {
            exception = Assert.assertThrows("constructor exception " + entry.getKey(),
                    IllegalArgumentException.class, new ThrowingRunnable() {
                        @Override
                        public void run() throws IllegalArgumentException {
                            new TaintPosition(entry.getKey());
                        }
                    });
            Assert.assertTrue("constructor exception " + entry.getKey(),
                    exception.getMessage().startsWith(entry.getValue()));
        }
    }

    @Test
    public void testIsObject() {
        Map<String, Boolean> tests = new HashMap<String, Boolean>() {{
            put("O", true);
            put("R", false);
            put("P1", false);
        }};

        for (Map.Entry<String, Boolean> entry : tests.entrySet()) {
            TaintPosition tp = new TaintPosition(entry.getKey());
            Assert.assertEquals("isObject " + entry.getKey(), entry.getValue(), tp.isObject());
        }
    }

    @Test
    public void testIsReturn() {
        Map<String, Boolean> tests = new HashMap<String, Boolean>() {{
            put("O", false);
            put("R", true);
            put("P1", false);
        }};

        for (Map.Entry<String, Boolean> entry : tests.entrySet()) {
            TaintPosition tp = new TaintPosition(entry.getKey());
            Assert.assertEquals("isReturn " + entry.getKey(), entry.getValue(), tp.isReturn());
        }
    }

    @Test
    public void testIsParameter() {
        Map<String, Boolean> tests = new HashMap<String, Boolean>() {{
            put("O", false);
            put("R", false);
            put("P1", true);
        }};

        for (Map.Entry<String, Boolean> entry : tests.entrySet()) {
            TaintPosition tp = new TaintPosition(entry.getKey());
            Assert.assertEquals("isParameter " + entry.getKey(), entry.getValue(), tp.isParameter());
        }
    }

    @Test
    public void testGetParameterIndex() {
        Map<String, Integer> tests = new HashMap<String, Integer>() {{
            put("P1", 0);
            put("P12", 11);
        }};

        for (Map.Entry<String, Integer> entry : tests.entrySet()) {
            TaintPosition tp = new TaintPosition(entry.getKey());
            Assert.assertEquals("getParameterIndex " + entry.getKey(),
                    entry.getValue().intValue(), tp.getParameterIndex());
        }
    }

    @Test
    public void testParse() throws TaintPositionException {
        Map<String, Set<TaintPosition>> tests = new HashMap<String, Set<TaintPosition>>() {{
            put("O", new HashSet<TaintPosition>(Collections.singletonList(new TaintPosition("O"))));
            put("R", new HashSet<TaintPosition>(Collections.singletonList(new TaintPosition("R"))));
            put("P3", new HashSet<TaintPosition>(Collections.singletonList(new TaintPosition("P3"))));
            put("P1,3,7", new HashSet<TaintPosition>(Arrays.asList(new TaintPosition("P1"), new TaintPosition("P3"),
                    new TaintPosition("P7"))));
            put("P1,3,3", new HashSet<TaintPosition>(Arrays.asList(new TaintPosition("P1"), new TaintPosition("P3"))));
            put("O|P2", new HashSet<TaintPosition>(Arrays.asList(new TaintPosition("O"), new TaintPosition("P2"))));
            put("O|O|P2|P2||", new HashSet<TaintPosition>(Arrays.asList(new TaintPosition("O"), new TaintPosition("P2"))));
            put("O|P2,3", new HashSet<TaintPosition>(Arrays.asList(new TaintPosition("O"), new TaintPosition("P2"),
                    new TaintPosition("P3"))));
            put(" O | P 2 , 3 ", new HashSet<TaintPosition>(Arrays.asList(new TaintPosition("O"), new TaintPosition("P2"),
                    new TaintPosition("P3"))));
        }};

        for (Map.Entry<String, Set<TaintPosition>> entry : tests.entrySet()) {
            Assert.assertEquals("parse " + entry.getKey(), entry.getValue(), TaintPosition.parse(entry.getKey()));
        }
    }

    @Test
    public void testHasObject() throws TaintPositionException {
        Map<String, Boolean> tests = new HashMap<String, Boolean>() {{
            put("O", true);
            put("R", false);
            put("P1", false);
            put("O|R", true);
            put("O|P1", true);
            put("R|P1", false);
            put("O|R|P1", true);
        }};

        for (Map.Entry<String, Boolean> entry : tests.entrySet()) {
            Set<TaintPosition> tps = TaintPosition.parse(entry.getKey());
            Assert.assertEquals("hasObject " + entry.getKey(), entry.getValue(), TaintPosition.hasObject(tps));
        }
    }

    @Test
    public void testHasReturn() throws TaintPositionException {
        Map<String, Boolean> tests = new HashMap<String, Boolean>() {{
            put("O", false);
            put("R", true);
            put("P1", false);
            put("O|R", true);
            put("O|P1", false);
            put("R|P1", true);
            put("O|R|P1", true);
        }};

        for (Map.Entry<String, Boolean> entry : tests.entrySet()) {
            Set<TaintPosition> tps = TaintPosition.parse(entry.getKey());
            Assert.assertEquals("hasReturn " + entry.getKey(), entry.getValue(), TaintPosition.hasReturn(tps));
        }
    }

    @Test
    public void testHasParameter() throws TaintPositionException {
        Map<String, Boolean> tests = new HashMap<String, Boolean>() {{
            put("O", false);
            put("R", false);
            put("P1", true);
            put("O|R", false);
            put("O|P1", true);
            put("R|P1", true);
            put("O|R|P1", true);
        }};

        for (Map.Entry<String, Boolean> entry : tests.entrySet()) {
            Set<TaintPosition> tps = TaintPosition.parse(entry.getKey());
            Assert.assertEquals("hasParameter " + entry.getKey(), entry.getValue(), TaintPosition.hasParameter(tps));
        }
    }

    @Test
    public void testHasParameterIndex() throws TaintPositionException {
        // @formatter:off
        Map<Map<String, Integer>, Boolean> tests = new HashMap<Map<String, Integer>, Boolean>() {{
            put(new HashMap<String, Integer>() {{ put("O", 0); }}, false);
            put(new HashMap<String, Integer>() {{ put("R", 1); }}, false);
            put(new HashMap<String, Integer>() {{ put("P2", 1); }}, true);
            put(new HashMap<String, Integer>() {{ put("P2,4", 1); }}, true);
            put(new HashMap<String, Integer>() {{ put("P2,4", 3); }}, true);
            put(new HashMap<String, Integer>() {{ put("P2,4", 0); }}, false);
            put(new HashMap<String, Integer>() {{ put("P2,4", 2); }}, false);
            put(new HashMap<String, Integer>() {{ put("P2,4", 4); }}, false);
        }};
        // @formatter:on

        for (Map.Entry<Map<String, Integer>, Boolean> entry : tests.entrySet()) {
            Set<TaintPosition> tps = TaintPosition.parse(entry.getKey().entrySet().iterator().next().getKey());
            int index = entry.getKey().entrySet().iterator().next().getValue();
            Assert.assertEquals("hasParameterIndex " + entry.getKey(), entry.getValue(),
                    TaintPosition.hasParameterIndex(tps, index));
        }
    }

    @Test
    public void testEquals() {
        Map<String, String> tests = new HashMap<String, String>() {{
            put("O", "O ");
            put("R", " R");
            put("P5", " P5 ");
            put("P3", " P3 ");
            put("P4", " P 4 ");
        }};

        for (Map.Entry<String, String> entry : tests.entrySet()) {
            TaintPosition tp1 = new TaintPosition(entry.getKey());
            TaintPosition tp2 = new TaintPosition(entry.getValue());
            Assert.assertEquals("equals " + entry.getKey() + " == " + entry.getValue(), tp1, tp2);
        }
    }
}