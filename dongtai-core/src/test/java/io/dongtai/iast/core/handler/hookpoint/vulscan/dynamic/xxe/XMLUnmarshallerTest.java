package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.xml.sax.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

public class XMLUnmarshallerTest extends XXECheckTest {
    private final static String NAME = "XMLUnmarshaller";

    public final static String LINUX_PAYLOAD = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ELEMENT foo ANY><!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]><foo><bar>&xxe;</bar></foo>";
    public final static String WINDOWS_PAYLOAD = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ELEMENT foo ANY><!ENTITY xxe SYSTEM \"file:///c:/windows/win.ini\" >]><foo><bar>&xxe;</bar></foo>";

    @Override
    public String getPayload() {
        if (isWindows()) {
            return WINDOWS_PAYLOAD;
        }
        return LINUX_PAYLOAD;
    }

    @Test
    public void testGetSupport() throws JAXBException, ParserConfigurationException, SAXException {
        JAXBContext context;
        Unmarshaller um;
        SAXParserFactory parser;
        XMLReader reader;
        Source source;
        Support support;
        XMLUnmarshallerCheck checker = new XMLUnmarshallerCheck();
        String realContent = getXXERealContent();
        String payload = getPayload();

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        Assert.assertTrue(NAME + " match XMLUnmarshallerCheck", checker.match(um));
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] default", realContent, getNode(um));

        // parser features

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser disallow-doctype-decl", Support.DISALLOWED, support);
        final Unmarshaller um11 = um;
        final Source source11 = source;
        Assert.assertThrows(NAME + "[C] parser disallow-doctype-decl", JAXBException.class, new ThrowingRunnable() {
            @Override
            public void run() throws JAXBException {
                getNode(um11, source11);
            }
        });

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser secure-processing", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] parser secure-processing", realContent, getNode(um, source));

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser disallow ege/epe/led", Support.DISALLOWED, support);
        Assert.assertEquals(NAME + "[C] parser disallow ege/epe/led", SAFE_OR_BLIND, getNode(um, source));

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser disallow epe/led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] parser disallow epe/led", realContent, getNode(um, source));

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser disallow epe/led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] parser disallow epe/led", SAFE_OR_BLIND, getNode(um, source));

        // reader features

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " reader disallow-doctype-decl", Support.DISALLOWED, support);
        final Unmarshaller um21 = um;
        final Source source21 = source;
        Assert.assertThrows(NAME + "[C] reader disallow-doctype-decl", JAXBException.class, new ThrowingRunnable() {
            @Override
            public void run() throws JAXBException {
                getNode(um21, source21);
            }
        });

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        reader = parser.newSAXParser().getXMLReader();
        reader.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " reader secure-processing", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] reader secure-processing", realContent, getNode(um, source));

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        reader = parser.newSAXParser().getXMLReader();
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " reader disallow ege/epe/led", Support.DISALLOWED, support);
        Assert.assertEquals(NAME + "[C] reader disallow ege/epe/led", SAFE_OR_BLIND, getNode(um, source));

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        reader = parser.newSAXParser().getXMLReader();
        reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " reader disallow epe/led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] reader disallow epe/led", realContent, getNode(um, source));

        context = JAXBContext.newInstance(Foo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        reader = parser.newSAXParser().getXMLReader();
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " reader disallow epe/led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] reader disallow epe/led", SAFE_OR_BLIND, getNode(um, source));
    }

    private String getNode(Unmarshaller um) throws JAXBException {
        return getNode(um, null);
    }

    private String getNode(Unmarshaller um, Source source) throws JAXBException {
        String payload = getPayload();
        Object obj;

        if (source != null) {
            obj = um.unmarshal(source);
        } else {
            obj = um.unmarshal(new InputSource(new StringReader(payload)));
        }

        if (obj instanceof Foo) {
            String foo = ((Foo) obj).bar;
            if ("".equals(foo)) {
                return SAFE_OR_BLIND;
            }
            return foo;
        }
        return "error: invalid xml";
    }

    @XmlRootElement
    private static class Foo {
        @XmlElement(name = "bar")
        public String bar;
    }
}
