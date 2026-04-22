/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.worksheetparser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings;
import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;
import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

public final class WorksheetParser {
  private WorksheetParser() {
  }

  public static ExtractedCellOutput extractCellOutputValue(
      final Element cellElement,
      final List<SharedStrings.SharedStringEntry> sharedStrings,
      final StylesParser.CellStyleInfo cellStyle,
      final WorksheetParserDependencies deps,
      final String formulaOverride) {
    final String type = trim(cellElement.getAttribute("t"));
    final Element valueNode = getFirstTag(cellElement, "v");
    final String valueText = deps.getTextContent(valueNode);
    final String rawFormulaText = formulaOverride == null || formulaOverride.isEmpty()
        ? deps.getTextContent(getFirstTag(cellElement, "f"))
        : formulaOverride;
    final String cachedValueState = rawFormulaText.isEmpty()
        ? null
        : (valueNode == null ? "absent" : (valueText.isEmpty() ? "present_empty" : "present_nonempty"));

    if (!rawFormulaText.isEmpty()) {
      final String normalizedFormula = rawFormulaText.startsWith("=") ? rawFormulaText : "=" + rawFormulaText;
      if (normalizedFormula.matches(".*\\[[^\\]]+\\.xlsx\\].*")) {
        return new ExtractedCellOutput(
            type.isEmpty() ? "formula" : type,
            valueText.isEmpty() ? normalizedFormula : valueText,
            normalizedFormula,
            normalizedFormula,
            "unsupported_external",
            "external_unsupported",
            cachedValueState,
            null);
      }
      if (valueNode != null) {
        final String formattedValue = deps.formatCellDisplayValue(valueText, cellStyle);
        return new ExtractedCellOutput(
            type.isEmpty() ? "formula" : type,
            valueText,
            formattedValue == null ? valueText : formattedValue,
            normalizedFormula,
            "resolved",
            "cached_value",
            cachedValueState,
            null);
      }
      return new ExtractedCellOutput(
          type.isEmpty() ? "formula" : type,
          normalizedFormula,
          normalizedFormula,
          normalizedFormula,
          "fallback_formula",
          "formula_text",
          cachedValueState,
          null);
    }

    if ("s".equals(type)) {
      final int sharedIndex = parseInt(valueText, 0);
      final SharedStrings.SharedStringEntry sharedEntry =
          sharedIndex >= 0 && sharedIndex < sharedStrings.size()
              ? sharedStrings.get(sharedIndex)
              : new SharedStrings.SharedStringEntry("", null);
      return new ExtractedCellOutput(
          type,
          valueText,
          sharedEntry.getText(),
          "",
          null,
          null,
          null,
          sharedEntry.getRuns());
    }
    if ("inlineStr".equals(type)) {
      final String inlineText = collectInlineText(cellElement, deps);
      return new ExtractedCellOutput(type, inlineText, inlineText, "", null, null, null, null);
    }
    if ("b".equals(type)) {
      final String boolText = "1".equals(valueText) ? "TRUE" : "FALSE";
      return new ExtractedCellOutput(type, valueText, boolText, "", null, null, null, null);
    }
    if ("str".equals(type) || "e".equals(type)) {
      return new ExtractedCellOutput(type, valueText, valueText, "", null, null, null, null);
    }
    if (!valueText.isEmpty()) {
      final String formattedValue = deps.formatCellDisplayValue(valueText, cellStyle);
      if (formattedValue != null) {
        return new ExtractedCellOutput(type, valueText, formattedValue, "", null, null, null, null);
      }
    }
    return new ExtractedCellOutput(type, valueText, valueText, "", null, null, null, null);
  }

  public static List<String> expandRangeAddresses(final String ref) {
    final AddressUtils.MergeRange range = AddressUtils.parseRangeRef(ref);
    final List<String> addresses = new ArrayList<String>();
    for (int row = Math.max(1, range.getStartRow()); row <= Math.max(range.getStartRow(), range.getEndRow()); row += 1) {
      for (int col = Math.max(1, range.getStartCol()); col <= Math.max(range.getStartCol(), range.getEndCol()); col += 1) {
        addresses.add(AddressUtils.colToLetters(col) + row);
      }
    }
    return addresses;
  }

