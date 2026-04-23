/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdownoptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MarkdownOptionsTest {
  @Test
  void normalizesModesAndBooleanDefaults() {
    final MarkdownOptions.ResolvedMarkdownOptions resolved = MarkdownOptions.resolveMarkdownOptions(
        new MarkdownOptions(null, Boolean.FALSE, null, Boolean.FALSE, null, "RAW", "GITHUB", "unknown"));

    assertEquals(true, resolved.isTreatFirstRowAsHeader());
    assertEquals(false, resolved.isTrimText());
    assertEquals(true, resolved.isRemoveEmptyRows());
    assertEquals(false, resolved.isRemoveEmptyColumns());
    assertEquals(true, resolved.isIncludeShapeDetails());
    assertEquals("raw", resolved.getOutputMode());
    assertEquals("github", resolved.getFormattingMode());
    assertEquals("balanced", resolved.getTableDetectionMode());
  }

  @Test
  void keepsCompatibilityAliasForBorderPriority() {
    assertEquals("border", MarkdownOptions.normalizeTableDetectionMode("border-priority"));
  }

  @Test
  void acceptsPlannerAwareTableDetectionMode() {
    assertEquals("planner-aware", MarkdownOptions.normalizeTableDetectionMode("planner-aware"));
  }
}
