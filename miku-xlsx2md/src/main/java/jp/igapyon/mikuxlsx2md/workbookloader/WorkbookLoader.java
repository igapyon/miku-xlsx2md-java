/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.workbookloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

public final class WorkbookLoader {
  private WorkbookLoader() {
  }

  public static List<DefinedName> parseDefinedNames(
      final Document workbookDoc,
      final List<String> sheetNames) {
    final List<DefinedName> result = new ArrayList<DefinedName>();
    for (final Element element : XmlUtils.getElementsByLocalName(workbookDoc, "definedName")) {
      final String name = element.getAttribute("name");
      if (name == null || name.isEmpty() || name.startsWith("_xlnm.")) {
        continue;
      }
      final String formulaText = XmlUtils.getTextContent(element).trim();
      if (formulaText.isEmpty()) {
        continue;
      }
      final String localSheetIdText = element.getAttribute("localSheetId");
      final String localSheetName;
      if (localSheetIdText == null || localSheetIdText.isEmpty()) {
        localSheetName = null;
      } else {
        final int localSheetId = Integer.parseInt(localSheetIdText);
        localSheetName = localSheetId >= 0 && localSheetId < sheetNames.size() ? sheetNames.get(localSheetId) : null;
      }
      result.add(new DefinedName(
          name,
          formulaText.startsWith("=") ? formulaText : "=" + formulaText,
          localSheetName));
    }
    return result;
  }

  public static ParsedWorkbook parseWorkbook(
      final byte[] workbookBytes,
      final String workbookName,
      final WorkbookLoaderDependencies deps) {
    final Map<String, byte[]> files = deps.unzipEntries(workbookBytes);
    final byte[] workbookXmlBytes = files.get("xl/workbook.xml");
    if (workbookXmlBytes == null) {
      throw new IllegalArgumentException("xl/workbook.xml was not found.");
    }
    final List<SharedStringEntry> sharedStrings = deps.parseSharedStrings(files);
    final List<CellStyleInfo> cellStyles = deps.parseCellStyles(files);
    final Map<String, String> rels = deps.parseRelationships(files, "xl/_rels/workbook.xml.rels", "xl/workbook.xml");
    final Document workbookDoc = deps.xmlToDocument(deps.decodeXmlText(workbookXmlBytes));
    final List<Element> sheetNodes = XmlUtils.getElementsByLocalName(workbookDoc, "sheet");
    final List<String> sheetNames = new ArrayList<String>();
    for (int index = 0; index < sheetNodes.size(); index += 1) {
      final String name = sheetNodes.get(index).getAttribute("name");
      sheetNames.add(name == null || name.isEmpty() ? "Sheet" + (index + 1) : name);
    }
    final List<DefinedName> definedNames = parseDefinedNames(workbookDoc, sheetNames);
    final List<Object> sheets = new ArrayList<Object>();
    for (int index = 0; index < sheetNodes.size(); index += 1) {
      final Element sheetNode = sheetNodes.get(index);
      final String name = sheetNode.getAttribute("name") == null || sheetNode.getAttribute("name").isEmpty()
          ? "Sheet" + (index + 1) : sheetNode.getAttribute("name");
      final String relId = sheetNode.getAttribute("r:id") == null ? "" : sheetNode.getAttribute("r:id");
      final String sheetPath = rels.containsKey(relId) ? rels.get(relId) : "";
      sheets.add(deps.parseWorksheet(files, name, sheetPath, index + 1, sharedStrings, cellStyles));
    }
    final ParsedWorkbook workbook = new ParsedWorkbook(workbookName, sheets, sharedStrings, definedNames);
    deps.postProcessWorkbook(workbook);
    return workbook;
  }

  public interface WorkbookLoaderDependencies {
    Map<String, byte[]> unzipEntries(byte[] workbookBytes);
    List<SharedStringEntry> parseSharedStrings(Map<String, byte[]> files);
    List<CellStyleInfo> parseCellStyles(Map<String, byte[]> files);
    Map<String, String> parseRelationships(Map<String, byte[]> files, String relsPath, String sourcePath);
    Document xmlToDocument(String xmlText);
    String decodeXmlText(byte[] bytes);
    Object parseWorksheet(
        Map<String, byte[]> files,
        String sheetName,
        String sheetPath,
        int sheetIndex,
        List<SharedStringEntry> sharedStrings,
        List<CellStyleInfo> cellStyles);
    void postProcessWorkbook(ParsedWorkbook workbook);
  }

  public static final class ParsedWorkbook {
    private final String name;
    private final List<Object> sheets;
    private final List<SharedStringEntry> sharedStrings;
    private final List<DefinedName> definedNames;

    public ParsedWorkbook(
        final String name,
        final List<Object> sheets,
        final List<SharedStringEntry> sharedStrings,
        final List<DefinedName> definedNames) {
      this.name = name;
      this.sheets = sheets;
      this.sharedStrings = sharedStrings;
      this.definedNames = definedNames;
    }

    public String getName() {
      return name;
    }

    public List<Object> getSheets() {
      return sheets;
    }

    public List<SharedStringEntry> getSharedStrings() {
      return sharedStrings;
    }

    public List<DefinedName> getDefinedNames() {
      return definedNames;
    }
  }

  public static final class DefinedName {
    private final String name;
    private final String formulaText;
    private final String localSheetName;

    public DefinedName(final String name, final String formulaText, final String localSheetName) {
      this.name = name;
      this.formulaText = formulaText;
      this.localSheetName = localSheetName;
    }

    public String getName() {
      return name;
    }

    public String getFormulaText() {
      return formulaText;
    }

    public String getLocalSheetName() {
      return localSheetName;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof DefinedName)) {
        return false;
      }
      final DefinedName that = (DefinedName) other;
      return java.util.Objects.equals(name, that.name)
          && java.util.Objects.equals(formulaText, that.formulaText)
          && java.util.Objects.equals(localSheetName, that.localSheetName);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(name, formulaText, localSheetName);
    }
  }

  public static final class SharedStringEntry {
    private final String text;

    public SharedStringEntry(final String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof SharedStringEntry)) {
        return false;
      }
      return java.util.Objects.equals(text, ((SharedStringEntry) other).text);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(text);
    }
  }

  public static final class CellStyleInfo {
    private final int numFmtId;
    private final String formatCode;

    public CellStyleInfo(final int numFmtId, final String formatCode) {
      this.numFmtId = numFmtId;
      this.formatCode = formatCode;
    }

    public int getNumFmtId() {
      return numFmtId;
    }

    public String getFormatCode() {
      return formatCode;
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
      return numFmtId == that.numFmtId && java.util.Objects.equals(formatCode, that.formatCode);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(Integer.valueOf(numFmtId), formatCode);
    }
  }
}
