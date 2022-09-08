package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.StringReader;

public class XMLParserCheckTest extends XXECheckTest {
    @Test
    public void testXMLReaderGetSupport() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf;
        SAXParser parser;
        XMLReader reader;
        Support support;
        XMLReaderCheck checker = new XMLReaderCheck();
        String realContent = getXXERealContent();

        spf = SAXParserFactory.newInstance();
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader default", Support.ALLOWED, support);
        Assert.assertEquals("XMLReader content default", realContent, xmlReaderGetNode(reader));

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader disallow-doctype-decl", Support.DISALLOWED, support);
        final XMLReader reader1 = reader;
        Assert.assertThrows("DocumentBuilder content disallow-doctype-decl", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                xmlReaderGetNode(reader1);
            }
        });

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader disallow ege/epe/led", Support.DISALLOWED, support);
        Assert.assertEquals("XMLReader content disallow ege/epe/led", SAFE_OR_BLIND, xmlReaderGetNode(reader));

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader disallow epe", Support.ALLOWED, support);
        Assert.assertEquals("XMLReader content disallow epe", realContent, xmlReaderGetNode(reader));

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader secure-processing", Support.DISALLOWED, support);
        final XMLReader reader2 = reader;
        Assert.assertThrows("DocumentBuilder content secure-processing", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                xmlReaderGetNode(reader2);
            }
        });

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader secure-processing and disallow led", Support.ALLOWED, support);
        Assert.assertEquals("XMLReader content secure-processing and disallow led", realContent, xmlReaderGetNode(reader));
    }

    private String xmlReaderGetNode(XMLReader reader) throws SAXException {
        String payload = getPayload();

        try {
            XMLReaderContentHandler handler = new XMLReaderContentHandler();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new StringReader(payload)));
            String foo = handler.getFoo();
            if (foo.isEmpty()) {
                return SAFE_OR_BLIND;
            }
            return foo;
        } catch (IOException e) {
            return "error: " + e.toString();
        }
    }

    private static class XMLReaderContentHandler extends DefaultHandler {
        private String foo;
        private StringBuffer tmp;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if ("foo".equals(qName)) {
                this.tmp = new StringBuffer();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            try {
                this.tmp.append(ch, start, length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if ("foo".equals(qName)) {
                this.foo = this.tmp.toString();
            }
        }

        public String getFoo() {
            return this.foo;
        }
    }
}
