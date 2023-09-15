package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintCommand;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintCommandRunner;
import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONArray;
import org.junit.*;
import org.junit.function.ThrowingRunnable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

public class PolicyBuilderTest {
    private static final String POLICY_DIR = "src/test/fixture/policy/";
    private static final String PROPERTY_FILE = "src/test/fixture/property/policy-test-invalid.properties";
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();


    @Before
    public void setUp() throws PropertyUtils.DongTaiPropertyConfigException, PropertyUtils.DongTaiEnvConfigException {
        PropertyUtils.getInstance(PROPERTY_FILE);
        DongTaiLog.ENABLED = true;
        clear();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        PropertyUtils.clear();
        DongTaiLog.ENABLED = false;
        clear();
        System.setOut(standardOut);
    }

    private void clear() {
        outputStreamCaptor.reset();
    }

    @Test
    public void testFetchFromServer() throws PolicyException {
        PolicyException exception = Assert.assertThrows("fetch server exception", PolicyException.class, new ThrowingRunnable() {
            @Override
            public void run() throws PolicyException {
                PolicyBuilder.fetchFromServer();
            }
        });
        Assert.assertEquals("fetch server exception", PolicyException.ERR_POLICY_CONFIG_FROM_SERVER_INVALID,
                exception.getMessage());
    }

    @Test
    public void testFetchFromFile() throws PolicyException {
        JSONArray policy = PolicyBuilder.fetchFromFile(POLICY_DIR + "policy.json");
        Assert.assertNotNull("fetch file", policy);
        Assert.assertEquals("fetch file", 3, policy.length());

        PolicyException exception;
        Map<String, String> exceptionTest = new HashMap<String, String>() {{
            put("policy-no-exists.json", PolicyException.ERR_POLICY_CONFIG_FILE_READ_FAILED);
            put("policy-invalid.json", PolicyException.ERR_POLICY_CONFIG_FILE_INVALID);
            put("policy-empty.json", PolicyException.ERR_POLICY_CONFIG_FILE_INVALID);
            put("policy-null-data.json", PolicyException.ERR_POLICY_CONFIG_FILE_INVALID);
        }};
        for (Map.Entry<String, String> entry : exceptionTest.entrySet()) {
            exception = Assert.assertThrows("fetch file exception" + entry.getKey(), PolicyException.class,
                    new ThrowingRunnable() {
                        @Override
                        public void run() throws PolicyException {
                            PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
                        }
                    });
            Assert.assertEquals("fetch file exception " + entry.getKey(),
                    String.format(entry.getValue(), POLICY_DIR + entry.getKey()), exception.getMessage());
        }
    }

    @Test
    public void testBuild() throws PolicyException {
        Map<String, List<Integer>> tests = new HashMap<String, List<Integer>>() {{
            put("policy-node-count-src0-p2-sink2-policy4-cls2.json", Arrays.asList(0, 2, 2, 4, 2));
            put("policy-node-count-src1-p2-sink1-policy4-cls4.json", Arrays.asList(1, 2, 1, 4, 4));
        }};
        for (Map.Entry<String, List<Integer>> entry : tests.entrySet()) {
            JSONArray policyConfig = PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
            Policy policy = PolicyBuilder.build(policyConfig);
            Assert.assertEquals("build source count " + entry.getKey(), entry.getValue().get(0).intValue(),
                    policy.getSources().size());
            Assert.assertEquals("build propagator count " + entry.getKey(), entry.getValue().get(1).intValue(),
                    policy.getPropagators().size());
            Assert.assertEquals("build sink count " + entry.getKey(), entry.getValue().get(2).intValue(),
                    policy.getSinks().size());
            Assert.assertEquals("build hook policy count" + entry.getKey(), entry.getValue().get(3).intValue(),
                    policy.getPolicyNodesMap().size());
            Set<String> classes = policy.getClassHooks();
            classes.addAll(policy.getAncestorClassHooks());
            Assert.assertEquals("build hook class count" + entry.getKey(), entry.getValue().get(4).intValue(),
                    classes.size());
        }

        PolicyException exception;
        Map<String, String> exceptionTests = new HashMap<String, String>() {{
            put("policy-data-empty.json", PolicyException.ERR_POLICY_CONFIG_EMPTY);
            put("policy-node-empty.json", PolicyException.ERR_POLICY_NODE_EMPTY);
        }};
        for (Map.Entry<String, String> entry : exceptionTests.entrySet()) {
            exception = Assert.assertThrows("build exception " + entry.getKey(), PolicyException.class,
                    new ThrowingRunnable() {
                        @Override
                        public void run() throws PolicyException {
                            JSONArray policyConfig = PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
                            PolicyBuilder.build(policyConfig);
                        }
                    });
            Assert.assertTrue("build exception " + entry.getKey() + " => " + exception.getMessage(),
                    exception.getMessage().startsWith(entry.getValue()));
        }
    }

