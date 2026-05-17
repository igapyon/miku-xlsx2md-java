/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.tabledetector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.bordergrid.BorderGrid;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

public final class TableDetector {
  public static final TableScoreWeights DEFAULT_TABLE_SCORE_WEIGHTS = new TableScoreWeights(2, 3, 2, 1, 2, -1, -2, 4);

  private TableDetector() {
  }

  public static Map<String, WorksheetParser.ParsedCell> buildCellMap(final WorksheetParser.ParsedSheet sheet) {
    final Map<String, WorksheetParser.ParsedCell> map = new LinkedHashMap<String, WorksheetParser.ParsedCell>();
    for (final WorksheetParser.ParsedCell cell : safeCells(sheet)) {
      map.put(cell.getRow() + ":" + cell.getCol(), cell);
    }
    return map;
  }

  public static List<WorksheetParser.ParsedCell> collectTableSeedCells(final WorksheetParser.ParsedSheet sheet) {
    final List<WorksheetParser.ParsedCell> seeds = new ArrayList<WorksheetParser.ParsedCell>();
    for (final WorksheetParser.ParsedCell cell : safeCells(sheet)) {
      if (!stringValue(cell.getOutputValue()).trim().isEmpty() || BorderGrid.hasAnyRawBorder(cell)) {
        seeds.add(cell);
      }
    }
    return seeds;
  }

  public static List<WorksheetParser.ParsedCell> collectBorderSeedCells(final WorksheetParser.ParsedSheet sheet) {
    final List<WorksheetParser.ParsedCell> seeds = new ArrayList<WorksheetParser.ParsedCell>();
    for (final WorksheetParser.ParsedCell cell : safeCells(sheet)) {
      if (BorderGrid.hasAnyRawBorder(cell)) {
        seeds.add(cell);
      }
    }
    return seeds;
  }

  public static boolean areBorderAdjacent(
      final WorksheetParser.ParsedCell current,
      final WorksheetParser.ParsedCell next) {
    if (current.getRow() == next.getRow() && Math.abs(current.getCol() - next.getCol()) == 1) {
      return (current.getBorders().isTop() && next.getBorders().isTop())
          || (current.getBorders().isBottom() && next.getBorders().isBottom())
          || (current.getCol() < next.getCol()
              ? current.getBorders().isRight() && next.getBorders().isLeft()
              : current.getBorders().isLeft() && next.getBorders().isRight());
    }
    if (current.getCol() == next.getCol() && Math.abs(current.getRow() - next.getRow()) == 1) {
      return (current.getBorders().isLeft() && next.getBorders().isLeft())
          || (current.getBorders().isRight() && next.getBorders().isRight())
          || (current.getRow() < next.getRow()
              ? current.getBorders().isBottom() && next.getBorders().isTop()
              : current.getBorders().isTop() && next.getBorders().isBottom());
    }
    return false;
  }

  public static List<List<WorksheetParser.ParsedCell>> collectConnectedComponents(
      final List<WorksheetParser.ParsedCell> seedCells) {
    return collectConnectedComponents(seedCells, "grid");
  }

  public static List<List<WorksheetParser.ParsedCell>> collectConnectedComponents(
      final List<WorksheetParser.ParsedCell> seedCells,
      final String adjacencyMode) {
    final Map<String, WorksheetParser.ParsedCell> positionMap = new LinkedHashMap<String, WorksheetParser.ParsedCell>();
    for (final WorksheetParser.ParsedCell cell : seedCells == null ? Collections.<WorksheetParser.ParsedCell>emptyList() : seedCells) {
      positionMap.put(cell.getRow() + ":" + cell.getCol(), cell);
    }
    final Set<String> visited = new HashSet<String>();
    final List<List<WorksheetParser.ParsedCell>> components = new ArrayList<List<WorksheetParser.ParsedCell>>();
    for (final WorksheetParser.ParsedCell cell : seedCells == null ? Collections.<WorksheetParser.ParsedCell>emptyList() : seedCells) {
      final String key = cell.getRow() + ":" + cell.getCol();
      if (visited.contains(key)) {
        continue;
      }
      final Queue<WorksheetParser.ParsedCell> queue = new ArrayDeque<WorksheetParser.ParsedCell>();
      final List<WorksheetParser.ParsedCell> component = new ArrayList<WorksheetParser.ParsedCell>();
      queue.add(cell);
      visited.add(key);
      while (!queue.isEmpty()) {
        final WorksheetParser.ParsedCell current = queue.remove();
        component.add(current);
        final int[][] deltas = new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (final int[] delta : deltas) {
          final String nextKey = (current.getRow() + delta[0]) + ":" + (current.getCol() + delta[1]);
          final WorksheetParser.ParsedCell next = positionMap.get(nextKey);
          if (next == null || visited.contains(nextKey)) {
            continue;
          }
          if ("border".equals(adjacencyMode) && !areBorderAdjacent(current, next)) {
            continue;
          }
          visited.add(nextKey);
          queue.add(next);
        }
      }
      components.add(component);
    }
    return components;
  }

