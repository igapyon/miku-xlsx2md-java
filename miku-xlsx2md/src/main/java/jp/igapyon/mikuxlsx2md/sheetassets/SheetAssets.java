/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sheetassets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

public final class SheetAssets {
  private SheetAssets() {
  }

  public static String createSafeSheetAssetDir(final String sheetName) {
    final String safe = stringValue(sheetName).replaceAll("[\\\\/:*?\"<>|]+", "_").trim();
    return safe.isEmpty() ? "Sheet" : safe;
  }

  public static String renderImageSection(final WorksheetParser.ParsedSheet sheet) {
    final List<String> entries = new ArrayList<String>();
    int index = 0;
    for (final WorksheetParser.ParsedImageAsset image : safeImages(sheet)) {
      entries.add(joinLines(asList(
          "### Image: " + leftPad(index + 1, 3) + " (" + image.getAnchor() + ")",
          "- File: " + image.getPath(),
          "",
          "![" + image.getFilename() + "](" + image.getPath() + ")")));
      index += 1;
    }
    return entries.isEmpty() ? "" : "\n\n" + joinParagraphs(entries);
  }

  public static String renderChartSection(final List<WorksheetParser.ParsedChartAsset> charts) {
    final List<String> entries = new ArrayList<String>();
    int index = 0;
    for (final WorksheetParser.ParsedChartAsset chart : charts == null ? Collections.<WorksheetParser.ParsedChartAsset>emptyList() : charts) {
      final List<String> lines = new ArrayList<String>();
      lines.add("### Chart: " + leftPad(index + 1, 3) + " (" + chart.getAnchor() + ")");
      lines.add("- Title: " + (stringValue(chart.getTitle()).isEmpty() ? "(none)" : chart.getTitle()));
      lines.add("- Type: " + chart.getChartType());
      if (chart.getSeries() != null && !chart.getSeries().isEmpty()) {
        lines.add("- Series:");
        for (final WorksheetParser.ParsedChartSeries series : chart.getSeries()) {
          lines.add("  - " + series.getName());
          if ("secondary".equals(series.getAxis())) {
            lines.add("    - Axis: secondary");
          }
          if (!stringValue(series.getCategoriesRef()).isEmpty()) {
            lines.add("    - categories: " + series.getCategoriesRef());
          }
          if (!stringValue(series.getValuesRef()).isEmpty()) {
            lines.add("    - values: " + series.getValuesRef());
          }
        }
      }
      entries.add(joinLines(lines));
      index += 1;
    }
    return entries.isEmpty() ? "" : "\n\n" + joinParagraphs(entries);
  }

  public static String renderShapeSection(
      final List<WorksheetParser.ParsedShapeAsset> shapes,
      final boolean includeShapeDetails) {
    if (!includeShapeDetails || shapes == null || shapes.isEmpty()) {
      return "";
    }
    final List<String> entries = new ArrayList<String>();
    int index = 0;
    for (final WorksheetParser.ParsedShapeAsset shape : shapes) {
      final List<String> lines = new ArrayList<String>();
      lines.add("#### Shape: " + leftPad(index + 1, 3) + " (" + shape.getAnchor() + ")");
      lines.addAll(renderHierarchicalRawEntries(shape.getRawEntries()));
      if (shape.getSvgPath() != null) {
        lines.add("- SVG: " + shape.getSvgPath());
        lines.add("");
        lines.add("![" + (shape.getSvgFilename() == null ? "shape_" + leftPad(index + 1, 3) + ".svg" : shape.getSvgFilename()) + "](" + shape.getSvgPath() + ")");
      }
      entries.add(joinLines(lines));
      index += 1;
    }
    return "\n\n" + joinParagraphs(entries);
  }

  public static List<String> renderHierarchicalRawEntries(final List<WorksheetParser.ParsedShapeRawEntry> entries) {
    final RawTreeNode root = new RawTreeNode();
    for (final WorksheetParser.ParsedShapeRawEntry entry : entries == null ? Collections.<WorksheetParser.ParsedShapeRawEntry>emptyList() : entries) {
      final String[] rawParts = stringValue(entry.getKey()).split("/");
      RawTreeNode current = root;
      for (final String part : rawParts) {
        if (part.isEmpty()) {
          continue;
        }
        RawTreeNode child = current.children.get(part);
        if (child == null) {
          child = new RawTreeNode();
          current.children.put(part, child);
        }
        current = child;
      }
      current.value = entry.getValue();
    }
    final List<String> lines = new ArrayList<String>();
    visitRawTree(root, 0, lines);
    return lines;
  }

