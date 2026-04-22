/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.cellformat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;

class CellFormatTest {
  @Test
  void formatsDatePercentFractionAndCurrencyValues() {
    assertEquals("1900/1/12", CellFormat.formatCellDisplayValue("13",
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 14, "yyyy/m/d", StylesParser.EMPTY_TEXT_STYLE)));
    assertEquals("98.7%", CellFormat.formatCellDisplayValue("0.987",
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 10, "0.0%", StylesParser.EMPTY_TEXT_STYLE)));
    assertEquals("3/4", CellFormat.formatCellDisplayValue("0.75",
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 12, "# ?/?", StylesParser.EMPTY_TEXT_STYLE)));
    assertEquals("¥ 1,024,768", CellFormat.formatCellDisplayValue("1024768",
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 42, "¥ * #,##0", StylesParser.EMPTY_TEXT_STYLE)));
  }

  @Test
  void formatsSpecialTextAndScientificNotationPatterns() {
    assertEquals("1 0 2 3 4 5 6", CellFormat.formatCellDisplayValue("1023456",
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 186, "[DBNum3]General", StylesParser.EMPTY_TEXT_STYLE)));
    assertEquals("1.023456E+06", CellFormat.formatCellDisplayValue("1023456",
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 11, "0.000000E+00", StylesParser.EMPTY_TEXT_STYLE)));
    assertEquals("-", CellFormat.formatCellDisplayValue("0",
        new StylesParser.CellStyleInfo(StylesParser.EMPTY_BORDERS, 0, "0;0;\"-\"", StylesParser.EMPTY_TEXT_STYLE)));
  }

  @Test
  void parsesDateLikeTextIntoExcelSerialCompatibleNumbers() {
    assertEquals(1024d, CellFormat.parseValueFunctionText("1,024").doubleValue());
    assertEquals(36598d, CellFormat.parseValueFunctionText("2000-03-13").doubleValue());
    assertEquals(36599d, CellFormat.parseValueFunctionText("3月14日").doubleValue());
    assertNull(CellFormat.parseValueFunctionText(""));
  }

  @Test
  void appliesResolvedFormulaFormattingBackOntoACell() {
    final MutableResolvedCell cell = new MutableResolvedCell();

    CellFormat.applyResolvedFormulaValue(cell, "0.125", "legacy_resolver");

    assertEquals("0.125", cell.rawValue);
    assertEquals("12.5%", cell.outputValue);
    assertEquals("resolved", cell.resolutionStatus);
    assertEquals("legacy_resolver", cell.resolutionSource);
  }

  private static final class MutableResolvedCell implements CellFormat.ResolvedCellLike {
    private String rawValue = "";
    private String outputValue = "";
    private String resolutionStatus;
    private String resolutionSource;

    @Override
    public StylesParser.BorderFlags getBorders() {
      return StylesParser.EMPTY_BORDERS;
    }

    @Override
    public int getNumFmtId() {
      return 10;
    }

    @Override
    public String getFormatCode() {
      return "0.0%";
    }

    @Override
    public void setRawValue(final String rawValue) {
      this.rawValue = rawValue;
    }

    @Override
    public void setOutputValue(final String outputValue) {
      this.outputValue = outputValue;
    }

    @Override
    public void setResolutionStatus(final String resolutionStatus) {
      this.resolutionStatus = resolutionStatus;
    }

    @Override
    public void setResolutionSource(final String resolutionSource) {
      this.resolutionSource = resolutionSource;
    }
  }
}