  public static boolean isWithinBounds(final Bounds bounds, final Bounds candidate) {
    return candidate.getStartRow() >= bounds.getStartRow()
        && candidate.getStartCol() >= bounds.getStartCol()
        && candidate.getEndRow() <= bounds.getEndRow()
        && candidate.getEndCol() <= bounds.getEndCol();
  }

  public static int getBoundsArea(final Bounds bounds) {
    return Math.max(1, (bounds.getEndRow() - bounds.getStartRow() + 1) * (bounds.getEndCol() - bounds.getStartCol() + 1));
  }

  public static int getCombinedCandidateArea(final List<TableCandidate> candidates) {
    int sum = 0;
    for (final TableCandidate candidate : candidates == null ? Collections.<TableCandidate>emptyList() : candidates) {
      sum += getBoundsArea(candidate);
    }
    return sum;
  }

  public static List<TableCandidate> pruneRedundantCandidates(final List<TableCandidate> candidates) {
    final List<TableCandidate> result = new ArrayList<TableCandidate>();
    final List<TableCandidate> safeCandidates = candidates == null ? Collections.<TableCandidate>emptyList() : candidates;
    for (int candidateIndex = 0; candidateIndex < safeCandidates.size(); candidateIndex += 1) {
      final TableCandidate candidate = safeCandidates.get(candidateIndex);
      final int candidateArea = getBoundsArea(candidate);
      boolean hasSingleDominatingContainedCandidate = false;
      for (int otherIndex = 0; otherIndex < safeCandidates.size(); otherIndex += 1) {
        final TableCandidate other = safeCandidates.get(otherIndex);
        if (candidateIndex == otherIndex || !isWithinBounds(candidate, other)) {
          continue;
        }
        final int otherArea = getBoundsArea(other);
        if (otherArea >= candidateArea * 0.4d && candidateArea > otherArea) {
          hasSingleDominatingContainedCandidate = true;
          break;
        }
      }
      if (hasSingleDominatingContainedCandidate) {
        continue;
      }
      final List<TableCandidate> containedCandidates = new ArrayList<TableCandidate>();
      for (int otherIndex = 0; otherIndex < safeCandidates.size(); otherIndex += 1) {
        final TableCandidate other = safeCandidates.get(otherIndex);
        if (candidateIndex != otherIndex && isWithinBounds(candidate, other) && getBoundsArea(other) < candidateArea) {
          containedCandidates.add(other);
        }
      }
      if (containedCandidates.size() >= 2 && getCombinedCandidateArea(containedCandidates) >= candidateArea * 0.6d) {
        continue;
      }
      result.add(candidate);
    }
    return result;
  }