  public static List<ShapeBlock> extractShapeBlocks(final List<ShapeBox> shapes, final ShapeBlockOptions options) {
    final ShapeBlockOptions resolved = options == null ? new ShapeBlockOptions(1, 1, 0, 0) : options;
    final List<ShapeBox> safeShapes = shapes == null ? Collections.<ShapeBox>emptyList() : shapes;
    if (safeShapes.isEmpty()) {
      return Collections.emptyList();
    }
    final boolean[] visited = new boolean[safeShapes.size()];
    final List<ShapeBlock> blocks = new ArrayList<ShapeBlock>();
    for (int index = 0; index < safeShapes.size(); index += 1) {
      if (visited[index]) {
        continue;
      }
      final List<Integer> queue = new ArrayList<Integer>();
      final List<Integer> shapeIndexes = new ArrayList<Integer>();
      queue.add(Integer.valueOf(index));
      visited[index] = true;
      while (!queue.isEmpty()) {
        final int currentIndex = queue.remove(0).intValue();
        shapeIndexes.add(Integer.valueOf(currentIndex));
        final ShapeBox current = safeShapes.get(currentIndex);
        for (int otherIndex = 0; otherIndex < safeShapes.size(); otherIndex += 1) {
          if (visited[otherIndex]) {
            continue;
          }
          final ShapeBox other = safeShapes.get(otherIndex);
          final Gap gap = bboxGap(current.getBbox(), other.getBbox());
          if (gap.dx <= resolved.getShapeBlockGapXEmu() && gap.dy <= resolved.getShapeBlockGapYEmu()) {
            visited[otherIndex] = true;
            queue.add(Integer.valueOf(otherIndex));
          }
        }
      }
      Collections.sort(shapeIndexes);
      blocks.add(createShapeBlock(safeShapes, shapeIndexes, resolved));
    }
    Collections.sort(blocks);
    return blocks;
  }

  private static ShapeBlock createShapeBlock(
      final List<ShapeBox> shapes,
      final List<Integer> shapeIndexes,
      final ShapeBlockOptions options) {
    long minLeft = Long.MAX_VALUE;
    long minTop = Long.MAX_VALUE;
    long maxRight = 0L;
    long maxBottom = 0L;
    for (final Integer index : shapeIndexes) {
      final BoundingBox bbox = shapes.get(index.intValue()).getBbox();
      minLeft = Math.min(minLeft, bbox.getLeft());
      minTop = Math.min(minTop, bbox.getTop());
      maxRight = Math.max(maxRight, bbox.getRight());
      maxBottom = Math.max(maxBottom, bbox.getBottom());
    }
    return new ShapeBlock(
        (int) Math.floor(minTop / (double) options.getDefaultCellHeightEmu()) + 1,
        (int) Math.floor(minLeft / (double) options.getDefaultCellWidthEmu()) + 1,
        (int) Math.floor(maxBottom / (double) options.getDefaultCellHeightEmu()) + 1,
        (int) Math.floor(maxRight / (double) options.getDefaultCellWidthEmu()) + 1,
        shapeIndexes);
  }

  private static Gap bboxGap(final BoundingBox left, final BoundingBox right) {
    final long dx = left.getRight() < right.getLeft()
        ? right.getLeft() - left.getRight()
        : (right.getRight() < left.getLeft() ? left.getLeft() - right.getRight() : 0L);
    final long dy = left.getBottom() < right.getTop()
        ? right.getTop() - left.getBottom()
        : (right.getBottom() < left.getTop() ? left.getTop() - right.getBottom() : 0L);
    return new Gap(dx, dy);
  }

  private static void visitRawTree(final RawTreeNode node, final int depth, final List<String> lines) {
    for (final Map.Entry<String, RawTreeNode> entry : node.children.entrySet()) {
      final String indent = repeat(" ", depth * 4);
      if (entry.getValue().value != null) {
        lines.add(indent + "- `" + entry.getKey() + "`: `" + entry.getValue().value + "`");
      } else {
        lines.add(indent + "- `" + entry.getKey() + "`");
      }
      visitRawTree(entry.getValue(), depth + 1, lines);
    }
  }

  private static List<WorksheetParser.ParsedImageAsset> safeImages(final WorksheetParser.ParsedSheet sheet) {
    return sheet == null || sheet.getImages() == null ? Collections.<WorksheetParser.ParsedImageAsset>emptyList() : sheet.getImages();
  }

  private static String joinLines(final List<String> values) {
    return join(values, "\n");
  }

  private static String joinParagraphs(final List<String> values) {
    return join(values, "\n\n");
  }

