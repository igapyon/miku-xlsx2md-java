/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.bordergrid;

import java.util.Collections;
import java.util.List;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

public final class TestCells {
  private TestCells() {
  }

  public static WorksheetParser.ParsedSheet sheet(final List<WorksheetParser.ParsedCell> cells) {
    return sheet(cells, Collections.<AddressUtils.MergeRange>emptyList());
  }

  public static WorksheetParser.ParsedSheet sheet(
      final List<WorksheetParser.ParsedCell> cells,
      final List<AddressUtils.MergeRange> merges) {
    return new WorksheetParser.ParsedSheet("Sheet1", 1, "xl/worksheets/sheet1.xml", cells, merges, 100, 50);
  }

  public static WorksheetParser.ParsedCell cell(
      final int row,
      final int col,
      final String value) {
    return cell(row, col, value, false, false, false, false);
  }

  public static WorksheetParser.ParsedCell cell(
      final int row,
      final int col,
      final String value,
      final boolean top,
      final boolean bottom,
      final boolean left,
      final boolean right) {
    return new WorksheetParser.ParsedCell(
        "A1",
        row,
        col,
        "s",
        value,
        value,
        "",
        null,
        null,
        "none",
        0,
        new StylesParser.BorderFlags(top, bottom, left, right),
        0,
        "General",
        new StylesParser.TextStyle(false, false, false, false),
        null,
        "",
        "",
        null);
  }
}
