/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.officedrawing;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.sheetassets.SheetAssets;
import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

public final class OfficeDrawing {
  private OfficeDrawing() {
  }

  public static SvgRenderResult renderShapeSvg(
      final Element shapeNode,
      final Element anchor,
      final String sheetName,
      final int shapeIndex) {
    final String kind = parseShapeKind(shapeNode);
    if (kind == null) {
      return null;
    }
    final String svg = "connector".equals(kind)
        ? renderConnectorSvg(shapeNode, anchor)
        : renderRectLikeSvg(shapeNode, anchor, parseShapeText(shapeNode), "textbox".equals(kind));
    final String filename = "shape_" + leftPad(shapeIndex, 3) + ".svg";
    return new SvgRenderResult(
        filename,
        "assets/" + SheetAssets.createSafeSheetAssetDir(sheetName) + "/" + filename,
        (svg + "\n").getBytes(StandardCharsets.UTF_8));
  }

  private static String renderRectLikeSvg(
      final Element shapeNode,
      final Element anchor,
      final String text,
      final boolean treatAsTextbox) {
    final ShapeDimensions dimensions = parseShapeDimensions(anchor, shapeNode);
    final Element spPr = XmlUtils.getDirectChildByLocalName(shapeNode, "spPr");
    final String fillColor = firstNonEmpty(parseHexColor(XmlUtils.getDirectChildByLocalName(spPr, "solidFill")), treatAsTextbox ? "#FFFFFF" : "#F3F3F3");
    final Element lineNode = XmlUtils.getDirectChildByLocalName(spPr, "ln");
    final String strokeColor = firstNonEmpty(parseHexColor(lineNode), "#333333");
    final int strokeWidth = Math.max(1, parseStrokeWidth(lineNode));
    final String safeText = escapeXml(text);
    final String textMarkup = safeText.isEmpty()
        ? ""
        : "  <text x=\"" + Math.round(dimensions.getWidthPx() / 2.0d) + "\" y=\"" + Math.round(dimensions.getHeightPx() / 2.0d)
            + "\" text-anchor=\"middle\" dominant-baseline=\"middle\" font-size=\"14\" font-family=\"sans-serif\" fill=\"#000000\">"
            + safeText + "</text>\n";
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + dimensions.getWidthPx() + "\" height=\"" + dimensions.getHeightPx()
        + "\" viewBox=\"0 0 " + dimensions.getWidthPx() + " " + dimensions.getHeightPx() + "\">\n"
        + "  <rect x=\"1\" y=\"1\" width=\"" + Math.max(1, dimensions.getWidthPx() - 2) + "\" height=\""
        + Math.max(1, dimensions.getHeightPx() - 2) + "\" fill=\"" + fillColor + "\" stroke=\"" + strokeColor
        + "\" stroke-width=\"" + strokeWidth + "\"/>\n"
        + textMarkup
        + "</svg>";
  }

  private static String renderConnectorSvg(final Element shapeNode, final Element anchor) {
    final ShapeDimensions dimensions = parseShapeDimensions(anchor, shapeNode);
    final Element spPr = XmlUtils.getDirectChildByLocalName(shapeNode, "spPr");
    final Element lineNode = XmlUtils.getDirectChildByLocalName(spPr, "ln");
    final String strokeColor = firstNonEmpty(parseHexColor(lineNode), "#333333");
    final int strokeWidth = Math.max(1, parseStrokeWidth(lineNode));
    final int effectiveHeight = Math.max(dimensions.getHeightPx(), 24);
    final int y = (int) Math.round(effectiveHeight / 2.0d);
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + dimensions.getWidthPx() + "\" height=\"" + effectiveHeight
        + "\" viewBox=\"0 0 " + dimensions.getWidthPx() + " " + effectiveHeight + "\">\n"
        + "  <defs>\n"
        + "    <marker id=\"arrow\" markerWidth=\"10\" markerHeight=\"10\" refX=\"8\" refY=\"3\" orient=\"auto\" markerUnits=\"strokeWidth\">\n"
        + "      <path d=\"M0,0 L0,6 L9,3 z\" fill=\"" + strokeColor + "\"/>\n"
        + "    </marker>\n"
        + "  </defs>\n"
        + "  <line x1=\"2\" y1=\"" + y + "\" x2=\"" + Math.max(2, dimensions.getWidthPx() - 4) + "\" y2=\"" + y
        + "\" stroke=\"" + strokeColor + "\" stroke-width=\"" + strokeWidth + "\" marker-end=\"url(#arrow)\"/>\n"
        + "</svg>";
  }

