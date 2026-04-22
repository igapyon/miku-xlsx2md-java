/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.mavenplugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikuxlsx2md.zipio.ZipIo;

class MikuXlsx2mdMojoTest {
  @TempDir
  Path tempDir;

  @Test
  void skipsWhenRequested() {
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setSkip(true);

    assertDoesNotThrow(() -> mojo.execute());
  }

  @Test
  void writesMarkdownThroughCoreConversion() throws java.io.IOException {
    final Path inputPath = tempDir.resolve("sample.xlsx");
    final Path outputPath = tempDir.resolve("out").resolve("sample.md");
    Files.write(inputPath, createWorkbookBytes());
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(inputPath.toFile());
    mojo.setOutputFile(outputPath.toFile());
    mojo.setOutputMode("both");
    mojo.setFormattingMode("github");
    mojo.setTableDetectionMode("border");
    mojo.setEncoding("utf-8");
    mojo.setBom("off");

    assertDoesNotThrow(() -> mojo.execute());
    assertEquals(inputPath.toFile(), mojo.getInputFile());
    assertEquals(outputPath.toFile(), mojo.getOutputFile());
    assertEquals("both", mojo.getOutputMode());
    assertEquals("github", mojo.getFormattingMode());
    assertEquals("border", mojo.getTableDetectionMode());
    assertEquals("utf-8", mojo.getEncoding());
    assertEquals("off", mojo.getBom());
    assertTrue(Files.isRegularFile(outputPath));
    assertTrue(new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8).contains("# Book: sample.xlsx"));
    assertTrue(new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8).contains("Hello [raw=0]"));
  }

  @Test
  void convertsUpstreamHyperlinkFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("link", "hyperlink-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("hyperlink.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());
    mojo.setFormattingMode("github");

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: hyperlink-basic-sample01.xlsx"));
    assertTrue(markdown.contains("[Open example](https://example.com/)"));
    assertTrue(markdown.contains("[Jump to Other](#other)"));
  }

  private static byte[] createWorkbookBytes() {
    return ZipIo.createStoredZip(new ZipIo.ExportEntry[] {
        entry("xl/workbook.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
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
  }

  private static ZipIo.ExportEntry entry(final String name, final String text) {
    return new ZipIo.ExportEntry(name, text.getBytes(StandardCharsets.UTF_8));
  }

  private static Path resolveFixturePath(final String group, final String fileName) {
    final Path local = Paths.get("workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
    if (Files.isRegularFile(local)) {
      return local;
    }
    return Paths.get("..", "workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
  }
}
