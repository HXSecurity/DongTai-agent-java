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

    public final static String LINUX_PAYLOAD = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]><foo><bar>&xxe;</bar></foo>";
    public final static String WINDOWS_PAYLOAD = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///c:/windows/win.ini\" >]><foo><bar>&xxe;</bar></foo>";

    @Override
    public String getPayload() {
        if (isWindows()) {
            return WINDOWS_PAYLOAD;
        }
        return LINUX_PAYLOAD;
    }

    @Test
    public void testGetSupport() throws JAXBException {
        JAXBContext context;
        Unmarshaller um;
        Support support;
        XMLUnmarshallerCheck checker = new XMLUnmarshallerCheck();

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        XXEChecker chk = XXECheck.getChecker(um);
        Assert.assertNotNull(chk);
        Assert.assertEquals(NAME + " match XMLUnmarshallerCheck", checker.getClass(), chk.getClass());
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " default", Support.DISALLOWED, support);
        final Unmarshaller um10 = um;
        Assert.assertThrows(NAME + "[C] default", SAXParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXParseException {
                getNode(um10);
            }
        });
    }

    @Test
    public void testGetSupportWithSAXParserFeature() throws JAXBException, ParserConfigurationException, SAXException {
        JAXBContext context;
        Unmarshaller um;
        SAXParserFactory parser;
        XMLReader reader;
        Source source;
        Support support;
        XMLUnmarshallerCheck checker = new XMLUnmarshallerCheck();
        String realContent = getXXERealContent();
        String payload = getPayload();

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser disallow-doctype-decl", Support.DISALLOWED, support);
        final Unmarshaller um1 = um;
        final Source source1 = source;
        Assert.assertThrows(NAME + "[C] parser disallow-doctype-decl", SAXParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXParseException {
                getNode(um1, source1);
            }
        });

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);


        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser enable doctype-decl", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] parser enable doctype-decl", realContent, getNode(um, source));

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser secure-processing", Support.DISALLOWED, support);
        final Unmarshaller um2 = um;
        final Source source2 = source;
        Assert.assertThrows(NAME + "[C] parser secure-processing", SAXParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXParseException {
                getNode(um2, source2);
            }
        });

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser secure-processing & disallow led", Support.ALLOWED, support);
//        Assert.assertEquals(NAME + "[C] parser secure-processing & disallow led", realContent, getNode(um, source));

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser secure-processing & disallow epe", Support.DISALLOWED, support);
        final Unmarshaller um3 = um;
        final Source source3 = source;
        Assert.assertThrows(NAME + "[C] parser secure-processing & disallow epe", SAXParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXParseException {
                getNode(um3, source3);
            }
        });

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
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

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
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

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader = parser.newSAXParser().getXMLReader();
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " parser disallow epe/led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] parser disallow epe/led", SAFE_OR_BLIND, getNode(um, source));
    }

    @Test
    public void testGetSupportWithXMLReaderFeature() throws JAXBException, ParserConfigurationException, SAXException {
        JAXBContext context;
        Unmarshaller um;
        SAXParserFactory parser;
        XMLReader reader;
        Source source;
        Support support;
        XMLUnmarshallerCheck checker = new XMLUnmarshallerCheck();
        String realContent = getXXERealContent();
        String payload = getPayload();

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        reader = parser.newSAXParser().getXMLReader();
        reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " reader disallow-doctype-decl", Support.DISALLOWED, support);
        final Unmarshaller um1 = um;
        final Source source1 = source;
        Assert.assertThrows(NAME + "[C] reader disallow-doctype-decl", SAXParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXParseException {
                getNode(um1, source1);
            }
        });

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
        um = context.createUnmarshaller();
        parser = SAXParserFactory.newInstance();
        reader = parser.newSAXParser().getXMLReader();
        reader.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        source = new SAXSource(reader, new InputSource(new StringReader(payload)));
        checker.setSourceObjectAndParameters(um, new Object[]{source});
        support = checker.getSupport(um);
        Assert.assertEquals(NAME + " reader secure-processing", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] reader secure-processing", realContent, getNode(um, source));

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
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

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
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

        context = JAXBContext.newInstance(XMLUnmarshallerTestFoo.class);
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

    private String getNode(Unmarshaller um) throws SAXParseException {
        return getNode(um, (Source) null);
    }

    private String getNode(Unmarshaller um, Source source) throws SAXParseException {
        String payload = getPayload();
        Object obj;

        try {

            if (source != null) {
                obj = um.unmarshal(source);
            } else {
                obj = um.unmarshal(new InputSource(new StringReader(payload)));
            }

            if (obj instanceof XMLUnmarshallerTestFoo) {
                String bar = ((XMLUnmarshallerTestFoo) obj).bar;
                if ("".equals(bar)) {
                    return SAFE_OR_BLIND;
                }
                return bar;
            }
            return "error: invalid xml";
        } catch (JAXBException e) {
            if (e.getLinkedException() != null && e.getLinkedException().getClass() == SAXParseException.class) {
                throw (SAXParseException) e.getLinkedException();
            }
            return "error: " + e.toString();
        }
    }

    @XmlRootElement(name = "foo")
    private static class XMLUnmarshallerTestFoo {
        @XmlElement(name = "bar")
        public String bar;
    }
}
