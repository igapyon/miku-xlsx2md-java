/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;
import jp.igapyon.mikuxlsx2md.zipio.ZipIo;

class CoreTest {
  @Test
  void parsesWorkbookThroughConnectedLoader() {
    final byte[] zipBytes = ZipIo.createStoredZip(new ZipIo.ExportEntry[] {
        entry("xl/workbook.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<definedNames><definedName name=\"MyName\">Sheet1!$A$1</definedName></definedNames>"
                + "<sheets><sheet name=\"Sheet1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>"),
        entry("xl/_rels/workbook.xml.rels",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Target=\"worksheets/sheet1.xml\"/>"
                + "</Relationships>"),
        entry("xl/sharedStrings.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<si><t>Hello</t></si>"
                + "</sst>"),
        entry("xl/styles.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/></border></borders>"
                + "<cellXfs count=\"1\"><xf numFmtId=\"0\" borderId=\"0\" fontId=\"0\"/></cellXfs>"
                + "</styleSheet>"),
        entry("xl/worksheets/sheet1.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<sheetData><row r=\"1\"><c r=\"A1\" t=\"s\"><v>0</v></c></row></sheetData>"
                + "</worksheet>")
    });

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(zipBytes, "book.xlsx");

    assertEquals("book.xlsx", workbook.getName());
    assertEquals(1, workbook.getSheets().size());
    assertEquals(1, workbook.getSharedStrings().size());
    assertEquals(new SharedStrings.SharedStringEntry("Hello", null), workbook.getSharedStrings().get(0));
    assertEquals(1, workbook.getDefinedNames().size());
    assertEquals("=Sheet1!$A$1", workbook.getDefinedNames().get(0).getFormulaText());
    assertEquals("Hello", workbook.getSheets().get(0).getCells().get(0).getOutputValue());
    assertEquals("A1", workbook.getSheets().get(0).getCells().get(0).getAddress());
    assertEquals("Sheet1", workbook.getSheets().get(0).getName());
  }

  private static ZipIo.ExportEntry entry(final String name, final String text) {
    return new ZipIo.ExportEntry(name, text.getBytes(StandardCharsets.UTF_8));
  }
}
