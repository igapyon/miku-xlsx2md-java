/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.stylesparser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

public final class StylesParser {
  public static final BorderFlags EMPTY_BORDERS = new BorderFlags(false, false, false, false);
  public static final TextStyle EMPTY_TEXT_STYLE = new TextStyle(false, false, false, false);
  public static final Map<Integer, String> BUILTIN_FORMAT_CODES = createBuiltinFormatCodes();

  private StylesParser() {
  }

  public static boolean hasBorderSide(final Element side) {
    if (side == null) {
      return false;
    }
    return side.hasAttribute("style") || side.getChildNodes().getLength() > 0;
  }

  public static TextStyle parseFontStyle(final Element fontElement) {
    return new TextStyle(
        hasEnabledBooleanValue(getNestedFirst(fontElement, "b")),
        hasEnabledBooleanValue(getNestedFirst(fontElement, "i")),
        hasEnabledBooleanValue(getNestedFirst(fontElement, "strike")),
        hasEnabledBooleanValue(getNestedFirst(fontElement, "u")));
  }

  public static List<CellStyleInfo> parseCellStyles(final Map<String, byte[]> files) {
    final byte[] stylesBytes = files.get("xl/styles.xml");
    if (stylesBytes == null) {
      return defaultStyles();
    }

    final Document doc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(stylesBytes));
    final List<BorderFlags> borders = parseBorders(doc);
    final List<TextStyle> fontStyles = parseFontStyles(doc);
    final Map<Integer, String> numFmtMap = parseNumFmtMap(doc);

    final List<Element> cellXfs = XmlUtils.getElementsByLocalName(doc, "cellXfs");
    if (cellXfs.isEmpty()) {
      final List<CellStyleInfo> result = new ArrayList<CellStyleInfo>();
      result.add(new CellStyleInfo(
          borders.isEmpty() ? EMPTY_BORDERS : borders.get(0),
          0,
          "General",
          fontStyles.isEmpty() ? EMPTY_TEXT_STYLE : fontStyles.get(0)));
      return result;
    }

