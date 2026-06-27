/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdownexport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.textencoding.TextEncoding;
import jp.igapyon.mikuxlsx2md.zipio.ZipIo;

class MarkdownExportTest {
  @Test
  void normalizesLineBreaksIntoSpaces() {
    assertEquals("a b c d", MarkdownExport.normalizeMarkdownLineBreaks("a\r\nb\nc\rd"));
  }

  @Test
  void rendersMarkdownTablesWithEscapedCellContent() {
    final String markdown = MarkdownExport.renderMarkdownTable(Arrays.asList(
        Arrays.asList("Name", "Notes"),
        Arrays.asList("A|B", "line1\nline2")),
        true);

    assertEquals("| Name | Notes |\n| --- | --- |\n| A\\|B | line1 line2 |", markdown);
  }

  @Test
  void escapesMarkdownCellsAndKeepsSpacingRules() {
    assertEquals("A\\| B", MarkdownExport.escapeMarkdownCell("A|\nB"));
    assertEquals("\\| a", MarkdownExport.escapeMarkdownCell("| a"));
    assertEquals("1. item \\| > quote", MarkdownExport.escapeMarkdownCell("1. item | > quote"));
    assertEquals("&lt;a&gt; &amp; b \\| c", MarkdownExport.escapeMarkdownCell("&lt;a&gt; &amp; b | c"));
    assertEquals("`code` ![alt](img.png) \\| c", MarkdownExport.escapeMarkdownCell("`code` ![alt](img.png) | c"));
    assertEquals("a   b \\| c", MarkdownExport.escapeMarkdownCell("a   b | c"));
    assertEquals("  a \\| b  ", MarkdownExport.escapeMarkdownCell("  a | b  "));
    assertEquals("a b \\| c", MarkdownExport.escapeMarkdownCell("a\tb | c"));
  }

  @Test
  void createsSanitizedOutputFileNamesWithoutModeSuffixes() {
    assertEquals("book name", MarkdownExport.stripWorkbookExtension("book name.xlsx"));
    assertEquals("book name", MarkdownExport.stripWorkbookExtension("book name"));
    assertEquals("book name.md", MarkdownExport.createCombinedMarkdownFileName("book name.xlsx"));
    assertEquals("workbook.md", MarkdownExport.createCombinedMarkdownFileName(""));
    assertEquals("output/assets/pic.png", MarkdownExport.createExportEntryName("assets/pic.png"));
    assertEquals(
        "book_name_002_A_B_東京.md",
        MarkdownExport.createOutputFileName("book name.xlsx", 2, "A/B:東京", "both"));
    assertEquals(
        "book_name_002_A_B_東京.md",
        MarkdownExport.createOutputFileName("book name.xlsx", 2, "A/B:東京", "display", "github"));
  }

  @Test
  void summarizesFormulaDiagnosticsAndTableScores() {
    final String summary = MarkdownExport.createSummaryText(new MarkdownExport.MarkdownFile(
        "sample.md",
        "Sheet1",
        "# Sheet1",
        new MarkdownExport.MarkdownSummary(
            "display",
            "plain",
            "balanced",
            2,
            1,
            1,
            0,
            0,
            0,
            8,
            Arrays.asList(new MarkdownExport.TableScoreDetail("A1-B2", 7, Arrays.asList("Has borders"))),
            Arrays.asList(
                new MarkdownExport.FormulaDiagnostic("B2", "=A2", "resolved", "cached_value", "1"),
                new MarkdownExport.FormulaDiagnostic("B3", "=X1", "unsupported_external", "external_unsupported", "")))));

    assertTrue(summary.contains("Output file: sample.md"));
    assertTrue(summary.contains("Formatting mode: plain"));
    assertTrue(summary.contains("Table detection mode: balanced"));
    assertTrue(summary.contains("Formula resolved: 1"));
    assertTrue(summary.contains("Formula unsupported_external: 1"));
    assertTrue(summary.contains("Table candidate A1-B2: score 7 / Has borders"));
  }

