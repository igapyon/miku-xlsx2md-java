/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sheetmarkdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
