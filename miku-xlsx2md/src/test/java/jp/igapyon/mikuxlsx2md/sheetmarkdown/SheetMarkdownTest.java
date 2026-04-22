/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sheetmarkdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.sheetassets.SheetAssets;
import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

class SheetMarkdownTest {
  @Test
  void extractsNarrativeBlocksOutsideTables() {
    final WorksheetParser.ParsedSheet sheet = sheet("Story", Arrays.asList(
        cell("A1", 1, 1, "Heading"),
        cell("B2", 2, 2, "Detail"),
        cell("A5", 5, 1, "Name"),
        cell("B5", 5, 2, "Score"),
        cell("A6", 6, 1, "A"),
        cell("B6", 6, 2, "10")));
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);
    final List<SheetMarkdown.TableCandidate> tables = Arrays.asList(new SheetMarkdown.TableCandidate(
        5, 1, 6, 2, 7, Arrays.asList("Minimum grid", "High density")));

    final List<SheetMarkdown.NarrativeBlock> blocks = SheetMarkdown.extractNarrativeBlocks(workbook, sheet, tables, new MarkdownOptions());

    assertEquals(1, blocks.size());
    assertEquals(Arrays.asList("Heading", "Detail"), blocks.get(0).getLines());
  }

  @Test
  void convertsSheetToMarkdownWithDetectedTableAndSummary() {
    final WorksheetParser.ParsedSheet sheet = sheet("Scores", Arrays.asList(
        cell("A1", 1, 1, "Name"),
        cell("B1", 1, 2, "Score"),
        cell("A2", 2, 1, "A|B"),
        cell("B2", 2, 2, "10")));
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final MarkdownExport.MarkdownFile file = SheetMarkdown.convertSheetToMarkdown(workbook, sheet, new MarkdownOptions());

    assertEquals("book_001_Scores.md", file.getFileName());
    assertTrue(file.getMarkdown().contains("# Book: book.xlsx"));
    assertTrue(file.getMarkdown().contains("### Table: 001 (A1-B2)"));
    assertTrue(file.getMarkdown().contains("| A\\|B | 10 |"));
    assertEquals(1, file.getSummary().getTables());
    assertEquals(4, file.getSummary().getCells());
  }

  @Test
  void formatsHyperlinksRawAndBothModes() {
    final WorksheetParser.ParsedCell cell = cell(
        "A1",
        1,
        1,
        "Displayed",
        "https://example.com",
        new WorksheetParser.Hyperlink("external", "https://example.com", "", "", ""));

    assertEquals("[Displayed](https://example.com)", SheetMarkdown.formatCellForMarkdown(cell, new MarkdownOptions()));
    assertEquals("[https://example.com](https://example.com)", SheetMarkdown.formatCellForMarkdown(cell,
        new MarkdownOptions(null, null, null, null, null, "raw", null, null)));
    assertEquals("[Displayed](https://example.com) [raw=https://example.com]", SheetMarkdown.formatCellForMarkdown(cell,
        new MarkdownOptions(null, null, null, null, null, "both", null, null)));
  }

  @Test
  void convertsWorkbookThroughCoreFacadeShape() {
    final WorksheetParser.ParsedSheet sheet = sheet("Only", Arrays.asList(cell("A1", 1, 1, "Note")));
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final List<MarkdownExport.MarkdownFile> files = jp.igapyon.mikuxlsx2md.core.Core.convertWorkbookToMarkdownFiles(workbook, new MarkdownOptions());

    assertEquals(1, files.size());
    assertTrue(files.get(0).getMarkdown().contains("## Sheet: Only"));
    assertTrue(files.get(0).getMarkdown().contains("Note"));
  }

  @Test
  void convertsSheetWithShapeBlocks() {
    final List<WorksheetParser.ParsedShapeAsset> shapes = Arrays.asList(
        shape("B3", new SheetAssets.BoundingBox(0, 0, 100, 20)),
        shape("E8", new SheetAssets.BoundingBox(3000000, 1600000, 3100000, 1700000)));
    final WorksheetParser.ParsedSheet sheet = new WorksheetParser.ParsedSheet(
        "Shapes",
        1,
        "xl/worksheets/sheet1.xml",
        Arrays.asList(cell("A1", 1, 1, "Note")),
        Collections.<AddressUtils.MergeRange>emptyList(),
        Collections.<WorksheetParser.ParsedImageAsset>emptyList(),
        Collections.<WorksheetParser.ParsedChartAsset>emptyList(),
        shapes,
        10,
        5);
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final MarkdownExport.MarkdownFile file = SheetMarkdown.convertSheetToMarkdown(workbook, sheet, new MarkdownOptions());

    assertTrue(file.getMarkdown().contains("### Shape Block: 001"));
    assertTrue(file.getMarkdown().contains("- Shapes: Shape 001"));
    assertTrue(file.getMarkdown().contains("#### Shape: 001 (B3)"));
    assertTrue(file.getMarkdown().contains("#### Shape: 002 (E8)"));
  }

  @Test
  void omitsShapeSectionsWhenIncludeShapeDetailsIsDisabled() {
    final List<WorksheetParser.ParsedShapeAsset> shapes = Arrays.asList(
        shape("B3", new SheetAssets.BoundingBox(0, 0, 100, 20)));
    final WorksheetParser.ParsedSheet sheet = new WorksheetParser.ParsedSheet(
        "Shapes",
        1,
        "xl/worksheets/sheet1.xml",
        Collections.<WorksheetParser.ParsedCell>emptyList(),
        Collections.<AddressUtils.MergeRange>emptyList(),
        Collections.<WorksheetParser.ParsedImageAsset>emptyList(),
        Collections.<WorksheetParser.ParsedChartAsset>emptyList(),
        shapes,
        10,
        5);
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final MarkdownExport.MarkdownFile enabled = SheetMarkdown.convertSheetToMarkdown(workbook, sheet, new MarkdownOptions());
    final MarkdownExport.MarkdownFile disabled = SheetMarkdown.convertSheetToMarkdown(workbook, sheet,
        new MarkdownOptions(null, null, null, null, Boolean.FALSE, null, null, null));

    assertTrue(enabled.getMarkdown().contains("### Shape Block: 001"));
    assertTrue(enabled.getMarkdown().contains("#### Shape: 001 (B3)"));
    assertFalse(disabled.getMarkdown().contains("### Shape Block:"));
    assertFalse(disabled.getMarkdown().contains("#### Shape:"));
  }

  @Test
  void keepsBlankLineBetweenShapeItemsWhenSvgOutputIsPresent() {
    final List<WorksheetParser.ParsedShapeAsset> shapes = Arrays.asList(
        shapeWithSvg("H3", "shape_001.svg", "assets/Shapes/shape_001.svg"),
        shapeWithSvg("K3", "shape_002.svg", "assets/Shapes/shape_002.svg"));
    final WorksheetParser.ParsedSheet sheet = new WorksheetParser.ParsedSheet(
        "Shapes",
        1,
        "xl/worksheets/sheet1.xml",
        Collections.<WorksheetParser.ParsedCell>emptyList(),
        Collections.<AddressUtils.MergeRange>emptyList(),
        Collections.<WorksheetParser.ParsedImageAsset>emptyList(),
        Collections.<WorksheetParser.ParsedChartAsset>emptyList(),
        shapes,
        10,
        11);
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final MarkdownExport.MarkdownFile file = SheetMarkdown.convertSheetToMarkdown(workbook, sheet, new MarkdownOptions());

    assertTrue(file.getMarkdown().contains("![shape_001.svg](assets/Shapes/shape_001.svg)\n\n#### Shape: 002 (K3)"));
  }

  @Test
  void keepsNearbyCalendarRowsInOneNarrativeBlock() {
    final WorksheetParser.ParsedSheet sheet = sheet("Calendar", Arrays.asList(
        cell("A1", 1, 1, "2021-01-03"),
        cell("B1", 1, 2, "2021-01-04"),
        cell("C1", 1, 3, "2021-01-05"),
        cell("D1", 1, 4, "2021-01-06"),
        cell("E1", 1, 5, "2021-01-07"),
        cell("F1", 1, 6, "2021-01-08"),
        cell("G1", 1, 7, "2021-01-09"),
        cell("J2", 2, 10, "仕事"),
        cell("K2", 2, 11, "私用")));
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final List<SheetMarkdown.NarrativeBlock> blocks = SheetMarkdown.extractNarrativeBlocks(
        workbook,
        sheet,
        Collections.<SheetMarkdown.TableCandidate>emptyList(),
        new MarkdownOptions());

    assertEquals(1, blocks.size());
    assertEquals(Arrays.asList(
        "2021-01-03 2021-01-04 2021-01-05 2021-01-06 2021-01-07 2021-01-08 2021-01-09",
        "仕事 私用"), blocks.get(0).getLines());
  }

  @Test
  void reordersCalendarLikeSectionsWithSidebar() {
    final WorksheetParser.ParsedSheet sheet = sheet("Calendar", Arrays.asList(
        cell("C2", 2, 3, "2021年1月"),
        cell("C11", 11, 3, "2021-01-03"),
        cell("D11", 11, 4, "2021-01-04"),
        cell("E11", 11, 5, "2021-01-05"),
        cell("F11", 11, 6, "2021-01-06"),
        cell("G11", 11, 7, "2021-01-07"),
        cell("H11", 11, 8, "2021-01-08"),
        cell("I11", 11, 9, "2021-01-09"),
        cell("Y24", 24, 25, "2020-12-01"),
        cell("Z24", 24, 26, "2020-12-02"),
        cell("AA24", 24, 27, "2020-12-03"),
        cell("AB24", 24, 28, "2020-12-04"),
        cell("AC24", 24, 29, "2020-12-05")));
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final MarkdownExport.MarkdownFile file = SheetMarkdown.convertSheetToMarkdown(workbook, sheet, new MarkdownOptions());
    final String markdown = file.getMarkdown();

    final int headerIndex = markdown.indexOf("2021年1月");
    final int mainIndex = markdown.indexOf("2021-01-03");
    final int sidebarHeadingIndex = markdown.indexOf("### Sidebar");
    final int sidebarIndex = markdown.indexOf("2020-12-01");
    assertTrue(headerIndex >= 0);
    assertTrue(mainIndex > headerIndex);
    assertTrue(sidebarHeadingIndex > mainIndex);
    assertTrue(sidebarIndex > sidebarHeadingIndex);
  }

  @Test
  void createsEmptyBodyFallbackSummary() {
    final WorksheetParser.ParsedSheet sheet = sheet("Empty", Collections.<WorksheetParser.ParsedCell>emptyList());
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final SheetMarkdown.SheetRenderState state = SheetMarkdown.collectSheetRenderState(workbook, sheet, new MarkdownOptions());
    final String markdown = SheetMarkdown.createSheetMarkdownText(workbook, sheet, state);
    final MarkdownExport.MarkdownSummary summary = SheetMarkdown.createSheetSummary(sheet, state);

    assertEquals("", state.getBody());
    assertTrue(markdown.contains("_No extractable body content was found._"));
    assertEquals(0, summary.getSections());
    assertEquals(0, summary.getTables());
    assertEquals(0, summary.getNarrativeBlocks());
  }

  @Test
  void preservesPlainAndGithubLineBreakDifferences() {
    final WorksheetParser.ParsedSheet sheet = sheet("Lines", Arrays.asList(cell("A1", 1, 1, "Line1\nLine2")));
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final MarkdownExport.MarkdownFile plain = SheetMarkdown.convertSheetToMarkdown(workbook, sheet, new MarkdownOptions());
    final MarkdownExport.MarkdownFile github = SheetMarkdown.convertSheetToMarkdown(workbook, sheet,
        new MarkdownOptions(null, null, null, null, null, null, "github", null));

    assertTrue(plain.getMarkdown().contains("Line1 Line2"));
    assertTrue(github.getMarkdown().contains("Line1<br>Line2"));
  }

  @Test
  void keepsMarkdownMarkersLiteralInNarrativeOutput() {
    final WorksheetParser.ParsedSheet sheet = sheet("Literal", Arrays.asList(
        cell("A1", 1, 1, "# heading"),
        cell("A2", 2, 1, "- item"),
        cell("A3", 3, 1, "1. item"),
        cell("A4", 4, 1, "> quote"),
        cell("A5", 5, 1, "![alt](img.png)"),
        cell("A6", 6, 1, "`code`"),
        cell("A7", 7, 1, "+ plus"),
        cell("A8", 8, 1, "* star"),
        cell("A9", 9, 1, "a & b")));
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final MarkdownExport.MarkdownFile file = SheetMarkdown.convertSheetToMarkdown(workbook, sheet, new MarkdownOptions());

    assertTrue(file.getMarkdown().contains("\\# heading"));
    assertTrue(file.getMarkdown().contains("\\- item"));
    assertTrue(file.getMarkdown().contains("1\\. item"));
    assertTrue(file.getMarkdown().contains("&gt; quote"));
    assertTrue(file.getMarkdown().contains("\\!\\[alt\\]\\(img.png\\)"));
    assertTrue(file.getMarkdown().contains("\\`code\\`"));
    assertTrue(file.getMarkdown().contains("\\+ plus"));
    assertTrue(file.getMarkdown().contains("\\* star"));
    assertTrue(file.getMarkdown().contains("a &amp; b"));
  }

  @Test
  void rendersExternalAndWorkbookHyperlinksAsMarkdownLinks() {
    final WorksheetParser.ParsedSheet source = sheet("Sheet1", Arrays.asList(
        cell("A1", 1, 1, "Open", "Open", new WorksheetParser.Hyperlink("external", "https://example.com/", "", "", "")),
        cell("A2", 2, 1, "Jump", "Jump", new WorksheetParser.Hyperlink("internal", "'Other Sheet'!C3", "'Other Sheet'!C3", "", ""))));
    final WorksheetParser.ParsedSheet target = sheet("Other Sheet", Collections.<WorksheetParser.ParsedCell>emptyList());
    final WorkbookLoader.ParsedWorkbook workbook = new WorkbookLoader.ParsedWorkbook(
        "book.xlsx",
        Arrays.asList(source, target),
        Collections.<jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings.SharedStringEntry>emptyList(),
        Collections.<WorkbookLoader.DefinedName>emptyList());

    final MarkdownExport.MarkdownFile file = SheetMarkdown.convertSheetToMarkdown(workbook, source,
        new MarkdownOptions(null, null, null, null, null, null, "github", null));

    assertTrue(file.getMarkdown().contains("[Open](https://example.com/)"));
    assertTrue(file.getMarkdown().contains("[Jump](#other-sheet) (Other Sheet!C3)"));
  }

  @Test
  void suppressesUnderlineMarkupForHyperlinkCellsInGithubMode() {
    final WorksheetParser.ParsedCell linkedCell = new WorksheetParser.ParsedCell(
        "A1",
        1,
        1,
        "s",
        "Linked",
        "Linked",
        "",
        null,
        null,
        "none",
        0,
        new StylesParser.BorderFlags(false, false, false, false),
        0,
        "General",
        new StylesParser.TextStyle(false, false, false, true),
        null,
        "",
        "",
        new WorksheetParser.Hyperlink("external", "https://example.com/", "", "", ""));
    final WorksheetParser.ParsedSheet sheet = sheet("Sheet1", Arrays.asList(linkedCell));
    final WorkbookLoader.ParsedWorkbook workbook = workbook(sheet);

    final MarkdownExport.MarkdownFile file = SheetMarkdown.convertSheetToMarkdown(workbook, sheet,
        new MarkdownOptions(null, null, null, null, null, null, "github", null));

    assertTrue(file.getMarkdown().contains("[Linked](https://example.com/)"));
    assertFalse(file.getMarkdown().contains("<ins>Linked</ins>"));
  }

  @Test
  void preservesHyperlinksInRawModeAndAppendsRawOnlyWhenValuesDifferInBothMode() {
    final WorksheetParser.ParsedSheet rawSheet = sheet("Raw", Arrays.asList(
        cell("A1", 1, 1, "Displayed", "https://raw.example/",
            new WorksheetParser.Hyperlink("external", "https://example.com/", "", "", ""))));
    final WorkbookLoader.ParsedWorkbook rawWorkbook = workbook(rawSheet);
    final MarkdownExport.MarkdownFile raw = SheetMarkdown.convertSheetToMarkdown(rawWorkbook, rawSheet,
        new MarkdownOptions(null, null, null, null, null, "raw", null, null));

    assertTrue(raw.getMarkdown().contains("[https://raw.example/](https://example.com/)"));

    final WorksheetParser.ParsedSheet bothSheet = sheet("Both", Arrays.asList(
        cell("A1", 1, 1, "Displayed", "RawValue", null),
        cell("A2", 2, 1, "SameValue", "SameValue", null)));
    final MarkdownExport.MarkdownFile both = SheetMarkdown.convertSheetToMarkdown(workbook(bothSheet), bothSheet,
        new MarkdownOptions(null, null, null, null, null, "both", null, null));

    assertTrue(both.getMarkdown().contains("Displayed [raw=RawValue]"));
    assertTrue(both.getMarkdown().contains("SameValue"));
    assertFalse(both.getMarkdown().contains("SameValue [raw=SameValue]"));
  }

  private static WorkbookLoader.ParsedWorkbook workbook(final WorksheetParser.ParsedSheet sheet) {
    return new WorkbookLoader.ParsedWorkbook(
        "book.xlsx",
        Arrays.asList(sheet),
        Collections.<jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings.SharedStringEntry>emptyList(),
        Collections.<WorkbookLoader.DefinedName>emptyList());
  }

  private static WorksheetParser.ParsedSheet sheet(final String name, final List<WorksheetParser.ParsedCell> cells) {
    return new WorksheetParser.ParsedSheet(
        name,
        1,
        "xl/worksheets/sheet1.xml",
        cells,
        Collections.<AddressUtils.MergeRange>emptyList(),
        10,
        5);
  }

  private static WorksheetParser.ParsedCell cell(final String address, final int row, final int col, final String outputValue) {
    return cell(address, row, col, outputValue, outputValue, null);
  }

  private static WorksheetParser.ParsedCell cell(
      final String address,
      final int row,
      final int col,
      final String outputValue,
      final String rawValue,
      final WorksheetParser.Hyperlink hyperlink) {
    return new WorksheetParser.ParsedCell(
        address,
        row,
        col,
        "s",
        rawValue,
        outputValue,
        "",
        null,
        null,
        "none",
        0,
        new StylesParser.BorderFlags(false, false, false, false),
        0,
        "General",
        new StylesParser.TextStyle(false, false, false, false),
        null,
        "",
        "",
        hyperlink);
  }

  private static WorksheetParser.ParsedShapeAsset shape(final String anchor, final SheetAssets.BoundingBox bbox) {
    return new WorksheetParser.ParsedShapeAsset(
        anchor,
        Arrays.asList(new WorksheetParser.ParsedShapeRawEntry("kind", "rect")),
        null,
        null,
        null,
        "Shape",
        "Rectangle",
        "",
        null,
        null,
        "xdr:sp",
        "xdr:twoCellAnchor",
        bbox);
  }

  private static WorksheetParser.ParsedShapeAsset shapeWithSvg(final String anchor, final String svgFilename, final String svgPath) {
    return new WorksheetParser.ParsedShapeAsset(
        anchor,
        Arrays.asList(new WorksheetParser.ParsedShapeRawEntry("kind", "rect")),
        svgFilename,
        svgPath,
        "<svg/>".getBytes(java.nio.charset.StandardCharsets.UTF_8),
        "Shape",
        "Rectangle",
        "",
        null,
        null,
        "xdr:sp",
        "xdr:twoCellAnchor",
        new SheetAssets.BoundingBox(0, 0, 100, 20));
  }
}
