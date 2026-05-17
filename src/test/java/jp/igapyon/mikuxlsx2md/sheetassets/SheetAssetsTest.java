/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sheetassets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  @Test
  void rendersUngroupedShapesAfterGroupedShapeBlocks() {
    final List<WorksheetParser.ParsedShapeAsset> shapes = Arrays.asList(
        new WorksheetParser.ParsedShapeAsset(
            "B3",
            Arrays.asList(new WorksheetParser.ParsedShapeRawEntry("kind", "rect")),
            null,
            null,
            null),
        new WorksheetParser.ParsedShapeAsset(
            "E8",
            Arrays.asList(new WorksheetParser.ParsedShapeRawEntry("kind", "rect")),
            "shape_002.svg",
            "assets/Sheet1/shape_002.svg",
            new byte[] {2}));
    final List<SheetAssets.ShapeBlock> shapeBlocks = Arrays.asList(
        new SheetAssets.ShapeBlock(3, 2, 3, 2, Arrays.asList(Integer.valueOf(0))));

    final String section = SheetAssets.renderShapeSection(shapes, shapeBlocks, true);

    assertTrue(section.contains("### Shape Block: 001 (B3-B3)"));
    assertTrue(section.contains("- Shapes: Shape 001"));
    assertTrue(section.contains("- anchorRange: B3-B3"));
    assertTrue(section.contains("### Ungrouped Shapes"));
    assertTrue(section.contains("#### Shape: 002 (E8)"));
    assertTrue(section.contains("![shape_002.svg](assets/Sheet1/shape_002.svg)"));
  }

  @Test
  void parsesDrawingImagesChartsAndShapes() {
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/_rels/sheet1.xml.rels", bytes(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rIdDrawing\" Target=\"../drawings/drawing1.xml\"/>"
            + "</Relationships>"));
    files.put("xl/drawings/_rels/drawing1.xml.rels", bytes(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rIdImage\" Target=\"../media/image1.png\"/>"
            + "<Relationship Id=\"rIdChart\" Target=\"../charts/chart1.xml\"/>"
            + "</Relationships>"));
    files.put("xl/media/image1.png", new byte[] {9, 8, 7});
    files.put("xl/charts/chart1.xml", bytes(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<c:chartSpace xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">"
            + "<c:chart><c:title><c:tx><c:rich><a:p xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"><a:r><a:t>Quarterly</a:t></a:r></a:p></c:rich></c:tx></c:title>"
            + "<c:plotArea><c:barChart><c:axId val=\"10\"/><c:ser>"
            + "<c:tx><c:v>Sales</c:v></c:tx>"
            + "<c:cat><c:strRef><c:f>Sheet1!$A$1:$A$2</c:f></c:strRef></c:cat>"
            + "<c:val><c:numRef><c:f>Sheet1!$B$1:$B$2</c:f></c:numRef></c:val>"
            + "</c:ser></c:barChart><c:valAx><c:axId val=\"10\"/><c:axPos val=\"l\"/></c:valAx></c:plotArea></c:chart></c:chartSpace>"));
    files.put("xl/drawings/drawing1.xml", bytes(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<xdr:wsDr xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" "
            + "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
            + "xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">"
            + "<xdr:oneCellAnchor><xdr:from><xdr:col>1</xdr:col><xdr:row>2</xdr:row></xdr:from><xdr:pic><xdr:blipFill><a:blip r:embed=\"rIdImage\"/></xdr:blipFill></xdr:pic></xdr:oneCellAnchor>"
            + "<xdr:oneCellAnchor><xdr:from><xdr:col>2</xdr:col><xdr:row>3</xdr:row></xdr:from><xdr:graphicFrame><a:graphic><a:graphicData><c:chart r:id=\"rIdChart\"/></a:graphicData></a:graphic></xdr:graphicFrame></xdr:oneCellAnchor>"
            + "<xdr:twoCellAnchor><xdr:from><xdr:col>3</xdr:col><xdr:row>4</xdr:row></xdr:from><xdr:to><xdr:col>4</xdr:col><xdr:row>5</xdr:row></xdr:to>"
            + "<xdr:sp><xdr:nvSpPr><xdr:cNvPr name=\"Box 1\"/><xdr:cNvSpPr txBox=\"1\"/></xdr:nvSpPr><xdr:spPr><a:prstGeom prst=\"rect\"/></xdr:spPr><xdr:txBody><a:p><a:r><a:t>Hello shape</a:t></a:r></a:p></xdr:txBody></xdr:sp></xdr:twoCellAnchor>"
            + "</xdr:wsDr>"));

    final List<WorksheetParser.ParsedImageAsset> images = SheetAssets.parseDrawingImages(files, "Asset Sheet", "xl/worksheets/sheet1.xml");
    final List<WorksheetParser.ParsedChartAsset> charts = SheetAssets.parseDrawingCharts(files, "Asset Sheet", "xl/worksheets/sheet1.xml");
    final List<WorksheetParser.ParsedShapeAsset> shapes = SheetAssets.parseDrawingShapes(files, "Asset Sheet", "xl/worksheets/sheet1.xml");

    assertEquals(1, images.size());
    assertEquals("B3", images.get(0).getAnchor());
    assertEquals("image_001.png", images.get(0).getFilename());
    assertEquals("xl/media/image1.png", images.get(0).getMediaPath());
    assertEquals(1, charts.size());
    assertEquals("C4", charts.get(0).getAnchor());
    assertEquals("Bar Chart", charts.get(0).getChartType());
    assertEquals("Sheet1!$B$1:$B$2", charts.get(0).getSeries().get(0).getValuesRef());
    assertEquals(1, shapes.size());
    assertEquals("D5", shapes.get(0).getAnchor());
    assertEquals("Box 1", shapes.get(0).getName());
    assertEquals("Text Box", shapes.get(0).getKind());
    assertEquals("Hello shape", shapes.get(0).getText());
    assertEquals("shape_001.svg", shapes.get(0).getSvgFilename());
    assertEquals("assets/Asset Sheet/shape_001.svg", shapes.get(0).getSvgPath());
    assertTrue(new String(shapes.get(0).getSvgData(), StandardCharsets.UTF_8).contains("Hello shape"));
    assertEquals(new SheetAssets.BoundingBox(2743200L, 762000L, 3657600L, 952500L), shapes.get(0).getBbox());
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

  private static byte[] bytes(final String text) {
    return text.getBytes(StandardCharsets.UTF_8);
  }
}