  public static List<TableCandidate> pruneCalendarLikeColumnCandidates(final List<TableCandidate> candidates) {
    final Set<String> dropKeys = new HashSet<String>();
    final List<TableCandidate> sorted = new ArrayList<TableCandidate>(candidates == null ? Collections.<TableCandidate>emptyList() : candidates);
    Collections.sort(sorted, new Comparator<TableCandidate>() {
      @Override
      public int compare(final TableCandidate left, final TableCandidate right) {
        if (left.getStartRow() != right.getStartRow()) {
          return Integer.compare(left.getStartRow(), right.getStartRow());
        }
        if (left.getEndRow() != right.getEndRow()) {
          return Integer.compare(left.getEndRow(), right.getEndRow());
        }
        return Integer.compare(left.getStartCol(), right.getStartCol());
      }
    });
    List<TableCandidate> cluster = new ArrayList<TableCandidate>();
    for (final TableCandidate candidate : sorted) {
      if (!isCalendarLikeColumn(candidate)) {
        flushCalendarCluster(cluster, dropKeys);
        cluster = new ArrayList<TableCandidate>();
        continue;
      }
      final TableCandidate previous = cluster.isEmpty() ? null : cluster.get(cluster.size() - 1);
      if (previous == null) {
        cluster.add(candidate);
        continue;
      }
      final boolean sameBand = candidate.getStartRow() == previous.getStartRow() && candidate.getEndRow() == previous.getEndRow();
      final int horizontalGap = candidate.getStartCol() - previous.getEndCol();
      if (sameBand && horizontalGap >= 1 && horizontalGap <= 2) {
        cluster.add(candidate);
      } else {
        flushCalendarCluster(cluster, dropKeys);
        cluster = new ArrayList<TableCandidate>();
        cluster.add(candidate);
      }
    }
    flushCalendarCluster(cluster, dropKeys);
    final List<TableCandidate> result = new ArrayList<TableCandidate>();
    for (final TableCandidate candidate : candidates == null ? Collections.<TableCandidate>emptyList() : candidates) {
      if (!dropKeys.contains(candidateKey(candidate))) {
        result.add(candidate);
      }
    }
    return result;
  }

  public static List<TableCandidate> detectTableCandidates(final WorksheetParser.ParsedSheet sheet) {
    return detectTableCandidates(sheet, DEFAULT_TABLE_SCORE_WEIGHTS, "balanced");
  }

  public static List<TableCandidate> detectTableCandidates(
      final WorksheetParser.ParsedSheet sheet,
      final TableScoreWeights scoreWeights,
      final String tableDetectionMode) {
    final String normalizedMode = MarkdownOptions.normalizeTableDetectionMode(tableDetectionMode);
    final boolean plannerAwareMode = "planner-aware".equals(normalizedMode);
    final Map<String, WorksheetParser.ParsedCell> cellMap = buildCellMap(sheet);
    final List<WorksheetParser.ParsedCell> allSeedCells = collectTableSeedCells(sheet);
    final List<WorksheetParser.ParsedCell> borderSeedCells = collectBorderSeedCells(sheet);
    final List<TableCandidate> candidates = new ArrayList<TableCandidate>();
    final Set<String> candidateKeys = new HashSet<String>();
    final TableScoreWeights weights = scoreWeights == null ? DEFAULT_TABLE_SCORE_WEIGHTS : scoreWeights;

    for (final List<WorksheetParser.ParsedCell> component : collectConnectedComponents(borderSeedCells, "border".equals(normalizedMode) ? "border" : "grid")) {
      maybePushCandidate(sheet, cellMap, component, "border", plannerAwareMode, weights, candidates, candidateKeys);
    }
    if (!"border".equals(normalizedMode)) {
      for (final List<WorksheetParser.ParsedCell> component : collectConnectedComponents(allSeedCells)) {
        final Bounds bounds = boundsFromComponent(component);
        final List<TableCandidate> containingBorderCandidates = new ArrayList<TableCandidate>();
        for (final TableCandidate candidate : candidates) {
          if (isWithinBounds(bounds, candidate)) {
            containingBorderCandidates.add(candidate);
          }
        }
        final int fallbackArea = getBoundsArea(bounds);
        boolean shadowedByBorderCandidate = false;
        for (final TableCandidate candidate : containingBorderCandidates) {
          if (getBoundsArea(candidate) >= fallbackArea * 0.4d) {
            shadowedByBorderCandidate = true;
            break;
          }
        }
        final boolean shadowedByMultipleBorderCandidates = containingBorderCandidates.size() >= 2
            && getCombinedCandidateArea(containingBorderCandidates) >= fallbackArea * 0.6d;
        if (shadowedByBorderCandidate || shadowedByMultipleBorderCandidates) {
          continue;
        }
        maybePushCandidate(sheet, cellMap, component, "fallback", plannerAwareMode, weights, candidates, candidateKeys);
      }
    }

    final List<TableCandidate> prunedCandidates = pruneRedundantCandidates(candidates);
    final List<TableCandidate> pruned = plannerAwareMode
        ? pruneCalendarLikeColumnCandidates(prunedCandidates)
        : prunedCandidates;
    Collections.sort(pruned, new Comparator<TableCandidate>() {
      @Override
      public int compare(final TableCandidate left, final TableCandidate right) {
        if (left.getStartRow() != right.getStartRow()) {
          return Integer.compare(left.getStartRow(), right.getStartRow());
        }
        return Integer.compare(left.getStartCol(), right.getStartCol());
      }
    });
    return pruned;
  }

