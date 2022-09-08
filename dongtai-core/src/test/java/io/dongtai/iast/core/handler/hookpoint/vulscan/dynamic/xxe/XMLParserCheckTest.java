package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.*;

public class XMLParserCheckTest {
    @Test
    public void testGetSupport() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf;
        SAXParser parser;
        XMLReader reader;
        Support support;
        XMLReaderCheck checker = new XMLReaderCheck();

        spf = SAXParserFactory.newInstance();
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader default", Support.ALLOWED, support);

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader disallow-doctype-decl", Support.DISALLOWED, support);

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader disallow ege/epe/led", Support.DISALLOWED, support);

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader disallow epe", Support.ALLOWED, support);

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader secure-processing", Support.DISALLOWED, support);

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader secure-processing and disallow led", Support.ALLOWED, support);
    }
}