    final List<CellStyleInfo> styles = new ArrayList<CellStyleInfo>();
    for (final Element xfElement : XmlUtils.getElementsByLocalName(cellXfs.get(0), "xf")) {
      final int borderId = parseIntAttribute(xfElement, "borderId", 0);
      final int numFmtId = parseIntAttribute(xfElement, "numFmtId", 0);
      final int fontId = parseIntAttribute(xfElement, "fontId", 0);
      styles.add(new CellStyleInfo(
          borderId >= 0 && borderId < borders.size() ? borders.get(borderId) : EMPTY_BORDERS,
          numFmtId,
          numFmtMap.containsKey(Integer.valueOf(numFmtId)) ? numFmtMap.get(Integer.valueOf(numFmtId))
              : (BUILTIN_FORMAT_CODES.containsKey(Integer.valueOf(numFmtId)) ? BUILTIN_FORMAT_CODES.get(Integer.valueOf(numFmtId)) : "General"),
          fontId >= 0 && fontId < fontStyles.size() ? fontStyles.get(fontId) : EMPTY_TEXT_STYLE));
    }
    return styles.isEmpty() ? defaultStyles() : styles;
  }

  private static List<CellStyleInfo> defaultStyles() {
    final List<CellStyleInfo> result = new ArrayList<CellStyleInfo>();
    result.add(new CellStyleInfo(EMPTY_BORDERS, 0, "General", EMPTY_TEXT_STYLE));
    return result;
  }

  private static List<BorderFlags> parseBorders(final Document doc) {
    final List<BorderFlags> result = new ArrayList<BorderFlags>();
    for (final Element borderElement : XmlUtils.getElementsByLocalName(doc, "border")) {
      result.add(new BorderFlags(
          hasBorderSide(getNestedFirst(borderElement, "top")),
          hasBorderSide(getNestedFirst(borderElement, "bottom")),
          hasBorderSide(getNestedFirst(borderElement, "left")),
          hasBorderSide(getNestedFirst(borderElement, "right"))));
    }
    return result;
  }

  private static List<TextStyle> parseFontStyles(final Document doc) {
    final List<TextStyle> result = new ArrayList<TextStyle>();
    for (final Element fontElement : XmlUtils.getElementsByLocalName(doc, "font")) {
      result.add(parseFontStyle(fontElement));
    }
    return result;
  }

  private static Map<Integer, String> parseNumFmtMap(final Document doc) {
    final Map<Integer, String> numFmtMap = new LinkedHashMap<Integer, String>();
    final List<Element> numFmts = XmlUtils.getElementsByLocalName(doc, "numFmts");
    if (numFmts.isEmpty()) {
      return numFmtMap;
    }
    for (final Element numFmtElement : XmlUtils.getElementsByLocalName(numFmts.get(0), "numFmt")) {
      final int numFmtId = parseIntAttribute(numFmtElement, "numFmtId", 0);
      final String formatCode = numFmtElement.getAttribute("formatCode");
      if (formatCode != null && !formatCode.isEmpty()) {
        numFmtMap.put(Integer.valueOf(numFmtId), formatCode);
      }
    }
    return numFmtMap;
  }

  private static Element getNestedFirst(final Element root, final String localName) {
    if (root == null) {
      return null;
    }
    final List<Element> elements = XmlUtils.getElementsByLocalName(root, localName);
    return elements.isEmpty() ? null : elements.get(0);
  }

  private static boolean hasEnabledBooleanValue(final Element node) {
    if (node == null) {
      return false;
    }
    final String value = node.getAttribute("val") == null ? "" : node.getAttribute("val").trim().toLowerCase();
    return !"false".equals(value) && !"0".equals(value) && !"none".equals(value);
  }

  private static int parseIntAttribute(final Element element, final String name, final int fallback) {
    try {
      final String value = element.getAttribute(name);
      return value == null || value.isEmpty() ? fallback : Integer.parseInt(value);
    } catch (final NumberFormatException ex) {
      return fallback;
    }
  }

  private static Map<Integer, String> createBuiltinFormatCodes() {
    final Map<Integer, String> result = new LinkedHashMap<Integer, String>();
    result.put(Integer.valueOf(0), "General");
    result.put(Integer.valueOf(1), "0");
    result.put(Integer.valueOf(2), "0.00");
    result.put(Integer.valueOf(3), "#,##0");
    result.put(Integer.valueOf(4), "#,##0.00");
    result.put(Integer.valueOf(9), "0%");
    result.put(Integer.valueOf(10), "0.00%");
    result.put(Integer.valueOf(11), "0.00E+00");
    result.put(Integer.valueOf(12), "# ?/?");
    result.put(Integer.valueOf(13), "# ??/??");
    result.put(Integer.valueOf(14), "yyyy/m/d");
    result.put(Integer.valueOf(15), "d-mmm-yy");
    result.put(Integer.valueOf(16), "d-mmm");
    result.put(Integer.valueOf(17), "mmm-yy");
    result.put(Integer.valueOf(18), "h:mm AM/PM");
    result.put(Integer.valueOf(19), "h:mm:ss AM/PM");
    result.put(Integer.valueOf(20), "h:mm");
    result.put(Integer.valueOf(21), "h:mm:ss");
    result.put(Integer.valueOf(22), "m/d/yy h:mm");
    result.put(Integer.valueOf(45), "mm:ss");
    result.put(Integer.valueOf(46), "[h]:mm:ss");
    result.put(Integer.valueOf(47), "mmss.0");
    result.put(Integer.valueOf(49), "@");
    result.put(Integer.valueOf(56), "m月d日");
    return result;
  }

  public static final class BorderFlags {
    private final boolean top;
    private final boolean bottom;
    private final boolean left;
    private final boolean right;

    public BorderFlags(final boolean top, final boolean bottom, final boolean left, final boolean right) {
      this.top = top;
      this.bottom = bottom;
      this.left = left;
      this.right = right;
    }

    public boolean isTop() {
      return top;
    }

    public boolean isBottom() {
      return bottom;
    }

    public boolean isLeft() {
      return left;
    }

    public boolean isRight() {
      return right;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof BorderFlags)) {
        return false;
      }
      final BorderFlags that = (BorderFlags) other;
      return top == that.top && bottom == that.bottom && left == that.left && right == that.right;
    }

    @Override
    public int hashCode() {
      return Objects.hash(Boolean.valueOf(top), Boolean.valueOf(bottom), Boolean.valueOf(left), Boolean.valueOf(right));
    }
  }

  public static final class TextStyle {
    private final boolean bold;
    private final boolean italic;
    private final boolean strike;
    private final boolean underline;

    public TextStyle(final boolean bold, final boolean italic, final boolean strike, final boolean underline) {
      this.bold = bold;
      this.italic = italic;
      this.strike = strike;
      this.underline = underline;
    }

    public boolean isBold() {
      return bold;
    }

    public boolean isItalic() {
      return italic;
    }

    public boolean isStrike() {
      return strike;
    }

    public boolean isUnderline() {
      return underline;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof TextStyle)) {
        return false;
      }
      final TextStyle that = (TextStyle) other;
      return bold == that.bold && italic == that.italic && strike == that.strike && underline == that.underline;
    }

    @Override
    public int hashCode() {
      return Objects.hash(Boolean.valueOf(bold), Boolean.valueOf(italic), Boolean.valueOf(strike), Boolean.valueOf(underline));
    }
  }

  public static final class CellStyleInfo {
    private final BorderFlags borders;
    private final int numFmtId;
    private final String formatCode;
    private final TextStyle textStyle;

    public CellStyleInfo(final BorderFlags borders, final int numFmtId, final String formatCode, final TextStyle textStyle) {
      this.borders = borders;
      this.numFmtId = numFmtId;
      this.formatCode = formatCode;
      this.textStyle = textStyle;
    }

    public BorderFlags getBorders() {
      return borders;
    }

    public int getNumFmtId() {
      return numFmtId;
    }

    public String getFormatCode() {
      return formatCode;
    }

    public TextStyle getTextStyle() {
      return textStyle;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof CellStyleInfo)) {
        return false;
      }
      final CellStyleInfo that = (CellStyleInfo) other;
      return numFmtId == that.numFmtId
          && Objects.equals(borders, that.borders)
          && Objects.equals(formatCode, that.formatCode)
          && Objects.equals(textStyle, that.textStyle);
    }

    @Override
    public int hashCode() {
      return Objects.hash(borders, Integer.valueOf(numFmtId), formatCode, textStyle);
    }
  }
}
