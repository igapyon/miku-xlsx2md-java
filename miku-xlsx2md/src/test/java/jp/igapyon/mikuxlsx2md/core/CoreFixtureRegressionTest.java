/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

class CoreFixtureRegressionTest {
  @Test
  void parsesUpstreamNamedRangeFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("named-range", "named-range-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "named-range-sample01.xlsx");

    assertEquals("named-range-sample01.xlsx", workbook.getName());
    assertEquals(Arrays.asList("Summary", "Other"), Arrays.asList(
        workbook.getSheets().get(0).getName(),
        workbook.getSheets().get(1).getName()));
    assertEquals(Arrays.asList(
        new WorkbookLoader.DefinedName("BaseName", "=Summary!$B$3", null),
        new WorkbookLoader.DefinedName("BaseRange", "=Summary!$B$4:$B$5", null),
        new WorkbookLoader.DefinedName("LocalCross", "=Other!$B$2", "Other")),
        workbook.getDefinedNames());
    assertTrue(workbook.getSharedStrings().stream().anyMatch((entry) -> entry.getText().contains("definedNames サンプル")));
    assertEquals("=BaseName", findCell(workbook.getSheets().get(0).getCells(), "D3").getFormulaText());
    assertEquals("Base", findCell(workbook.getSheets().get(0).getCells(), "D3").getOutputValue());
    assertEquals("=SUM(BaseRange)", findCell(workbook.getSheets().get(0).getCells(), "D4").getFormulaText());
    assertEquals("30", findCell(workbook.getSheets().get(0).getCells(), "D4").getOutputValue());
    assertEquals("=LocalCross", findCell(workbook.getSheets().get(1).getCells(), "D2").getFormulaText());
    assertEquals("CrossRef", findCell(workbook.getSheets().get(1).getCells(), "D2").getOutputValue());
  }

  @Test
  void parsesUpstreamHyperlinkFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("link", "hyperlink-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "hyperlink-basic-sample01.xlsx");
    final List<WorksheetParser.ParsedSheet> sheets = workbook.getSheets();
    final WorksheetParser.ParsedSheet summary = sheets.get(0);

    assertEquals(Arrays.asList("Summary", "Other"), Arrays.asList(sheets.get(0).getName(), sheets.get(1).getName()));
    assertEquals(8, workbook.getSharedStrings().size());
    assertEquals("Open example", findCell(summary.getCells(), "A1").getOutputValue());
    assertEquals("Jump to Other", findCell(summary.getCells(), "A2").getOutputValue());
    assertEquals("https://example.com/", findCell(summary.getCells(), "A1").getHyperlink().getTarget());
    assertEquals("external", findCell(summary.getCells(), "A1").getHyperlink().getKind());
    assertEquals("Other!A1", findCell(summary.getCells(), "A2").getHyperlink().getTarget());
    assertEquals("internal", findCell(summary.getCells(), "A2").getHyperlink().getKind());
    assertEquals("https://example.com/docs", findCell(summary.getCells(), "B5").getHyperlink().getTarget());
    assertEquals("Jump to Other", findCell(summary.getCells(), "B6").getHyperlink().getDisplay());
  }

  @Test
  void parsesUpstreamDisplayFormatFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("display", "display-format-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "display-format-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);

