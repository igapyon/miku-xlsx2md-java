/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.worksheetparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.relsparser.RelsParser;
import jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings;
import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;
import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

class WorksheetParserTest {
  @Test
  void extractsSharedStringAndBooleanCellValues() {
    final StylesParser.CellStyleInfo cellStyle =
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", StylesParser.EMPTY_TEXT_STYLE);
    final Element sharedCell = XmlUtils.xmlToDocument("<c t=\"s\"><v>1</v></c>").getDocumentElement();
    final Element boolCell = XmlUtils.xmlToDocument("<c t=\"b\"><v>1</v></c>").getDocumentElement();
    final WorksheetParser.WorksheetParserDependencies deps = createDeps();

    final WorksheetParser.ExtractedCellOutput sharedResult = WorksheetParser.extractCellOutputValue(
        sharedCell,
        Arrays.asList(
            new SharedStrings.SharedStringEntry("A", null),
            new SharedStrings.SharedStringEntry("B", null)),
        cellStyle,
        deps,
        "");
    final WorksheetParser.ExtractedCellOutput boolResult =
        WorksheetParser.extractCellOutputValue(boolCell, new ArrayList<SharedStrings.SharedStringEntry>(), cellStyle, deps, "");

    assertEquals("s", sharedResult.getValueType());
    assertEquals("B", sharedResult.getOutputValue());
    assertEquals("b", boolResult.getValueType());
    assertEquals("TRUE", boolResult.getOutputValue());
  }

  @Test
  void translatesSharedFormulasAcrossRelativeReferences() {
    assertEquals("=A3+$B$1", WorksheetParser.translateSharedFormula("=A1+$B$1", "C1", "C3"));
    assertEquals("=B3+$B$1+'Other Sheet'!D3", WorksheetParser.translateSharedFormula(
        "=A1+$B$1+'Other Sheet'!C1",
        "C1",
        "D3"));
  }