  public static Map<String, Hyperlink> parseWorksheetHyperlinks(
      final Map<String, byte[]> files,
      final Document worksheetDoc,
      final String sheetPath,
      final WorksheetParserDependencies deps) {
    final Map<String, Hyperlink> hyperlinks = new LinkedHashMap<String, Hyperlink>();
    final String relsPath = deps.buildRelsPath(sheetPath);
    final Map<String, RelationshipEntryLike> relEntries = deps.parseRelationshipEntries(files, relsPath, sheetPath);
    for (final Element node : XmlUtils.getElementsByLocalName(worksheetDoc, "hyperlink")) {
      final String ref = trim(node.getAttribute("ref"));
      if (ref.isEmpty()) {
        continue;
      }
      final String relId = trim(node.getAttribute("r:id")).isEmpty() ? trim(node.getAttribute("id")) : trim(node.getAttribute("r:id"));
      final RelationshipEntryLike relEntry = relId.isEmpty() ? null : relEntries.get(relId);
      final String display = trim(node.getAttribute("display"));
      final String tooltip = trim(node.getAttribute("tooltip"));
      final String location = trim(node.getAttribute("location")).replaceFirst("^#", "");
      final String rawTarget = relEntry == null ? "" : trim(relEntry.getTarget());
      final String targetMode = relEntry == null ? "" : relEntry.getTargetMode();

      final String kind;
      if ("external".equalsIgnoreCase(targetMode)) {
        kind = "external";
      } else if (!location.isEmpty()) {
        kind = "internal";
      } else if (rawTarget.startsWith("#")) {
        kind = "internal";
      } else if (!rawTarget.isEmpty()) {
        kind = "external";
      } else {
        kind = null;
      }
      if (kind == null) {
        continue;
      }
      final String target = "internal".equals(kind) ? (!location.isEmpty() ? location : rawTarget.replaceFirst("^#", "")) : rawTarget;
      if (target.isEmpty()) {
        continue;
      }
      final Hyperlink hyperlink = new Hyperlink(
          kind,
          target,
          !location.isEmpty() ? location : ("internal".equals(kind) ? target : ""),
          tooltip,
          display);
      for (final String address : expandRangeAddresses(ref)) {
        hyperlinks.put(address, hyperlink);
      }
    }
    return hyperlinks;
  }

  public static String shiftReferenceAddress(final String addressText, final int rowOffset, final int colOffset) {
    final java.util.regex.Matcher match = java.util.regex.Pattern.compile("^(\\$?)([A-Z]+)(\\$?)(\\d+)$", java.util.regex.Pattern.CASE_INSENSITIVE)
        .matcher(String.valueOf(addressText));
    if (!match.matches()) {
      return addressText;
    }
    final boolean colAbsolute = "$".equals(match.group(1));
    final boolean rowAbsolute = "$".equals(match.group(3));
    final int baseCol = AddressUtils.lettersToCol(match.group(2));
    final int baseRow = Integer.parseInt(match.group(4));
    final int shiftedCol = colAbsolute ? baseCol : baseCol + colOffset;
    final int shiftedRow = rowAbsolute ? baseRow : baseRow + rowOffset;
    return (colAbsolute ? "$" : "") + AddressUtils.colToLetters(Math.max(1, shiftedCol))
        + (rowAbsolute ? "$" : "") + Math.max(1, shiftedRow);
  }

  public static String translateSharedFormula(final String baseFormulaText, final String baseAddress, final String targetAddress) {
    final AddressUtils.CellAddress basePos = AddressUtils.parseCellAddress(baseAddress);
    final AddressUtils.CellAddress targetPos = AddressUtils.parseCellAddress(targetAddress);
    if (basePos.getRow() == 0 || basePos.getCol() == 0 || targetPos.getRow() == 0 || targetPos.getCol() == 0) {
      return baseFormulaText;
    }
    final int rowOffset = targetPos.getRow() - basePos.getRow();
    final int colOffset = targetPos.getCol() - basePos.getCol();
    final String normalized = String.valueOf(baseFormulaText).replaceFirst("^=", "");
    final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
        "(?:'((?:[^']|'')+)'|([A-Za-z0-9_ ]+))!(\\$?[A-Z]+\\$?\\d+)|(\\$?[A-Z]+\\$?\\d+)");
    final java.util.regex.Matcher matcher = pattern.matcher(normalized);
    final StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      final String quotedSheet = matcher.group(1);
      final String plainSheet = matcher.group(2);
      final String qualifiedAddress = matcher.group(3);
      final String localAddress = matcher.group(4);
      final String address = qualifiedAddress != null ? qualifiedAddress : localAddress;
      if (address == null) {
        matcher.appendReplacement(buffer, java.util.regex.Matcher.quoteReplacement(matcher.group()));
        continue;
      }
      final String shifted = shiftReferenceAddress(address, rowOffset, colOffset);
      final String replacement = qualifiedAddress != null
          ? ((quotedSheet != null ? "'" + quotedSheet + "'" : plainSheet) + "!" + shifted)
          : shifted;
      matcher.appendReplacement(buffer, java.util.regex.Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(buffer);
    final String translated = buffer.toString();
    return translated.startsWith("=") ? translated : "=" + translated;
  }