  public static Bounds trimTableCandidateBounds(
      final Map<String, WorksheetParser.ParsedCell> cellMap,
      final Bounds bounds) {
    int startRow = bounds.getStartRow();
    final int startCol = bounds.getStartCol();
    int endRow = bounds.getEndRow();
    final int endCol = bounds.getEndCol();
    final int minBorderedCells = Math.max(2, (int) Math.ceil((endCol - startCol + 1) * 0.5d));

    while (endRow - startRow + 1 >= 2) {
      final BorderGrid.EdgeStats topStats = BorderGrid.collectTableEdgeStats(cellMap, startRow, startCol, endCol);
      final BorderGrid.EdgeStats nextStats = BorderGrid.collectTableEdgeStats(cellMap, startRow + 1, startCol, endCol);
      final boolean shouldTrimTop = topStats.getNonEmptyCount() <= 2
          && topStats.getRawBorderCount() == 0
          && nextStats.getBorderCount() >= minBorderedCells
          && nextStats.getNonEmptyCount() >= Math.max(2, (int) Math.ceil((endCol - startCol + 1) * 0.5d));
      if (!shouldTrimTop) {
        break;
      }
      startRow += 1;
    }

    for (int row = startRow + 1; row <= endRow; row += 1) {
      final BorderGrid.EdgeStats currentStats = BorderGrid.collectTableEdgeStats(cellMap, row, startCol, endCol);
      final BorderGrid.EdgeStats previousStats = BorderGrid.collectTableEdgeStats(cellMap, row - 1, startCol, endCol);
      final boolean shouldBreakAtCurrentRow = (previousStats.getBorderCount() >= minBorderedCells
          || previousStats.getBottomCount() >= minBorderedCells
          || currentStats.getTopCount() >= minBorderedCells)
          && currentStats.getRawBorderCount() == 0
          && currentStats.getNonEmptyCount() <= 1;
      if (shouldBreakAtCurrentRow) {
        endRow = row - 1;
        break;
      }
    }

    while (endRow - startRow + 1 >= 2) {
      final BorderGrid.EdgeStats bottomStats = BorderGrid.collectTableEdgeStats(cellMap, endRow, startCol, endCol);
      final BorderGrid.EdgeStats previousStats = BorderGrid.collectTableEdgeStats(cellMap, endRow - 1, startCol, endCol);
      final boolean shouldTrimBottom = ((previousStats.getBorderCount() >= minBorderedCells
          || previousStats.getBottomCount() >= minBorderedCells
          || bottomStats.getTopCount() >= minBorderedCells)
          && bottomStats.getRawBorderCount() == 0
          && bottomStats.getNonEmptyCount() <= 1)
          || (bottomStats.getNonEmptyCount() <= 1
              && bottomStats.getRawBorderCount() == 0
              && bottomStats.getMaxTextLength() >= 12
              && previousStats.getNonEmptyCount() >= Math.max(2, (int) Math.ceil((endCol - startCol + 1) * 0.5d)));
      if (!shouldTrimBottom) {
        break;
      }
      endRow -= 1;
    }
    return new Bounds(startRow, startCol, endRow, endCol);
  }