  @Test
  void createsExportEntriesAndZipArchivesIncludingMarkdownAndAssets() {
    final MarkdownExport.ExportWorkbook workbook = new MarkdownExport.ExportWorkbook(
        "sample.xlsx",
        Arrays.asList(new MarkdownExport.ExportSheet(
            Arrays.asList(new MarkdownExport.ExportImage("images/pic.png", new byte[] {1, 2, 3})),
            Arrays.asList(new MarkdownExport.ExportShape("shapes/shape_001.svg", new byte[] {4, 5})))));
    final List<MarkdownExport.MarkdownFile> markdownFiles = Arrays.asList(new MarkdownExport.MarkdownFile(
        "sample_001_Sheet1.md",
        "Sheet1",
        "# Sheet1",
        new MarkdownExport.MarkdownSummary("display", "plain", "balanced", 1, 0, 1, 0, 1, 0, 1,
            Collections.<MarkdownExport.TableScoreDetail>emptyList(),
            Collections.<MarkdownExport.FormulaDiagnostic>emptyList())));

    final List<ZipIo.ExportEntry> entries = MarkdownExport.createExportEntries(workbook, markdownFiles, new TextEncoding.MarkdownEncodingOptions());
    final byte[] archive = MarkdownExport.createWorkbookExportArchive(workbook, markdownFiles, new TextEncoding.MarkdownEncodingOptions());
    final Map<String, byte[]> extracted = ZipIo.unzipEntries(archive);

    assertEquals(Arrays.asList(
        "output/sample.md",
        "output/images/pic.png",
        "output/shapes/shape_001.svg"),
        Arrays.asList(entries.get(0).getName(), entries.get(1).getName(), entries.get(2).getName()));
    assertTrue(new String(extracted.get("output/sample.md"), StandardCharsets.UTF_8).contains("# Sheet1"));
    assertArrayEquals(new byte[] {1, 2, 3}, extracted.get("output/images/pic.png"));
    assertArrayEquals(new byte[] {4, 5}, extracted.get("output/shapes/shape_001.svg"));
  }

  @Test
  void createsAssetEntriesWithoutMarkdownAndSkipsIncompleteShapeSvgAssets() {
    final MarkdownExport.ExportWorkbook workbook = new MarkdownExport.ExportWorkbook(
        "sample.xlsx",
        Arrays.asList(new MarkdownExport.ExportSheet(
            Arrays.asList(new MarkdownExport.ExportImage("images/pic.png", new byte[] {1, 2, 3})),
            Arrays.asList(
                new MarkdownExport.ExportShape("shapes/shape_001.svg", new byte[] {4, 5}),
                new MarkdownExport.ExportShape(null, new byte[] {6}),
                new MarkdownExport.ExportShape("shapes/shape_003.svg", null)))));

    assertNull(MarkdownExport.createMarkdownExportEntry(workbook, Collections.<MarkdownExport.MarkdownFile>emptyList(),
        new TextEncoding.MarkdownEncodingOptions()));
    final List<ZipIo.ExportEntry> assetEntries = MarkdownExport.createAssetExportEntries(workbook);
    assertEquals(2, assetEntries.size());
    assertEquals("output/images/pic.png", assetEntries.get(0).getName());
    assertEquals("output/shapes/shape_001.svg", assetEntries.get(1).getName());
    assertEquals(Arrays.asList("output/images/pic.png", "output/shapes/shape_001.svg"),
        Arrays.asList(
            MarkdownExport.createExportEntries(workbook, Collections.<MarkdownExport.MarkdownFile>emptyList(),
                new TextEncoding.MarkdownEncodingOptions()).get(0).getName(),
            MarkdownExport.createExportEntries(workbook, Collections.<MarkdownExport.MarkdownFile>emptyList(),
                new TextEncoding.MarkdownEncodingOptions()).get(1).getName()));
  }

  @Test
  void createsEncodedPayloadBytesForUtf16BeWithBom() {
    final MarkdownExport.CombinedMarkdownExportPayload payload = MarkdownExport.createCombinedMarkdownExportPayload(
        new MarkdownExport.ExportWorkbook("sample.xlsx", Arrays.asList(new MarkdownExport.ExportSheet(
            Collections.<MarkdownExport.ExportImage>emptyList(),
            Collections.<MarkdownExport.ExportShape>emptyList()))),
        Arrays.asList(new MarkdownExport.MarkdownFile(
            "sample_001_Sheet1.md",
            "Sheet1",
            "# A",
            new MarkdownExport.MarkdownSummary("display", "plain", "balanced", 1, 0, 1, 0, 0, 0, 1,
                Collections.<MarkdownExport.TableScoreDetail>emptyList(),
                Collections.<MarkdownExport.FormulaDiagnostic>emptyList()))),
        new TextEncoding.MarkdownEncodingOptions("utf-16be", "on"));

    assertEquals("text/markdown;charset=utf-16be", payload.getMimeType());
    assertArrayEquals(new byte[] {(byte) 0xfe, (byte) 0xff, 0x00, 0x2d},
        Arrays.copyOfRange(payload.getData(), 0, 4));
  }

