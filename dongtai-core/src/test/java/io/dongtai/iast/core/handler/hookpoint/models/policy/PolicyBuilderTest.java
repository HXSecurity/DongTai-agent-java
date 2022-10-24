package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.utils.PropertyUtils;
import org.json.JSONArray;
import org.junit.*;
import org.junit.function.ThrowingRunnable;

import java.util.*;

public class PolicyBuilderTest {
    private static final String POLICY_DIR = "src/test/fixture/policy/";
    private static final String PROPERTY_FILE = "src/test/fixture/property/policy-test-invalid.properties";

    @Before
    public void setUp() {
        PropertyUtils.getInstance(PROPERTY_FILE);
        System.setProperty("dongtai.log", "false");
    }

    @After
    public void tearDown() {
        PropertyUtils.clear();
    }

    @Test
    public void testFetchFromServer() throws PolicyException {
        PolicyException exception = Assert.assertThrows("fetch server exception", PolicyException.class, new ThrowingRunnable() {
            @Override
            public void run() throws PolicyException {
                PolicyBuilder.fetchFromServer();
            }
        });
        Assert.assertEquals("fetch server exception", PolicyBuilder.ERR_POLICY_CONFIG_FROM_SERVER_INVALID,
                exception.getMessage());
    }

    @Test
    public void testFetchFromFile() throws PolicyException {
        JSONArray policy = PolicyBuilder.fetchFromFile(POLICY_DIR + "policy.json");
        Assert.assertNotNull("fetch file", policy);
        Assert.assertEquals("fetch file", 3, policy.length());

        PolicyException exception;
        Map<String, String> exceptionTest = new HashMap<String, String>() {{
            put("policy-no-exists.json", PolicyBuilder.ERR_POLICY_CONFIG_FILE_READ_FAILED);
            put("policy-invalid.json", PolicyBuilder.ERR_POLICY_CONFIG_FILE_INVALID);
            put("policy-empty.json", PolicyBuilder.ERR_POLICY_CONFIG_FILE_INVALID);
            put("policy-null-data.json", PolicyBuilder.ERR_POLICY_CONFIG_FILE_INVALID);
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
            put("policy-node-count-src1-p2-sink1-hc4.json", Arrays.asList(1, 2, 1, 4));
            put("policy-node-count-src0-p2-sink2-hc2.json", Arrays.asList(0, 2, 2, 2));
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
            Assert.assertEquals("build hook class count" + entry.getKey(), entry.getValue().get(3).intValue(),
                    policy.getHookClasses().size());
        }

        PolicyException exception;
        Map<String, String> exceptionTests = new HashMap<String, String>() {{
            put("policy-data-empty.json", PolicyBuilder.ERR_POLICY_CONFIG_EMPTY);
            put("policy-node-empty.json", PolicyBuilder.ERR_POLICY_NODE_EMPTY);
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
            put("policy-node-no-signature.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_NOT_EXISTS);
            put("policy-node-signature-empty.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_EMPTY);
            put("policy-node-signature-invalid.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_INVALID);
            put("policy-source-node-no-target.json", PolicyBuilder.ERR_POLICY_SOURCE_NODE_INVALID);
            put("policy-source-node-target-invalid.json", PolicyBuilder.ERR_POLICY_SOURCE_NODE_TARGET_INVALID);
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
            put("policy-node-no-signature.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_NOT_EXISTS);
            put("policy-node-signature-empty.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_EMPTY);
            put("policy-node-signature-invalid.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_INVALID);
            put("policy-propagator-node-no-source.json", PolicyBuilder.ERR_POLICY_PROPAGATOR_NODE_INVALID);
            put("policy-propagator-node-no-target.json", PolicyBuilder.ERR_POLICY_PROPAGATOR_NODE_INVALID);
            put("policy-propagator-node-source-invalid.json", PolicyBuilder.ERR_POLICY_PROPAGATOR_NODE_SOURCE_INVALID);
            put("policy-propagator-node-target-invalid.json", PolicyBuilder.ERR_POLICY_PROPAGATOR_NODE_TARGET_INVALID);
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
            put("policy-node-no-signature.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_NOT_EXISTS);
            put("policy-node-signature-empty.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_EMPTY);
            put("policy-node-signature-invalid.json", PolicyBuilder.ERR_POLICY_NODE_SIGNATURE_INVALID);
            put("policy-sink-node-no-source.json", PolicyBuilder.ERR_POLICY_SINK_NODE_INVALID);
            put("policy-sink-node-source-invalid.json", PolicyBuilder.ERR_POLICY_SINK_NODE_SOURCE_INVALID);
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
}