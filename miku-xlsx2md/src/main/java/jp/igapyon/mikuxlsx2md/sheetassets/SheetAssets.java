/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sheetassets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.officedrawing.OfficeDrawing;
import jp.igapyon.mikuxlsx2md.relsparser.RelsParser;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;
import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

public final class SheetAssets {
  private SheetAssets() {
  }

  public static String createSafeSheetAssetDir(final String sheetName) {
    final String safe = stringValue(sheetName).replaceAll("[\\\\/:*?\"<>|]+", "_").trim();
    return safe.isEmpty() ? "Sheet" : safe;
  }

  public static List<WorksheetParser.ParsedImageAsset> parseDrawingImages(
      final Map<String, byte[]> files,
      final String sheetName,
      final String sheetPath) {
    final Map<String, String> sheetRels = RelsParser.parseRelationships(files, RelsParser.buildRelsPath(sheetPath), sheetPath);
    final List<WorksheetParser.ParsedImageAsset> images = new ArrayList<WorksheetParser.ParsedImageAsset>();
    int imageCounter = 1;
    for (final String drawingPath : sheetRels.values()) {
      if (!isDrawingXmlPath(drawingPath)) {
        continue;
      }
      final byte[] drawingBytes = files.get(drawingPath);
      if (drawingBytes == null) {
        continue;
      }
      final Document drawingDoc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(drawingBytes));
      final Map<String, String> drawingRels = RelsParser.parseRelationships(files, RelsParser.buildRelsPath(drawingPath), drawingPath);
      for (final Element anchor : drawingAnchors(drawingDoc)) {
        final AnchorPoint point = parseAnchorPoint(anchor);
        if (point == null) {
          continue;
        }
        final List<Element> blips = XmlUtils.getElementsByLocalName(anchor, "blip");
        final Element blip = blips.isEmpty() ? null : blips.get(0);
        final String embedId = firstNonEmpty(attribute(blip, "r:embed"), attribute(blip, "embed"));
        final String mediaPath = stringValue(drawingRels.get(embedId));
        if (mediaPath.isEmpty()) {
          continue;
        }
        final byte[] mediaBytes = files.get(mediaPath);
        if (mediaBytes == null) {
          continue;
        }
        final String extension = getImageExtension(mediaPath);
        final String filename = "image_" + leftPad(imageCounter, 3) + "." + extension;
        images.add(new WorksheetParser.ParsedImageAsset(
            point.toAddress(),
            filename,
            "assets/" + createSafeSheetAssetDir(sheetName) + "/" + filename,
            Arrays.copyOf(mediaBytes, mediaBytes.length),
            mediaPath));
        imageCounter += 1;
      }
    }
    return images;
  }

  public static List<WorksheetParser.ParsedChartAsset> parseDrawingCharts(
      final Map<String, byte[]> files,
      final String sheetName,
      final String sheetPath) {
    final Map<String, String> sheetRels = RelsParser.parseRelationships(files, RelsParser.buildRelsPath(sheetPath), sheetPath);
    final List<WorksheetParser.ParsedChartAsset> charts = new ArrayList<WorksheetParser.ParsedChartAsset>();
    for (final String drawingPath : sheetRels.values()) {
      if (!isDrawingXmlPath(drawingPath)) {
        continue;
      }
      final byte[] drawingBytes = files.get(drawingPath);
      if (drawingBytes == null) {
        continue;
      }
      final Document drawingDoc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(drawingBytes));
      final Map<String, String> drawingRels = RelsParser.parseRelationships(files, RelsParser.buildRelsPath(drawingPath), drawingPath);
      for (final Element anchor : drawingAnchors(drawingDoc)) {
        final AnchorPoint point = parseAnchorPoint(anchor);
        if (point == null) {
          continue;
        }
        final Element graphicFrame = XmlUtils.getFirstChildByLocalName(anchor, "graphicFrame");
        final List<Element> chartRefs = XmlUtils.getElementsByLocalName(graphicFrame == null ? anchor : graphicFrame, "chart");
        final Element chartRef = chartRefs.isEmpty() ? null : chartRefs.get(0);
        final String relId = firstNonEmpty(attribute(chartRef, "r:id"), attribute(chartRef, "id"));
        final String chartPath = stringValue(drawingRels.get(relId));
        if (chartPath.isEmpty()) {
          continue;
        }
        final byte[] chartBytes = files.get(chartPath);
        if (chartBytes == null) {
          continue;
        }
        final Document chartDoc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(chartBytes));
        charts.add(new WorksheetParser.ParsedChartAsset(
            point.toAddress(),
            parseChartTitle(chartDoc),
            parseChartType(chartDoc),
            parseChartSeries(chartDoc),
            chartPath));
      }
    }
    return charts;
  }

  public static List<WorksheetParser.ParsedShapeAsset> parseDrawingShapes(
      final Map<String, byte[]> files,
      final String sheetName,
      final String sheetPath) {
    final Map<String, String> sheetRels = RelsParser.parseRelationships(files, RelsParser.buildRelsPath(sheetPath), sheetPath);
    final List<WorksheetParser.ParsedShapeAsset> shapes = new ArrayList<WorksheetParser.ParsedShapeAsset>();
    int shapeCounter = 1;
    for (final String drawingPath : sheetRels.values()) {
      if (!isDrawingXmlPath(drawingPath)) {
        continue;
      }
      final byte[] drawingBytes = files.get(drawingPath);
      if (drawingBytes == null) {
        continue;
      }
      final Document drawingDoc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(drawingBytes));
      for (final Element anchor : drawingAnchors(drawingDoc)) {
        final AnchorPoint point = parseAnchorPoint(anchor);
        if (point == null) {
          continue;
        }
        if (!XmlUtils.getElementsByLocalName(anchor, "blip").isEmpty() || !XmlUtils.getElementsByLocalName(anchor, "chart").isEmpty()) {
          continue;
        }
        final Element shapeNode = firstElement(
            XmlUtils.getFirstChildByLocalName(anchor, "sp"),
            XmlUtils.getFirstChildByLocalName(anchor, "cxnSp"));
        if (shapeNode == null) {
          continue;
        }
        final Element nonVisual = XmlUtils.getFirstChildByLocalName(shapeNode, "sp".equals(shapeNode.getLocalName()) ? "nvSpPr" : "nvCxnSpPr");
        final Element cNvPr = XmlUtils.getFirstChildByLocalName(nonVisual == null ? shapeNode : nonVisual, "cNvPr");
        final ShapeExtent extent = parseShapeExt(anchor, shapeNode);
        final BoundingBox bbox = parseShapeBoundingBox(anchor, shapeNode, extent);
        final OfficeDrawing.SvgRenderResult svgAsset = OfficeDrawing.renderShapeSvg(shapeNode, anchor, sheetName, shapeCounter);
        shapes.add(new WorksheetParser.ParsedShapeAsset(
            point.toAddress(),
            parseShapeRawEntries(anchor),
            svgAsset == null ? null : svgAsset.getFilename(),
            svgAsset == null ? null : svgAsset.getPath(),
            svgAsset == null ? null : svgAsset.getData(),
            stringValue(attribute(cNvPr, "name")).trim().isEmpty() ? "Shape" : attribute(cNvPr, "name").trim(),
            parseShapeKind(shapeNode),
            parseShapeText(shapeNode),
            extent.getWidthEmu(),
            extent.getHeightEmu(),
            "xdr:" + shapeNode.getLocalName(),
            firstNonEmpty(anchor.getTagName(), anchor.getNodeName(), anchor.getLocalName(), "anchor"),
            bbox));
        shapeCounter += 1;
      }
    }
    return shapes;
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
    return renderShapeSection(shapes, Collections.<ShapeBlock>emptyList(), includeShapeDetails);
  }

  public static String renderShapeSection(
      final List<WorksheetParser.ParsedShapeAsset> shapes,
      final List<ShapeBlock> shapeBlocks,
      final boolean includeShapeDetails) {
    if (!includeShapeDetails || shapes == null || shapes.isEmpty()) {
      return "";
    }
    final List<String> entries = new ArrayList<String>();
    final List<ShapeBlock> safeShapeBlocks = shapeBlocks == null ? Collections.<ShapeBlock>emptyList() : shapeBlocks;
    for (int blockIndex = 0; blockIndex < safeShapeBlocks.size(); blockIndex += 1) {
      entries.add(createShapeBlockEntry(safeShapeBlocks.get(blockIndex), blockIndex, shapes));
    }
    final List<IndexedShape> ungrouped = collectUngroupedShapes(shapes, safeShapeBlocks);
    if (!ungrouped.isEmpty()) {
      if (!entries.isEmpty()) {
        entries.add("### Ungrouped Shapes");
      }
      for (final IndexedShape indexedShape : ungrouped) {
        entries.add(renderShapeDetails(indexedShape.getShape(), indexedShape.getIndex()));
      }
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

  public static List<ShapeBlock> extractShapeBlocksFromAssets(
      final List<WorksheetParser.ParsedShapeAsset> shapes,
      final ShapeBlockOptions options) {
    final List<ShapeBox> boxes = new ArrayList<ShapeBox>();
    for (final WorksheetParser.ParsedShapeAsset shape : shapes == null ? Collections.<WorksheetParser.ParsedShapeAsset>emptyList() : shapes) {
      if (shape != null && shape.getBbox() != null) {
        boxes.add(new ShapeBox(shape.getBbox()));
      }
    }
    return extractShapeBlocks(boxes, options);
  }

  private static String renderShapeDetails(final WorksheetParser.ParsedShapeAsset shape, final int shapeIndex) {
    final List<String> lines = new ArrayList<String>();
    lines.add("#### Shape: " + leftPad(shapeIndex + 1, 3) + " (" + shape.getAnchor() + ")");
    lines.addAll(renderHierarchicalRawEntries(shape.getRawEntries()));
    if (shape.getSvgPath() != null) {
      lines.add("- SVG: " + shape.getSvgPath());
      lines.add("");
      lines.add("![" + (shape.getSvgFilename() == null ? "shape_" + leftPad(shapeIndex + 1, 3) + ".svg" : shape.getSvgFilename()) + "](" + shape.getSvgPath() + ")");
    }
    return joinLines(lines);
  }

  private static String createShapeBlockEntry(
      final ShapeBlock block,
      final int blockIndex,
      final List<WorksheetParser.ParsedShapeAsset> shapes) {
    final List<String> lines = new ArrayList<String>();
    lines.add("### Shape Block: " + leftPad(blockIndex + 1, 3) + " ("
        + AddressUtils.formatRange(block.getStartRow(), block.getStartCol(), block.getEndRow(), block.getEndCol()) + ")");
    lines.add("- Shapes: " + createShapeBlockSummaryLine(block.getShapeIndexes()));
    lines.add("- anchorRange: " + AddressUtils.colToLetters(block.getStartCol()) + block.getStartRow()
        + "-" + AddressUtils.colToLetters(block.getEndCol()) + block.getEndRow());
    final List<String> details = new ArrayList<String>();
    for (final Integer shapeIndex : block.getShapeIndexes()) {
      final int index = shapeIndex.intValue();
      if (index >= 0 && index < shapes.size()) {
        details.add(renderShapeDetails(shapes.get(index), index));
      }
    }
    if (!details.isEmpty()) {
      lines.add("");
      lines.add(joinParagraphs(details));
    }
    return joinLines(lines);
  }

  private static String createShapeBlockSummaryLine(final List<Integer> shapeIndexes) {
    final List<String> values = new ArrayList<String>();
    for (final Integer shapeIndex : shapeIndexes == null ? Collections.<Integer>emptyList() : shapeIndexes) {
      values.add("Shape " + leftPad(shapeIndex.intValue() + 1, 3));
    }
    return join(values, ", ");
  }

  private static List<IndexedShape> collectUngroupedShapes(
      final List<WorksheetParser.ParsedShapeAsset> shapes,
      final List<ShapeBlock> shapeBlocks) {
    final java.util.Set<Integer> grouped = new java.util.LinkedHashSet<Integer>();
    for (final ShapeBlock block : shapeBlocks == null ? Collections.<ShapeBlock>emptyList() : shapeBlocks) {
      grouped.addAll(block.getShapeIndexes());
    }
    final List<IndexedShape> ungrouped = new ArrayList<IndexedShape>();
    for (int index = 0; index < shapes.size(); index += 1) {
      if (!grouped.contains(Integer.valueOf(index))) {
        ungrouped.add(new IndexedShape(shapes.get(index), index));
      }
    }
    return ungrouped;
  }

  private static String getImageExtension(final String mediaPath) {
    final java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\.([a-z0-9]+)$", java.util.regex.Pattern.CASE_INSENSITIVE)
        .matcher(stringValue(mediaPath));
    return matcher.find() ? matcher.group(1).toLowerCase(java.util.Locale.ROOT) : "bin";
  }

  private static boolean isDrawingXmlPath(final String path) {
    return java.util.regex.Pattern.compile("/drawings/.+\\.xml$", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(stringValue(path)).find();
  }

  private static List<Element> drawingAnchors(final Document drawingDoc) {
    final List<Element> anchors = new ArrayList<Element>();
    anchors.addAll(XmlUtils.getElementsByLocalName(drawingDoc, "oneCellAnchor"));
    anchors.addAll(XmlUtils.getElementsByLocalName(drawingDoc, "twoCellAnchor"));
    return anchors;
  }

  private static AnchorPoint parseAnchorPoint(final Element anchor) {
    final Element from = XmlUtils.getFirstChildByLocalName(anchor, "from");
    final Long col = parseLongText(XmlUtils.getFirstChildByLocalName(from == null ? anchor : from, "col"));
    final Long row = parseLongText(XmlUtils.getFirstChildByLocalName(from == null ? anchor : from, "row"));
    if (col == null || row == null || col.longValue() < 0L || row.longValue() < 0L) {
      return null;
    }
    return new AnchorPoint(row.intValue() + 1, col.intValue() + 1);
  }

  private static String parseChartTitle(final Document chartDoc) {
    final StringBuilder builder = new StringBuilder();
    for (final Element node : XmlUtils.getElementsByLocalName(chartDoc, "t")) {
      final String text = XmlUtils.getTextContent(node);
      if (!text.isEmpty()) {
        builder.append(text);
      }
    }
    return builder.toString().trim();
  }

  private static String parseChartType(final Document chartDoc) {
    final List<String> matched = new ArrayList<String>();
    final String[][] typeMap = {
        {"barChart", "Bar Chart"},
        {"lineChart", "Line Chart"},
        {"pieChart", "Pie Chart"},
        {"doughnutChart", "Doughnut Chart"},
        {"areaChart", "Area Chart"},
        {"scatterChart", "Scatter Chart"},
        {"radarChart", "Radar Chart"},
        {"bubbleChart", "Bubble Chart"}
    };
    for (final String[] entry : typeMap) {
      if (!XmlUtils.getElementsByLocalName(chartDoc, entry[0]).isEmpty()) {
        matched.add(entry[1]);
      }
    }
    if (matched.isEmpty()) {
      return "Chart";
    }
    return matched.size() == 1 ? matched.get(0) : join(matched, " + ") + " (Combined)";
  }

  private static List<WorksheetParser.ParsedChartSeries> parseChartSeries(final Document chartDoc) {
    final Element plotArea = firstElement(XmlUtils.getFirstChildByLocalName(chartDoc, "plotArea"), chartDoc.getDocumentElement());
    final Map<String, String> axisPositionById = new LinkedHashMap<String, String>();
    for (final Element axisNode : XmlUtils.getElementsByLocalName(plotArea, "valAx")) {
      final Element axisIdNode = XmlUtils.getFirstChildByLocalName(axisNode, "axId");
      final Element axisPosNode = XmlUtils.getFirstChildByLocalName(axisNode, "axPos");
      final String axisId = firstNonEmpty(attribute(axisIdNode, "val"), XmlUtils.getTextContent(axisIdNode));
      final String axisPos = firstNonEmpty(attribute(axisPosNode, "val"), XmlUtils.getTextContent(axisPosNode));
      if (!axisId.isEmpty()) {
        axisPositionById.put(axisId, axisPos);
      }
    }
    final List<WorksheetParser.ParsedChartSeries> series = new ArrayList<WorksheetParser.ParsedChartSeries>();
    for (final String localName : asList("barChart", "lineChart", "pieChart", "doughnutChart", "areaChart", "scatterChart", "radarChart", "bubbleChart")) {
      for (final Element chartNode : XmlUtils.getElementsByLocalName(plotArea, localName)) {
        boolean secondary = false;
        for (final Element axisIdNode : XmlUtils.getElementsByLocalName(chartNode, "axId")) {
          final String axisId = firstNonEmpty(attribute(axisIdNode, "val"), XmlUtils.getTextContent(axisIdNode));
          secondary = secondary || "r".equals(axisPositionById.get(axisId));
        }
        for (final Element seriesNode : XmlUtils.getElementsByLocalName(chartNode, "ser")) {
          final Element txNode = firstElement(XmlUtils.getFirstChildByLocalName(seriesNode, "tx"), seriesNode);
          final Element nameRef = XmlUtils.getFirstChildByLocalName(txNode, "f");
          final Element nameValue = XmlUtils.getFirstChildByLocalName(txNode, "v");
          final String nameText = collectTextNodes(txNode).trim();
          final Element catNode = firstElement(XmlUtils.getFirstChildByLocalName(seriesNode, "cat"), seriesNode);
          final Element valNode = firstElement(XmlUtils.getFirstChildByLocalName(seriesNode, "val"), seriesNode);
          final Element catRef = firstElement(
              XmlUtils.getFirstChildByLocalName(firstElement(XmlUtils.getFirstChildByLocalName(catNode, "strRef"), catNode), "f"),
              XmlUtils.getFirstChildByLocalName(firstElement(XmlUtils.getFirstChildByLocalName(catNode, "numRef"), catNode), "f"));
          final Element valRef = firstElement(
              XmlUtils.getFirstChildByLocalName(valNode, "f"),
              XmlUtils.getFirstChildByLocalName(firstElement(XmlUtils.getFirstChildByLocalName(valNode, "numRef"), valNode), "f"));
          series.add(new WorksheetParser.ParsedChartSeries(
              firstNonEmpty(nameText, XmlUtils.getTextContent(nameValue), XmlUtils.getTextContent(nameRef), "Series"),
              XmlUtils.getTextContent(catRef),
              XmlUtils.getTextContent(valRef),
              secondary ? "secondary" : "primary"));
        }
      }
    }
    return series;
  }

  private static String parseShapeKind(final Element shapeNode) {
    if (shapeNode == null) {
      return "Shape";
    }
    if ("cxnSp".equals(shapeNode.getLocalName())) {
      final Element geomNode = XmlUtils.getFirstChildByLocalName(firstElement(XmlUtils.getFirstChildByLocalName(shapeNode, "spPr"), shapeNode), "prstGeom");
      final String prst = stringValue(attribute(geomNode, "prst")).trim();
      return "straightConnector1".equals(prst) ? "Straight Arrow Connector" : (prst.isEmpty() ? "Connector" : "Connector (" + prst + ")");
    }
    if (!"sp".equals(shapeNode.getLocalName())) {
      return "Shape";
    }
    final Element nvSpPr = XmlUtils.getFirstChildByLocalName(shapeNode, "nvSpPr");
    final Element cNvSpPr = XmlUtils.getFirstChildByLocalName(nvSpPr == null ? shapeNode : nvSpPr, "cNvSpPr");
    if ("1".equals(attribute(cNvSpPr, "txBox"))) {
      return "Text Box";
    }
    final Element geomNode = XmlUtils.getFirstChildByLocalName(firstElement(XmlUtils.getFirstChildByLocalName(shapeNode, "spPr"), shapeNode), "prstGeom");
    final String prst = stringValue(attribute(geomNode, "prst")).trim();
    return "rect".equals(prst) ? "Rectangle" : (prst.isEmpty() ? "Shape" : "Shape (" + prst + ")");
  }

  private static String parseShapeText(final Element shapeNode) {
    return collectTextNodes(shapeNode).trim();
  }

  private static ShapeExtent parseShapeExt(final Element anchor, final Element shapeNode) {
    final Element shapeTransform = XmlUtils.getDirectChildByLocalName(
        firstElement(XmlUtils.getDirectChildByLocalName(shapeNode == null ? anchor : shapeNode, "spPr"), shapeNode, anchor),
        "xfrm");
    final Element extNode = firstElement(
        XmlUtils.getDirectChildByLocalName(anchor, "ext"),
        XmlUtils.getDirectChildByLocalName(shapeTransform, "ext"));
    return new ShapeExtent(parseLongAttribute(extNode, "cx"), parseLongAttribute(extNode, "cy"));
  }

  private static List<WorksheetParser.ParsedShapeRawEntry> parseShapeRawEntries(final Element anchor) {
    final List<WorksheetParser.ParsedShapeRawEntry> entries = new ArrayList<WorksheetParser.ParsedShapeRawEntry>();
    flattenXmlNodeEntries(anchor, "", entries);
    return entries;
  }

  private static void flattenXmlNodeEntries(
      final Element node,
      final String path,
      final List<WorksheetParser.ParsedShapeRawEntry> entries) {
    if (node == null) {
      return;
    }
    final String nodeName = firstNonEmpty(node.getTagName(), node.getNodeName(), node.getLocalName(), "node");
    final String currentPath = path.isEmpty() ? nodeName : path + "/" + nodeName;
    final NamedNodeMap attributes = node.getAttributes();
    for (int index = 0; index < attributes.getLength(); index += 1) {
      final Node attribute = attributes.item(index);
      entries.add(new WorksheetParser.ParsedShapeRawEntry(currentPath + "@" + attribute.getNodeName(), attribute.getNodeValue()));
    }
    final String directText = collectDirectText(node);
    if (!directText.isEmpty()) {
      entries.add(new WorksheetParser.ParsedShapeRawEntry(currentPath + "#text", directText));
    }
    final NodeList childNodes = node.getChildNodes();
    for (int index = 0; index < childNodes.getLength(); index += 1) {
      final Node child = childNodes.item(index);
      if (child != null && child.getNodeType() == Node.ELEMENT_NODE) {
        flattenXmlNodeEntries((Element) child, currentPath, entries);
      }
    }
  }

  private static BoundingBox parseShapeBoundingBox(
      final Element anchor,
      final Element shapeNode,
      final ShapeExtent extent) {
    final long defaultCellWidthEmu = 914400L;
    final long defaultCellHeightEmu = 190500L;
    final long fromCol = valueOrZero(parseAnchorLong(anchor, "from", "col"));
    final long fromRow = valueOrZero(parseAnchorLong(anchor, "from", "row"));
    final long fromColOff = valueOrZero(parseAnchorLong(anchor, "from", "colOff"));
    final long fromRowOff = valueOrZero(parseAnchorLong(anchor, "from", "rowOff"));
    final Long toCol = parseAnchorLong(anchor, "to", "col");
    final Long toRow = parseAnchorLong(anchor, "to", "row");
    final long toColOff = valueOrZero(parseAnchorLong(anchor, "to", "colOff"));
    final long toRowOff = valueOrZero(parseAnchorLong(anchor, "to", "rowOff"));
    final long left = fromCol * defaultCellWidthEmu + fromColOff;
    final long top = fromRow * defaultCellHeightEmu + fromRowOff;
    if (toCol != null && toRow != null) {
      return new BoundingBox(left, top, toCol.longValue() * defaultCellWidthEmu + toColOff, toRow.longValue() * defaultCellHeightEmu + toRowOff);
    }
    final ShapeExtent parsedExtent = parseShapeExt(anchor, shapeNode);
    final long width = Math.max(1L, firstPositive(parsedExtent.getWidthEmu(), extent.getWidthEmu(), Long.valueOf(defaultCellWidthEmu)));
    final long height = Math.max(1L, firstPositive(parsedExtent.getHeightEmu(), extent.getHeightEmu(), Long.valueOf(defaultCellHeightEmu)));
    return new BoundingBox(left, top, left + width, top + height);
  }

  private static Long parseAnchorLong(final Element anchor, final String parentName, final String childName) {
    final Element parent = XmlUtils.getFirstChildByLocalName(anchor, parentName);
    return parseLongText(XmlUtils.getFirstChildByLocalName(parent == null ? anchor : parent, childName));
  }

  private static String collectTextNodes(final Element root) {
    final StringBuilder builder = new StringBuilder();
    for (final Element node : XmlUtils.getElementsByLocalName(root, "t")) {
      builder.append(XmlUtils.getTextContent(node));
    }
    return builder.toString();
  }

  private static String collectDirectText(final Element node) {
    final List<String> values = new ArrayList<String>();
    final NodeList childNodes = node.getChildNodes();
    for (int index = 0; index < childNodes.getLength(); index += 1) {
      final Node child = childNodes.item(index);
      if (child != null && child.getNodeType() == Node.TEXT_NODE) {
        final String value = stringValue(child.getTextContent()).trim();
        if (!value.isEmpty()) {
          values.add(value);
        }
      }
    }
    return join(values, " ");
  }

  private static Element firstElement(final Element... values) {
    if (values == null) {
      return null;
    }
    for (final Element value : values) {
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private static String firstNonEmpty(final String... values) {
    if (values == null) {
      return "";
    }
    for (final String value : values) {
      final String normalized = stringValue(value);
      if (!normalized.isEmpty()) {
        return normalized;
      }
    }
    return "";
  }

  private static String attribute(final Element element, final String name) {
    return element == null ? "" : stringValue(element.getAttribute(name));
  }

  private static Long parseLongText(final Element element) {
    return parseLong(XmlUtils.getTextContent(element));
  }

  private static Long parseLongAttribute(final Element element, final String name) {
    return parseLong(attribute(element, name));
  }

  private static Long parseLong(final String value) {
    final String normalized = stringValue(value).trim();
    if (normalized.isEmpty()) {
      return null;
    }
    try {
      return Long.valueOf((long) Double.parseDouble(normalized));
    } catch (final NumberFormatException ex) {
      return null;
    }
  }

  private static long valueOrZero(final Long value) {
    return value == null ? 0L : value.longValue();
  }

  private static long firstPositive(final Long... values) {
    for (final Long value : values) {
      if (value != null && value.longValue() > 0L) {
        return value.longValue();
      }
    }
    return 0L;
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

  private static final class IndexedShape {
    private final WorksheetParser.ParsedShapeAsset shape;
    private final int index;

    private IndexedShape(final WorksheetParser.ParsedShapeAsset shape, final int index) {
      this.shape = shape;
      this.index = index;
    }

    private WorksheetParser.ParsedShapeAsset getShape() {
      return shape;
    }

    private int getIndex() {
      return index;
    }
  }

  private static final class AnchorPoint {
    private final int row;
    private final int col;

    private AnchorPoint(final int row, final int col) {
      this.row = row;
      this.col = col;
    }

    private String toAddress() {
      return AddressUtils.colToLetters(col) + row;
    }
  }

  private static final class ShapeExtent {
    private final Long widthEmu;
    private final Long heightEmu;

    private ShapeExtent(final Long widthEmu, final Long heightEmu) {
      this.widthEmu = widthEmu;
      this.heightEmu = heightEmu;
    }

    private Long getWidthEmu() {
      return widthEmu;
    }

    private Long getHeightEmu() {
      return heightEmu;
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
