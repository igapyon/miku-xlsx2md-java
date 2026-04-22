/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.xmlutils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public final class XmlUtils {
  private XmlUtils() {
  }

  public static Document xmlToDocument(final String xmlText) {
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      final DocumentBuilder builder = factory.newDocumentBuilder();
      final InputSource inputSource = new InputSource(new ByteArrayInputStream((xmlText == null ? "" : xmlText).getBytes(StandardCharsets.UTF_8)));
      return builder.parse(inputSource);
    } catch (final Exception ex) {
      throw new IllegalArgumentException("Failed to parse XML.", ex);
    }
  }

  public static List<Element> getElementsByLocalName(final Node root, final String localName) {
    final List<Element> result = new ArrayList<Element>();
    collectElementsByLocalName(root, localName, result);
    return result;
  }

  public static Element getFirstChildByLocalName(final Node root, final String localName) {
    final List<Element> elements = getElementsByLocalName(root, localName);
    return elements.isEmpty() ? null : elements.get(0);
  }

  public static Element getDirectChildByLocalName(final Node root, final String localName) {
    if (root == null) {
      return null;
    }
    final NodeList childNodes = root.getChildNodes();
    for (int index = 0; index < childNodes.getLength(); index += 1) {
      final Node child = childNodes.item(index);
      if (child != null && child.getNodeType() == Node.ELEMENT_NODE && localName.equals(child.getLocalName())) {
        return (Element) child;
      }
    }
    return null;
  }

  public static String decodeXmlText(final byte[] bytes) {
    return new String(bytes == null ? new byte[0] : bytes, StandardCharsets.UTF_8);
  }

  public static String getTextContent(final Element node) {
    return (node == null || node.getTextContent() == null ? "" : node.getTextContent()).replace("\r\n", "\n");
  }

  private static void collectElementsByLocalName(final Node node, final String localName, final List<Element> result) {
    if (node == null) {
      return;
    }
    if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName())) {
      result.add((Element) node);
    }
    final NodeList childNodes = node.getChildNodes();
    for (int index = 0; index < childNodes.getLength(); index += 1) {
      collectElementsByLocalName(childNodes.item(index), localName, result);
    }
  }
}
