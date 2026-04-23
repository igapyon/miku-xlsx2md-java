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
    assertTrue(asString(stdout).contains("GUI-aligned defaults:"));
    assertTrue(asString(stdout).contains("formatting-mode=github"));
    assertTrue(asString(stdout).contains("shape-details=exclude"));
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

  @Test
  void convertsUpstreamDisplayFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("display", "display-format-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("display.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString(),
            "--summary"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("[workbook] display-format-sample01.xlsx"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: display-format-sample01.xlsx"));
    assertTrue(markdown.contains("1,024,768"));
    assertTrue(markdown.contains("令和8年3月17日"));
  }

  @Test
  void convertsUpstreamNamedRangeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("named-range", "named-range-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("named-range.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString()
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertEquals("", asString(stdout));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: named-range-sample01.xlsx"));
    assertTrue(markdown.contains("## Sheet: Summary"));
    assertTrue(markdown.contains("| BaseName元 | Base |"));
  }

  @Test
  void convertsUpstreamNarrativeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("narrative", "narrative-vs-table-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("narrative.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString()
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertEquals("", asString(stdout));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: narrative-vs-table-sample01.xlsx"));
    assertTrue(markdown.contains("地の文と表の判定"));
    assertTrue(markdown.contains("### Table: 001 (B8-F11)"));
  }

  @Test
  void convertsUpstreamBasicFixtureInBothModeWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("", "xlsx2md-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("xlsx2md-basic-both.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString(),
            "--output-mode", "both",
            "--summary"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("[workbook] xlsx2md-basic-sample01.xlsx"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: xlsx2md-basic-sample01.xlsx"));
    assertTrue(markdown.contains("### Table: 004 (B33-F46)"));
    assertTrue(markdown.contains("1 0 2 3 4 5 6 [raw=1023456]"));
    assertTrue(markdown.contains("令和8年3月17日 [raw=46098]"));
  }

  @Test
  void convertsUpstreamImageFixtureSample02WhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("image", "image-basic-sample02.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("image-basic-sample02.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString(),
            "--summary"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("[workbook] image-basic-sample02.xlsx"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: image-basic-sample02.xlsx"));
    assertTrue(markdown.contains("| 2024年 | 13,568 | 9,072 |"));
    assertTrue(markdown.contains("### Chart: 001 (B9)"));
    assertTrue(markdown.contains("### Image: 001 (H3)"));
  }

  @Test
  void convertsUpstreamFlowchartShapeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-flowchart-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("shape-flowchart.md");

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
    assertTrue(asString(stdout).contains("[workbook] shape-flowchart-sample01.xlsx"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: shape-flowchart-sample01.xlsx"));
    assertTrue(markdown.contains("### Shape Block: 001 (K3-AB7)"));
    assertTrue(markdown.contains("- `a:prstGeom@prst`: `flowChartTerminator`"));
    assertTrue(markdown.contains("![shape_005.svg](assets/shape-flowchart/shape_005.svg)"));
  }

  @Test
  void convertsUpstreamBlockArrowShapeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-block-arrow-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("shape-block-arrow.md");

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
    assertTrue(asString(stdout).contains("[workbook] shape-block-arrow-sample01.xlsx"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: shape-block-arrow-sample01.xlsx"));
    assertTrue(markdown.contains("### Shape Block: 001 (K3-AA14)"));
    assertTrue(markdown.contains("- `a:prstGeom@prst`: `rightArrow`"));
    assertTrue(markdown.contains("- `a:prstGeom@prst`: `quadArrow`"));
  }

  @Test
  void convertsUpstreamCalloutShapeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-callout-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("shape-callout.md");

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
    assertTrue(asString(stdout).contains("[workbook] shape-callout-sample01.xlsx"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: shape-callout-sample01.xlsx"));
    assertTrue(markdown.contains("## Sheet: shape-callout"));
    assertTrue(markdown.contains("- `a:prstGeom@prst`: `wedgeRoundRectCallout`"));
    assertTrue(markdown.contains("- `a:t#text`: `角四角`"));
  }

  @Test
  void convertsUpstreamWeirdSheetNameFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("edge", "edge-weird-sheetname-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    final Path outputPath = tempDir.resolve("edge-weird-sheetname.md");

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            fixturePath.toString(),
            "--out", outputPath.toString(),
            "--summary"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("[workbook] edge-weird-sheetname-sample01.xlsx"));
    assertEquals("", asString(stderr));
    assertTrue(markdown.contains("# Book: edge-weird-sheetname-sample01.xlsx"));
    assertTrue(markdown.contains("## Sheet: A B-東京&大阪.01"));
    assertTrue(markdown.contains("| 3 | 登録日 | 3月13日 | 何かの登録日 |"));
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
