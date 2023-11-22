package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.function.ThrowingRunnable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class DocumentBuilderTest extends XXECheckTest {
    private final static String NAME = "DocumentBuilder";

//    @Test
    public void testGetSupport() throws ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbf;
        DocumentBuilder builder;
        Support support;
        DocumentBuilderCheck checker = new DocumentBuilderCheck();
        String realContent = getXXERealContent();

        dbf = DocumentBuilderFactory.newInstance();
        builder = dbf.newDocumentBuilder();
        XXEChecker chk = XXECheck.getChecker(builder);
        Assert.assertNotNull(chk);
        Assert.assertEquals(NAME + " match DocumentBuilderCheck", checker.getClass(), chk.getClass());
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] default", realContent, getNode(builder));

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/xinclude", true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " xinclude", Support.ALLOWED, support);
        // @TODO xinclude payload

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " disallow-doctype-decl", Support.DISALLOWED, support);
        final DocumentBuilder finalBuilder1 = builder;
        Assert.assertThrows(NAME + "[C] disallow-doctype-decl", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                getNode(finalBuilder1);
            }
        });

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " secure-processing", Support.DISALLOWED, support);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
        final DocumentBuilder finalBuilder2 = builder;
        Assert.assertThrows(NAME + "[C] secure-processing", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                getNode(finalBuilder2);
            }
        });

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " secure-processing & disallow led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] secure-processing & disallow led", realContent, getNode(builder));

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " secure-processing & disallow epe", Support.DISALLOWED, support);
        final DocumentBuilder finalBuilder3 = builder;
        Assert.assertThrows(NAME + "[C] secure-processing & disallow epe", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                getNode(finalBuilder3);
            }
        });

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " disallow ege/epe/led", Support.DISALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow ege/epe/led", SAFE_OR_BLIND, getNode(builder));

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " disallow epe/led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow epe/led", realContent, getNode(builder));

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals(NAME + " disallow ege", Support.ALLOWED, support);
//        Assert.assertEquals(NAME + "[C] disallow ege", SAFE_OR_BLIND, getNode(builder));
    }

    private String getNode(DocumentBuilder builder) throws SAXException {
        String payload = getPayload();

        try {
            Document doc = builder.parse(new InputSource(new StringReader(payload)));

            NodeList nodes = doc.getElementsByTagName("foo");
            if (nodes.getLength() == 0) {
                return "safe empty nodes";
            }
            Node node = nodes.item(0).getFirstChild();
            if (node != null) {
                return node.getNodeValue().replaceAll("\\r\\n?", "\n");
            }
            return SAFE_OR_BLIND;
        } catch (IOException e) {
            return "error: " + e.toString();
        }
    }
}