  public static ParsedSheet parseWorksheet(
      final Map<String, byte[]> files,
      final String sheetName,
      final String sheetPath,
      final int sheetIndex,
      final List<SharedStrings.SharedStringEntry> sharedStrings,
      final List<StylesParser.CellStyleInfo> cellStyles,
      final WorksheetParserDependencies deps) {
    final byte[] bytes = files.get(sheetPath);
    if (bytes == null) {
      throw new IllegalArgumentException("Sheet XML not found: " + sheetPath);
    }
    final Document doc = deps.xmlToDocument(deps.decodeXmlText(bytes));
    final Map<String, SharedFormulaBase> sharedFormulaMap = new LinkedHashMap<String, SharedFormulaBase>();
    final Map<String, Hyperlink> hyperlinks = parseWorksheetHyperlinks(files, doc, sheetPath, deps);
    final List<ParsedCell> cells = new ArrayList<ParsedCell>();

    for (final Element cellElement : XmlUtils.getElementsByLocalName(doc, "c")) {
      final String address = trim(cellElement.getAttribute("r"));
      final AddressUtils.CellAddress position = AddressUtils.parseCellAddress(address);
      final int styleIndex = parseInt(cellElement.getAttribute("s"), 0);
      final StylesParser.CellStyleInfo cellStyle =
          styleIndex >= 0 && styleIndex < cellStyles.size()
              ? cellStyles.get(styleIndex)
              : new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", StylesParser.EMPTY_TEXT_STYLE);

      String formulaOverride = "";
      final Element formulaElement = getFirstTag(cellElement, "f");
      final String formulaType = formulaElement == null ? "" : trim(formulaElement.getAttribute("t"));
      final String spillRef = formulaElement == null ? "" : trim(formulaElement.getAttribute("ref"));
      final String sharedIndex = formulaElement == null ? "" : trim(formulaElement.getAttribute("si"));
      final String formulaText = formulaElement == null ? "" : deps.getTextContent(formulaElement);
      if ("shared".equals(formulaType) && !sharedIndex.isEmpty()) {
        if (!formulaText.isEmpty()) {
          final String normalizedFormula = formulaText.startsWith("=") ? formulaText : "=" + formulaText;
          sharedFormulaMap.put(sharedIndex, new SharedFormulaBase(address, normalizedFormula));
          formulaOverride = normalizedFormula;
        } else if (sharedFormulaMap.containsKey(sharedIndex)) {
          final SharedFormulaBase sharedBase = sharedFormulaMap.get(sharedIndex);
          formulaOverride = translateSharedFormula(sharedBase.getFormulaText(), sharedBase.getAddress(), address);
        }
      }

      final ExtractedCellOutput output = extractCellOutputValue(cellElement, sharedStrings, cellStyle, deps, formulaOverride);
      cells.add(new ParsedCell(
          address,
          position.getRow(),
          position.getCol(),
          output.getValueType(),
          output.getRawValue(),
          output.getOutputValue(),
          output.getFormulaText(),
          output.getResolutionStatus(),
          output.getResolutionSource(),
          output.getCachedValueState(),
          styleIndex,
          cellStyle.getBorders(),
          cellStyle.getNumFmtId(),
          cellStyle.getFormatCode(),
          cellStyle.getTextStyle(),
          output.getRichTextRuns(),
          formulaType,
          spillRef,
          hyperlinks.get(address)));
    }

    final List<AddressUtils.MergeRange> merges = new ArrayList<AddressUtils.MergeRange>();
    for (final Element mergeElement : XmlUtils.getElementsByLocalName(doc, "mergeCell")) {
      merges.add(AddressUtils.parseRangeRef(trim(mergeElement.getAttribute("ref"))));
    }

    int maxRow = 0;
    int maxCol = 0;
    for (final ParsedCell cell : cells) {
      if (cell.getRow() > maxRow) {
        maxRow = cell.getRow();
      }
      if (cell.getCol() > maxCol) {
        maxCol = cell.getCol();
      }
    }
    for (final AddressUtils.MergeRange merge : merges) {
      if (merge.getEndRow() > maxRow) {
        maxRow = merge.getEndRow();
      }
      if (merge.getEndCol() > maxCol) {
        maxCol = merge.getEndCol();
      }
    }

    return new ParsedSheet(sheetName, sheetIndex, sheetPath, cells, merges, maxRow, maxCol);
  }

