/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.worksheetparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assumptions;
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
  void parsesSharedFormulasWithCrossSheetAndAbsoluteReferences() {
    final WorksheetParser.WorksheetParserDependencies deps = createDeps();
    final String worksheetXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        + "<sheetData>"
        + "<row r=\"1\">"
        + "<c r=\"C1\"><f t=\"shared\" si=\"2\">A1+Sheet2!B1+'Other Sheet'!$C1+$D$1</f><v>10</v></c>"
        + "<c r=\"D1\"><f t=\"shared\" si=\"2\"/><v>11</v></c>"
        + "</row>"
        + "<row r=\"3\">"
        + "<c r=\"C3\"><f t=\"shared\" si=\"2\"/><v>12</v></c>"
        + "<c r=\"D3\"><f t=\"shared\" si=\"2\"/><v>13</v></c>"
        + "</row>"
        + "</sheetData>"
        + "</worksheet>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/sheet1.xml", worksheetXml.getBytes(StandardCharsets.UTF_8));
    final List<StylesParser.CellStyleInfo> cellStyles = Arrays.asList(
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", StylesParser.EMPTY_TEXT_STYLE));

    final WorksheetParser.ParsedSheet sheet = WorksheetParser.parseWorksheet(
        files,
        "Sheet1",
        "xl/worksheets/sheet1.xml",
        1,
        new ArrayList<SharedStrings.SharedStringEntry>(),
        cellStyles,
        deps);

    assertEquals("=A1+Sheet2!B1+'Other Sheet'!$C1+$D$1", findCell(sheet, "C1").getFormulaText());
    assertEquals("=B1+Sheet2!C1+'Other Sheet'!$C1+$D$1", findCell(sheet, "D1").getFormulaText());
    assertEquals("=A3+Sheet2!B3+'Other Sheet'!$C3+$D$1", findCell(sheet, "C3").getFormulaText());
    assertEquals("=B3+Sheet2!C3+'Other Sheet'!$C3+$D$1", findCell(sheet, "D3").getFormulaText());
    assertEquals("13", findCell(sheet, "D3").getOutputValue());
    assertEquals(3, sheet.getMaxRow());
    assertEquals(4, sheet.getMaxCol());
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
  void expandsHyperlinkRangesAndHashTargetsAcrossCells() {
    final WorksheetParser.WorksheetParserDependencies deps = createDeps();
    final String worksheetXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
        + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
        + "<sheetData>"
        + "<row r=\"1\">"
        + "<c r=\"A1\" t=\"inlineStr\"><is><t>A</t></is></c>"
        + "<c r=\"B1\" t=\"inlineStr\"><is><t>B</t></is></c>"
        + "</row>"
        + "<row r=\"2\">"
        + "<c r=\"A2\" t=\"inlineStr\"><is><t>C</t></is></c>"
        + "<c r=\"B2\" t=\"inlineStr\"><is><t>D</t></is></c>"
        + "</row>"
        + "</sheetData>"
        + "<hyperlinks><hyperlink ref=\"A1:B2\" location=\"#'Other Sheet'!D4\" display=\"Range\" tooltip=\"jump\"/></hyperlinks>"
        + "</worksheet>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/worksheets/sheet1.xml", worksheetXml.getBytes(StandardCharsets.UTF_8));

    final WorksheetParser.ParsedSheet sheet = WorksheetParser.parseWorksheet(
        files,
        "Sheet1",
        "xl/worksheets/sheet1.xml",
        1,
        new ArrayList<SharedStrings.SharedStringEntry>(),
        Arrays.asList(new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "General", StylesParser.EMPTY_TEXT_STYLE)),
        deps);

    final WorksheetParser.Hyperlink expected =
        new WorksheetParser.Hyperlink("internal", "'Other Sheet'!D4", "'Other Sheet'!D4", "jump", "Range");
    assertEquals(expected, findCell(sheet, "A1").getHyperlink());
    assertEquals(expected, findCell(sheet, "B1").getHyperlink());
    assertEquals(expected, findCell(sheet, "A2").getHyperlink());
    assertEquals(expected, findCell(sheet, "B2").getHyperlink());
    assertEquals(Arrays.asList("B2", "C2", "B3", "C3"), WorksheetParser.expandRangeAddresses("B2:C3"));
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

  @Test
  void parsesUpstreamFormulaCrossSheetFixtureWithConcreteFollowerCoverageWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-crosssheet-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader.ParsedWorkbook workbook =
        jp.igapyon.mikuxlsx2md.core.Core.parseWorkbook(Files.readAllBytes(fixturePath), "formula-crosssheet-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet1 = workbook.getSheets().get(0);
    final WorksheetParser.ParsedSheet sheet2 = workbook.getSheets().get(1);
    final WorksheetParser.ParsedSheet sheet3 = workbook.getSheets().get(2);

    assertEquals(Arrays.asList("Sheet1", "Sheet2", "日本語シート"),
        Arrays.asList(sheet1.getName(), sheet2.getName(), sheet3.getName()));
    assertEquals("xl/worksheets/sheet1.xml", sheet1.getPath());
    assertEquals("xl/worksheets/sheet2.xml", sheet2.getPath());
    assertEquals("xl/worksheets/sheet3.xml", sheet3.getPath());
    assertEquals(23, sheet1.getCells().size());
    assertEquals(5, sheet2.getCells().size());
    assertEquals(1, sheet3.getCells().size());
    assertEquals("複数シート参照サンプル", findCell(sheet1, "A1").getOutputValue());
    assertEquals("=Sheet2!B3", findCell(sheet1, "B3").getFormulaText());
    assertEquals("CrossValue", findCell(sheet1, "B3").getOutputValue());
    assertEquals("resolved", findCell(sheet1, "B3").getResolutionStatus());
    assertEquals("cached_value", findCell(sheet1, "B3").getResolutionSource());
    assertEquals("present_nonempty", findCell(sheet1, "B3").getCachedValueState());
    assertEquals("=日本語シート!C4", findCell(sheet1, "B4").getFormulaText());
    assertEquals("日本語参照値", findCell(sheet1, "B4").getOutputValue());
    assertEquals("resolved", findCell(sheet1, "B4").getResolutionStatus());
    assertEquals("=SUM(Sheet2!A1:B2)", findCell(sheet1, "B5").getFormulaText());
    assertEquals("10", findCell(sheet1, "B5").getOutputValue());
    assertEquals("CrossValue", findCell(sheet2, "B3").getOutputValue());
    assertEquals("日本語参照値", findCell(sheet3, "C4").getOutputValue());
  }

  @Test
  void parsesUpstreamFormulaSharedFixtureWithExtendedFollowerCoverageWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("formula", "formula-shared-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader.ParsedWorkbook workbook =
        jp.igapyon.mikuxlsx2md.core.Core.parseWorkbook(Files.readAllBytes(fixturePath), "formula-shared-sample01.xlsx");
    final WorksheetParser.ParsedSheet sheet = workbook.getSheets().get(0);

    assertEquals("formula", sheet.getName());
    assertEquals("xl/worksheets/sheet1.xml", sheet.getPath());
    assertEquals(13, sheet.getMaxRow());
    assertEquals(4, sheet.getMaxCol());
    assertEquals(27, sheet.getCells().size());
    assertEquals("No", findCell(sheet, "A1").getOutputValue());
    assertEquals("連番", findCell(sheet, "B1").getOutputValue());
    assertEquals("shared formula サンプル", findCell(sheet, "D1").getOutputValue());
    assertEquals("=B2+1", findCell(sheet, "B3").getFormulaText());
    assertEquals("2", findCell(sheet, "B3").getOutputValue());
    assertEquals("resolved", findCell(sheet, "B3").getResolutionStatus());
    assertEquals("cached_value", findCell(sheet, "B3").getResolutionSource());
    assertEquals("present_nonempty", findCell(sheet, "B3").getCachedValueState());
    assertEquals("=B3+1", findCell(sheet, "B4").getFormulaText());
    assertEquals("3", findCell(sheet, "B4").getOutputValue());
    assertEquals("=B4+1", findCell(sheet, "B5").getFormulaText());
    assertEquals("4", findCell(sheet, "B5").getOutputValue());
    assertEquals("=B5+1", findCell(sheet, "B6").getFormulaText());
    assertEquals("5", findCell(sheet, "B6").getOutputValue());
    assertEquals("=B6+1", findCell(sheet, "B7").getFormulaText());
    assertEquals("6", findCell(sheet, "B7").getOutputValue());
    assertEquals("=B7+1", findCell(sheet, "B8").getFormulaText());
    assertEquals("7", findCell(sheet, "B8").getOutputValue());
    assertEquals("=B8+1", findCell(sheet, "B9").getFormulaText());
    assertEquals("8", findCell(sheet, "B9").getOutputValue());
    assertEquals("=B9+1", findCell(sheet, "B10").getFormulaText());
    assertEquals("9", findCell(sheet, "B10").getOutputValue());
    assertEquals("=B10+1", findCell(sheet, "B11").getFormulaText());
    assertEquals("10", findCell(sheet, "B11").getOutputValue());
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

  private static Path resolveFixturePath(final String group, final String fileName) {
    final Path local = Paths.get("workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
    if (Files.isRegularFile(local)) {
      return local;
    }
    return Paths.get("..", "workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
  }
}