  public static List<List<String>> matrixFromCandidate(
      final WorksheetParser.ParsedSheet sheet,
      final Bounds candidate,
      final MarkdownOptions options,
      final CellFormatter formatter) {
    final MarkdownOptions.ResolvedMarkdownOptions resolvedOptions = MarkdownOptions.resolveMarkdownOptions(options);
    final Map<String, WorksheetParser.ParsedCell> cellMap = buildCellMap(sheet);
    final List<List<String>> rows = new ArrayList<List<String>>();
    for (int row = candidate.getStartRow(); row <= candidate.getEndRow(); row += 1) {
      final List<String> currentRow = new ArrayList<String>();
      for (int col = candidate.getStartCol(); col <= candidate.getEndCol(); col += 1) {
        final WorksheetParser.ParsedCell cell = cellMap.get(row + ":" + col);
        String value = formatter.formatCellForMarkdown(cell, options);
        if (resolvedOptions.isTrimText()) {
          value = value.trim();
        }
        currentRow.add(value);
      }
      rows.add(currentRow);
    }
    applyMergeTokens(rows, safeMerges(sheet), candidate.getStartRow(), candidate.getStartCol(), candidate.getEndRow(), candidate.getEndCol());

    List<List<String>> normalizedRows = rows;
    if (resolvedOptions.isRemoveEmptyRows()) {
      final List<List<String>> keptRows = new ArrayList<List<String>>();
      for (final List<String> row : normalizedRows) {
        boolean keep = false;
        for (final String cell : row) {
          if (isMeaningfulMarkdownCell(cell)) {
            keep = true;
            break;
          }
        }
        if (keep) {
          keptRows.add(row);
        }
      }
      normalizedRows = keptRows;
    }
    if (resolvedOptions.isRemoveEmptyColumns() && !normalizedRows.isEmpty()) {
      final boolean[] keepColumnFlags = new boolean[normalizedRows.get(0).size()];
      for (int colIndex = 0; colIndex < keepColumnFlags.length; colIndex += 1) {
        for (final List<String> row : normalizedRows) {
          if (isMeaningfulMarkdownCell(row.get(colIndex))) {
            keepColumnFlags[colIndex] = true;
            break;
          }
        }
      }
      final List<List<String>> trimmedRows = new ArrayList<List<String>>();
      for (final List<String> row : normalizedRows) {
        final List<String> trimmedRow = new ArrayList<String>();
        for (int colIndex = 0; colIndex < row.size(); colIndex += 1) {
          if (keepColumnFlags[colIndex]) {
            trimmedRow.add(row.get(colIndex));
          }
        }
        trimmedRows.add(trimmedRow);
      }
      normalizedRows = trimmedRows;
    }
    return normalizedRows;
  }

  public static boolean isMeaningfulMarkdownCell(final String value) {
    final String text = stringValue(value).trim();
    return !text.isEmpty() && !"[←M←]".equals(text) && !"[↑M↑]".equals(text);
  }

  public static void applyMergeTokens(
      final List<List<String>> matrix,
      final List<AddressUtils.MergeRange> merges,
      final int startRow,
      final int startCol,
      final int endRow,
      final int endCol) {
    for (final AddressUtils.MergeRange merge : merges == null ? Collections.<AddressUtils.MergeRange>emptyList() : merges) {
      if (merge.getEndRow() < startRow || merge.getStartRow() > endRow || merge.getEndCol() < startCol || merge.getStartCol() > endCol) {
        continue;
      }
      for (int row = merge.getStartRow(); row <= merge.getEndRow(); row += 1) {
        for (int col = merge.getStartCol(); col <= merge.getEndCol(); col += 1) {
          if (row == merge.getStartRow() && col == merge.getStartCol()) {
            continue;
          }
          final int matrixRow = row - startRow;
          final int matrixCol = col - startCol;
          if (matrixRow < 0 || matrixRow >= matrix.size()) {
            continue;
          }
          final List<String> rowValues = matrix.get(matrixRow);
          if (matrixCol < 0 || matrixCol >= rowValues.size()) {
            continue;
          }
          rowValues.set(matrixCol, row == merge.getStartRow() ? "[←M←]" : "[↑M↑]");
        }
      }
    }
  }