    @Test
    public void testBuildSource() {
        PolicyException exception;
        Map<String, String> exceptionTest = new HashMap<String, String>() {{
            put("policy-source-node-no-target.json", PolicyException.ERR_POLICY_NODE_TARGET_INVALID);
            put("policy-source-node-target-invalid.json", PolicyException.ERR_POLICY_NODE_TARGET_INVALID);
        }};
        for (Map.Entry<String, String> entry : exceptionTest.entrySet()) {
            exception = Assert.assertThrows("buildSource exception " + entry.getKey(), PolicyException.class,
                    new ThrowingRunnable() {
                        @Override
                        public void run() throws PolicyException {
                            JSONArray policyConfig = PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
                            PolicyBuilder.buildSource(new Policy(), PolicyNodeType.SOURCE, policyConfig.getJSONObject(0));
                        }
                    });
            Assert.assertTrue("buildSource exception " + entry.getKey() + " => " + exception.getMessage(),
                    exception.getMessage().startsWith(entry.getValue()));
        }
    }

    @Test
    public void testBuildPropagator() {
        PolicyException exception;
        Map<String, String> exceptionTest = new HashMap<String, String>() {{
            put("policy-node-no-signature.json", PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID);
            put("policy-node-signature-empty.json", PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID);
            put("policy-node-signature-invalid.json", PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID);
            put("policy-node-no-inherit.json", PolicyException.ERR_POLICY_NODE_INHERITABLE_INVALID);
            put("policy-node-inherit-empty.json", PolicyException.ERR_POLICY_NODE_INHERITABLE_INVALID);
            put("policy-node-inherit-invalid.json", PolicyException.ERR_POLICY_NODE_INHERITABLE_INVALID);
            put("policy-propagator-node-no-source.json", PolicyException.ERR_POLICY_NODE_SOURCE_INVALID);
            put("policy-propagator-node-no-target.json", PolicyException.ERR_POLICY_NODE_TARGET_INVALID);
            put("policy-propagator-node-source-invalid.json", PolicyException.ERR_POLICY_NODE_SOURCE_INVALID);
            put("policy-propagator-node-target-invalid.json", PolicyException.ERR_POLICY_NODE_TARGET_INVALID);
        }};
        for (Map.Entry<String, String> entry : exceptionTest.entrySet()) {
            exception = Assert.assertThrows("buildPropagator exception " + entry.getKey(), PolicyException.class,
                    new ThrowingRunnable() {
                        @Override
                        public void run() throws PolicyException {
                            JSONArray policyConfig = PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
                            PolicyBuilder.buildPropagator(new Policy(), PolicyNodeType.PROPAGATOR,
                                    policyConfig.getJSONObject(0));
                        }
                    });
            Assert.assertTrue("buildPropagator exception " + entry.getKey() + " => " + exception.getMessage(),
                    exception.getMessage().startsWith(entry.getValue()));
        }
    }

    @Test
    public void testBuildSink() {
        PolicyException exception;
        Map<String, String> exceptionTest = new HashMap<String, String>() {{
            put("policy-sink-node-no-source.json", PolicyException.ERR_POLICY_NODE_SOURCE_INVALID);
            put("policy-sink-node-source-invalid.json", PolicyException.ERR_POLICY_NODE_SOURCE_INVALID);
            put("policy-sink-node-no-vul-type.json", PolicyException.ERR_POLICY_SINK_NODE_VUL_TYPE_INVALID);
            put("policy-sink-node-vul-type-invalid.json", PolicyException.ERR_POLICY_SINK_NODE_VUL_TYPE_INVALID);
        }};
        for (Map.Entry<String, String> entry : exceptionTest.entrySet()) {
            exception = Assert.assertThrows("buildSink exception " + entry.getKey(), PolicyException.class,
                    new ThrowingRunnable() {
                        @Override
                        public void run() throws PolicyException {
                            JSONArray policyConfig = PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
                            PolicyBuilder.buildSink(new Policy(), PolicyNodeType.SINK, policyConfig.getJSONObject(0));
                        }
                    });
            Assert.assertTrue("buildSink exception " + entry.getKey() + " => " + exception.getMessage(),
                    exception.getMessage().startsWith(entry.getValue()));
        }
    }

