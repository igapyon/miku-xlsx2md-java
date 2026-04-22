/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.stylesparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

class StylesParserTest {
  @Test
  void returnsAGeneralDefaultStyleWhenStylesXmlIsMissing() {
    assertEquals(
        Arrays.asList(new StylesParser.CellStyleInfo(
            StylesParser.EMPTY_BORDERS,
            0,
            "General",
            StylesParser.EMPTY_TEXT_STYLE)),
        StylesParser.parseCellStyles(new LinkedHashMap<String, byte[]>()));
  }

  @Test
  void detectsBorderSidesFromStyleAttributesOrChildNodes() {
    final Document doc = XmlUtils.xmlToDocument("<root><top style=\"thin\"/><bottom><color rgb=\"000000\"/></bottom><left/><right/></root>");

    assertEquals(true, StylesParser.hasBorderSide(XmlUtils.getFirstChildByLocalName(doc, "top")));
    assertEquals(true, StylesParser.hasBorderSide(XmlUtils.getFirstChildByLocalName(doc, "bottom")));
    assertEquals(false, StylesParser.hasBorderSide(XmlUtils.getFirstChildByLocalName(doc, "left")));
    assertEquals(false, StylesParser.hasBorderSide(XmlUtils.getFirstChildByLocalName(doc, "right")));
  }

  @Test
  void parsesBordersAndCustomNumberFormatsFromStylesXml() {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"yyyy-mm-dd\"/></numFmts>"
        + "<borders count=\"2\">"
        + "<border><left/><right/><top/><bottom/></border>"
        + "<border><left style=\"thin\"/><right/><top style=\"thin\"/><bottom/></border>"
        + "</borders>"
        + "<cellXfs count=\"2\">"
        + "<xf numFmtId=\"0\" borderId=\"0\"/>"
        + "<xf numFmtId=\"164\" borderId=\"1\"/>"
        + "</cellXfs>"
        + "</styleSheet>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/styles.xml", xml.getBytes(StandardCharsets.UTF_8));

    final List<StylesParser.CellStyleInfo> styles = StylesParser.parseCellStyles(files);
    assertEquals(2, styles.size());
    assertEquals(new StylesParser.CellStyleInfo(
        new StylesParser.BorderFlags(false, false, false, false),
        0,
        "General",
        StylesParser.EMPTY_TEXT_STYLE), styles.get(0));
    assertEquals(new StylesParser.CellStyleInfo(
        new StylesParser.BorderFlags(true, false, true, false),
        164,
        "yyyy-mm-dd",
        StylesParser.EMPTY_TEXT_STYLE), styles.get(1));
  }

  @Test
  void fallsBackToBuiltInFormatCodesWhenNoCustomNumFmtExists() {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        + "<borders count=\"1\"><border><left/><right/><top/><bottom/></border></borders>"
        + "<cellXfs count=\"1\"><xf numFmtId=\"14\" borderId=\"0\"/></cellXfs>"
        + "</styleSheet>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/styles.xml", xml.getBytes(StandardCharsets.UTF_8));

    final List<StylesParser.CellStyleInfo> styles = StylesParser.parseCellStyles(files);
    assertEquals("yyyy/m/d", styles.get(0).getFormatCode());
  }
}
