/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sheetassets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

class SheetAssetsTest {
  @Test
  void sanitizesSheetAssetDirectories() {
    assertEquals("A_B_Sheet", SheetAssets.createSafeSheetAssetDir("A/B:Sheet"));
    assertEquals("Sheet", SheetAssets.createSafeSheetAssetDir("  "));
  }

  @Test
  void rendersHierarchicalRawEntries() {
    final List<String> lines = SheetAssets.renderHierarchicalRawEntries(Arrays.asList(
        new WorksheetParser.ParsedShapeRawEntry("xdr:sp/xdr:nvSpPr/xdr:cNvPr@name", "Rectangle 1"),
        new WorksheetParser.ParsedShapeRawEntry("xdr:sp/xdr:txBody/a:p/a:r/a:t#text", "Hello")));
    final String rendered = join(lines, "\n");

    assertTrue(rendered.contains("- `xdr:sp`"));
    assertTrue(rendered.contains("`xdr:cNvPr@name`: `Rectangle 1`"));
    assertTrue(rendered.contains("`a:t#text`: `Hello`"));
  }

  @Test
  void groupsNearbyShapesIntoShapeBlocks() {
    final List<SheetAssets.ShapeBlock> blocks = SheetAssets.extractShapeBlocks(Arrays.asList(
        new SheetAssets.ShapeBox(new SheetAssets.BoundingBox(0, 0, 100, 20)),
        new SheetAssets.ShapeBox(new SheetAssets.BoundingBox(140, 0, 240, 20)),
        new SheetAssets.ShapeBox(new SheetAssets.BoundingBox(1000, 400, 1100, 420))),
        new SheetAssets.ShapeBlockOptions(100, 20, 100, 40));

    assertEquals(2, blocks.size());
    assertEquals(Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)), blocks.get(0).getShapeIndexes());
    assertEquals(Arrays.asList(Integer.valueOf(2)), blocks.get(1).getShapeIndexes());
  }

  @Test
  void rendersImageChartAndShapeSections() {
    final WorksheetParser.ParsedSheet sheet = new WorksheetParser.ParsedSheet(
        "Assets",
        1,
        "xl/worksheets/sheet1.xml",
        Collections.<WorksheetParser.ParsedCell>emptyList(),
        Collections.<AddressUtils.MergeRange>emptyList(),
        Arrays.asList(new WorksheetParser.ParsedImageAsset("B3", "image_001.png", "assets/Assets/image_001.png", new byte[] {1, 2, 3})),
        Arrays.asList(new WorksheetParser.ParsedChartAsset(
            "C4",
            "",
            "Bar Chart",
            Arrays.asList(new WorksheetParser.ParsedChartSeries("Sales", "Sheet1!$A$1:$A$3", "Sheet1!$B$1:$B$3", "secondary")))),
        Arrays.asList(new WorksheetParser.ParsedShapeAsset(
            "D5",
            Arrays.asList(new WorksheetParser.ParsedShapeRawEntry("xdr:sp/xdr:cNvPr@name", "Shape 1")),
            "shape_001.svg",
            "assets/Assets/shape_001.svg",
            new byte[] {4, 5, 6})),
        10,
        5);

    assertTrue(SheetAssets.renderImageSection(sheet).contains("![image_001.png](assets/Assets/image_001.png)"));
    assertTrue(SheetAssets.renderChartSection(sheet.getCharts()).contains("- Title: (none)"));
    assertTrue(SheetAssets.renderChartSection(sheet.getCharts()).contains("    - Axis: secondary"));
    assertTrue(SheetAssets.renderShapeSection(sheet.getShapes(), true).contains("![shape_001.svg](assets/Assets/shape_001.svg)"));
    assertEquals("", SheetAssets.renderShapeSection(sheet.getShapes(), false));
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
}
