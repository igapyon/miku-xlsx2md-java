/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.relsparser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class RelsParserTest {
  @Test
  void normalizesZipPathsRelativeToTheSourcePath() {
    assertEquals("xl/drawings/drawing1.xml", RelsParser.normalizeZipPath("xl/worksheets/sheet1.xml", "../drawings/drawing1.xml"));
    assertEquals("docProps/app.xml", RelsParser.normalizeZipPath("xl/workbook.xml", "/docProps/app.xml"));
  }

  @Test
  void buildsRelsPathsNextToTheSourceFile() {
    assertEquals("xl/_rels/workbook.xml.rels", RelsParser.buildRelsPath("xl/workbook.xml"));
    assertEquals("xl/worksheets/_rels/sheet1.xml.rels", RelsParser.buildRelsPath("xl/worksheets/sheet1.xml"));
  }

  @Test
  void parsesRelationshipTargetsIntoAMap() {
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/_rels/workbook.xml.rels", (
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Target=\"worksheets/sheet1.xml\"/>"
            + "<Relationship Id=\"rId2\" Target=\"../sharedStrings.xml\"/>"
            + "</Relationships>").getBytes(StandardCharsets.UTF_8));

    final Map<String, String> rels = RelsParser.parseRelationships(files, "xl/_rels/workbook.xml.rels", "xl/workbook.xml");
    assertEquals("xl/worksheets/sheet1.xml", rels.get("rId1"));
    assertEquals("sharedStrings.xml", rels.get("rId2"));
  }

  @Test
  void keepsExternalRelationshipTargetsAsIs() {
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/_rels/sheet1.xml.rels", (
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Target=\"https://example.com/docs?a=1&amp;b=2\" TargetMode=\"External\"/>"
            + "</Relationships>").getBytes(StandardCharsets.UTF_8));

    final Map<String, RelsParser.RelationshipEntry> rels =
        RelsParser.parseRelationshipEntries(files, "xl/worksheets/_rels/sheet1.xml.rels", "xl/worksheets/sheet1.xml");

    assertEquals(new RelsParser.RelationshipEntry("https://example.com/docs?a=1&b=2", "External", ""), rels.get("rId1"));
  }
}
