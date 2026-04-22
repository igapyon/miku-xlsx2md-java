/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdownnormalize;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MarkdownNormalizeTest {
  @Test
  void replacesLineBreaksTabsAndControls() {
    assertEquals(" a b c d ", MarkdownNormalize.normalizeMarkdownText(" a\r\nb\tc\u0007d "));
  }

  @Test
  void normalizesMarkdownNewlines() {
    assertEquals("a\nb\nc\nd", MarkdownNormalize.normalizeMarkdownNewlines("a\r\nb\rc\nd"));
    assertEquals("a / b", MarkdownNormalize.normalizeMarkdownNewlines("a\r\nb", " / "));
  }

  @Test
  void replacesUnsafeUnicode() {
    assertEquals("a b c d e f g h",
        MarkdownNormalize.normalizeMarkdownText("a\u0085b\u200Bc\u2028d\u202Ee\u2060f\uFEFFg\u00ADh"));
  }

  @Test
  void escapesPipesInTableCells() {
    assertEquals("A\\| B", MarkdownNormalize.normalizeMarkdownTableCell("A|\nB"));
    assertEquals("  A \\| B  ", MarkdownNormalize.normalizeMarkdownTableCell("  A\t|\tB  "));
  }

  @Test
  void removesHeadingAndListMarkers() {
    assertEquals("heading", MarkdownNormalize.normalizeMarkdownHeadingText("### heading"));
    assertEquals("item", MarkdownNormalize.normalizeMarkdownListItemText("- item"));
    assertEquals("item", MarkdownNormalize.normalizeMarkdownListItemText("1. item"));
  }
}
