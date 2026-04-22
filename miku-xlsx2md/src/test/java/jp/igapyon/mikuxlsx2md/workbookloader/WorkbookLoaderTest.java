/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.workbookloader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

class WorkbookLoaderTest {
  @Test
  void parsesDefinedNamesAndSkipsXlnmEntries() {
    final Document workbookDoc = XmlUtils.xmlToDocument(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
            + "<definedNames>"
            + "<definedName name=\"ValidName\">Sheet1!$A$1</definedName>"
            + "<definedName name=\"_xlnm.Print_Area\">Sheet1!$A$1:$B$2</definedName>"
            + "<definedName name=\"LocalName\" localSheetId=\"1\">Sheet2!$C$3</definedName>"
            + "</definedNames>"
            + "</workbook>");

    final List<WorkbookLoader.DefinedName> result = WorkbookLoader.parseDefinedNames(workbookDoc, Arrays.asList("Sheet1", "Sheet2"));
    assertEquals(Arrays.asList(
        new WorkbookLoader.DefinedName("ValidName", "=Sheet1!$A$1", null),
        new WorkbookLoader.DefinedName("LocalName", "=Sheet2!$C$3", "Sheet2")),
        result);
  }

  @Test
  void loadsWorkbookPartsAndInvokesWorksheetParsingAndPostProcessing() {
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/workbook.xml", (
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            + "<sheets>"
            + "<sheet name=\"First\" r:id=\"rId1\"/>"
            + "<sheet name=\"Second\" r:id=\"rId2\"/>"
            + "</sheets>"
            + "</workbook>").getBytes(StandardCharsets.UTF_8));
    final List<String> seen = new ArrayList<String>();
    final int[] post = new int[] {0};

    final WorkbookLoader.ParsedWorkbook workbook = WorkbookLoader.parseWorkbook(new byte[0], "book.xlsx", new WorkbookLoader.WorkbookLoaderDependencies() {
      @Override
      public Map<String, byte[]> unzipEntries(final byte[] workbookBytes) {
        return files;
      }

      @Override
      public List<WorkbookLoader.SharedStringEntry> parseSharedStrings(final Map<String, byte[]> inputFiles) {
        return Arrays.asList(new WorkbookLoader.SharedStringEntry("A"));
      }

      @Override
      public List<WorkbookLoader.CellStyleInfo> parseCellStyles(final Map<String, byte[]> inputFiles) {
        return Arrays.asList(new WorkbookLoader.CellStyleInfo(0, "General"));
      }

      @Override
      public Map<String, String> parseRelationships(final Map<String, byte[]> inputFiles, final String relsPath, final String sourcePath) {
        final Map<String, String> rels = new LinkedHashMap<String, String>();
        rels.put("rId1", "xl/worksheets/sheet1.xml");
        rels.put("rId2", "xl/worksheets/sheet2.xml");
        return rels;
      }

      @Override
      public Document xmlToDocument(final String xmlText) {
        return XmlUtils.xmlToDocument(xmlText);
      }

      @Override
      public String decodeXmlText(final byte[] bytes) {
        return XmlUtils.decodeXmlText(bytes);
      }

      @Override
      public Object parseWorksheet(
          final Map<String, byte[]> inputFiles,
          final String sheetName,
          final String sheetPath,
          final int sheetIndex,
          final List<WorkbookLoader.SharedStringEntry> sharedStrings,
          final List<WorkbookLoader.CellStyleInfo> cellStyles) {
        seen.add(sheetName + "|" + sheetPath + "|" + sheetIndex);
        return sheetName;
      }

      @Override
      public void postProcessWorkbook(final WorkbookLoader.ParsedWorkbook parsedWorkbook) {
        post[0] += 1;
      }
    });

    assertEquals("book.xlsx", workbook.getName());
    assertEquals(Arrays.asList(new WorkbookLoader.SharedStringEntry("A")), workbook.getSharedStrings());
    assertEquals(2, workbook.getSheets().size());
    assertEquals(Arrays.asList(
        "First|xl/worksheets/sheet1.xml|1",
        "Second|xl/worksheets/sheet2.xml|2"),
        seen);
    assertEquals(1, post[0]);
  }

  @Test
  void throwsWhenWorkbookXmlIsMissing() {
    final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        WorkbookLoader.parseWorkbook(new byte[0], "book.xlsx", new WorkbookLoader.WorkbookLoaderDependencies() {
          @Override
          public Map<String, byte[]> unzipEntries(final byte[] workbookBytes) {
            return new LinkedHashMap<String, byte[]>();
          }

          @Override
          public List<WorkbookLoader.SharedStringEntry> parseSharedStrings(final Map<String, byte[]> files) {
            return new ArrayList<WorkbookLoader.SharedStringEntry>();
          }

          @Override
          public List<WorkbookLoader.CellStyleInfo> parseCellStyles(final Map<String, byte[]> files) {
            return new ArrayList<WorkbookLoader.CellStyleInfo>();
          }

          @Override
          public Map<String, String> parseRelationships(final Map<String, byte[]> files, final String relsPath, final String sourcePath) {
            return new LinkedHashMap<String, String>();
          }

          @Override
          public Document xmlToDocument(final String xmlText) {
            return XmlUtils.xmlToDocument("<root/>");
          }

          @Override
          public String decodeXmlText(final byte[] bytes) {
            return "";
          }

          @Override
          public Object parseWorksheet(
              final Map<String, byte[]> files,
              final String sheetName,
              final String sheetPath,
              final int sheetIndex,
              final List<WorkbookLoader.SharedStringEntry> sharedStrings,
              final List<WorkbookLoader.CellStyleInfo> cellStyles) {
            return new Object();
          }

          @Override
          public void postProcessWorkbook(final WorkbookLoader.ParsedWorkbook workbook) {
          }
        }));

    assertEquals("xl/workbook.xml was not found.", exception.getMessage());
  }
}
