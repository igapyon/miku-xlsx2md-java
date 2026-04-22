/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.bordergrid;

import java.util.Map;
import java.util.Objects;

import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

public final class BorderGrid {
  private BorderGrid() {
  }

  public static WorksheetParser.ParsedCell getCellAt(
      final Map<String, WorksheetParser.ParsedCell> cellMap,
      final int row,
      final int col) {
    return cellMap == null ? null : cellMap.get(row + ":" + col);
  }

  public static boolean hasNormalizedBorderOnSide(
      final Map<String, WorksheetParser.ParsedCell> cellMap,
      final int row,
      final int col,
      final String side) {
    final WorksheetParser.ParsedCell cell = getCellAt(cellMap, row, col);
    if ("top".equals(side)) {
      final WorksheetParser.ParsedCell above = getCellAt(cellMap, row - 1, col);
      return hasRawBorder(cell, "top") || hasRawBorder(above, "bottom");
    }
    if ("bottom".equals(side)) {
      final WorksheetParser.ParsedCell below = getCellAt(cellMap, row + 1, col);
      return hasRawBorder(cell, "bottom") || hasRawBorder(below, "top");
    }
    if ("left".equals(side)) {
      final WorksheetParser.ParsedCell left = getCellAt(cellMap, row, col - 1);
      return hasRawBorder(cell, "left") || hasRawBorder(left, "right");
    }
    final WorksheetParser.ParsedCell right = getCellAt(cellMap, row, col + 1);
    return hasRawBorder(cell, "right") || hasRawBorder(right, "left");
  }

  public static boolean hasAnyNormalizedBorder(
      final Map<String, WorksheetParser.ParsedCell> cellMap,
      final int row,
      final int col) {
    return hasNormalizedBorderOnSide(cellMap, row, col, "top")
        || hasNormalizedBorderOnSide(cellMap, row, col, "bottom")
        || hasNormalizedBorderOnSide(cellMap, row, col, "left")
        || hasNormalizedBorderOnSide(cellMap, row, col, "right");
  }

  public static EdgeStats collectTableEdgeStats(
      final Map<String, WorksheetParser.ParsedCell> cellMap,
      final int row,
      final int startCol,
      final int endCol) {
    int nonEmptyCount = 0;
    int borderCount = 0;
    int rawBorderCount = 0;
    int topCount = 0;
    int bottomCount = 0;
    int maxTextLength = 0;
    for (int col = startCol; col <= endCol; col += 1) {
      final WorksheetParser.ParsedCell cell = getCellAt(cellMap, row, col);
      final String text = stringValue(cell == null ? "" : cell.getOutputValue()).trim();
      if (!text.isEmpty()) {
        nonEmptyCount += 1;
        maxTextLength = Math.max(maxTextLength, text.length());
      }
      if (hasAnyNormalizedBorder(cellMap, row, col)) {
        borderCount += 1;
      }
      if (hasAnyRawBorder(cell)) {
        rawBorderCount += 1;
      }
      if (hasNormalizedBorderOnSide(cellMap, row, col, "top")) {
        topCount += 1;
      }
      if (hasNormalizedBorderOnSide(cellMap, row, col, "bottom")) {
        bottomCount += 1;
      }
    }
    return new EdgeStats(nonEmptyCount, borderCount, rawBorderCount, topCount, bottomCount, maxTextLength);
  }

  public static int countNormalizedBorderedCells(
      final Map<String, WorksheetParser.ParsedCell> cellMap,
      final int startRow,
      final int startCol,
      final int endRow,
      final int endCol) {
    int count = 0;
    for (int row = startRow; row <= endRow; row += 1) {
      for (int col = startCol; col <= endCol; col += 1) {
        if (hasAnyNormalizedBorder(cellMap, row, col)) {
          count += 1;
        }
      }
    }
    return count;
  }

  public static boolean hasAnyRawBorder(final WorksheetParser.ParsedCell cell) {
    if (cell == null || cell.getBorders() == null) {
      return false;
    }
    final StylesParser.BorderFlags borders = cell.getBorders();
    return borders.isTop() || borders.isBottom() || borders.isLeft() || borders.isRight();
  }

  private static boolean hasRawBorder(final WorksheetParser.ParsedCell cell, final String side) {
    if (cell == null || cell.getBorders() == null) {
      return false;
    }
    if ("top".equals(side)) {
      return cell.getBorders().isTop();
    }
    if ("bottom".equals(side)) {
      return cell.getBorders().isBottom();
    }
    if ("left".equals(side)) {
      return cell.getBorders().isLeft();
    }
    return cell.getBorders().isRight();
  }

  private static String stringValue(final String value) {
    return value == null ? "" : value;
  }

  public static final class EdgeStats {
    private final int nonEmptyCount;
    private final int borderCount;
    private final int rawBorderCount;
    private final int topCount;
    private final int bottomCount;
    private final int maxTextLength;

    public EdgeStats(
        final int nonEmptyCount,
        final int borderCount,
        final int rawBorderCount,
        final int topCount,
        final int bottomCount,
        final int maxTextLength) {
      this.nonEmptyCount = nonEmptyCount;
      this.borderCount = borderCount;
      this.rawBorderCount = rawBorderCount;
      this.topCount = topCount;
      this.bottomCount = bottomCount;
      this.maxTextLength = maxTextLength;
    }

    public int getNonEmptyCount() {
      return nonEmptyCount;
    }

    public int getBorderCount() {
      return borderCount;
    }

    public int getRawBorderCount() {
      return rawBorderCount;
    }

    public int getTopCount() {
      return topCount;
    }

    public int getBottomCount() {
      return bottomCount;
    }

    public int getMaxTextLength() {
      return maxTextLength;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof EdgeStats)) {
        return false;
      }
      final EdgeStats that = (EdgeStats) other;
      return nonEmptyCount == that.nonEmptyCount
          && borderCount == that.borderCount
          && rawBorderCount == that.rawBorderCount
          && topCount == that.topCount
          && bottomCount == that.bottomCount
          && maxTextLength == that.maxTextLength;
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          Integer.valueOf(nonEmptyCount),
          Integer.valueOf(borderCount),
          Integer.valueOf(rawBorderCount),
          Integer.valueOf(topCount),
          Integer.valueOf(bottomCount),
          Integer.valueOf(maxTextLength));
    }
  }
}
