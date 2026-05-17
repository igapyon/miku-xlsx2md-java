/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.addressutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AddressUtilsTest {
  @Test
  void convertsBetweenColumnNumbersAndLetters() {
    assertEquals("A", AddressUtils.colToLetters(1));
    assertEquals("AB", AddressUtils.colToLetters(28));
    assertEquals(28, AddressUtils.lettersToCol("AB"));
  }

  @Test
  void parsesAndNormalizesCellAndRangeAddresses() {
    assertEquals(new AddressUtils.CellAddress(12, 3), AddressUtils.parseCellAddress("$C$12"));
    assertEquals("D7", AddressUtils.normalizeFormulaAddress("$d$7"));
    assertEquals(new AddressUtils.RangeAddress("A1", "C3"), AddressUtils.parseRangeAddress("$A$1:$C$3"));
    assertNull(AddressUtils.parseRangeAddress("not-a-range"));
  }

  @Test
  void formatsDisplayRangesAndMergedRangeRefs() {
    assertEquals("B12-F17", AddressUtils.formatRange(12, 2, 17, 6));
    assertEquals(
        new AddressUtils.MergeRange(12, 2, 17, 6, "B12:F17"),
        AddressUtils.parseRangeRef("B12:F17"));
  }
}
