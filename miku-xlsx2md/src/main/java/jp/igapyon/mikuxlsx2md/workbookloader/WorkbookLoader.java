/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.workbookloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.relsparser.RelsParser;
import jp.igapyon.mikuxlsx2md.cellformat.CellFormat;
import jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings;
import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;
import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;
import jp.igapyon.mikuxlsx2md.zipio.ZipIo;

public final class WorkbookLoader {
  private WorkbookLoader() {
  }

  public static ParsedWorkbook parseWorkbook(final byte[] workbookBytes, final String workbookName) {
    return parseWorkbook(workbookBytes, workbookName, createDefaultDependencies());
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
    final List<SharedStrings.SharedStringEntry> sharedStrings = deps.parseSharedStrings(files);
    final List<StylesParser.CellStyleInfo> cellStyles = deps.parseCellStyles(files);
    final Map<String, String> rels = deps.parseRelationships(files, "xl/_rels/workbook.xml.rels", "xl/workbook.xml");
    final Document workbookDoc = deps.xmlToDocument(deps.decodeXmlText(workbookXmlBytes));
    final List<Element> sheetNodes = XmlUtils.getElementsByLocalName(workbookDoc, "sheet");
    final List<String> sheetNames = new ArrayList<String>();
    for (int index = 0; index < sheetNodes.size(); index += 1) {
      final String name = sheetNodes.get(index).getAttribute("name");
      sheetNames.add(name == null || name.isEmpty() ? "Sheet" + (index + 1) : name);
    }
    final List<DefinedName> definedNames = parseDefinedNames(workbookDoc, sheetNames);
    final List<WorksheetParser.ParsedSheet> sheets = new ArrayList<WorksheetParser.ParsedSheet>();
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

  private static WorkbookLoaderDependencies createDefaultDependencies() {
    return new WorkbookLoaderDependencies() {
      @Override
      public Map<String, byte[]> unzipEntries(final byte[] workbookBytes) {
        return ZipIo.unzipEntries(workbookBytes);
      }

      @Override
      public List<SharedStrings.SharedStringEntry> parseSharedStrings(final Map<String, byte[]> files) {
        return SharedStrings.parseSharedStrings(files);
      }

      @Override
      public List<StylesParser.CellStyleInfo> parseCellStyles(final Map<String, byte[]> files) {
        return StylesParser.parseCellStyles(files);
      }

      @Override
      public Map<String, String> parseRelationships(final Map<String, byte[]> files, final String relsPath, final String sourcePath) {
        return RelsParser.parseRelationships(files, relsPath, sourcePath);
      }

      @Override
      public Document xmlToDocument(final String xmlText) {
        return XmlUtils.xmlToDocument(xmlText);
      }

      @Override
      public String decodeXmlText(final byte[] bytes) {
        return XmlUtils.decodeXmlText(bytes);
      }

      @Override
      public WorksheetParser.ParsedSheet parseWorksheet(
          final Map<String, byte[]> files,
          final String sheetName,
          final String sheetPath,
          final int sheetIndex,
          final List<SharedStrings.SharedStringEntry> sharedStrings,
          final List<StylesParser.CellStyleInfo> cellStyles) {
        return WorksheetParser.parseWorksheet(files, sheetName, sheetPath, sheetIndex, sharedStrings, cellStyles, createWorksheetParserDependencies());
      }

      @Override
      public void postProcessWorkbook(final ParsedWorkbook workbook) {
      }
    };
  }

  private static WorksheetParser.WorksheetParserDependencies createWorksheetParserDependencies() {
    return new WorksheetParser.WorksheetParserDependencies() {
      @Override
      public Document xmlToDocument(final String xmlText) {
        return XmlUtils.xmlToDocument(xmlText);
      }

      @Override
      public String decodeXmlText(final byte[] bytes) {
        return XmlUtils.decodeXmlText(bytes);
      }

      @Override
      public String getTextContent(final Element node) {
        return XmlUtils.getTextContent(node);
      }

      @Override
      public Map<String, WorksheetParser.RelationshipEntryLike> parseRelationshipEntries(
          final Map<String, byte[]> files,
          final String relsPath,
          final String sourcePath) {
        final Map<String, WorksheetParser.RelationshipEntryLike> result = new LinkedHashMap<String, WorksheetParser.RelationshipEntryLike>();
        for (final Map.Entry<String, RelsParser.RelationshipEntry> entry :
            RelsParser.parseRelationshipEntries(files, relsPath, sourcePath).entrySet()) {
          final RelsParser.RelationshipEntry relationshipEntry = entry.getValue();
          result.put(entry.getKey(), new WorksheetParser.RelationshipEntryLike() {
            @Override
            public String getTarget() {
              return relationshipEntry.getTarget();
            }

            @Override
            public String getTargetMode() {
              return relationshipEntry.getTargetMode();
            }

            @Override
            public String getType() {
              return relationshipEntry.getType();
            }
          });
        }
        return result;
      }

      @Override
      public String buildRelsPath(final String sourcePath) {
        return RelsParser.buildRelsPath(sourcePath);
      }

      @Override
      public String formatCellDisplayValue(final String rawValue, final StylesParser.CellStyleInfo style) {
        return CellFormat.formatCellDisplayValue(rawValue, style);
      }
    };
  }

  public interface WorkbookLoaderDependencies {
    Map<String, byte[]> unzipEntries(byte[] workbookBytes);
    List<SharedStrings.SharedStringEntry> parseSharedStrings(Map<String, byte[]> files);
    List<StylesParser.CellStyleInfo> parseCellStyles(Map<String, byte[]> files);
    Map<String, String> parseRelationships(Map<String, byte[]> files, String relsPath, String sourcePath);
    Document xmlToDocument(String xmlText);
    String decodeXmlText(byte[] bytes);
    WorksheetParser.ParsedSheet parseWorksheet(
        Map<String, byte[]> files,
        String sheetName,
        String sheetPath,
        int sheetIndex,
        List<SharedStrings.SharedStringEntry> sharedStrings,
        List<StylesParser.CellStyleInfo> cellStyles);
    void postProcessWorkbook(ParsedWorkbook workbook);
  }

  public static final class ParsedWorkbook {
    private final String name;
    private final List<WorksheetParser.ParsedSheet> sheets;
    private final List<SharedStrings.SharedStringEntry> sharedStrings;
    private final List<DefinedName> definedNames;

    public ParsedWorkbook(
        final String name,
        final List<WorksheetParser.ParsedSheet> sheets,
        final List<SharedStrings.SharedStringEntry> sharedStrings,
        final List<DefinedName> definedNames) {
      this.name = name;
      this.sheets = sheets;
      this.sharedStrings = sharedStrings;
      this.definedNames = definedNames;
    }

    public String getName() {
      return name;
    }

    public List<WorksheetParser.ParsedSheet> getSheets() {
      return sheets;
    }

    public List<SharedStrings.SharedStringEntry> getSharedStrings() {
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
}