  private static String join(final List<String> values, final String delimiter) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < values.size(); index += 1) {
      if (index > 0) {
        builder.append(delimiter);
      }
      builder.append(values.get(index));
    }
    return builder.toString();
  }

  private static List<String> asList(final String... values) {
    final List<String> result = new ArrayList<String>();
    Collections.addAll(result, values);
    return result;
  }

  private static String leftPad(final int value, final int width) {
    final String text = String.valueOf(value);
    if (text.length() >= width) {
      return text;
    }
    final StringBuilder builder = new StringBuilder();
    for (int index = text.length(); index < width; index += 1) {
      builder.append('0');
    }
    return builder.append(text).toString();
  }

  private static String repeat(final String text, final int count) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < count; index += 1) {
      builder.append(text);
    }
    return builder.toString();
  }

  private static String stringValue(final String value) {
    return value == null ? "" : value;
  }

  private static final class RawTreeNode {
    private final Map<String, RawTreeNode> children = new LinkedHashMap<String, RawTreeNode>();
    private String value;
  }

  private static final class Gap {
    private final long dx;
    private final long dy;

    private Gap(final long dx, final long dy) {
      this.dx = dx;
      this.dy = dy;
    }
  }

  public static final class BoundingBox {
    private final long left;
    private final long top;
    private final long right;
    private final long bottom;

    public BoundingBox(final long left, final long top, final long right, final long bottom) {
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
    }

    public long getLeft() {
      return left;
    }

    public long getTop() {
      return top;
    }

    public long getRight() {
      return right;
    }

    public long getBottom() {
      return bottom;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof BoundingBox)) {
        return false;
      }
      final BoundingBox that = (BoundingBox) other;
      return left == that.left && top == that.top && right == that.right && bottom == that.bottom;
    }

    @Override
    public int hashCode() {
      return Objects.hash(Long.valueOf(left), Long.valueOf(top), Long.valueOf(right), Long.valueOf(bottom));
    }
  }

  public static final class ShapeBox {
    private final BoundingBox bbox;

    public ShapeBox(final BoundingBox bbox) {
      this.bbox = bbox;
    }

    public BoundingBox getBbox() {
      return bbox;
    }
  }

  public static final class ShapeBlockOptions {
    private final long defaultCellWidthEmu;
    private final long defaultCellHeightEmu;
    private final long shapeBlockGapXEmu;
    private final long shapeBlockGapYEmu;

    public ShapeBlockOptions(
        final long defaultCellWidthEmu,
        final long defaultCellHeightEmu,
        final long shapeBlockGapXEmu,
        final long shapeBlockGapYEmu) {
      this.defaultCellWidthEmu = defaultCellWidthEmu;
      this.defaultCellHeightEmu = defaultCellHeightEmu;
      this.shapeBlockGapXEmu = shapeBlockGapXEmu;
      this.shapeBlockGapYEmu = shapeBlockGapYEmu;
    }

    public long getDefaultCellWidthEmu() {
      return defaultCellWidthEmu;
    }

    public long getDefaultCellHeightEmu() {
      return defaultCellHeightEmu;
    }

    public long getShapeBlockGapXEmu() {
      return shapeBlockGapXEmu;
    }

    public long getShapeBlockGapYEmu() {
      return shapeBlockGapYEmu;
    }
  }

  public static final class ShapeBlock implements Comparable<ShapeBlock> {
    private final int startRow;
    private final int startCol;
    private final int endRow;
    private final int endCol;
    private final List<Integer> shapeIndexes;

    public ShapeBlock(
        final int startRow,
        final int startCol,
        final int endRow,
        final int endCol,
        final List<Integer> shapeIndexes) {
      this.startRow = startRow;
      this.startCol = startCol;
      this.endRow = endRow;
      this.endCol = endCol;
      this.shapeIndexes = shapeIndexes == null ? Collections.<Integer>emptyList() : shapeIndexes;
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

    public List<Integer> getShapeIndexes() {
      return shapeIndexes;
    }

    @Override
    public int compareTo(final ShapeBlock other) {
      if (startRow != other.startRow) {
        return Integer.compare(startRow, other.startRow);
      }
      return Integer.compare(startCol, other.startCol);
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ShapeBlock)) {
        return false;
      }
      final ShapeBlock that = (ShapeBlock) other;
      return startRow == that.startRow
          && startCol == that.startCol
          && endRow == that.endRow
          && endCol == that.endCol
          && Objects.equals(shapeIndexes, that.shapeIndexes);
    }

    @Override
    public int hashCode() {
      return Objects.hash(Integer.valueOf(startRow), Integer.valueOf(startCol), Integer.valueOf(endRow), Integer.valueOf(endCol), shapeIndexes);
    }
  }
}
