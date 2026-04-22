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
