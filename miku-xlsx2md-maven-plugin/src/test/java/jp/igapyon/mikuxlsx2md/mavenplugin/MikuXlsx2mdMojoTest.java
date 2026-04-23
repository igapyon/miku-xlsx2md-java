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
    mojo.setVerbose(true);

    assertDoesNotThrow(() -> mojo.execute());
    assertEquals(inputPath.toFile(), mojo.getInputFile());
    assertEquals(outputPath.toFile(), mojo.getOutputFile());
    assertEquals("both", mojo.getOutputMode());
    assertEquals("github", mojo.getFormattingMode());
    assertEquals("border", mojo.getTableDetectionMode());
    assertEquals("utf-8", mojo.getEncoding());
    assertEquals("off", mojo.getBom());
    assertEquals(true, mojo.isVerbose());
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

  @Test
  void convertsUpstreamShapeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("shape.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: shape-basic-sample01.xlsx"));
    assertTrue(markdown.contains("### Shape Block: 001"));
    assertTrue(markdown.contains("![shape_003.svg](assets/shape-basic/shape_003.svg)"));
  }

  @Test
  void convertsUpstreamDisplayFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("display", "display-format-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("display.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: display-format-sample01.xlsx"));
    assertTrue(markdown.contains("1,024,768"));
    assertTrue(markdown.contains("令和8年3月17日"));
  }

  @Test
  void convertsUpstreamNamedRangeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("named-range", "named-range-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("named-range.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: named-range-sample01.xlsx"));
    assertTrue(markdown.contains("## Sheet: Summary"));
    assertTrue(markdown.contains("| BaseName元 | Base |"));
  }

  @Test
  void convertsUpstreamNarrativeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("narrative", "narrative-vs-table-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("narrative.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: narrative-vs-table-sample01.xlsx"));
    assertTrue(markdown.contains("地の文と表の判定"));
    assertTrue(markdown.contains("### Table: 001 (B8-F11)"));
  }

  @Test
  void convertsUpstreamRichUsecaseFixtureInGithubModeWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("rich", "rich-usecase-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("rich-usecase.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());
    mojo.setFormattingMode("github");

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: rich-usecase-sample01.xlsx"));
    assertTrue(markdown.contains("[Apple](https://www.apple.com/)"));
    assertTrue(markdown.contains("***Apple***"));
    assertTrue(markdown.contains("実店舗とともに<br>**ネットショップ**でもお世話になっています。"));
  }

  @Test
  void convertsUpstreamMergeMultilineFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("merge", "merge-multiline-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("merge-multiline.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: merge-multiline-sample01.xlsx"));
    assertTrue(markdown.contains("### Table: 001 (A1-C4)"));
    assertTrue(markdown.contains("| 1 | 1行目 2行目 | [←M←] |"));
    assertTrue(markdown.contains("※結合セル内の改行確認用"));
  }

  @Test
  void convertsUpstreamChartBasicFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("chart", "chart-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("chart-basic.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: chart-basic-sample01.xlsx"));
    assertTrue(markdown.contains("### Chart: 001 (B10)"));
    assertTrue(markdown.contains("- Title: 棒グラフのグラフ"));
    assertTrue(markdown.contains("    - values: 'chart-basic'!$D$4:$D$7"));
  }

  @Test
  void convertsUpstreamRichMarkdownEscapeFixtureInGithubModeWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("rich", "rich-markdown-escape-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("rich-markdown-escape.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());
    mojo.setFormattingMode("github");

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: rich-markdown-escape-sample01.xlsx"));
    assertTrue(markdown.contains("line1 \\* x<br>**line2 \\[y\\]\\(z\\)**"));
    assertTrue(markdown.contains("| Header \\| One | Header \\*Two\\* | Header \\[Three\\]\\(x\\) |"));
  }

  @Test
  void convertsUpstreamFormulaBasicFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("formula-basic.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: formula-basic-sample01.xlsx"));
    assertTrue(markdown.contains("| if | OK |"));
    assertTrue(markdown.contains("| date | 2024/3/17 |"));
  }

  @Test
  void convertsUpstreamFormulaSpillFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-spill-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("formula-spill.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: formula-spill-sample01.xlsx"));
    assertTrue(markdown.contains("spill サンプル"));
    assertTrue(markdown.contains("1 1 6"));
  }

  @Test
  void convertsUpstreamChartMixedFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("chart", "chart-mixed-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("chart-mixed.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: chart-mixed-sample01.xlsx"));
    assertTrue(markdown.contains("- Type: Bar Chart + Line Chart (Combined)"));
    assertTrue(markdown.contains("  - 利益率"));
    assertTrue(markdown.contains("    - Axis: secondary"));
  }

  @Test
  void convertsUpstreamFormulaCrossSheetFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-crosssheet-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("formula-crosssheet.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: formula-crosssheet-sample01.xlsx"));
    assertTrue(markdown.contains("| sheet2\\_ref | CrossValue |"));
    assertTrue(markdown.contains("| jp\\_sheet\\_ref | 日本語参照値 |"));
    assertTrue(markdown.contains("| sum\\_range | 10 |"));
  }

  @Test
  void convertsUpstreamFormulaSharedFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-shared-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("formula-shared.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: formula-shared-sample01.xlsx"));
    assertTrue(markdown.contains("| No | 連番 |"));
    assertTrue(markdown.contains("| 1 | 1 |"));
    assertTrue(markdown.contains("| 10 | 10 |"));
  }

  @Test
  void convertsUpstreamImageFixtureSample01WhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("image", "image-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("image-basic-sample01.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: image-basic-sample01.xlsx"));
    assertTrue(markdown.contains("画像抽出サンプル"));
    assertTrue(markdown.contains("### Image: 001 (C8)"));
    assertTrue(markdown.contains("![image_002.png](assets/image/image_002.png)"));
  }

  @Test
  void convertsUpstreamEdgeEmptyFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("edge", "edge-empty-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("edge-empty.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: edge-empty-sample01.xlsx"));
    assertTrue(markdown.contains("空系境界サンプル"));
    assertTrue(markdown.contains("only-value"));
    assertTrue(!markdown.contains("### Table:"));
  }

  @Test
  void convertsUpstreamBorderPriorityFixtureInBorderModeWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("table", "table-border-priority-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("border-priority.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());
    mojo.setTableDetectionMode("border-priority");

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: table-border-priority-sample01.xlsx"));
    assertTrue(markdown.contains("※罫線優先モード確認用"));
    assertTrue(!markdown.contains("### Table: 001"));
  }

  @Test
  void convertsUpstreamBasicFixtureInBothModeWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("", "xlsx2md-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("xlsx2md-basic-both.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());
    mojo.setOutputMode("both");

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: xlsx2md-basic-sample01.xlsx"));
    assertTrue(markdown.contains("### Table: 004 (B33-F46)"));
    assertTrue(markdown.contains("1 0 2 3 4 5 6 [raw=1023456]"));
    assertTrue(markdown.contains("令和8年3月17日 [raw=46098]"));
  }

  @Test
  void convertsUpstreamImageFixtureSample02WhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("image", "image-basic-sample02.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("image-basic-sample02.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: image-basic-sample02.xlsx"));
    assertTrue(markdown.contains("| 2024年 | 13,568 | 9,072 |"));
    assertTrue(markdown.contains("### Chart: 001 (B9)"));
    assertTrue(markdown.contains("### Image: 001 (H3)"));
  }

  @Test
  void convertsUpstreamFlowchartShapeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-flowchart-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("shape-flowchart.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: shape-flowchart-sample01.xlsx"));
    assertTrue(markdown.contains("### Shape Block: 001 (K3-AB7)"));
    assertTrue(markdown.contains("- `a:prstGeom@prst`: `flowChartTerminator`"));
    assertTrue(markdown.contains("![shape_005.svg](assets/shape-flowchart/shape_005.svg)"));
  }

  @Test
  void convertsUpstreamBlockArrowShapeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-block-arrow-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("shape-block-arrow.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: shape-block-arrow-sample01.xlsx"));
    assertTrue(markdown.contains("### Shape Block: 001 (K3-AA14)"));
    assertTrue(markdown.contains("- `a:prstGeom@prst`: `rightArrow`"));
    assertTrue(markdown.contains("- `a:prstGeom@prst`: `quadArrow`"));
  }

  @Test
  void convertsUpstreamCalloutShapeFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-callout-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("shape-callout.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: shape-callout-sample01.xlsx"));
    assertTrue(markdown.contains("## Sheet: shape-callout"));
    assertTrue(markdown.contains("- `a:prstGeom@prst`: `wedgeRoundRectCallout`"));
    assertTrue(markdown.contains("- `a:t#text`: `角四角`"));
  }

  @Test
  void convertsUpstreamWeirdSheetNameFixtureWhenAvailable() throws java.io.IOException {
    final Path fixturePath = resolveFixturePath("edge", "edge-weird-sheetname-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");
    final Path outputPath = tempDir.resolve("out").resolve("edge-weird-sheetname.md");
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(fixturePath.toFile());
    mojo.setOutputFile(outputPath.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final String markdown = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
    assertTrue(markdown.contains("# Book: edge-weird-sheetname-sample01.xlsx"));
    assertTrue(markdown.contains("## Sheet: A B-東京&大阪.01"));
    assertTrue(markdown.contains("| 3 | 登録日 | 3月13日 | 何かの登録日 |"));
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
    final Path local = group.isEmpty()
        ? Paths.get("workplace", "miku-xlsx2md", "tests", "fixtures", fileName)
        : Paths.get("workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
    if (Files.isRegularFile(local)) {
      return local;
    }
    return group.isEmpty()
        ? Paths.get("..", "workplace", "miku-xlsx2md", "tests", "fixtures", fileName)
        : Paths.get("..", "workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
  }
}
