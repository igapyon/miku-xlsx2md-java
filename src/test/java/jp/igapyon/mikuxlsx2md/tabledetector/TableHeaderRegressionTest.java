/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.tabledetector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.bordergrid.TestCells;
import jp.igapyon.mikuxlsx2md.core.Core;
import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

class TableHeaderRegressionTest {
  private static final String[] MODES = {"balanced", "border", "planner-aware"};

  @Test
  void matchesPinnedUpstreamHeaderFixtureRangesInAllDetectionModes() throws IOException {
    final String[][] fixtures = {
        {"table-header-width-unbordered-sample01.xlsx", "A1-D2", "", "A1-D2"},
        {"table-header-width-bordered-sample01.xlsx", "A1-D2", "A1-D2", "A1-D2"},
        {"table-header-width-mixed-borders-sample01.xlsx", "A1-C2", "A1-C2", "A1-C2"},
        {"table-merged-header-unbordered-sample01.xlsx", "A1-C3", "", "A1-C3"},
        {"table-merged-header-bordered-sample01.xlsx", "A1-C3", "A1-C3", "A1-C3"},
        {"table-merged-header-mixed-borders-sample01.xlsx", "A1-C3", "A2-C3", "A2-C3"}
    };
    for (final String[] fixture : fixtures) {
      final WorkbookLoader.ParsedWorkbook workbook = parseFixture(fixture[0]);
      final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);
      for (int mode = 0; mode < MODES.length; mode += 1) {
        final List<TableDetector.TableCandidate> candidates = TableDetector.detectTableCandidates(sheet, null, MODES[mode]);
        assertEquals(fixture[mode + 1].isEmpty() ? Collections.emptyList() : Arrays.asList(fixture[mode + 1]),
            candidateRanges(candidates), fixture[0] + " / " + MODES[mode]);

        final List<MarkdownExport.MarkdownFile> output = Core.convertWorkbookToMarkdownFiles(
            workbook, new MarkdownOptions(null, null, null, null, null, null, null, MODES[mode]));
        assertEquals(1, output.size(), fixture[0] + " / " + MODES[mode]);
        assertEquals(candidates.size(), output.get(0).getSummary().getTables());
        assertEquals(candidateRanges(candidates), tableScoreRanges(output.get(0)));
        assertTrue(output.get(0).getMarkdown().contains("## Sheet:"));
        assertEquals(candidates.isEmpty(), !output.get(0).getMarkdown().contains("### Table:"));
      }
    }
  }

  @Test
  void mixedBorderFixtureHonorsNoHeaderAndEmptyRowColumnOptions() throws IOException {
    final WorkbookLoader.ParsedWorkbook workbook = parseFixture("table-merged-header-mixed-borders-sample01.xlsx");
    final MarkdownExport.MarkdownFile noHeader = Core.convertWorkbookToMarkdownFiles(workbook,
        new MarkdownOptions(Boolean.FALSE, null, null, null, null, null, null, "balanced")).get(0);
    final MarkdownExport.MarkdownFile preserveEmpty = Core.convertWorkbookToMarkdownFiles(workbook,
        new MarkdownOptions(null, null, Boolean.FALSE, Boolean.FALSE, null, null, null, "balanced")).get(0);

    assertEquals(1, noHeader.getSummary().getTables());
    assertTrue(noHeader.getMarkdown().contains("A1-C3"));
    assertTrue(preserveEmpty.getMarkdown().contains("[←M←]"));
    assertTrue(preserveEmpty.getMarkdown().contains("|"));
    assertEquals("A1-C3", preserveEmpty.getSummary().getTableScores().get(0).getRange());
  }

  @Test
  void usesEcmaScriptTrimSetForMergedHeaderLabels() {
    final char[] trimWhitespace = {
        0x0009, 0x000A, 0x000B, 0x000C, 0x000D, 0x0020, 0x00A0, 0x1680,
        0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005, 0x2006, 0x2007,
        0x2008, 0x2009, 0x200A, 0x2028, 0x2029, 0x202F, 0x205F, 0x3000, 0xFEFF
    };
    for (final char whitespace : trimWhitespace) {
      assertEquals("A2-C3", detectExpandedRange(repeat(whitespace, 1)),
          String.format("U+%04X should trim to an empty, invalid label", Integer.valueOf(whitespace)));
      assertEquals("A2-C3", detectExpandedRange(String.valueOf(whitespace) + "123" + whitespace),
          String.format("U+%04X should trim before numeric-label validation", Integer.valueOf(whitespace)));
    }
    for (final char retained : new char[] {0x0085, 0x180E, 0x200B}) {
      assertEquals("A1-C3", detectExpandedRange(String.valueOf(retained) + "Group" + retained),
          String.format("U+%04X must not be removed by ECMAScript trim", Integer.valueOf(retained)));
    }
    assertEquals("A1-C3", detectExpandedRange(repeat('\u3000', 12) + "Group" + repeat('\u3000', 12)),
        "Label length is checked after ECMAScript trimming");
    assertEquals("A1-C3", detectExpandedRange(repeat('x', 24)));
    assertEquals("A2-C3", detectExpandedRange(repeat('x', 25)));
    assertEquals("A2-C3", detectExpandedRange("Group\rLine"));
    assertEquals("A2-C3", detectExpandedRange("Group\nLine"));
    assertEquals("A1-C3", detectExpandedRange("１２３"));
  }

  @Test
  void balancedExpansionPreservesScoreAndReasonAndDoesNotMutateInput() {
    final WorksheetParser.ParsedSheet sheet = syntheticHeaderSheet("Group", "Details", "", false);
    final List<WorksheetParser.ParsedCell> originalCells = new ArrayList<WorksheetParser.ParsedCell>(sheet.getCells());
    final List<TableDetector.TableCandidate> expanded = TableDetector.detectTableCandidates(sheet, null, "balanced");
    final List<TableDetector.TableCandidate> bordered = TableDetector.detectTableCandidates(sheet, null, "border");

    assertEquals(1, expanded.size());
    assertEquals("A1-C3", range(expanded.get(0)));
    assertEquals("Included borderless merged header row (no score change)",
        expanded.get(0).getReasonSummary().get(expanded.get(0).getReasonSummary().size() - 1));
    assertEquals(1, Collections.frequency(expanded.get(0).getReasonSummary(),
        "Included borderless merged header row (no score change)"));
    assertEquals(1, bordered.size());
    assertEquals(expanded.get(0).getScore(), bordered.get(0).getScore());
    assertEquals(originalCells, sheet.getCells());
    assertFalse(TableDetector.detectTableCandidates(sheet, null, "border").get(0).getStartRow() == 1);
  }

  @Test
  void rejectsInvalidMergedHeaderStructuresAndSpillValues() {
    assertNotExpanded(syntheticHeaderSheet("Group", "", "", false));
    assertNotExpanded(syntheticHeaderSheet("Group", "Details", "hidden", false));
    assertNotExpanded(syntheticHeaderSheet("Group", "Details", "", true));

    final List<WorksheetParser.ParsedCell> noMergeCells = bodyCells("Group", "Details", "", false);
    assertNotExpanded(TestCells.sheet(noMergeCells));

    final List<WorksheetParser.ParsedCell> fullWidthCells = bodyCells("Group", "", "", false);
    fullWidthCells.add(TestCells.cell(1, 2, ""));
    fullWidthCells.add(TestCells.cell(1, 3, ""));
    assertNotExpanded(TestCells.sheet(fullWidthCells,
        Arrays.asList(new AddressUtils.MergeRange(1, 1, 1, 3, "A1:C1"))));

    final WorksheetParser.ParsedSheet narrow = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, "Group"), TestCells.cell(1, 2, "Details"),
        TestCells.cell(2, 1, "id", true, true, true, false), TestCells.cell(2, 2, "value", true, true, false, true),
        TestCells.cell(3, 1, "1", false, true, true, false), TestCells.cell(3, 2, "one", false, true, false, true)),
        Arrays.asList(new AddressUtils.MergeRange(1, 1, 1, 1, "A1:A1")));
    assertNotExpanded(narrow);

    final List<WorksheetParser.ParsedCell> ordinaryCells = bodyCells("Group", "Details", "", false);
    assertNotExpanded(TestCells.sheet(ordinaryCells, Arrays.asList(
        new AddressUtils.MergeRange(1, 1, 2, 2, "A1:B2"))));
    assertNotExpanded(TestCells.sheet(bodyCells("Group", "Details", "", false), Arrays.asList(
        new AddressUtils.MergeRange(1, 1, 1, 2, "A1:B1"),
        new AddressUtils.MergeRange(1, 2, 1, 3, "B1:C1"))));
    assertNotExpanded(TestCells.sheet(bodyCells("Group", "Details", "", false), Arrays.asList(
        new AddressUtils.MergeRange(1, 1, 1, 4, "A1:D1"))));

    final WorksheetParser.ParsedSheet leftSpill = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, "spill"), TestCells.cell(1, 2, "Group"), TestCells.cell(1, 3, ""), TestCells.cell(1, 4, "Details"),
        TestCells.cell(2, 2, "id", true, true, true, false), TestCells.cell(2, 3, "name", true, true, false, false),
        TestCells.cell(2, 4, "value", true, true, false, true),
        TestCells.cell(3, 2, "1", false, true, true, false), TestCells.cell(3, 3, "one", false, true, false, false),
        TestCells.cell(3, 4, "10", false, true, false, true)),
        Arrays.asList(new AddressUtils.MergeRange(1, 2, 1, 3, "B1:C1")));
    final List<TableDetector.TableCandidate> leftCandidates = TableDetector.detectTableCandidates(leftSpill);
    assertEquals(1, leftCandidates.size());
    assertEquals(2, leftCandidates.get(0).getStartRow(), "A left-side adjacent value must block expansion");
  }

  @Test
  void expandsIndependentSideBySideTablesSeparately() {
    final List<WorksheetParser.ParsedCell> cells = new ArrayList<WorksheetParser.ParsedCell>();
    cells.add(TestCells.cell(1, 1, "Group"));
    cells.add(TestCells.cell(1, 2, ""));
    cells.add(TestCells.cell(1, 3, "Details"));
    cells.add(TestCells.cell(1, 5, "Team"));
    cells.add(TestCells.cell(1, 6, ""));
    cells.add(TestCells.cell(1, 7, "Values"));
    for (int row = 2; row <= 3; row += 1) {
      for (int startCol : new int[] {1, 5}) {
        for (int offset = 0; offset < 3; offset += 1) {
          final boolean top = row == 2;
          final boolean bottom = row == 3;
          final boolean left = offset == 0;
          final boolean right = offset == 2;
          cells.add(TestCells.cell(row, startCol + offset, row == 2 ? "H" + offset : "V" + offset,
              top, bottom, left, right));
        }
      }
    }
    final WorksheetParser.ParsedSheet sheet = TestCells.sheet(cells, Arrays.asList(
        new AddressUtils.MergeRange(1, 1, 1, 2, "A1:B1"),
        new AddressUtils.MergeRange(1, 5, 1, 6, "E1:F1")));

    assertEquals(Arrays.asList("A1-C3", "E1-G3"), candidateRanges(
        TableDetector.detectTableCandidates(sheet, null, "balanced")));
  }

  @Test
  void requiresEcmaScriptNonEmptyCellsInBorderedBodyHeaderRow() {
    final WorksheetParser.ParsedSheet sheet = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, "Group"), TestCells.cell(1, 2, ""), TestCells.cell(1, 3, "Details"),
        TestCells.cell(2, 1, "Only label", true, true, true, false),
        TestCells.cell(2, 2, "\u00A0", true, true, false, false),
        TestCells.cell(2, 3, "\u00A0", true, true, false, true),
        TestCells.cell(3, 1, "1", false, true, true, false),
        TestCells.cell(3, 2, "2", false, true, false, false),
        TestCells.cell(3, 3, "3", false, true, false, true)),
        Arrays.asList(new AddressUtils.MergeRange(1, 1, 1, 2, "A1:B1")));

    final List<TableDetector.TableCandidate> candidates = TableDetector.detectTableCandidates(sheet);
    assertEquals(1, candidates.size());
    assertEquals("A2-C3", range(candidates.get(0)),
        "NBSP-only bordered cells must not meet the two-cell body-label threshold");
  }

  @Test
  void expandsHeaderCoveredByTwoSeparatePartialMerges() {
    final WorksheetParser.ParsedSheet sheet = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, "Group"), TestCells.cell(1, 2, ""),
        TestCells.cell(1, 3, "Section"), TestCells.cell(1, 4, ""),
        TestCells.cell(2, 1, "A", true, true, true, false),
        TestCells.cell(2, 2, "B", true, true, false, false),
        TestCells.cell(2, 3, "C", true, true, false, false),
        TestCells.cell(2, 4, "D", true, true, false, true),
        TestCells.cell(3, 1, "1", false, true, true, false),
        TestCells.cell(3, 2, "2", false, true, false, false),
        TestCells.cell(3, 3, "3", false, true, false, false),
        TestCells.cell(3, 4, "4", false, true, false, true)),
        Arrays.asList(
            new AddressUtils.MergeRange(1, 1, 1, 2, "A1:B1"),
            new AddressUtils.MergeRange(1, 3, 1, 4, "C1:D1")));

    final List<TableDetector.TableCandidate> candidates = TableDetector.detectTableCandidates(sheet);
    assertEquals(1, candidates.size());
    assertEquals("A1-D3", range(candidates.get(0)));
  }

  private static String detectExpandedRange(final String label) {
    final List<TableDetector.TableCandidate> candidates = TableDetector.detectTableCandidates(syntheticHeaderSheet(label));
    return candidates.size() == 1 ? range(candidates.get(0)) : "count=" + candidates.size();
  }

  private static WorksheetParser.ParsedSheet syntheticHeaderSheet(final String label) {
    return syntheticHeaderSheet(label, "Details", "", false);
  }

  private static WorksheetParser.ParsedSheet syntheticHeaderSheet(
      final String label, final String detail, final String hidden, final boolean rightSpill) {
    return TestCells.sheet(bodyCells(label, detail, hidden, rightSpill),
        Arrays.asList(new AddressUtils.MergeRange(1, 1, 1, 2, "A1:B1")));
  }

  private static List<WorksheetParser.ParsedCell> bodyCells(
      final String label, final String detail, final String hidden, final boolean rightSpill) {
    final List<WorksheetParser.ParsedCell> cells = new ArrayList<WorksheetParser.ParsedCell>(Arrays.asList(
        TestCells.cell(1, 1, label), TestCells.cell(1, 2, hidden), TestCells.cell(1, 3, detail),
        TestCells.cell(2, 1, "id", true, true, true, false),
        TestCells.cell(2, 2, "name", true, true, false, false),
        TestCells.cell(2, 3, "value", true, true, false, true),
        TestCells.cell(3, 1, "1", false, true, true, false),
        TestCells.cell(3, 2, "one", false, true, false, false),
        TestCells.cell(3, 3, "10", false, true, false, true)));
    if (rightSpill) {
      cells.add(TestCells.cell(1, 4, "spill"));
    }
    return cells;
  }

  private static void assertNotExpanded(final WorksheetParser.ParsedSheet sheet) {
    final List<TableDetector.TableCandidate> candidates = TableDetector.detectTableCandidates(sheet);
    assertFalse(candidates.isEmpty());
    for (final TableDetector.TableCandidate candidate : candidates) {
      assertEquals(2, candidate.getStartRow(), "Invalid header evidence must not expand the candidate");
    }
  }

  private static WorkbookLoader.ParsedWorkbook parseFixture(final String filename) throws IOException {
    final String resource = "/upstream-v1.3.1/" + filename;
    try (InputStream stream = TableHeaderRegressionTest.class.getResourceAsStream(resource)) {
      if (stream == null) {
        throw new IOException("Missing fixture resource: " + resource);
      }
      return Core.parseWorkbook(readAll(stream), filename);
    }
  }

  private static byte[] readAll(final InputStream stream) throws IOException {
    final java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
    final byte[] buffer = new byte[8192];
    int count;
    while ((count = stream.read(buffer)) >= 0) {
      output.write(buffer, 0, count);
    }
    return output.toByteArray();
  }

  private static List<String> candidateRanges(final List<TableDetector.TableCandidate> candidates) {
    final List<String> ranges = new ArrayList<String>();
    for (final TableDetector.TableCandidate candidate : candidates) {
      ranges.add(range(candidate));
    }
    return ranges;
  }

  private static List<String> tableScoreRanges(final MarkdownExport.MarkdownFile file) {
    final List<String> ranges = new ArrayList<String>();
    for (final MarkdownExport.TableScoreDetail detail : file.getSummary().getTableScores()) {
      ranges.add(detail.getRange());
    }
    return ranges;
  }

  private static String range(final TableDetector.TableCandidate candidate) {
    return columnName(candidate.getStartCol()) + candidate.getStartRow() + "-"
        + columnName(candidate.getEndCol()) + candidate.getEndRow();
  }

  private static String columnName(int col) {
    final StringBuilder name = new StringBuilder();
    while (col > 0) {
      col -= 1;
      name.insert(0, (char) ('A' + col % 26));
      col /= 26;
    }
    return name.toString();
  }

  private static String repeat(final char value, final int count) {
    final StringBuilder output = new StringBuilder();
    for (int index = 0; index < count; index += 1) {
      output.append(value);
    }
    return output.toString();
  }
}