    @Test
    public void testTags() throws PolicyException {
        Map<String, List<String[]>> tests = new HashMap<String, List<String[]>>() {{
            put("tags/policy-tags-empty.json", Arrays.asList(new String[0], new String[0]));
            put("tags/policy-tags-invalid.json", Arrays.asList(new String[0], new String[0]));
            put("tags/policy-tags-invalid-type-tags.json", Arrays.asList(new String[0], new String[0]));
            put("tags/policy-tags-invalid-type-tags-value.json", Arrays.asList(new String[0], new String[0]));
            put("tags/policy-tags-invalid-type-untags.json", Arrays.asList(new String[0], new String[0]));
            put("tags/policy-tags-invalid-type-untags-value.json", Arrays.asList(new String[0], new String[0]));
            put("tags/policy-tags-valid-empty.json", Arrays.asList(new String[0], new String[0]));
            put("tags/policy-tags-valid-single.json", Arrays.asList(
                    new String[]{TaintTag.URL_ENCODED.getKey()}, new String[]{TaintTag.URL_DECODED.getKey()}));
            put("tags/policy-tags-valid-multi.json", Arrays.asList(
                    new String[]{TaintTag.URL_ENCODED.getKey(), TaintTag.HTML_ENCODED.getKey()},
                    new String[]{TaintTag.URL_DECODED.getKey(), TaintTag.HTML_DECODED.getKey()}));
            put("tags/policy-tags-multi-has-invalid.json", Arrays.asList(
                    new String[]{TaintTag.URL_ENCODED.getKey(), TaintTag.HTML_ENCODED.getKey()},
                    new String[]{TaintTag.URL_DECODED.getKey(), TaintTag.HTML_DECODED.getKey()}));
            put("tags/policy-tags-multi-has-dup.json", Arrays.asList(
                    new String[]{TaintTag.URL_ENCODED.getKey(), TaintTag.HTML_ENCODED.getKey()},
                    new String[]{TaintTag.URL_DECODED.getKey(), TaintTag.HTML_ENCODED.getKey()}));
        }};
        for (Map.Entry<String, List<String[]>> entry : tests.entrySet()) {
            clear();
            JSONArray policyConfig = PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
            Policy policy = new Policy();
            PolicyBuilder.buildPropagator(policy, PolicyNodeType.PROPAGATOR, policyConfig.getJSONObject(0));

            if ("tags/policy-tags-invalid.json".equals(entry.getKey())
                    || "tags/policy-tags-invalid-type-tags.json".equals(entry.getKey())
                    || "tags/policy-tags-invalid-type-tags-value.json".equals(entry.getKey())
                    || "tags/policy-tags-invalid-type-untags.json".equals(entry.getKey())
                    || "tags/policy-tags-invalid-type-untags-value.json".equals(entry.getKey())
                    || "tags/policy-tags-multi-has-invalid.json".equals(entry.getKey())
                    || "tags/policy-tags-multi-has-dup.json".equals(entry.getKey())) {
                Assert.assertTrue("policy tags/untags warn " + entry.getKey(), outputStreamCaptor.size() > 0);
            } else {
                Assert.assertEquals("policy tags/untags no warn " + entry.getKey(), 0, outputStreamCaptor.size());
            }

            Assert.assertEquals("policy tags/untags length " + entry.getKey(), 1, policy.getPolicyNodesMap().size());
            String[] tags = policy.getPropagators().get(0).getTags();
            String[] untags = policy.getPropagators().get(0).getUntags();
            Assert.assertArrayEquals("policy tags " + entry.getKey(), entry.getValue().get(0), tags);
            Assert.assertArrayEquals("policy untags " + entry.getKey(), entry.getValue().get(1), untags);
        }
    }

