package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.*;
import java.io.StringReader;

public class XMLStreamReaderCheckTest extends XXECheckTest {
    private final static String NAME = "XMLStreamReader";

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
    public void testGetSupport() throws Throwable {
        XMLInputFactory xif;
        XMLStreamReader reader;
        Support support;
        XMLStreamReaderCheck checker = new XMLStreamReaderCheck();
        String realContent = getXXERealContent();
        String payload = getPayload();

        xif = XMLInputFactory.newInstance();
        reader = xif.createXMLStreamReader(new StringReader(payload));
        XXEChecker chk = XXECheck.getChecker(reader);
        Assert.assertNotNull(chk);
        Assert.assertEquals(NAME + " match XMLStreamReaderCheck", checker.getClass(), chk.getClass());
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] default", realContent, getNode(reader));

        xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        reader = xif.createXMLStreamReader(new StringReader(payload));
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow supportDTD", Support.DISALLOWED, support);
        final XMLStreamReader reader1 = reader;
        Assert.assertThrows(NAME + "[C] disallow supportDTD", XMLStreamException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                getNode(reader1);
            }
        });

        xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        reader = xif.createXMLStreamReader(new StringReader(payload));
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow isSupportingExternalEntities", Support.DISALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow isSupportingExternalEntities", SAFE_OR_BLIND, getNode(reader));

        xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        reader = xif.createXMLStreamReader(new StringReader(payload));
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " empty accessExternalDTD", Support.DISALLOWED, support);
        final XMLStreamReader reader2 = reader;
        Assert.assertThrows(NAME + "[C] empty accessExternalDTD", XMLStreamException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                getNode(reader2);
            }
        });
    }

    private String getNode(XMLStreamReader reader) throws Throwable {
        Object obj;
        try {
            JAXBContext context = JAXBContext.newInstance(XMLStreamReaderCheckTestFoo.class);
            Unmarshaller um = context.createUnmarshaller();
            obj = um.unmarshal(reader);

            if (obj instanceof XMLStreamReaderCheckTestFoo) {
                String bar = ((XMLStreamReaderCheckTestFoo) obj).bar;
                if ("".equals(bar)) {
                    return SAFE_OR_BLIND;
                }
                return bar;
            }
            return "error: invalid xml";
        } catch (UnmarshalException e) {
            if (e.getLinkedException().getClass() == XMLStreamException.class) {
                throw (XMLStreamException) e.getLinkedException();
            }
            return "error: " + e.toString();
        }
    }

    @XmlRootElement(name = "foo")
    private static class XMLStreamReaderCheckTestFoo {
        @XmlElement(name = "bar")
        public String bar;
    }
}
