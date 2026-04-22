/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.tabledetector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.bordergrid.TestCells;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

class TableDetectorTest {
  @Test
  void collectsSeedCellsFromValuesOrBorders() {
    final WorksheetParser.ParsedSheet sheet = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, ""),
        TestCells.cell(1, 2, "項目"),
        TestCells.cell(2, 1, "", false, true, false, false)));

    assertEquals(Arrays.asList("1:2", "2:1"), positions(TableDetector.collectTableSeedCells(sheet)));
    assertEquals(Arrays.asList("2:1"), positions(TableDetector.collectBorderSeedCells(sheet)));
  }

  @Test
  void trimsBorderedTableBeforeFollowingBorderlessNoteRow() {
    final WorksheetParser.ParsedSheet sheet = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, "項番", true, true, true, false),
        TestCells.cell(1, 2, "名称", true, true, false, true),
        TestCells.cell(2, 1, "1", false, true, true, false),
        TestCells.cell(2, 2, "コード", false, true, false, true),
        TestCells.cell(3, 1, "※注記"),
        TestCells.cell(3, 2, "")));

    assertEquals(
        new TableDetector.Bounds(1, 1, 2, 2),
        TableDetector.trimTableCandidateBounds(TableDetector.buildCellMap(sheet), new TableDetector.Bounds(1, 1, 3, 2)));
  }

  @Test
  void normalizesCandidateMatricesWithMergeTokensAndEmptyTrimming() {
    final WorksheetParser.ParsedSheet sheet = TestCells.sheet(
        Arrays.asList(
            TestCells.cell(1, 1, "Header"),
            TestCells.cell(1, 2, ""),
            TestCells.cell(1, 3, ""),
            TestCells.cell(2, 1, "Value"),
            TestCells.cell(2, 2, "A"),
            TestCells.cell(2, 3, ""),
            TestCells.cell(3, 1, ""),
            TestCells.cell(3, 2, ""),
            TestCells.cell(3, 3, "")),
        Arrays.asList(new AddressUtils.MergeRange(1, 1, 1, 2, "A1:B1")));

    final List<List<String>> matrix = TableDetector.matrixFromCandidate(
        sheet,
        new TableDetector.TableCandidate(1, 1, 3, 3, 5, Collections.<String>emptyList()),
        new MarkdownOptions(),
        new TableDetector.CellFormatter() {
          @Override
          public String formatCellForMarkdown(final WorksheetParser.ParsedCell cell, final MarkdownOptions options) {
            return cell == null ? "" : cell.getOutputValue();
          }
        });

    assertEquals(Arrays.asList(
        Arrays.asList("Header", "[←M←]"),
        Arrays.asList("Value", "A")), matrix);
  }

  @Test
  void detectsBorderedDenseGridAsTableCandidate() {
    final WorksheetParser.ParsedSheet sheet = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, "項番", true, true, true, false),
        TestCells.cell(1, 2, "名称", true, true, false, true),
        TestCells.cell(2, 1, "1", false, true, true, false),
        TestCells.cell(2, 2, "コード", false, true, false, true)));

    final List<TableDetector.TableCandidate> candidates = TableDetector.detectTableCandidates(sheet);

    assertEquals(1, candidates.size());
    assertEquals(new TableDetector.Bounds(1, 1, 2, 2), new TableDetector.Bounds(
        candidates.get(0).getStartRow(),
        candidates.get(0).getStartCol(),
        candidates.get(0).getEndRow(),
        candidates.get(0).getEndCol()));
    assertTrue(candidates.get(0).getScore() >= TableDetector.DEFAULT_TABLE_SCORE_WEIGHTS.getThreshold());
  }

  @Test
  void prunesRedundantAndCalendarLikeCandidates() {
    assertEquals(
        Arrays.asList(new TableDetector.TableCandidate(2, 1, 10, 7, 8, Collections.<String>emptyList())),
        TableDetector.pruneRedundantCandidates(Arrays.asList(
            new TableDetector.TableCandidate(2, 1, 10, 12, 7, Collections.<String>emptyList()),
            new TableDetector.TableCandidate(2, 1, 10, 7, 8, Collections.<String>emptyList()))));

    assertEquals(Collections.<TableDetector.TableCandidate>emptyList(), TableDetector.pruneCalendarLikeColumnCandidates(Arrays.asList(
        new TableDetector.TableCandidate(1, 1, 4, 2, 8, Collections.<String>emptyList()),
        new TableDetector.TableCandidate(1, 4, 4, 5, 8, Collections.<String>emptyList()),
        new TableDetector.TableCandidate(1, 7, 4, 8, 8, Collections.<String>emptyList()))));
  }

  @Test
  void borderModeExcludesDenseBorderlessBlocksButKeepsBorderedTables() {
    final WorksheetParser.ParsedSheet borderless = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, "項目"),
        TestCells.cell(1, 2, "値"),
        TestCells.cell(2, 1, "A"),
        TestCells.cell(2, 2, "100")));
    final WorksheetParser.ParsedSheet bordered = TestCells.sheet(Arrays.asList(
        TestCells.cell(1, 1, "項目", true, true, true, false),
        TestCells.cell(1, 2, "値", true, true, false, true),
        TestCells.cell(2, 1, "A", false, true, true, false),
        TestCells.cell(2, 2, "100", false, true, false, true)));

    assertEquals(1, TableDetector.detectTableCandidates(borderless, null, "balanced").size());
    assertEquals(0, TableDetector.detectTableCandidates(borderless, null, "border").size());
    assertEquals(1, TableDetector.detectTableCandidates(bordered, null, "border").size());
  }

  @Test
  void doesNotTreatMergeHeavyFormBlocksAsTables() {
    final java.util.ArrayList<WorksheetParser.ParsedCell> cells = new java.util.ArrayList<WorksheetParser.ParsedCell>();
    for (int row = 1; row <= 6; row += 1) {
      for (int col = 1; col <= 12; col += 1) {
        String value = "";
        if (col == 1) {
          value = "label" + row;
        }
        if (row == 2 && col == 8) {
          value = "開始日時";
        }
        if (row == 4 && col == 8) {
          value = "終了日時";
        }
        cells.add(TestCells.cell(row, col, value, true, true, true, true));
      }
    }
    final WorksheetParser.ParsedSheet sheet = TestCells.sheet(cells, Arrays.asList(
        new AddressUtils.MergeRange(1, 2, 1, 12, "B1:L1"),
        new AddressUtils.MergeRange(2, 2, 2, 7, "B2:G2"),
        new AddressUtils.MergeRange(2, 8, 2, 12, "H2:L2"),
        new AddressUtils.MergeRange(3, 2, 3, 12, "B3:L3"),
        new AddressUtils.MergeRange(4, 2, 4, 7, "B4:G4"),
        new AddressUtils.MergeRange(4, 8, 4, 12, "H4:L4"),
        new AddressUtils.MergeRange(5, 2, 5, 12, "B5:L5"),
        new AddressUtils.MergeRange(6, 2, 6, 12, "B6:L6")));

    assertEquals(0, TableDetector.detectTableCandidates(sheet).size());
  }

  private static List<String> positions(final List<WorksheetParser.ParsedCell> cells) {
    final java.util.ArrayList<String> positions = new java.util.ArrayList<String>();
    for (final WorksheetParser.ParsedCell cell : cells) {
      positions.add(cell.getRow() + ":" + cell.getCol());
    }
    return positions;
  }
}