  private static String collectInlineText(final Element cellElement, final WorksheetParserDependencies deps) {
    final StringBuilder inlineText = new StringBuilder();
    for (final Element textNode : XmlUtils.getElementsByLocalName(cellElement, "t")) {
      inlineText.append(deps.getTextContent(textNode));
    }
    return inlineText.toString();
  }

  private static Element getFirstTag(final Element root, final String localName) {
    final List<Element> elements = XmlUtils.getElementsByLocalName(root, localName);
    return elements.isEmpty() ? null : elements.get(0);
  }

  private static int parseInt(final String value, final int fallback) {
    try {
      return value == null || value.isEmpty() ? fallback : Integer.parseInt(value);
    } catch (final NumberFormatException ex) {
      return fallback;
    }
  }

  private static String trim(final String value) {
    return value == null ? "" : value.trim();
  }

  public interface WorksheetParserDependencies {
    Document xmlToDocument(String xmlText);
    String decodeXmlText(byte[] bytes);
    String getTextContent(Element node);
    Map<String, RelationshipEntryLike> parseRelationshipEntries(Map<String, byte[]> files, String relsPath, String sourcePath);
    String buildRelsPath(String sourcePath);
    String formatCellDisplayValue(String rawValue, StylesParser.CellStyleInfo style);
  }

  public interface RelationshipEntryLike {
    String getTarget();
    String getTargetMode();
    String getType();
  }

  private static final class SharedFormulaBase {
    private final String address;
    private final String formulaText;

    private SharedFormulaBase(final String address, final String formulaText) {
      this.address = address;
      this.formulaText = formulaText;
    }

    private String getAddress() {
      return address;
    }

    private String getFormulaText() {
      return formulaText;
    }
  }

  public static final class Hyperlink {
    private final String kind;
    private final String target;
    private final String location;
    private final String tooltip;
    private final String display;

    public Hyperlink(final String kind, final String target, final String location, final String tooltip, final String display) {
      this.kind = kind;
      this.target = target;
      this.location = location;
      this.tooltip = tooltip;
      this.display = display;
    }

    public String getKind() {
      return kind;
    }

    public String getTarget() {
      return target;
    }

    public String getLocation() {
      return location;
    }

    public String getTooltip() {
      return tooltip;
    }