  @Test
  void writesTheBookHeadingOnlyOnceInCombinedMarkdown() {
    final MarkdownExport.CombinedMarkdownExportFile payload = MarkdownExport.createCombinedMarkdownExportFile(
        new MarkdownExport.ExportWorkbook("sales.xlsx", Arrays.asList(
            new MarkdownExport.ExportSheet(Collections.<MarkdownExport.ExportImage>emptyList(), Collections.<MarkdownExport.ExportShape>emptyList()),
            new MarkdownExport.ExportSheet(Collections.<MarkdownExport.ExportImage>emptyList(), Collections.<MarkdownExport.ExportShape>emptyList()))),
        Arrays.asList(
            new MarkdownExport.MarkdownFile(
                "sales_001_Summary.md",
                "Summary",
                "# Book: sales.xlsx\n\n## Sheet: Summary\n\nSummary body",
                new MarkdownExport.MarkdownSummary("display", "plain", "balanced", 1, 0, 1, 0, 0, 0, 1,
                    Collections.<MarkdownExport.TableScoreDetail>emptyList(),
                    Collections.<MarkdownExport.FormulaDiagnostic>emptyList())),
            new MarkdownExport.MarkdownFile(
                "sales_002_Detail.md",
                "Detail",
                "# Book: sales.xlsx\n\n## Sheet: Detail\n\nDetail body",
                new MarkdownExport.MarkdownSummary("display", "plain", "balanced", 1, 0, 1, 0, 0, 0, 1,
                    Collections.<MarkdownExport.TableScoreDetail>emptyList(),
                    Collections.<MarkdownExport.FormulaDiagnostic>emptyList()))));

    assertEquals("sales.md", payload.getFileName());
    assertTrue(payload.getContent().startsWith("---\ntitle: \"sales.xlsx\""));
    assertEquals(1, payload.getContent().split("# Book: sales\\.xlsx", -1).length - 1);
    assertTrue(payload.getContent().contains("## Sheet: Summary"));
    assertTrue(payload.getContent().contains("## Sheet: Detail"));
  }

  @Test
  void prependsWorkbookLevelFrontMatterToCombinedMarkdown() {
    final MarkdownExport.CombinedMarkdownExportFile payload = MarkdownExport.createCombinedMarkdownExportFile(
        new MarkdownExport.ExportWorkbook("sales.xlsx", Arrays.asList(
            new MarkdownExport.ExportSheet(Collections.<MarkdownExport.ExportImage>emptyList(), Collections.<MarkdownExport.ExportShape>emptyList()))),
        Arrays.asList(new MarkdownExport.MarkdownFile(
            "sales_001_Summary.md",
            "Summary",
            "# Book: sales.xlsx\n\n## Sheet: Summary\n\nSummary body",
            new MarkdownExport.MarkdownSummary("both", "github", "border", 1, 0, 1, 0, 0, 0, 1,
                Collections.<MarkdownExport.TableScoreDetail>emptyList(),
                Collections.<MarkdownExport.FormulaDiagnostic>emptyList()))),
        new MarkdownExport.MarkdownExportOptions(null, null, "./fixtures/sales.xlsx", "1.2.3", "include", "2026-01-02"));

    assertTrue(payload.getContent().startsWith("---\ntitle: \"sales.xlsx\""));
    assertTrue(payload.getContent().contains("type: converted"));
    assertTrue(payload.getContent().contains("category: converted"));
    assertTrue(payload.getContent().contains("  - workbook-conversion"));
    assertTrue(payload.getContent().contains("status: generated"));
    assertTrue(payload.getContent().contains("created: \"2026-01-02\""));
    assertTrue(payload.getContent().contains("updated: \"2026-01-02\""));
    assertTrue(payload.getContent().contains("    path: \"./fixtures/sales.xlsx\""));
    assertTrue(payload.getContent().contains("  version: \"1.2.3\""));
    assertTrue(payload.getContent().contains("  output_mode: both"));
    assertTrue(payload.getContent().contains("  formatting_mode: github"));
    assertTrue(payload.getContent().contains("  table_detection_mode: border"));
    assertTrue(payload.getContent().contains("  shape_details: include"));
    assertTrue(payload.getContent().contains("---\n\n# Book: sales.xlsx"));
  }

  @Test
  void createsMarkdownExportEntryWhenMarkdownExists() {
    final MarkdownExport.ExportWorkbook workbook = new MarkdownExport.ExportWorkbook(
        "sample.xlsx",
        Arrays.asList(new MarkdownExport.ExportSheet(
            Collections.<MarkdownExport.ExportImage>emptyList(),
            Collections.<MarkdownExport.ExportShape>emptyList())));
    final ZipIo.ExportEntry entry = MarkdownExport.createMarkdownExportEntry(
        workbook,
        Arrays.asList(new MarkdownExport.MarkdownFile(
            "sample_001_Sheet1.md",
            "Sheet1",
            "# Sheet1",
            new MarkdownExport.MarkdownSummary("display", "plain", "balanced", 1, 0, 1, 0, 0, 0, 1,
                Collections.<MarkdownExport.TableScoreDetail>emptyList(),
                Collections.<MarkdownExport.FormulaDiagnostic>emptyList()))),
        new TextEncoding.MarkdownEncodingOptions());

    assertNotNull(entry);
    assertEquals("output/sample.md", entry.getName());
    assertFalse(new String(entry.getData(), StandardCharsets.UTF_8).isEmpty());
  }
}
