/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.xmlutils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XmlUtilsTest {
  @Test
  void findsElementsByLocalNameAcrossNamespaces() {
    final Document doc = XmlUtils.xmlToDocument("<root xmlns:a=\"urn:test\"><a:item>v1</a:item><a:item>v2</a:item></root>");
    final List<String> values = new ArrayList<String>();
    for (final Element node : XmlUtils.getElementsByLocalName(doc, "item")) {
      values.add(node.getTextContent());
    }

    assertEquals(2, values.size());
    assertEquals("v1", values.get(0));
    assertEquals("v2", values.get(1));
    assertEquals("v1", XmlUtils.getFirstChildByLocalName(doc, "item").getTextContent());
  }

  @Test
  void findsOnlyDirectChildrenForALocalName() {
    final Document doc = XmlUtils.xmlToDocument("<root><item><item>nested</item></item><item>direct</item></root>");
    final Element root = doc.getDocumentElement();

    assertEquals("nested", XmlUtils.getDirectChildByLocalName(root, "item").getTextContent());
  }

  @Test
  void decodesUtf8BytesAndNormalizesCrLfTextContent() {
    final byte[] encoded = "<root>line1\r\nline2</root>".getBytes(StandardCharsets.UTF_8);
    final Document doc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(encoded));

    assertEquals("line1\nline2", XmlUtils.getTextContent(doc.getDocumentElement()));
  }
}