    public String getDisplay() {
      return display;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof Hyperlink)) {
        return false;
      }
      final Hyperlink that = (Hyperlink) other;
      return Objects.equals(kind, that.kind)
          && Objects.equals(target, that.target)
          && Objects.equals(location, that.location)
          && Objects.equals(tooltip, that.tooltip)
          && Objects.equals(display, that.display);
    }

    @Override
    public int hashCode() {
      return Objects.hash(kind, target, location, tooltip, display);
    }
  }

  public static final class ExtractedCellOutput {
    private final String valueType;
    private final String rawValue;
    private final String outputValue;
    private final String formulaText;
    private final String resolutionStatus;
    private final String resolutionSource;
    private final String cachedValueState;
    private final List<SharedStrings.RichTextRun> richTextRuns;

    public ExtractedCellOutput(
        final String valueType,
        final String rawValue,
        final String outputValue,
        final String formulaText,
        final String resolutionStatus,
        final String resolutionSource,
        final String cachedValueState,
        final List<SharedStrings.RichTextRun> richTextRuns) {
      this.valueType = valueType;
      this.rawValue = rawValue;
      this.outputValue = outputValue;
      this.formulaText = formulaText;
      this.resolutionStatus = resolutionStatus;
      this.resolutionSource = resolutionSource;
      this.cachedValueState = cachedValueState;
      this.richTextRuns = richTextRuns;
    }

    public String getValueType() {
      return valueType;
    }

    public String getRawValue() {
      return rawValue;
    }

    public String getOutputValue() {
      return outputValue;
    }

    public String getFormulaText() {
      return formulaText;
    }

    public String getResolutionStatus() {
      return resolutionStatus;
    }

    public String getResolutionSource() {
      return resolutionSource;
    }

    public String getCachedValueState() {
      return cachedValueState;
    }

    public List<SharedStrings.RichTextRun> getRichTextRuns() {
      return richTextRuns;
    }
  }

  public static final class ParsedCell {
    private final String address;
    private final int row;
    private final int col;
    private final String valueType;
    private final String rawValue;
    private final String outputValue;
    private final String formulaText;
    private final String resolutionStatus;
    private final String resolutionSource;
    private final String cachedValueState;
    private final int styleIndex;
    private final StylesParser.BorderFlags borders;
    private final int numFmtId;
    private final String formatCode;
    private final StylesParser.TextStyle textStyle;
    private final List<SharedStrings.RichTextRun> richTextRuns;
    private final String formulaType;
    private final String spillRef;
    private final Hyperlink hyperlink;

    public ParsedCell(
        final String address,
        final int row,
        final int col,
        final String valueType,
        final String rawValue,
        final String outputValue,
        final String formulaText,
        final String resolutionStatus,
        final String resolutionSource,
        final String cachedValueState,
        final int styleIndex,
        final StylesParser.BorderFlags borders,
        final int numFmtId,
        final String formatCode,
        final StylesParser.TextStyle textStyle,
        final List<SharedStrings.RichTextRun> richTextRuns,
        final String formulaType,
        final String spillRef,
        final Hyperlink hyperlink) {
      this.address = address;
      this.row = row;
      this.col = col;
      this.valueType = valueType;
      this.rawValue = rawValue;
      this.outputValue = outputValue;
      this.formulaText = formulaText;
      this.resolutionStatus = resolutionStatus;
      this.resolutionSource = resolutionSource;
      this.cachedValueState = cachedValueState;
      this.styleIndex = styleIndex;
      this.borders = borders;
      this.numFmtId = numFmtId;
      this.formatCode = formatCode;
      this.textStyle = textStyle;
      this.richTextRuns = richTextRuns;
      this.formulaType = formulaType;
      this.spillRef = spillRef;
      this.hyperlink = hyperlink;
    }

    public String getAddress() {
      return address;
    }

    public int getRow() {
      return row;
    }

    public int getCol() {
      return col;
    }

    public String getOutputValue() {
      return outputValue;
    }

    public String getRawValue() {
      return rawValue;
    }

    public String getValueType() {
      return valueType;
    }

    public String getFormulaText() {
      return formulaText;
    }

    public String getResolutionStatus() {
      return resolutionStatus;
    }

    public String getResolutionSource() {
      return resolutionSource;
    }

    public int getStyleIndex() {
      return styleIndex;
    }

    public StylesParser.BorderFlags getBorders() {
      return borders;
    }

    public int getNumFmtId() {
      return numFmtId;
    }

    public String getFormatCode() {
      return formatCode;
    }

    public StylesParser.TextStyle getTextStyle() {
      return textStyle;
    }

    public List<SharedStrings.RichTextRun> getRichTextRuns() {
      return richTextRuns;
    }

    public Hyperlink getHyperlink() {
      return hyperlink;
    }
  }

  public static final class ParsedSheet {
    private final String name;
    private final int index;
    private final String path;
    private final List<ParsedCell> cells;
    private final List<AddressUtils.MergeRange> merges;
    private final List<ParsedImageAsset> images;
    private final List<ParsedChartAsset> charts;
    private final List<ParsedShapeAsset> shapes;
    private final int maxRow;
    private final int maxCol;

    public ParsedSheet(
        final String name,
        final int index,
        final String path,
        final List<ParsedCell> cells,
        final List<AddressUtils.MergeRange> merges,
        final int maxRow,
        final int maxCol) {
      this(name, index, path, cells, merges, new ArrayList<ParsedImageAsset>(), new ArrayList<ParsedChartAsset>(), new ArrayList<ParsedShapeAsset>(), maxRow, maxCol);
    }

    public ParsedSheet(
        final String name,
        final int index,
        final String path,
        final List<ParsedCell> cells,
        final List<AddressUtils.MergeRange> merges,
        final List<ParsedImageAsset> images,
        final List<ParsedChartAsset> charts,
        final List<ParsedShapeAsset> shapes,
        final int maxRow,
        final int maxCol) {
      this.name = name;
      this.index = index;
      this.path = path;
      this.cells = cells;
      this.merges = merges;
      this.images = images;
      this.charts = charts;
      this.shapes = shapes;
      this.maxRow = maxRow;
      this.maxCol = maxCol;
    }

    public String getName() {
      return name;
    }

    public int getIndex() {
      return index;
    }

    public String getPath() {
      return path;
    }

    public List<ParsedCell> getCells() {
      return cells;
    }

    public List<AddressUtils.MergeRange> getMerges() {
      return merges;
    }

    public List<ParsedImageAsset> getImages() {
      return images;
    }

    public List<ParsedChartAsset> getCharts() {
      return charts;
    }

    public List<ParsedShapeAsset> getShapes() {
      return shapes;
    }

    public int getMaxRow() {
      return maxRow;
    }

    public int getMaxCol() {
      return maxCol;
    }
  }

  public static final class ParsedImageAsset {
    private final String anchor;
    private final String filename;
    private final String path;
    private final byte[] data;

    public ParsedImageAsset(final String anchor, final String filename, final String path, final byte[] data) {
      this.anchor = anchor;
      this.filename = filename;
      this.path = path;
      this.data = data;
    }

    public String getAnchor() {
      return anchor;
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

  public static final class ParsedChartSeries {
    private final String name;
    private final String categoriesRef;
    private final String valuesRef;
    private final String axis;

    public ParsedChartSeries(final String name, final String categoriesRef, final String valuesRef, final String axis) {
      this.name = name;
      this.categoriesRef = categoriesRef;
      this.valuesRef = valuesRef;
      this.axis = axis;
    }

    public String getName() {
      return name;
    }

    public String getCategoriesRef() {
      return categoriesRef;
    }

    public String getValuesRef() {
      return valuesRef;
    }

    public String getAxis() {
      return axis;
    }
  }

  public static final class ParsedChartAsset {
    private final String anchor;
    private final String title;
    private final String chartType;
    private final List<ParsedChartSeries> series;

    public ParsedChartAsset(final String anchor, final String title, final String chartType, final List<ParsedChartSeries> series) {
      this.anchor = anchor;
      this.title = title;
      this.chartType = chartType;
      this.series = series;
    }

    public String getAnchor() {
      return anchor;
    }

    public String getTitle() {
      return title;
    }

    public String getChartType() {
      return chartType;
    }

    public List<ParsedChartSeries> getSeries() {
      return series;
    }
  }

  public static final class ParsedShapeRawEntry {
    private final String key;
    private final String value;

    public ParsedShapeRawEntry(final String key, final String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }

  public static final class ParsedShapeAsset {
    private final String anchor;
    private final List<ParsedShapeRawEntry> rawEntries;
    private final String svgFilename;
    private final String svgPath;
    private final byte[] svgData;

    public ParsedShapeAsset(
        final String anchor,
        final List<ParsedShapeRawEntry> rawEntries,
        final String svgFilename,
        final String svgPath,
        final byte[] svgData) {
      this.anchor = anchor;
      this.rawEntries = rawEntries;
      this.svgFilename = svgFilename;
      this.svgPath = svgPath;
      this.svgData = svgData;
    }

    public String getAnchor() {
      return anchor;
    }

    public List<ParsedShapeRawEntry> getRawEntries() {
      return rawEntries;
    }

    public String getSvgFilename() {
      return svgFilename;
    }

    public String getSvgPath() {
      return svgPath;
    }

    public byte[] getSvgData() {
      return svgData;
    }
  }
}