  private static ShapeDimensions parseShapeDimensions(final Element anchor, final Element shapeNode) {
    final Element extNode = firstElement(
        XmlUtils.getDirectChildByLocalName(anchor, "ext"),
        XmlUtils.getDirectChildByLocalName(
            XmlUtils.getDirectChildByLocalName(XmlUtils.getDirectChildByLocalName(shapeNode == null ? anchor : shapeNode, "spPr"), "xfrm"),
            "ext"));
    return new ShapeDimensions(
        emuToPx(parseLongAttribute(extNode, "cx"), 160),
        emuToPx(parseLongAttribute(extNode, "cy"), 48));
  }

  private static String parseShapeKind(final Element shapeNode) {
    if (shapeNode == null) {
      return null;
    }
    if ("cxnSp".equals(shapeNode.getLocalName())) {
      return "connector";
    }
    if (!"sp".equals(shapeNode.getLocalName())) {
      return null;
    }
    final Element nvSpPr = XmlUtils.getDirectChildByLocalName(shapeNode, "nvSpPr");
    final Element cNvSpPr = XmlUtils.getDirectChildByLocalName(nvSpPr, "cNvSpPr");
    if ("1".equals(attribute(cNvSpPr, "txBox"))) {
      return "textbox";
    }
    final Element spPr = XmlUtils.getDirectChildByLocalName(shapeNode, "spPr");
    final Element prstGeom = XmlUtils.getDirectChildByLocalName(spPr, "prstGeom");
    return "rect".equals(attribute(prstGeom, "prst").trim()) ? "rect" : null;
  }

  private static String parseShapeText(final Element shapeNode) {
    final StringBuilder builder = new StringBuilder();
    for (final Element node : XmlUtils.getElementsByLocalName(shapeNode, "t")) {
      final String text = XmlUtils.getTextContent(node).trim();
      if (!text.isEmpty()) {
        if (builder.length() > 0) {
          builder.append('\n');
        }
        builder.append(text);
      }
    }
    return builder.toString().trim();
  }

  private static String parseHexColor(final Element root) {
    final Element srgb = firstElementFromLocalName(root, "srgbClr");
    if (!attribute(srgb, "val").trim().isEmpty()) {
      return "#" + attribute(srgb, "val").trim();
    }
    final Element scheme = firstElementFromLocalName(root, "schemeClr");
    final String schemeVal = attribute(scheme, "val").trim();
    final Map<String, String> schemeMap = new LinkedHashMap<String, String>();
    schemeMap.put("accent1", "#4472C4");
    schemeMap.put("accent2", "#ED7D31");
    schemeMap.put("accent3", "#A5A5A5");
    schemeMap.put("accent4", "#FFC000");
    schemeMap.put("accent5", "#5B9BD5");
    schemeMap.put("accent6", "#70AD47");
    schemeMap.put("tx1", "#000000");
    schemeMap.put("tx2", "#44546A");
    schemeMap.put("lt1", "#FFFFFF");
    schemeMap.put("lt2", "#E7E6E6");
    return schemeMap.get(schemeVal);
  }

  private static Element firstElementFromLocalName(final Element root, final String localName) {
    final java.util.List<Element> elements = XmlUtils.getElementsByLocalName(root, localName);
    return elements.isEmpty() ? null : elements.get(0);
  }

  private static int parseStrokeWidth(final Element lineNode) {
    final Long value = parseLongAttribute(lineNode, "w");
    return value == null ? 1 : (int) Math.round(value.longValue() / 9525.0d);
  }

  private static int emuToPx(final Long emu, final int fallback) {
    if (emu == null || emu.longValue() <= 0L) {
      return fallback;
    }
    return Math.max(1, (int) Math.round(emu.longValue() / 9525.0d));
  }

  private static String escapeXml(final String text) {
    return stringValue(text)
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
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

  private static String firstNonEmpty(final String first, final String second) {
    return first == null || first.isEmpty() ? stringValue(second) : first;
  }

  private static Long parseLongAttribute(final Element element, final String name) {
    final String value = attribute(element, name).trim();
    if (value.isEmpty()) {
      return null;
    }
    try {
      return Long.valueOf((long) Double.parseDouble(value));
    } catch (final NumberFormatException ex) {
      return null;
    }
  }

  private static String attribute(final Element element, final String name) {
    return element == null ? "" : stringValue(element.getAttribute(name));
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

  private static String stringValue(final String value) {
    return value == null ? "" : value;
  }

  private static final class ShapeDimensions {
    private final int widthPx;
    private final int heightPx;

    private ShapeDimensions(final int widthPx, final int heightPx) {
      this.widthPx = widthPx;
      this.heightPx = heightPx;
    }

    private int getWidthPx() {
      return widthPx;
    }

    private int getHeightPx() {
      return heightPx;
    }
  }

  public static final class SvgRenderResult {
    private final String filename;
    private final String path;
    private final byte[] data;

    public SvgRenderResult(final String filename, final String path, final byte[] data) {
      this.filename = filename;
      this.path = path;
      this.data = data;
    }

    public String getFilename() {
      return filename;
    }

    public String getPath() {
      return path;
    }

    public byte[] getData() {
      return data;
    }
  }
}
