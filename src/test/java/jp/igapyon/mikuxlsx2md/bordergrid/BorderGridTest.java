/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.bordergrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.tabledetector.TableDetector;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

class BorderGridTest {
  @Test
  void returnsFalseWhenNeitherAdjacentCellOwnsTheQueriedEdge() {
    final Map<String, WorksheetParser.ParsedCell> cellMap = cellMap(
        cell(1, 1, "A", false, false, false, false),
        cell(2, 1, "B", false, false, false, false));

    assertFalse(BorderGrid.hasNormalizedBorderOnSide(cellMap, 2, 1, "top"));
    assertFalse(BorderGrid.hasAnyNormalizedBorder(cellMap, 2, 1));
  }

  @Test
  void normalizesBordersFromAdjacentCells() {
    final Map<String, WorksheetParser.ParsedCell> horizontal = cellMap(
        cell(1, 1, "Header", false, true, false, false),
        cell(2, 1, "Note", false, false, false, false));
    final Map<String, WorksheetParser.ParsedCell> vertical = cellMap(
        cell(1, 1, "A", false, false, false, true),
        cell(1, 2, "B", false, false, false, false));

    assertTrue(BorderGrid.hasNormalizedBorderOnSide(horizontal, 2, 1, "top"));
    assertTrue(BorderGrid.hasNormalizedBorderOnSide(vertical, 1, 2, "left"));
  }

  @Test
  void collectsRowStatsWithNormalizedAndRawBorderCountsSeparately() {
    final Map<String, WorksheetParser.ParsedCell> cellMap = cellMap(
        cell(1, 1, "H1", true, true, true, true),
        cell(1, 2, "H2", true, true, true, true),
        cell(2, 1, "Note line", false, false, false, false),
        cell(2, 2, "", false, false, false, false));

    final BorderGrid.EdgeStats firstRow = BorderGrid.collectTableEdgeStats(cellMap, 1, 1, 2);
    final BorderGrid.EdgeStats secondRow = BorderGrid.collectTableEdgeStats(cellMap, 2, 1, 2);

    assertEquals(2, firstRow.getBorderCount());
    assertEquals(2, firstRow.getRawBorderCount());
    assertEquals(2, firstRow.getBottomCount());
    assertEquals(1, secondRow.getNonEmptyCount());
    assertEquals(0, secondRow.getRawBorderCount());
    assertEquals(2, secondRow.getTopCount());
    assertEquals(2, secondRow.getBorderCount());
    assertEquals("Note line".length(), secondRow.getMaxTextLength());
  }

  @Test
  void countsNormalizedBorderedCellsAcrossACandidateRange() {
    final Map<String, WorksheetParser.ParsedCell> cellMap = cellMap(
        cell(1, 1, "A1", true, false, true, false),
        cell(1, 2, "B1", true, false, false, true),
        cell(2, 1, "A2", false, true, true, false),
        cell(2, 2, "B2", false, true, false, true),
        cell(3, 1, "Tail", false, false, false, false));

    assertEquals(4, BorderGrid.countNormalizedBorderedCells(cellMap, 1, 1, 2, 2));
    assertEquals(1, BorderGrid.countNormalizedBorderedCells(cellMap, 3, 1, 3, 1));
  }

  private static Map<String, WorksheetParser.ParsedCell> cellMap(final WorksheetParser.ParsedCell... cells) {
    return TableDetector.buildCellMap(TestCells.sheet(Arrays.asList(cells)));
  }

  private static WorksheetParser.ParsedCell cell(
      final int row,
      final int col,
      final String value,
      final boolean top,
      final boolean bottom,
      final boolean left,
      final boolean right) {
    return TestCells.cell(row, col, value, top, bottom, left, right);
  }
}
