package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import javax.xml.XMLConstants;
import javax.xml.stream.*;
import java.io.StringReader;

public class XMLInputFactoryTest extends XXECheckTest {
    private final static String NAME = "XMLInputFactory";

    @Test
    public void testGetSupport() throws XMLStreamException {
        XMLInputFactory xif;
        Support support;
        XMLInputFactoryCheck checker = new XMLInputFactoryCheck();
        String realContent = getXXERealContent();

        xif = XMLInputFactory.newInstance();
        Assert.assertTrue(NAME + " match XMLInputFactoryCheck", checker.match(xif));
        XXEChecker chk = XXECheck.getChecker(xif);
        Assert.assertNotNull(chk);
        Assert.assertEquals(NAME + " match XMLInputFactoryCheck", checker.getClass(), chk.getClass());
        support = checker.getSupport(xif);
        Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] default", realContent, getNode(xif));

        xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        support = checker.getSupport(xif);
        Assert.assertEquals(NAME + " disallow supportDTD", Support.DISALLOWED, support);
        final XMLInputFactory xif1 = xif;
        Assert.assertThrows(NAME + "[C] disallow supportDTD", XMLStreamException.class, new ThrowingRunnable() {
            @Override
            public void run() throws XMLStreamException {
                getNode(xif1);
            }
        });

        xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        support = checker.getSupport(xif);
        Assert.assertEquals(NAME + " disallow isSupportingExternalEntities", Support.DISALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow isSupportingExternalEntities", SAFE_OR_BLIND, getNode(xif));


        xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        support = checker.getSupport(xif);
        Assert.assertEquals(NAME + " empty accessExternalDTD", Support.DISALLOWED, support);
        final XMLInputFactory xif2 = xif;
        Assert.assertThrows(NAME + "[C] empty accessExternalDTD", XMLStreamException.class, new ThrowingRunnable() {
            @Override
            public void run() throws XMLStreamException {
                getNode(xif2);
            }
        });
    }

    private String getNode(XMLInputFactory xif) throws XMLStreamException {
        String payload = getPayload();
        XMLStreamReader reader = xif.createXMLStreamReader(new StringReader(payload));
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("foo".equals(reader.getName().toString())) {
                    String text = reader.getElementText();
                    if ("".equals(text)) {
                        return SAFE_OR_BLIND;
                    }
                    return text;
                }
            }
        }
        return "safe empty nodes";
    }
}