    @Test
    public void testCommand() throws PolicyException {
        Map<String, TaintCommandRunner> tests = new HashMap<String, TaintCommandRunner>() {{
            put("command/policy-command-empty.json", null);
            put("command/policy-command-invalid.json", null);
            put("command/policy-command-invalid-type.json", null);
            put("command/policy-command-invalid-cmd.json", null);
            put("command/policy-command-invalid-args.json", null);
            put("command/policy-keep-empty-args.json", TaintCommandRunner.create("",
                    TaintCommand.KEEP, new String[0]));
            put("command/policy-append-p2-p3.json", TaintCommandRunner.create("", TaintCommand.APPEND,
                    new String[]{"P2", "P3"}));
            put("command/policy-append-p2-p3-0.json", TaintCommandRunner.create("", TaintCommand.APPEND,
                    new String[]{"P2", "P3", "0"}));
            put("command/policy-append-p2-p3-0-with-space.json", TaintCommandRunner.create("", TaintCommand.APPEND,
                    new String[]{"P2", "P3", "0"}));
            put("command/policy-subset-0-p1.json", TaintCommandRunner.create("", TaintCommand.SUBSET,
                    new String[]{"0", "P1"}));
        }};
        for (Map.Entry<String, TaintCommandRunner> entry : tests.entrySet()) {
            clear();
            JSONArray policyConfig = PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
            Policy policy = new Policy();
            PolicyBuilder.buildPropagator(policy, PolicyNodeType.PROPAGATOR, policyConfig.getJSONObject(0));
            TaintCommandRunner r = policy.getPropagators().get(0).getCommandRunner();

            if ("command/policy-command-invalid.json".equals(entry.getKey())
                    || "command/policy-command-invalid-type.json".equals(entry.getKey())
                    || "command/policy-command-invalid-cmd.json".equals(entry.getKey())
                    || "command/policy-command-invalid-args.json".equals(entry.getKey())) {
                Assert.assertTrue("policy command warn " + entry.getKey(), outputStreamCaptor.size() > 0);
            } else {
                Assert.assertEquals("policy command no warn " + entry.getKey(), 0, outputStreamCaptor.size());
            }

            if (entry.getValue() == null) {
                Assert.assertNull(r);
            } else {
                Assert.assertEquals("policy command " + entry.getKey(), entry.getValue().getCommand(), r.getCommand());
                Assert.assertEquals("policy command args " + entry.getKey(),
                        entry.getValue().getOrigParams(), r.getOrigParams());
            }
        }
    }

    @Test
    public void testStackBlacklist() throws PolicyException {
        Map<String, String[]> tests = new HashMap<String, String[]>() {{
            put("stack-blacklist/policy-stack-blacklist-empty.json", null);
            put("stack-blacklist/policy-stack-blacklist-invalid.json", null);
            put("stack-blacklist/policy-stack-blacklist-invalid-type.json", null);
            put("stack-blacklist/policy-stack-blacklist-invalid-type-value.json", null);
            put("stack-blacklist/policy-stack-blacklist-valid-empty.json", new String[0]);
            put("stack-blacklist/policy-stack-blacklist-valid.json", new String[]{"foo.Bar", "foo.Bar(baz)"});
        }};
        for (Map.Entry<String, String[]> entry : tests.entrySet()) {
            clear();
            JSONArray policyConfig = PolicyBuilder.fetchFromFile(POLICY_DIR + entry.getKey());
            Policy policy = new Policy();
            PolicyBuilder.buildSink(policy, PolicyNodeType.SINK, policyConfig.getJSONObject(0));
            SinkNode sinkNode = policy.getSinks().get(0);

            if ("stack-blacklist/policy-stack-blacklist-invalid.json".equals(entry.getKey())
                    || "stack-blacklist/policy-stack-blacklist-invalid-type.json".equals(entry.getKey())
                    || "stack-blacklist/policy-stack-blacklist-invalid-type-value.json".equals(entry.getKey())) {
                Assert.assertTrue("policy stack blacklist warn " + entry.getKey(), outputStreamCaptor.size() > 0);
            } else {
                Assert.assertEquals("policy stack blacklist no warn " + entry.getKey(), 0, outputStreamCaptor.size());
            }

            Assert.assertArrayEquals("policy stack blacklist " + entry.getKey(),
                    entry.getValue(), sinkNode.getStackDenyList());
        }
    }
}