  private static void maybePushCandidate(
      final WorksheetParser.ParsedSheet sheet,
      final Map<String, WorksheetParser.ParsedCell> cellMap,
      final List<WorksheetParser.ParsedCell> component,
      final String sourceKind,
      final boolean plannerAwareMode,
      final TableScoreWeights scoreWeights,
      final List<TableCandidate> candidates,
      final Set<String> candidateKeys) {
    if (component == null || component.isEmpty()) {
      return;
    }
    final Bounds bounds = boundsFromComponent(component);
    final int area = getBoundsArea(bounds);
    final int rowCount = bounds.getEndRow() - bounds.getStartRow() + 1;
    final int colCount = bounds.getEndCol() - bounds.getStartCol() + 1;
    final List<WorksheetParser.ParsedCell> nonEmptyCells = nonEmptyCells(component);
    final double density = nonEmptyCells.size() / (double) area;
    final int sparseRowCount = countSparseRows(component, bounds.getStartRow(), bounds.getEndRow());
    if (rowCount < 2 || colCount < 2) {
      return;
    }

    int score = 0;
    final List<String> reasons = new ArrayList<String>();
    final int normalizedBorderedCellCount = BorderGrid.countNormalizedBorderedCells(
        cellMap, bounds.getStartRow(), bounds.getStartCol(), bounds.getEndRow(), bounds.getEndCol());
    score += scoreWeights.getMinGrid();
    reasons.add("At least 2x2 (+" + scoreWeights.getMinGrid() + ")");
    if (normalizedBorderedCellCount >= Math.max(2, (int) Math.ceil(component.size() * 0.3d))) {
      score += scoreWeights.getBorderPresence();
      reasons.add("Has borders (+" + scoreWeights.getBorderPresence() + ")");
    }
    if (density >= 0.55d) {
      score += scoreWeights.getDensityHigh();
      reasons.add("High density (+" + scoreWeights.getDensityHigh() + ")");
    }
    if (density >= 0.8d) {
      score += scoreWeights.getDensityVeryHigh();
      reasons.add("Very high density (+" + scoreWeights.getDensityVeryHigh() + ")");
    }
    final int headerishCount = countHeaderishFirstRowCells(component, bounds.getStartRow());
    if (headerishCount >= 2) {
      score += scoreWeights.getHeaderish();
      reasons.add("Header-like first row (+" + scoreWeights.getHeaderish() + ")");
    }

    final int mergedArea = countIntersectingMerges(safeMerges(sheet), bounds);
    if (mergedArea >= Math.max(2, (int) Math.ceil(area * 0.08d))) {
      score += scoreWeights.getMergeHeavyPenalty();
      reasons.add("Many merged cells (" + scoreWeights.getMergeHeavyPenalty() + ")");
    }

    if ("border".equals(sourceKind)) {
      final boolean looksLikeTinyMergedLabelStub = rowCount <= 2
          && colCount <= 2
          && mergedArea >= 1
          && nonEmptyCells.size() <= 2
          && headerishCount <= 1;
      if (looksLikeTinyMergedLabelStub) {
        return;
      }
      if (mergedArea >= 2 && density < 0.25d && headerishCount < 2) {
        return;
      }
      if (plannerAwareMode) {
        final boolean looksLikeWideSparseMergeForm = colCount >= 8
            && rowCount >= 4
            && density < 0.45d
            && mergedArea >= Math.max(4, rowCount - 1)
            && sparseRowCount >= Math.ceil(rowCount * 0.7d);
        if (looksLikeWideSparseMergeForm) {
          return;
        }
      }
    } else {
      if (mergedArea >= 2 && rowCount <= 6 && colCount >= 10 && density < 0.25d) {
        return;
      }
      if (plannerAwareMode) {
        final boolean looksLikeMixedLayoutSheet = rowCount >= 20
            && colCount >= 8
            && mergedArea >= 4
            && sparseRowCount >= 4
            && density < 0.8d;
        if (looksLikeMixedLayoutSheet) {
          return;
        }
      }
    }

    final double avgTextLength = averageTextLength(nonEmptyCells);
    if (avgTextLength > 36d && density < 0.7d) {
      score += scoreWeights.getProsePenalty();
      reasons.add("Mostly long prose (" + scoreWeights.getProsePenalty() + ")");
    }

    if (score >= scoreWeights.getThreshold()) {
      final Bounds normalizedBounds = trimTableCandidateBounds(cellMap, bounds);
      if (normalizedBounds.getEndRow() - normalizedBounds.getStartRow() + 1 < 2
          || normalizedBounds.getEndCol() - normalizedBounds.getStartCol() + 1 < 2) {
        return;
      }
      final String key = candidateKey(normalizedBounds);
      if (candidateKeys.contains(key)) {
        return;
      }
      candidateKeys.add(key);
      candidates.add(new TableCandidate(
          normalizedBounds.getStartRow(),
          normalizedBounds.getStartCol(),
          normalizedBounds.getEndRow(),
          normalizedBounds.getEndCol(),
          score,
          reasons));
    }
  }