    assertEquals("display-format", sheet.getName());
    assertEquals(13, sheet.getMaxRow());
    assertEquals(5, sheet.getMaxCol());
    assertEquals("1,024,768", findCell(sheet.getCells(), "D3").getOutputValue());
    assertEquals("¥1,024,768", findCell(sheet.getCells(), "D4").getOutputValue());
    assertEquals("¥ 1,024,768", findCell(sheet.getCells(), "D5").getOutputValue());
    assertEquals("1996/2/28", findCell(sheet.getCells(), "D6").getOutputValue());
    assertEquals("12:34:56", findCell(sheet.getCells(), "D7").getOutputValue());
    assertEquals("98.7%", findCell(sheet.getCells(), "D8").getOutputValue());
    assertEquals("3/4", findCell(sheet.getCells(), "D9").getOutputValue());
    assertEquals("1.023456E+06", findCell(sheet.getCells(), "D10").getOutputValue());
    assertEquals("1023456", findCell(sheet.getCells(), "D11").getOutputValue());
    assertEquals("1023456", findCell(sheet.getCells(), "D12").getOutputValue());
    assertEquals("令和8年3月17日", findCell(sheet.getCells(), "D13").getOutputValue());
  }

  @Test
  void convertsUpstreamNarrativeFixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("narrative", "narrative-vs-table-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "narrative-vs-table-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("narrative-vs-table", sheet.getName());
    assertEquals(13, sheet.getMaxRow());
    assertEquals(6, sheet.getMaxCol());
    assertEquals("地の文と表の判定", findCell(sheet.getCells(), "A1").getOutputValue());
    assertEquals("この設計書は受注入力画面を説明する。", findCell(sheet.getCells(), "A3").getOutputValue());
    assertEquals("=B9+1", findCell(sheet.getCells(), "B10").getFormulaText());
    assertEquals("2", findCell(sheet.getCells(), "B10").getOutputValue());
    assertEquals("=B10+1", findCell(sheet.getCells(), "B11").getFormulaText());
    assertEquals("3", findCell(sheet.getCells(), "B11").getOutputValue());
    assertEquals("3月13日", findCell(sheet.getCells(), "E11").getOutputValue());
    assertEquals(1, files.size());
    assertEquals("narrative-vs-table-sample01_001_narrative-vs-table.md", files.get(0).getFileName());
    assertEquals(1, files.get(0).getSummary().getTables());
    assertEquals(2, files.get(0).getSummary().getFormulaDiagnostics().size());
    assertEquals(Arrays.asList("B8-F11"),
        Arrays.asList(files.get(0).getSummary().getTableScores().get(0).getRange()));
    assertTrue(files.get(0).getMarkdown().contains("## Sheet: narrative-vs-table"));
    assertTrue(files.get(0).getMarkdown().contains("地の文と表の判定"));
    assertTrue(files.get(0).getMarkdown().contains("この設計書は受注入力画面を説明する。"));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 001 (B8-F11)"));
    assertTrue(files.get(0).getMarkdown().contains("| 1 | コード | code | 101 | 何かのコード |"));
    assertTrue(files.get(0).getMarkdown().contains("※注記: この表はサンプルです。"));
  }

  @Test
  void convertsUpstreamEdgeEmptyFixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("edge", "edge-empty-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "edge-empty-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("edge-empty", sheet.getName());
    assertEquals(7, sheet.getMaxRow());
    assertEquals(3, sheet.getMaxCol());
    assertEquals("空系境界サンプル", findCell(sheet.getCells(), "A1").getOutputValue());
    assertEquals("only-value", findCell(sheet.getCells(), "C7").getOutputValue());
    assertEquals(1, files.size());
    assertEquals("edge-empty-sample01_001_edge-empty.md", files.get(0).getFileName());
    assertEquals(0, files.get(0).getSummary().getTables());
    assertEquals(0, files.get(0).getSummary().getTableScores().size());
    assertTrue(files.get(0).getMarkdown().contains("## Sheet: edge-empty"));
    assertTrue(files.get(0).getMarkdown().contains("空系境界サンプル"));
    assertTrue(files.get(0).getMarkdown().contains("only-value"));
    assertTrue(!files.get(0).getMarkdown().contains("### Table:"));
  }

  @Test
  void convertsUpstreamDisplayFormatFixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("display", "display-format-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "display-format-sample01.xlsx");
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals(1, files.size());
    assertEquals("display-format-sample01_001_display-format.md", files.get(0).getFileName());
    assertEquals("display-format", files.get(0).getSheetName());
    assertTrue(files.get(0).getMarkdown().contains("# Book: display-format-sample01.xlsx"));
    assertTrue(files.get(0).getMarkdown().contains("## Sheet: display-format"));
    assertTrue(files.get(0).getMarkdown().contains("1,024,768"));
    assertTrue(files.get(0).getMarkdown().contains("令和8年3月17日"));
    assertEquals("display", files.get(0).getSummary().getOutputMode());
    assertEquals("plain", files.get(0).getSummary().getFormattingMode());
    assertEquals(65, files.get(0).getSummary().getCells());
  }

  @Test
  void convertsUpstreamHyperlinkFixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("link", "hyperlink-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "hyperlink-basic-sample01.xlsx");
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals(2, files.size());
    assertEquals("Summary", files.get(0).getSheetName());
    assertEquals("Other", files.get(1).getSheetName());
    assertTrue(files.get(0).getMarkdown().contains("[Open example](https://example.com/)"));
    assertTrue(files.get(0).getMarkdown().contains("[Jump to Other](#other)"));
  }

  @Test
  void convertsUpstreamRichUsecaseFixtureToGithubMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("rich", "rich-usecase-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "rich-usecase-sample01.xlsx");
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook,
        new MarkdownOptions(null, null, null, null, null, null, "github", null));

    assertEquals(1, files.size());
    assertEquals("github", files.get(0).getSummary().getFormattingMode());
    assertEquals("rich+usecase", files.get(0).getSheetName());
    assertTrue(files.get(0).getMarkdown().contains("[Apple](https://www.apple.com/)"));
    assertTrue(files.get(0).getMarkdown().contains("***Apple***"));
    assertTrue(files.get(0).getMarkdown().contains("<ins>購入できます</ins>"));
    assertTrue(files.get(0).getMarkdown().contains("実店舗とともに<br>**ネットショップ**でもお世話になっています。"));
  }

  @Test
  void parsesUpstreamMergePatternFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("merge", "merge-pattern-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "merge-pattern-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("merge", sheet.getName());
    assertEquals(12, sheet.getMerges().size());
    assertEquals("B15:C16", sheet.getMerges().get(0).getRef());
    assertEquals("横結合", findCell(sheet.getCells(), "B2").getOutputValue());
    assertEquals("", findCell(sheet.getCells(), "C2").getOutputValue());
    assertEquals("2x2結合", findCell(sheet.getCells(), "B15").getOutputValue());
    assertEquals("", findCell(sheet.getCells(), "C15").getOutputValue());
    assertEquals(12, files.get(0).getSummary().getMerges());
    assertTrue(files.get(0).getMarkdown().contains("横結合"));
    assertTrue(files.get(0).getMarkdown().contains("2x2結合"));
  }

  @Test
  void parsesUpstreamFormulaBasicFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "formula-basic-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("formula", sheet.getName());
    assertEquals("=B3", findCell(sheet.getCells(), "B5").getFormulaText());
    assertEquals("10", findCell(sheet.getCells(), "B5").getOutputValue());
    assertEquals("resolved", findCell(sheet.getCells(), "B5").getResolutionStatus());
    assertEquals("=IF(B3>B4,\"OK\",\"NG\")", findCell(sheet.getCells(), "B7").getFormulaText());
    assertEquals("OK", findCell(sheet.getCells(), "B7").getOutputValue());
    assertEquals("=SUM(B3:B4)", findCell(sheet.getCells(), "B8").getFormulaText());
    assertEquals("15", findCell(sheet.getCells(), "B8").getOutputValue());
    assertEquals("=DATE(2024,3,17)", findCell(sheet.getCells(), "B11").getFormulaText());
    assertEquals("2024/3/17", findCell(sheet.getCells(), "B11").getOutputValue());
    assertEquals(9, files.get(0).getSummary().getFormulaDiagnostics().size());
    assertTrue(files.get(0).getSummary().getFormulaDiagnostics().stream().allMatch((diagnostic) -> "cached_value".equals(diagnostic.getSource())));
    assertTrue(files.get(0).getMarkdown().contains("| if | OK |"));
    assertTrue(files.get(0).getMarkdown().contains("| date | 2024/3/17 |"));
  }

  @Test
  void parsesUpstreamFormulaCrossSheetFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-crosssheet-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "formula-crosssheet-sample01.xlsx");
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());
    final WorksheetParser.ParsedSheet sheet1 = workbook.getSheets().get(0);
    final WorksheetParser.ParsedSheet sheet2 = workbook.getSheets().get(1);
    final WorksheetParser.ParsedSheet sheet3 = workbook.getSheets().get(2);

    assertEquals(Arrays.asList("Sheet1", "Sheet2", "日本語シート"), Arrays.asList(sheet1.getName(), sheet2.getName(), sheet3.getName()));
    assertEquals("複数シート参照サンプル", findCell(sheet1.getCells(), "A1").getOutputValue());
    assertEquals("=Sheet2!B3", findCell(sheet1.getCells(), "B3").getFormulaText());
    assertEquals("CrossValue", findCell(sheet1.getCells(), "B3").getOutputValue());
    assertEquals("resolved", findCell(sheet1.getCells(), "B3").getResolutionStatus());
    assertEquals("=日本語シート!C4", findCell(sheet1.getCells(), "B4").getFormulaText());
    assertEquals("日本語参照値", findCell(sheet1.getCells(), "B4").getOutputValue());
    assertEquals("resolved", findCell(sheet1.getCells(), "B4").getResolutionStatus());
    assertEquals("=SUM(Sheet2!A1:B2)", findCell(sheet1.getCells(), "B5").getFormulaText());
    assertEquals("10", findCell(sheet1.getCells(), "B5").getOutputValue());
    assertEquals("CrossValue", findCell(sheet2.getCells(), "B3").getOutputValue());
    assertEquals("日本語参照値", findCell(sheet3.getCells(), "C4").getOutputValue());
    assertEquals(3, files.get(0).getSummary().getFormulaDiagnostics().size());
    assertTrue(files.get(0).getSummary().getFormulaDiagnostics().stream().allMatch((diagnostic) -> "cached_value".equals(diagnostic.getSource())));
    assertTrue(files.get(0).getMarkdown().contains("| sheet2\\_ref | CrossValue |"));
    assertTrue(files.get(0).getMarkdown().contains("| jp\\_sheet\\_ref | 日本語参照値 |"));
    assertTrue(files.get(0).getMarkdown().contains("| sum\\_range | 10 |"));
    assertTrue(files.get(1).getMarkdown().contains("|  | CrossValue |"));
    assertTrue(files.get(2).getMarkdown().contains("日本語参照値"));
  }

  @Test
  void parsesUpstreamFormulaSharedFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-shared-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "formula-shared-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("formula", sheet.getName());
    assertEquals("No", findCell(sheet.getCells(), "A1").getOutputValue());
    assertEquals("連番", findCell(sheet.getCells(), "B1").getOutputValue());
    assertEquals("shared formula サンプル", findCell(sheet.getCells(), "D1").getOutputValue());
    assertEquals("=B2+1", findCell(sheet.getCells(), "B3").getFormulaText());
    assertEquals("2", findCell(sheet.getCells(), "B3").getOutputValue());
    assertEquals("=B3+1", findCell(sheet.getCells(), "B4").getFormulaText());
    assertEquals("3", findCell(sheet.getCells(), "B4").getOutputValue());
    assertEquals("=B10+1", findCell(sheet.getCells(), "B11").getFormulaText());
    assertEquals("10", findCell(sheet.getCells(), "B11").getOutputValue());
    assertEquals("resolved", findCell(sheet.getCells(), "B11").getResolutionStatus());
    assertEquals(9, files.get(0).getSummary().getFormulaDiagnostics().size());
    assertTrue(files.get(0).getMarkdown().contains("| No | 連番 |"));
    assertTrue(files.get(0).getMarkdown().contains("| 1 | 1 |"));
    assertTrue(files.get(0).getMarkdown().contains("| 5 | 5 |"));
    assertTrue(files.get(0).getMarkdown().contains("| 10 | 10 |"));
  }

  @Test
  void parsesUpstreamFormulaSpillFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-spill-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "formula-spill-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("spill-sample", sheet.getName());
    assertEquals("1", findCell(sheet.getCells(), "A4").getOutputValue());
    assertEquals("3", findCell(sheet.getCells(), "A6").getOutputValue());
    assertEquals("=_xlfn.SEQUENCE(3)", findCell(sheet.getCells(), "C4").getFormulaText());
    assertEquals("1", findCell(sheet.getCells(), "C4").getOutputValue());
    assertEquals("resolved", findCell(sheet.getCells(), "C4").getResolutionStatus());
    assertEquals("2", findCell(sheet.getCells(), "C5").getOutputValue());
    assertEquals("3", findCell(sheet.getCells(), "C6").getOutputValue());
    assertEquals("=SUM(_xlfn.ANCHORARRAY(C4))", findCell(sheet.getCells(), "E4").getFormulaText());
    assertEquals("6", findCell(sheet.getCells(), "E4").getOutputValue());
    assertEquals(2, files.get(0).getSummary().getFormulaDiagnostics().size());
    assertTrue(files.get(0).getMarkdown().contains("spill サンプル"));
    assertTrue(files.get(0).getMarkdown().contains("1 1 6"));
  }

  @Test
  void parsesUpstreamChartBasicFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("chart", "chart-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "chart-basic-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());
    final WorksheetParser.ParsedChartAsset chart = sheet.getCharts().get(0);

    assertEquals("chart-basic", sheet.getName());
    assertEquals(1, sheet.getCharts().size());
    assertEquals("B10", chart.getAnchor());
    assertEquals("棒グラフのグラフ", chart.getTitle());
    assertEquals("Bar Chart", chart.getChartType());
    assertEquals(2, chart.getSeries().size());
    assertEquals("値A", chart.getSeries().get(0).getName());
    assertEquals("'chart-basic'!$B$4:$B$7", chart.getSeries().get(0).getCategoriesRef());
    assertEquals("'chart-basic'!$C$4:$C$7", chart.getSeries().get(0).getValuesRef());
    assertEquals("primary", chart.getSeries().get(0).getAxis());
    assertEquals(1, files.get(0).getSummary().getCharts());
    assertTrue(files.get(0).getMarkdown().contains("### Chart: 001 (B10)"));
    assertTrue(files.get(0).getMarkdown().contains("- Title: 棒グラフのグラフ"));
    assertTrue(files.get(0).getMarkdown().contains("    - values: 'chart-basic'!$D$4:$D$7"));
  }

  @Test
  void parsesUpstreamChartMixedFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("chart", "chart-mixed-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "chart-mixed-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());
    final WorksheetParser.ParsedChartAsset chart = sheet.getCharts().get(0);

    assertEquals("chart-mixed", sheet.getName());
    assertEquals(1, sheet.getCharts().size());
    assertEquals("B10", chart.getAnchor());
    assertEquals("棒と折れ線", chart.getTitle());
    assertEquals("Bar Chart + Line Chart (Combined)", chart.getChartType());
    assertEquals(3, chart.getSeries().size());
    assertEquals("利益率", chart.getSeries().get(2).getName());
    assertEquals("'chart-mixed'!$B$4:$B$8", chart.getSeries().get(2).getCategoriesRef());
    assertEquals("'chart-mixed'!$E$4:$E$8", chart.getSeries().get(2).getValuesRef());
    assertEquals("secondary", chart.getSeries().get(2).getAxis());
    assertEquals(1, files.get(0).getSummary().getCharts());
    assertTrue(files.get(0).getMarkdown().contains("- Type: Bar Chart + Line Chart (Combined)"));
    assertTrue(files.get(0).getMarkdown().contains("  - 利益率"));
    assertTrue(files.get(0).getMarkdown().contains("    - Axis: secondary"));
  }

  @Test
  void parsesUpstreamImageFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("image", "image-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "image-basic-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("image", sheet.getName());
    assertEquals(13, sheet.getMaxRow());
    assertEquals(6, sheet.getMaxCol());
    assertEquals(2, sheet.getImages().size());
    assertEquals("image_001.png", sheet.getImages().get(0).getFilename());
    assertEquals("assets/image/image_001.png", sheet.getImages().get(0).getPath());
    assertEquals("C8", sheet.getImages().get(0).getAnchor());
    assertEquals("xl/media/image1.png", sheet.getImages().get(0).getMediaPath());
    assertEquals("image_002.png", sheet.getImages().get(1).getFilename());
    assertEquals("assets/image/image_002.png", sheet.getImages().get(1).getPath());
    assertEquals("F8", sheet.getImages().get(1).getAnchor());
    assertEquals("xl/media/image2.png", sheet.getImages().get(1).getMediaPath());
    assertEquals("画像抽出サンプル", findCell(sheet.getCells(), "A1").getOutputValue());
    assertEquals(2, files.get(0).getSummary().getImages());
    assertTrue(files.get(0).getMarkdown().contains("### Image: 001 (C8)"));
    assertTrue(files.get(0).getMarkdown().contains("![image_002.png](assets/image/image_002.png)"));
  }

  @Test
  void convertsUpstreamBorderPriorityFixtureDifferentlyBetweenBalancedAndBorderModesWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-border-priority-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-border-priority-sample01.xlsx");
    final List<MarkdownExport.MarkdownFile> balancedFiles = Core.convertWorkbookToMarkdownFiles(workbook,
        new MarkdownOptions(null, null, null, null, null, null, null, "balanced"));
    final List<MarkdownExport.MarkdownFile> borderFiles = Core.convertWorkbookToMarkdownFiles(workbook,
        new MarkdownOptions(null, null, null, null, null, null, null, "border"));

    assertEquals(1, balancedFiles.size());
    assertEquals(1, borderFiles.size());
    assertEquals(1, balancedFiles.get(0).getSummary().getTables());
    assertEquals("balanced", balancedFiles.get(0).getSummary().getTableDetectionMode());
    assertEquals(0, borderFiles.get(0).getSummary().getTables());
    assertEquals("border", borderFiles.get(0).getSummary().getTableDetectionMode());
    assertTrue(balancedFiles.get(0).getMarkdown().contains("### Table: 001 (A3-B4)"));
    assertTrue(balancedFiles.get(0).getMarkdown().contains("| 項目 | 値 |"));
    assertTrue(borderFiles.get(0).getMarkdown().contains("※罫線優先モード確認用"));
    assertTrue(!borderFiles.get(0).getMarkdown().contains("### Table: 001"));
  }

  @Test
  void convertsUpstreamTableBasicSample01FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(13, sheet.getMaxRow());
    assertEquals(6, sheet.getMaxCol());
    assertEquals("隣接するテーブルのテスト（縦に密接）", findCell(sheet.getCells(), "A1").getOutputValue());
    assertEquals("隣接するテーブルその1", findCell(sheet.getCells(), "B2").getOutputValue());
    assertEquals("隣接するテーブルその2", findCell(sheet.getCells(), "B8").getOutputValue());
    assertEquals("=B4+1", findCell(sheet.getCells(), "B5").getFormulaText());
    assertEquals("=B12+1", findCell(sheet.getCells(), "B13").getFormulaText());
    assertEquals(1, files.size());
    assertEquals(2, files.get(0).getSummary().getTables());
    assertEquals(Arrays.asList("B3-F7", "B9-F13"),
        Arrays.asList(files.get(0).getSummary().getTableScores().get(0).getRange(),
            files.get(0).getSummary().getTableScores().get(1).getRange()));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 001 (B3-F7)"));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 002 (B9-F13)"));
    assertTrue(files.get(0).getMarkdown().contains("| 3 | 登録日 | createdAt | 3月15日 | 登録した日 |"));
  }

  @Test
  void convertsUpstreamTableBasicSample02FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample02.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample02.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(7, sheet.getMaxRow());
    assertEquals(12, sheet.getMaxCol());
    assertEquals("隣接するテーブルのテスト（横に密接）", findCell(sheet.getCells(), "A1").getOutputValue());
    assertEquals("隣接するテーブルその1", findCell(sheet.getCells(), "B2").getOutputValue());
    assertEquals("隣接するテーブルその2", findCell(sheet.getCells(), "H2").getOutputValue());
    assertEquals("=H6+1", findCell(sheet.getCells(), "H7").getFormulaText());
    assertEquals(1, files.size());
    assertEquals(2, files.get(0).getSummary().getTables());
    assertEquals(Arrays.asList("B3-F7", "H3-L7"),
        Arrays.asList(files.get(0).getSummary().getTableScores().get(0).getRange(),
            files.get(0).getSummary().getTableScores().get(1).getRange()));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 001 (B3-F7)"));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 002 (H3-L7)"));
    assertTrue(files.get(0).getMarkdown().contains("| 2 | 別名 | altname | Hanako | 何かの別名 |"));
  }

  @Test
  void convertsUpstreamTableBasicSample03FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample03.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample03.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(13, sheet.getMaxRow());
    assertEquals(12, sheet.getMaxCol());
    assertEquals("隣接するテーブルのテスト（縦横に密接）", findCell(sheet.getCells(), "A1").getOutputValue());
    assertEquals("隣接するテーブルその3", findCell(sheet.getCells(), "B8").getOutputValue());
    assertEquals("隣接するテーブルその4", findCell(sheet.getCells(), "H8").getOutputValue());
    assertEquals("=H12+1", findCell(sheet.getCells(), "H13").getFormulaText());
    assertEquals(1, files.size());
    assertEquals(4, files.get(0).getSummary().getTables());
    assertEquals(Arrays.asList("B3-F7", "H3-L7", "B9-F13", "H9-L13"),
        Arrays.asList(
            files.get(0).getSummary().getTableScores().get(0).getRange(),
            files.get(0).getSummary().getTableScores().get(1).getRange(),
            files.get(0).getSummary().getTableScores().get(2).getRange(),
            files.get(0).getSummary().getTableScores().get(3).getRange()));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 004 (H9-L13)"));
    assertTrue(!files.get(0).getMarkdown().contains("### Table: 005"));
    assertTrue(files.get(0).getMarkdown().contains("| 2 | 別名 | altname | Sawada | 何かの別名 |"));
  }

  @Test
  void convertsUpstreamTableBasicSample11FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample11.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample11.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(7, sheet.getMaxRow());
    assertEquals(20, sheet.getMaxCol());
    assertEquals(20, sheet.getMerges().size());
    assertEquals("方眼紙的様式のテスト", findCell(sheet.getCells(), "A1").getOutputValue());
    assertEquals("テーブルその1", findCell(sheet.getCells(), "B2").getOutputValue());
    assertEquals("=B4+1", findCell(sheet.getCells(), "B5").getFormulaText());
    assertEquals("3月13日", findCell(sheet.getCells(), "L6").getOutputValue());
    assertEquals(1, files.size());
    assertEquals(1, files.get(0).getSummary().getTables());
    assertEquals(20, files.get(0).getSummary().getMerges());
    assertEquals("B3-T7", files.get(0).getSummary().getTableScores().get(0).getRange());
    assertTrue(files.get(0).getMarkdown().contains("### Table: 001 (B3-T7)"));
    assertTrue(files.get(0).getMarkdown().contains("| 4 | 更新日 | updatedate | 3月14日 | 何かの更新日 |"));
  }

  @Test
  void convertsUpstreamTableBasicSample12FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample12.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample12.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(14, sheet.getMaxRow());
    assertEquals(20, sheet.getMaxCol());
    assertEquals(40, sheet.getMerges().size());
    assertEquals("方眼紙風のためにセル結合が多用されます", findCell(sheet.getCells(), "B8").getOutputValue());
    assertEquals("テーブルその2", findCell(sheet.getCells(), "B9").getOutputValue());
    assertEquals("=B13+1", findCell(sheet.getCells(), "B14").getFormulaText());
    assertEquals(1, files.size());
    assertEquals(2, files.get(0).getSummary().getTables());
    assertEquals(Arrays.asList("B3-T7", "B10-T14"),
        Arrays.asList(files.get(0).getSummary().getTableScores().get(0).getRange(),
            files.get(0).getSummary().getTableScores().get(1).getRange()));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 002 (B10-T14)"));
    assertTrue(files.get(0).getMarkdown().contains("方眼紙風のためにセル結合が多用されます"));
  }

  @Test
  void convertsUpstreamTableBasicSample13FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample13.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample13.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(14, sheet.getMaxRow());
    assertEquals(40, sheet.getMaxCol());
    assertEquals(80, sheet.getMerges().size());
    assertEquals("テーブルその3", findCell(sheet.getCells(), "B9").getOutputValue());
    assertEquals("テーブルその4", findCell(sheet.getCells(), "V9").getOutputValue());
    assertEquals("=V13+1", findCell(sheet.getCells(), "V14").getFormulaText());
    assertEquals(1, files.size());
    assertEquals(4, files.get(0).getSummary().getTables());
    assertEquals(Arrays.asList("B3-T7", "V3-AN7", "B10-T14", "V10-AN14"),
        Arrays.asList(
            files.get(0).getSummary().getTableScores().get(0).getRange(),
            files.get(0).getSummary().getTableScores().get(1).getRange(),
            files.get(0).getSummary().getTableScores().get(2).getRange(),
            files.get(0).getSummary().getTableScores().get(3).getRange()));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 004 (V10-AN14)"));
    assertTrue(files.get(0).getMarkdown().contains("| 2 | 名前 | name | Sabro | 何かの名前 |"));
    assertTrue(files.get(0).getMarkdown().contains("| 2 | 名前 | name | Jiro | 何かの名前 |"));
  }

  @Test
  void convertsUpstreamTableBasicSample14FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample14.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample14.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(8, sheet.getMaxRow());
    assertEquals(20, sheet.getMaxCol());
    assertEquals(18, sheet.getMerges().size());
    assertEquals("テーブルその1", findCell(sheet.getCells(), "B2").getOutputValue());
    assertEquals("Taro", findCell(sheet.getCells(), "L5").getOutputValue());
    assertEquals("3月13日", findCell(sheet.getCells(), "L6").getOutputValue());
    assertEquals(1, files.size());
    assertEquals(1, files.get(0).getSummary().getTables());
    assertEquals("B3-T7", files.get(0).getSummary().getTableScores().get(0).getRange());
    assertTrue(files.get(0).getMarkdown().contains("| 2 | 名前 | name | Taro | 何かの名前 |"));
    assertTrue(files.get(0).getMarkdown().contains("たまに結合漏れのセルがある場合"));
  }

  @Test
  void convertsUpstreamTableBasicSample15FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample15.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample15.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(8, sheet.getMaxRow());
    assertEquals(20, sheet.getMaxCol());
    assertEquals(19, sheet.getMerges().size());
    assertEquals("※方眼紙＋結合＋さらに縦結合", findCell(sheet.getCells(), "B8").getOutputValue());
    assertEquals("登録および更新日", findCell(sheet.getCells(), "P6").getOutputValue());
    assertEquals("", findCell(sheet.getCells(), "P7").getOutputValue());
    assertEquals(1, files.size());
    assertEquals(1, files.get(0).getSummary().getTables());
    assertEquals("B3-T7", files.get(0).getSummary().getTableScores().get(0).getRange());
    assertTrue(files.get(0).getMarkdown().contains("| 3 | 登録日 | entrydate | 3月13日 | 登録および更新日 |"));
    assertTrue(files.get(0).getMarkdown().contains("| 4 | 更新日 | updatedate | 3月14日 | [↑M↑] |"));
    assertTrue(files.get(0).getMarkdown().contains("※方眼紙＋結合＋さらに縦結合"));
  }

  @Test
  void convertsUpstreamTableBasicSample16FixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "table-basic-sample16.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "table-basic-sample16.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("table-basic", sheet.getName());
    assertEquals(8, sheet.getMaxRow());
    assertEquals(20, sheet.getMaxCol());
    assertEquals(18, sheet.getMerges().size());
    assertEquals("Taro", findCell(sheet.getCells(), "L5").getOutputValue());
    assertEquals("Ito", findCell(sheet.getCells(), "N5").getOutputValue());
    assertEquals(1, files.size());
    assertEquals(1, files.get(0).getSummary().getTables());
    assertEquals("B3-T7", files.get(0).getSummary().getTableScores().get(0).getRange());
    assertTrue(files.get(0).getMarkdown().contains("| 項番 | 項目名称 | 物理名 | デフォルト値 | [←M←] | 備考 |"));
    assertTrue(files.get(0).getMarkdown().contains("| 2 | 名前 | name | Taro | Ito | 何かの名前 |"));
    assertTrue(files.get(0).getMarkdown().contains("たまに結合漏れのセルがあって、さらに複数文字が登場"));
  }

  @Test
  void convertsUpstreamGridLayoutFixtureWorkbookToMarkdownWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("table", "grid-layout-sample-01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "grid-layout-sample-01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("grid-layout", sheet.getName());
    assertEquals(16, sheet.getMaxRow());
    assertEquals(22, sheet.getMaxCol());
    assertEquals(70, sheet.getMerges().size());
    assertEquals("項番", findCell(sheet.getCells(), "C8").getOutputValue());
    assertEquals("担当コードの値", findCell(sheet.getCells(), "R9").getOutputValue());
    assertEquals("=C14+1", findCell(sheet.getCells(), "C15").getFormulaText());
    assertEquals("システムへの更新日", findCell(sheet.getCells(), "R16").getOutputValue());
    assertEquals(1, files.size());
    assertEquals(2, files.get(0).getSummary().getTables());
    assertEquals(Arrays.asList("B2-U6", "C8-V16"),
        Arrays.asList(files.get(0).getSummary().getTableScores().get(0).getRange(),
            files.get(0).getSummary().getTableScores().get(1).getRange()));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 001 (B2-U6)"));
    assertTrue(files.get(0).getMarkdown().contains("### Table: 002 (C8-V16)"));
    assertTrue(files.get(0).getMarkdown().contains("| 8 | 更新日 | updatedate |  | システムへの更新日 |"));
  }

  @Test
  void parsesUpstreamShapeFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "shape-basic-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("shape-basic", sheet.getName());
    assertEquals(3, sheet.getShapes().size());
    assertEquals("H3", sheet.getShapes().get(0).getAnchor());
    assertEquals("テキスト ボックス 1", sheet.getShapes().get(0).getName());
    assertEquals("Text Box", sheet.getShapes().get(0).getKind());
    assertEquals("テキストボックスの例", sheet.getShapes().get(0).getText());
    assertEquals(Long.valueOf(1980029L), sheet.getShapes().get(0).getWidthEmu());
    assertEquals(Long.valueOf(392608L), sheet.getShapes().get(0).getHeightEmu());
    assertEquals("shape_001.svg", sheet.getShapes().get(0).getSvgFilename());
    assertEquals("assets/shape-basic/shape_001.svg", sheet.getShapes().get(0).getSvgPath());
    assertTrue(new String(sheet.getShapes().get(0).getSvgData(), StandardCharsets.UTF_8).contains("テキストボックスの例"));
    assertEquals("H8", sheet.getShapes().get(1).getAnchor());
    assertEquals("Straight Arrow Connector", sheet.getShapes().get(1).getKind());
    assertEquals("shape_002.svg", sheet.getShapes().get(1).getSvgFilename());
    assertEquals("K3", sheet.getShapes().get(2).getAnchor());
    assertEquals("Rectangle", sheet.getShapes().get(2).getKind());
    assertEquals("shape_003.svg", sheet.getShapes().get(2).getSvgFilename());
    assertEquals(0, files.get(0).getSummary().getImages());
    assertEquals(0, files.get(0).getSummary().getCharts());
    assertTrue(files.get(0).getMarkdown().contains("### Shape Block: 001"));
    assertTrue(files.get(0).getMarkdown().contains("- Shapes: Shape 001, Shape 002, Shape 003"));
    assertTrue(files.get(0).getMarkdown().contains("#### Shape: 001 (H3)"));
    assertTrue(files.get(0).getMarkdown().contains("![shape_003.svg](assets/shape-basic/shape_003.svg)"));
  }

  @Test
  void parsesUpstreamCalloutShapeFixtureWithoutSvgAssetsWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("shape", "shape-callout-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "shape-callout-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
    final List<MarkdownExport.MarkdownFile> files = Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals("shape-callout", sheet.getName());
    assertEquals(4, sheet.getShapes().size());
    assertEquals("Shape (wedgeRoundRectCallout)", sheet.getShapes().get(0).getKind());
    assertEquals("角四角", sheet.getShapes().get(0).getText());
    assertNull(sheet.getShapes().get(0).getSvgPath());
    assertTrue(files.get(0).getMarkdown().contains("- `a:prstGeom@prst`: `wedgeRoundRectCallout`"));
    assertTrue(files.get(0).getMarkdown().contains("- `a:t#text`: `角四角`"));
  }

  private static WorksheetParser.ParsedCell findCell(final List<WorksheetParser.ParsedCell> cells, final String address) {
    for (final WorksheetParser.ParsedCell cell : cells) {
      if (address.equals(cell.getAddress())) {
        assertNotNull(cell);
        return cell;
      }
    }
    throw new AssertionError("Cell was not found: " + address);
  }

  private static Path resolveFixturePath(final String group, final String fileName) {
    final Path local = Paths.get("workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
    if (Files.isRegularFile(local)) {
      return local;
    }
    return Paths.get("..", "workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
  }
}
