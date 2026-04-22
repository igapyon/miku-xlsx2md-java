/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.addressutils;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AddressUtils {
  private static final Pattern CELL_ADDRESS_PATTERN = Pattern.compile("^([A-Z]+)(\\d+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern RANGE_ADDRESS_PATTERN =
      Pattern.compile("^(\\$?[A-Z]+\\$?\\d+):(\\$?[A-Z]+\\$?\\d+)$", Pattern.CASE_INSENSITIVE);

  private AddressUtils() {
  }

  public static String colToLetters(final int col) {
    int current = col;
    final StringBuilder result = new StringBuilder();
    while (current > 0) {
      final int remainder = (current - 1) % 26;
      result.insert(0, (char) ('A' + remainder));
      current = (current - 1) / 26;
    }
    return result.toString();
  }

  public static int lettersToCol(final String letters) {
    int result = 0;
    final String normalized = letters == null ? "" : letters.toUpperCase(Locale.ROOT);
    for (int index = 0; index < normalized.length(); index += 1) {
      result = result * 26 + (normalized.charAt(index) - 64);
    }
    return result;
  }

  public static CellAddress parseCellAddress(final String address) {
    final String normalized = address == null ? "" : address.trim().replace("$", "");
    final Matcher matcher = CELL_ADDRESS_PATTERN.matcher(normalized);
    if (!matcher.matches()) {
      return new CellAddress(0, 0);
    }
    return new CellAddress(Integer.parseInt(matcher.group(2)), lettersToCol(matcher.group(1)));
  }

  public static String normalizeFormulaAddress(final String address) {
    return address == null ? "" : address.trim().replace("$", "").toUpperCase(Locale.ROOT);
  }

  public static String formatRange(final int startRow, final int startCol, final int endRow, final int endCol) {
    return colToLetters(startCol) + startRow + "-" + colToLetters(endCol) + endRow;
  }

  public static MergeRange parseRangeRef(final String ref) {
    final String[] parts = ref == null ? new String[] {""} : ref.split(":");
    final CellAddress start = parseCellAddress(parts.length > 0 ? parts[0] : "");
    final CellAddress end = parseCellAddress(parts.length > 1 ? parts[1] : (parts.length > 0 ? parts[0] : ""));
    return new MergeRange(start.getRow(), start.getCol(), end.getRow(), end.getCol(), ref);
  }

  public static RangeAddress parseRangeAddress(final String rawRange) {
    final String normalized = rawRange == null ? "" : rawRange.trim();
    final Matcher matcher = RANGE_ADDRESS_PATTERN.matcher(normalized);
    if (!matcher.matches()) {
      return null;
    }
    return new RangeAddress(normalizeFormulaAddress(matcher.group(1)), normalizeFormulaAddress(matcher.group(2)));
  }

  public static final class CellAddress {
    private final int row;
    private final int col;

    public CellAddress(final int row, final int col) {
      this.row = row;
      this.col = col;
    }

    public int getRow() {
      return row;
    }

    public int getCol() {
      return col;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof CellAddress)) {
        return false;
      }
      final CellAddress that = (CellAddress) other;
      return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
      return Objects.hash(row, col);
    }
  }

  public static final class RangeAddress {
    private final String start;
    private final String end;

    public RangeAddress(final String start, final String end) {
      this.start = start;
      this.end = end;
    }

    public String getStart() {
      return start;
    }

    public String getEnd() {
      return end;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof RangeAddress)) {
        return false;
      }
      final RangeAddress that = (RangeAddress) other;
      return Objects.equals(start, that.start) && Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
      return Objects.hash(start, end);
    }
  }

  public static final class MergeRange {
    private final int startRow;
    private final int startCol;
    private final int endRow;
    private final int endCol;
    private final String ref;

    public MergeRange(final int startRow, final int startCol, final int endRow, final int endCol, final String ref) {
      this.startRow = startRow;
      this.startCol = startCol;
      this.endRow = endRow;
      this.endCol = endCol;
      this.ref = ref;
    }

    public int getStartRow() {
      return startRow;
    }

    public int getStartCol() {
      return startCol;
    }

    public int getEndRow() {
      return endRow;
    }

    public int getEndCol() {
      return endCol;
    }

    public String getRef() {
      return ref;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof MergeRange)) {
        return false;
      }
      final MergeRange that = (MergeRange) other;
      return startRow == that.startRow
          && startCol == that.startCol
          && endRow == that.endRow
          && endCol == that.endCol
          && Objects.equals(ref, that.ref);
    }

    @Override
    public int hashCode() {
      return Objects.hash(startRow, startCol, endRow, endCol, ref);
    }
  }
}