  private static Bounds boundsFromComponent(final List<WorksheetParser.ParsedCell> component) {
    int startRow = Integer.MAX_VALUE;
    int startCol = Integer.MAX_VALUE;
    int endRow = 0;
    int endCol = 0;
    for (final WorksheetParser.ParsedCell cell : component == null ? Collections.<WorksheetParser.ParsedCell>emptyList() : component) {
      startRow = Math.min(startRow, cell.getRow());
      startCol = Math.min(startCol, cell.getCol());
      endRow = Math.max(endRow, cell.getRow());
      endCol = Math.max(endCol, cell.getCol());
    }
    return new Bounds(startRow, startCol, endRow, endCol);
  }

  private static int countSparseRows(final List<WorksheetParser.ParsedCell> component, final int startRow, final int endRow) {
    int sparseRows = 0;
    for (int row = startRow; row <= endRow; row += 1) {
      int nonEmptyCount = 0;
      for (final WorksheetParser.ParsedCell entry : component) {
        if (entry.getRow() == row && !stringValue(entry.getOutputValue()).trim().isEmpty()) {
          nonEmptyCount += 1;
        }
      }
      if (nonEmptyCount <= 2) {
        sparseRows += 1;
      }
    }
    return sparseRows;
  }

  private static int countHeaderishFirstRowCells(final List<WorksheetParser.ParsedCell> component, final int startRow) {
    int count = 0;
    for (final WorksheetParser.ParsedCell entry : component) {
      if (entry.getRow() != startRow) {
        continue;
      }
      final String value = stringValue(entry.getOutputValue()).trim();
      if (!value.isEmpty() && value.length() <= 24 && !value.matches("^\\d+(?:\\.\\d+)?$")) {
        count += 1;
      }
    }
    return count;
  }

  private static int countIntersectingMerges(final List<AddressUtils.MergeRange> merges, final Bounds bounds) {
    int count = 0;
    for (final AddressUtils.MergeRange merge : merges == null ? Collections.<AddressUtils.MergeRange>emptyList() : merges) {
      if (!(merge.getEndRow() < bounds.getStartRow()
          || merge.getStartRow() > bounds.getEndRow()
          || merge.getEndCol() < bounds.getStartCol()
          || merge.getStartCol() > bounds.getEndCol())) {
        count += 1;
      }
    }
    return count;
  }

  private static List<WorksheetParser.ParsedCell> nonEmptyCells(final List<WorksheetParser.ParsedCell> component) {
    final List<WorksheetParser.ParsedCell> result = new ArrayList<WorksheetParser.ParsedCell>();
    for (final WorksheetParser.ParsedCell entry : component == null ? Collections.<WorksheetParser.ParsedCell>emptyList() : component) {
      if (!stringValue(entry.getOutputValue()).trim().isEmpty()) {
        result.add(entry);
      }
    }
    return result;
  }

  private static double averageTextLength(final List<WorksheetParser.ParsedCell> cells) {
    int sum = 0;
    for (final WorksheetParser.ParsedCell entry : cells == null ? Collections.<WorksheetParser.ParsedCell>emptyList() : cells) {
      sum += stringValue(entry.getOutputValue()).trim().length();
    }
    return sum / (double) Math.max(1, cells == null ? 0 : cells.size());
  }

  private static boolean isCalendarLikeColumn(final TableCandidate candidate) {
    final int rowCount = candidate.getEndRow() - candidate.getStartRow() + 1;
    final int colCount = candidate.getEndCol() - candidate.getStartCol() + 1;
    return rowCount >= 4 && colCount <= 3;
  }

