package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.parsers.*;

public class DocumentBuilderCheckTest {
    @Test
    public void testGetSupport() throws ParserConfigurationException {
        DocumentBuilderFactory dbf;
        DocumentBuilder builder;
        Support support;
        DocumentBuilderCheck checker = new DocumentBuilderCheck();

        dbf = DocumentBuilderFactory.newInstance();
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder default", Support.ALLOWED, support);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/xinclude", true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder xinclude", Support.ALLOWED, support);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder disallow-doctype-decl", Support.DISALLOWED, support);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder secure-processing", Support.DISALLOWED, support);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder secure-processing & disallow led", Support.ALLOWED, support);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder secure-processing & disallow epe", Support.DISALLOWED, support);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder disallow ege/epe/led", Support.DISALLOWED, support);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder disallow epe/led", Support.ALLOWED, support);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder disallow ege", Support.ALLOWED, support);
    }
}
