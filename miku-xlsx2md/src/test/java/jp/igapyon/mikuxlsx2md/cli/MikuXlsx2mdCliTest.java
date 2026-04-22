/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikuxlsx2md.zipio.ZipIo;

class MikuXlsx2mdCliTest {
  @TempDir
  Path tempDir;

  @Test
  void printsHelpAndExitsSuccessfully() {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    final int exitCode = MikuXlsx2mdCli.run(new String[] {"--help"}, asPrintStream(stdout), asPrintStream(stderr));

    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("Usage:"));
    assertTrue(asString(stdout).contains("--shape-details"));
    assertTrue(asString(stdout).contains("--include-shape-details"));
    assertTrue(asString(stdout).contains("--encoding"));
    assertTrue(asString(stdout).contains("--bom"));
    assertTrue(asString(stdout).contains("--formatting-mode"));
    assertTrue(asString(stdout).contains("--table-detection-mode"));
    assertTrue(asString(stdout).contains("Exit codes:"));
    assertEquals("", asString(stderr));
  }

  @Test
  void failsForUnknownOption() {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {"sample.xlsx", "--unknown-option"},
        asPrintStream(stdout),
        asPrintStream(stderr));

    assertEquals(1, exitCode);
    assertTrue(asString(stderr).contains("Unknown option: --unknown-option"));
  }

  @Test
  void acceptsKnownOptionsAndWritesConvertedMarkdown() throws java.io.IOException {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path inputPath = tempDir.resolve("sample.xlsx");
    final Path outputPath = tempDir.resolve("out").resolve("sample.md");
    Files.write(inputPath, createWorkbookBytes());

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            inputPath.toString(),
            "--out", outputPath.toString(),
            "--output-mode", "both",
            "--formatting-mode", "github",
            "--table-detection-mode", "border-priority",
            "--shape-details", "include",
            "--keep-empty-rows",
            "--keep-empty-columns",
            "--summary"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("[workbook] sample.xlsx"));
    assertTrue(asString(stdout).contains("Output file: sample_001_Sheet1.md"));
    assertEquals("", asString(stderr));
    assertTrue(Files.isRegularFile(outputPath));
    assertTrue(new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8).contains("# Book: sample.xlsx"));
    assertTrue(new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8).contains("Hello [raw=0]"));
  }

  @Test
  void writesZipOnlyWhenZipPathIsSpecified() throws java.io.IOException {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path inputPath = tempDir.resolve("sample.xlsx");
    final Path zipPath = tempDir.resolve("out").resolve("sample.zip");
    Files.write(inputPath, createWorkbookBytes());

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            inputPath.toString(),
            "--zip", zipPath.toString()
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    assertEquals(0, exitCode);
    assertEquals("", asString(stdout));
    assertEquals("", asString(stderr));
    assertTrue(Files.isRegularFile(zipPath));
    assertTrue(ZipIo.unzipEntries(Files.readAllBytes(zipPath)).containsKey("output/sample.md"));
  }

  @Test
  void rejectsShiftJisBomCombination() {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {"sample.xlsx", "--encoding", "shift_jis", "--bom", "on"},
        asPrintStream(stdout),
        asPrintStream(stderr));

    assertEquals(1, exitCode);
    assertTrue(asString(stderr).contains("BOM cannot be enabled for shift_jis."));
  }

  @Test
  void convertsUpstreamShapeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("shape.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString(),
            "--shape-details", "include",
            "--summary"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("[workbook] shape-basic-sample01.xlsx"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: shape-basic-sample01.xlsx"));
    assertTrue(markdown.contains("### Shape Block: 001"));
    assertTrue(markdown.contains("![shape_003.svg](assets/shape-basic/shape_003.svg)"));
  }

  @Test
  void keepsBorderPriorityAsCompatibilityAliasWhenUsingUpstreamTableFixture() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("table", "table-border-priority-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("border-priority.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString(),
            "--table-detection-mode", "border-priority",
            "--summary"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("[workbook] table-border-priority-sample01.xlsx"));
    assertTrue(asString(stdout).contains("Table detection mode: border"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: table-border-priority-sample01.xlsx"));
  }

  @Test
  void keepsIncludeShapeDetailsAsCompatibilityAliasWhenUsingUpstreamShapeFixture() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("shape-alias.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString(),
            "--include-shape-details"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertEquals("", asString(stdout));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("### Shape Block: 001"));
    assertTrue(markdown.contains("![shape_003.svg](assets/shape-basic/shape_003.svg)"));
  }

  private static PrintStream asPrintStream(final ByteArrayOutputStream buffer) {
    try {
      return new PrintStream(buffer, true, StandardCharsets.UTF_8.name());
    } catch (final java.io.UnsupportedEncodingException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static String asString(final ByteArrayOutputStream buffer) {
    return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
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