  private static void flushCalendarCluster(final List<TableCandidate> cluster, final Set<String> dropKeys) {
    if (cluster.size() < 3) {
      return;
    }
    for (final TableCandidate candidate : cluster) {
      dropKeys.add(candidateKey(candidate));
    }
  }

  private static String candidateKey(final Bounds candidate) {
    return candidate.getStartRow() + ":" + candidate.getStartCol() + ":" + candidate.getEndRow() + ":" + candidate.getEndCol();
  }

  private static List<WorksheetParser.ParsedCell> safeCells(final WorksheetParser.ParsedSheet sheet) {
    return sheet == null || sheet.getCells() == null ? Collections.<WorksheetParser.ParsedCell>emptyList() : sheet.getCells();
  }

  private static List<AddressUtils.MergeRange> safeMerges(final WorksheetParser.ParsedSheet sheet) {
    return sheet == null || sheet.getMerges() == null ? Collections.<AddressUtils.MergeRange>emptyList() : sheet.getMerges();
  }

  private static String stringValue(final String value) {
    return value == null ? "" : value;
  }

  public interface CellFormatter {
    String formatCellForMarkdown(WorksheetParser.ParsedCell cell, MarkdownOptions options);
  }

  public static class Bounds {
    private final int startRow;
    private final int startCol;
    private final int endRow;
    private final int endCol;

    public Bounds(final int startRow, final int startCol, final int endRow, final int endCol) {
      this.startRow = startRow;
      this.startCol = startCol;
      this.endRow = endRow;
      this.endCol = endCol;
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

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof Bounds)) {
        return false;
      }
      final Bounds that = (Bounds) other;
      return startRow == that.startRow && startCol == that.startCol && endRow == that.endRow && endCol == that.endCol;
    }

    @Override
    public int hashCode() {
      return Objects.hash(Integer.valueOf(startRow), Integer.valueOf(startCol), Integer.valueOf(endRow), Integer.valueOf(endCol));
    }
  }

  public static final class TableCandidate extends Bounds {
    private final int score;
    private final List<String> reasonSummary;

    public TableCandidate(
        final int startRow,
        final int startCol,
        final int endRow,
        final int endCol,
        final int score,
        final List<String> reasonSummary) {
      super(startRow, startCol, endRow, endCol);
      this.score = score;
      this.reasonSummary = reasonSummary == null ? Collections.<String>emptyList() : reasonSummary;
    }

    public int getScore() {
      return score;
    }

    public List<String> getReasonSummary() {
      return reasonSummary;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof TableCandidate)) {
        return false;
      }
      final TableCandidate that = (TableCandidate) other;
      return super.equals(other) && score == that.score && Objects.equals(reasonSummary, that.reasonSummary);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), Integer.valueOf(score), reasonSummary);
    }
  }

  public static final class TableScoreWeights {
    private final int minGrid;
    private final int borderPresence;
    private final int densityHigh;
    private final int densityVeryHigh;
    private final int headerish;
    private final int mergeHeavyPenalty;
    private final int prosePenalty;
    private final int threshold;

    public TableScoreWeights(
        final int minGrid,
        final int borderPresence,
        final int densityHigh,
        final int densityVeryHigh,
        final int headerish,
        final int mergeHeavyPenalty,
        final int prosePenalty,
        final int threshold) {
      this.minGrid = minGrid;
      this.borderPresence = borderPresence;
      this.densityHigh = densityHigh;
      this.densityVeryHigh = densityVeryHigh;
      this.headerish = headerish;
      this.mergeHeavyPenalty = mergeHeavyPenalty;
      this.prosePenalty = prosePenalty;
      this.threshold = threshold;
    }

    public int getMinGrid() {
      return minGrid;
    }

    public int getBorderPresence() {
      return borderPresence;
    }

    public int getDensityHigh() {
      return densityHigh;
    }

    public int getDensityVeryHigh() {
      return densityVeryHigh;
    }

    public int getHeaderish() {
      return headerish;
    }

    public int getMergeHeavyPenalty() {
      return mergeHeavyPenalty;
    }

    public int getProsePenalty() {
      return prosePenalty;
    }

    public int getThreshold() {
      return threshold;
    }
  }
}