  @Test
  void parsesWorksheetCellsMergesAndSharedFormulas() {
    final WorksheetParser.WorksheetParserDependencies deps = createDeps();
    final String worksheetXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        + "<sheetData>"
        + "<row r=\"1\">"
        + "<c r=\"A1\" t=\"s\"><v>0</v></c>"
        + "<c r=\"B1\" s=\"1\"><v>0.125</v></c>"
        + "<c r=\"C1\"><f t=\"shared\" si=\"0\">A1</f><v>1</v></c>"
        + "</row>"
        + "<row r=\"2\">"
        + "<c r=\"C2\"><f t=\"shared\" si=\"0\"/><v>2</v></c>"
        + "</row>"
        + "</sheetData>"
        + "<mergeCells count=\"1\"><mergeCell ref=\"A1:B2\"/></mergeCells>"
        + "</worksheet>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/sheet1.xml", worksheetXml.getBytes(StandardCharsets.UTF_8));
    final List<StylesParser.CellStyleInfo> cellStyles = Arrays.asList(
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", StylesParser.EMPTY_TEXT_STYLE),
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 10, "0.0%", StylesParser.EMPTY_TEXT_STYLE));

    final WorksheetParser.ParsedSheet sheet = WorksheetParser.parseWorksheet(
        files,
        "Sheet1",
        "xl/worksheets/sheet1.xml",
        1,
        Arrays.asList(new SharedStrings.SharedStringEntry("Hello", null)),
        cellStyles,
        deps);

    assertEquals("Hello", findCell(sheet, "A1").getOutputValue());
    assertEquals("12.5%", findCell(sheet, "B1").getOutputValue());
    assertEquals("=A1", findCell(sheet, "C1").getFormulaText());
    assertEquals("=A2", findCell(sheet, "C2").getFormulaText());
    assertEquals(Arrays.asList(AddressUtilsLike.mergeRange("A1:B2")), sheet.getMerges());
    assertEquals(2, sheet.getMaxRow());
    assertEquals(3, sheet.getMaxCol());
  }

  @Test
  void parsesWorksheetHyperlinksFromLocalRefsAndExternalRelationships() {
    final WorksheetParser.WorksheetParserDependencies deps = createDeps();
    final String worksheetXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
        + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
        + "<sheetData>"
        + "<row r=\"1\">"
        + "<c r=\"A1\" t=\"inlineStr\"><is><t>Open</t></is></c>"
        + "<c r=\"B1\" t=\"inlineStr\"><is><t>Jump</t></is></c>"
        + "</row>"
        + "</sheetData>"
        + "<hyperlinks>"
        + "<hyperlink ref=\"A1\" r:id=\"rId1\"/>"
        + "<hyperlink ref=\"B1\" location=\"'Other Sheet'!C3\" tooltip=\"go\"/>"
        + "</hyperlinks>"
        + "</worksheet>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/sheet1.xml", worksheetXml.getBytes(StandardCharsets.UTF_8));
    files.put("xl/worksheets/_rels/sheet1.xml.rels", (
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Target=\"https://example.com/\" TargetMode=\"External\"/>"
            + "</Relationships>").getBytes(StandardCharsets.UTF_8));

    final WorksheetParser.ParsedSheet sheet = WorksheetParser.parseWorksheet(
        files,
        "Sheet1",
        "xl/worksheets/sheet1.xml",
        1,
        new ArrayList<SharedStrings.SharedStringEntry>(),
        Arrays.asList(new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", StylesParser.EMPTY_TEXT_STYLE)),
        deps);

    assertEquals(
        new WorksheetParser.Hyperlink("external", "https://example.com/", "", "", ""),
        findCell(sheet, "A1").getHyperlink());
    assertEquals(
        new WorksheetParser.Hyperlink("internal", "'Other Sheet'!C3", "'Other Sheet'!C3", "go", ""),
        findCell(sheet, "B1").getHyperlink());
  }

  @Test
  void attachesCellTextStyleToSharedInlineBooleanAndFormattedValues() {
    final WorksheetParser.WorksheetParserDependencies deps = createDeps();
    final StylesParser.CellStyleInfo boldStyle =
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", new StylesParser.TextStyle(true, false, false, false));
    final StylesParser.CellStyleInfo underlineStyle =
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", new StylesParser.TextStyle(false, false, false, true));
    final StylesParser.CellStyleInfo strikePercentStyle =
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 10, "0.0%", new StylesParser.TextStyle(false, false, true, false));

    final Element sharedCell = XmlUtils.xmlToDocument("<c t=\"s\"><v>0</v></c>").getDocumentElement();
    final WorksheetParser.ExtractedCellOutput sharedResult = WorksheetParser.extractCellOutputValue(
        sharedCell,
        Arrays.asList(new SharedStrings.SharedStringEntry("Styled", Arrays.asList(
            new SharedStrings.RichTextRun("Sty", false, true, false, false),
            new SharedStrings.RichTextRun("led", false, true, false, false)))),
        boldStyle,
        deps,
        "");

    assertEquals(Arrays.asList(new SharedStrings.RichTextRun("Styled", true, true, false, false)), sharedResult.getRichTextRuns());

    final Element inlineCell = XmlUtils.xmlToDocument(
        "<c t=\"inlineStr\"><is>"
            + "<r><rPr><i/></rPr><t>In</t></r>"
            + "<r><rPr><i/></rPr><t>line</t></r>"
            + "</is></c>").getDocumentElement();
    final WorksheetParser.ExtractedCellOutput inlineResult =
        WorksheetParser.extractCellOutputValue(inlineCell, new ArrayList<SharedStrings.SharedStringEntry>(), underlineStyle, deps, "");

    assertEquals("Inline", inlineResult.getOutputValue());
    assertEquals(Arrays.asList(new SharedStrings.RichTextRun("Inline", false, true, false, true)), inlineResult.getRichTextRuns());

    final Element boolCell = XmlUtils.xmlToDocument("<c t=\"b\"><v>1</v></c>").getDocumentElement();
    final WorksheetParser.ExtractedCellOutput boolResult =
        WorksheetParser.extractCellOutputValue(boolCell, new ArrayList<SharedStrings.SharedStringEntry>(), strikePercentStyle, deps, "");

    assertEquals(Arrays.asList(new SharedStrings.RichTextRun("TRUE", false, false, true, false)), boolResult.getRichTextRuns());

    final Element formattedCell = XmlUtils.xmlToDocument("<c><v>0.125</v></c>").getDocumentElement();
    final WorksheetParser.ExtractedCellOutput formattedResult =
        WorksheetParser.extractCellOutputValue(formattedCell, new ArrayList<SharedStrings.SharedStringEntry>(), strikePercentStyle, deps, "");

    assertEquals("12.5%", formattedResult.getOutputValue());
    assertEquals(Arrays.asList(new SharedStrings.RichTextRun("12.5%", false, false, true, false)), formattedResult.getRichTextRuns());
  }

  @Test
  void exposesFormulaCachedStateTypeAndSpillRef() {
    final WorksheetParser.WorksheetParserDependencies deps = createDeps();
    final StylesParser.CellStyleInfo cellStyle =
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", StylesParser.EMPTY_TEXT_STYLE);

    final WorksheetParser.ExtractedCellOutput fallback = WorksheetParser.extractCellOutputValue(
        XmlUtils.xmlToDocument("<c><f>A1</f></c>").getDocumentElement(),
        new ArrayList<SharedStrings.SharedStringEntry>(),
        cellStyle,
        deps,
        "");
    assertEquals("fallback_formula", fallback.getResolutionStatus());
    assertEquals("formula_text", fallback.getResolutionSource());
    assertEquals("absent", fallback.getCachedValueState());

    final WorksheetParser.ExtractedCellOutput emptyCached = WorksheetParser.extractCellOutputValue(
        XmlUtils.xmlToDocument("<c><f>A1</f><v/></c>").getDocumentElement(),
        new ArrayList<SharedStrings.SharedStringEntry>(),
        cellStyle,
        deps,
        "");
    assertEquals("resolved", emptyCached.getResolutionStatus());
    assertEquals("cached_value", emptyCached.getResolutionSource());
    assertEquals("present_empty", emptyCached.getCachedValueState());

    final WorksheetParser.ExtractedCellOutput external = WorksheetParser.extractCellOutputValue(
        XmlUtils.xmlToDocument("<c><f>'[other.xlsx]Sheet1'!A1</f></c>").getDocumentElement(),
        new ArrayList<SharedStrings.SharedStringEntry>(),
        cellStyle,
        deps,
        "");
    assertEquals("unsupported_external", external.getResolutionStatus());
    assertEquals("external_unsupported", external.getResolutionSource());
    assertEquals("='[other.xlsx]Sheet1'!A1", external.getFormulaText());

    final String worksheetXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        + "<sheetData><row r=\"1\"><c r=\"A1\"><f t=\"array\" ref=\"A1:B2\">SUM(A1:B1)</f><v>3</v></c></row></sheetData>"
        + "</worksheet>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/sheet1.xml", worksheetXml.getBytes(StandardCharsets.UTF_8));
    final WorksheetParser.ParsedSheet sheet = WorksheetParser.parseWorksheet(
        files,
        "Sheet1",
        "xl/worksheets/sheet1.xml",
        1,
        new ArrayList<SharedStrings.SharedStringEntry>(),
        Arrays.asList(cellStyle),
        deps);

    assertEquals("array", findCell(sheet, "A1").getFormulaType());
    assertEquals("A1:B2", findCell(sheet, "A1").getSpillRef());
    assertEquals("present_nonempty", findCell(sheet, "A1").getCachedValueState());
    assertNull(findCell(sheet, "A1").getRichTextRuns());
  }

  private static WorksheetParser.ParsedCell findCell(final WorksheetParser.ParsedSheet sheet, final String address) {
    for (final WorksheetParser.ParsedCell cell : sheet.getCells()) {
      if (address.equals(cell.getAddress())) {
        return cell;
      }
    }
    throw new IllegalArgumentException("Cell not found: " + address);
  }

  private static WorksheetParser.WorksheetParserDependencies createDeps() {
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
          final RelsParser.RelationshipEntry relEntry = entry.getValue();
          result.put(entry.getKey(), new WorksheetParser.RelationshipEntryLike() {
            @Override
            public String getTarget() {
              return relEntry.getTarget();
            }

            @Override
            public String getTargetMode() {
              return relEntry.getTargetMode();
            }

            @Override
            public String getType() {
              return relEntry.getType();
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
        if (style.getNumFmtId() == 10) {
          return String.format(java.util.Locale.ROOT, "%.1f%%", Double.parseDouble(rawValue) * 100.0d);
        }
        return null;
      }
    };
  }

  private static final class AddressUtilsLike {
    private static jp.igapyon.mikuxlsx2md.addressutils.AddressUtils.MergeRange mergeRange(final String ref) {
      return jp.igapyon.mikuxlsx2md.addressutils.AddressUtils.parseRangeRef(ref);
    }
  }
}
