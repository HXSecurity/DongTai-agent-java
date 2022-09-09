package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

/** xom dependency xalan will cause feature secure-processing failed

 import nu.xom.*;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.function.ThrowingRunnable;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;

 import java.io.ByteArrayInputStream;
import java.io.IOException;

 public class XomTest extends XXECheckTest {
 private final static String NAME = "XomBuilder";

 @Test public void testGetSupport() throws ParsingException, SAXException {
 XMLReader reader;
 Builder builder;
 Support support;
 XomCheck checker = new XomCheck();
 String realContent = getXXERealContent();

 builder = new Builder();
 Assert.assertTrue(NAME + " match XomCheck", checker.match(builder));
 support = checker.getSupport(builder);
 Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
 Assert.assertEquals(NAME + "[C] default", realContent, getNode(builder));

 reader = XMLReaderFactory.createXMLReader();
 reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
 builder = new Builder(reader);
 support = checker.getSupport(builder);
 Assert.assertEquals(NAME + " disallow-doctype-decl", Support.DISALLOWED, support);
 final Builder builder1 = builder;
 Assert.assertThrows(NAME + "[C] disallow-doctype-decl", ParsingException.class, new ThrowingRunnable() {
 @Override public void run() throws ParsingException {
 getNode(builder1);
 }
 });

 // xom not safe for this
 reader = XMLReaderFactory.createXMLReader();
 reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
 reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
 reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
 builder = new Builder(reader);
 support = checker.getSupport(builder);
 Assert.assertEquals(NAME + " disallow ege/epe/led", Support.ALLOWED, support);
 Assert.assertEquals(NAME + "[C] disallow ege/epe/led", realContent, getNode(builder));

 reader = XMLReaderFactory.createXMLReader();
 reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
 builder = new Builder(reader);
 support = checker.getSupport(builder);
 Assert.assertEquals(NAME + " disallow epe", Support.ALLOWED, support);
 Assert.assertEquals(NAME + "[C] disallow epe", realContent, getNode(builder));
 }

 private String getNode(Builder builder) throws ParsingException {
 String payload = getPayload();

 try {
 Document doc = builder.build(new ByteArrayInputStream(payload.getBytes()));
 Element root = doc.getRootElement();
 String text = root.getValue();
 if ("".equals(text)) {
 return SAFE_OR_BLIND;
 }
 return text;
 } catch (IOException e) {
 return "error: " + e.toString();
 }
 }
 }
 //*/