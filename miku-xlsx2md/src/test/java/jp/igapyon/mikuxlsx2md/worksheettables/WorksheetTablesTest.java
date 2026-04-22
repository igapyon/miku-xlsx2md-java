/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.worksheettables;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

class WorksheetTablesTest {
  @Test
  void normalizesStructuredTableKeys() {
    assertEquals("TABLE 1", WorksheetTables.normalizeStructuredTableKey("  Ｔａｂｌｅ　１ "));
  }

  @Test
  void returnsNoTablesWhenNoTablePartsExist() {
    final Document worksheetDoc = XmlUtils.xmlToDocument(
        "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"></worksheet>");

    assertEquals(Collections.emptyList(),
        WorksheetTables.parseWorksheetTables(Collections.<String, byte[]>emptyMap(), worksheetDoc, "Sheet1", "xl/worksheets/sheet1.xml"));
  }

  @Test
  void resolvesWorksheetTablePartsThroughRelationships() {
    final String worksheetXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            + "<tableParts count=\"1\"><tablePart r:id=\"rId1\"/></tableParts>"
            + "</worksheet>";
    final String relsXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Target=\"../tables/table1.xml\"/>"
            + "</Relationships>";
    final String tableXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<table xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
            + "id=\"1\" name=\"SalesTable\" displayName=\"Sales Table\" ref=\"$B$2:$D$5\" "
            + "headerRowCount=\"1\" totalsRowCount=\"1\">"
            + "<tableColumns count=\"3\">"
            + "<tableColumn id=\"1\" name=\"Code\"/>"
            + "<tableColumn id=\"2\" name=\"Name\"/>"
            + "<tableColumn id=\"3\" name=\"Amount\"/>"
            + "</tableColumns>"
            + "</table>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/_rels/sheet1.xml.rels", relsXml.getBytes(StandardCharsets.UTF_8));
    files.put("xl/tables/table1.xml", tableXml.getBytes(StandardCharsets.UTF_8));
    final Document worksheetDoc = XmlUtils.xmlToDocument(worksheetXml);

    final List<WorksheetTables.ParsedTable> tables =
        WorksheetTables.parseWorksheetTables(files, worksheetDoc, "Report", "xl/worksheets/sheet1.xml");

    assertEquals(Arrays.asList(new WorksheetTables.ParsedTable(
        "Report",
        "SalesTable",
        "Sales Table",
        "B2",
        "D5",
        Arrays.asList("Code", "Name", "Amount"),
        1,
        1)), tables);
  }

  @Test
  void ignoresTableDefinitionsWithInvalidRanges() {
    final String worksheetXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            + "<tableParts count=\"1\"><tablePart r:id=\"rId1\"/></tableParts>"
            + "</worksheet>";
    final String relsXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Target=\"../tables/table1.xml\"/>"
            + "</Relationships>";
    final String tableXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<table xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
            + "id=\"1\" name=\"BrokenTable\" ref=\"not-a-range\"/>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/_rels/sheet1.xml.rels", relsXml.getBytes(StandardCharsets.UTF_8));
    files.put("xl/tables/table1.xml", tableXml.getBytes(StandardCharsets.UTF_8));
    final Document worksheetDoc = XmlUtils.xmlToDocument(worksheetXml);

    assertEquals(Collections.emptyList(),
        WorksheetTables.parseWorksheetTables(files, worksheetDoc, "Report", "xl/worksheets/sheet1.xml"));
  }
}
