/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.narrativestructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown;

class NarrativeStructureTest {
  @Test
  void rendersAnIndentedParentChildBlockAsHeadingPlusBullets() {
    final String markdown = NarrativeStructure.renderNarrativeBlock(block(
        item(1, 1, "通常のテキスト", "通常のテキスト"),
        item(2, 2, "字下げされたテキスト", "字下げされたテキスト"),
        item(3, 2, "テキスト", "テキスト")));

    assertTrue(markdown.contains("### 通常のテキスト"));
    assertTrue(markdown.contains("- 字下げされたテキスト"));
    assertTrue(markdown.contains("- テキスト"));
  }

  @Test
  void rendersFlatNarrativeRowsAsPlainParagraphs() {
    final String markdown = NarrativeStructure.renderNarrativeBlock(block(
        item(1, 1, "一行目", "一行目"),
        item(2, 1, "二行目", "二行目")));

    assertEquals("一行目\n\n二行目", markdown);
  }

  @Test
  void normalizesHeadingAndListMarkersInsideHierarchicalNarrativeItems() {
    final String markdown = NarrativeStructure.renderNarrativeBlock(block(
        item(1, 1, "### 親", "### 親"),
        item(2, 2, "- 子", "- 子"),
        item(3, 2, "1. 孫", "1. 孫")));

    assertTrue(markdown.contains("### 親"));
    assertTrue(markdown.contains("- 子"));
    assertTrue(markdown.contains("- 孫"));
  }

  @Test
  void startsANewHeadingWhenIndentationReturnsToTheParentLevel() {
    final String markdown = NarrativeStructure.renderNarrativeBlock(block(
        item(1, 1, "親1", "親1"),
        item(2, 2, "子1", "子1"),
        item(3, 2, "子2", "子2"),
        item(4, 1, "親2", "親2"),
        item(5, 3, "子3", "子3")));

    assertTrue(markdown.contains("### 親1"));
    assertTrue(markdown.contains("- 子1"));
    assertTrue(markdown.contains("- 子2"));
    assertTrue(markdown.contains("### 親2"));
    assertTrue(markdown.contains("- 子3"));
  }

  @Test
  void detectsAHeadingBlockOnlyWhenTheSecondItemIsIndentedDeeper() {
    assertTrue(NarrativeStructure.isSectionHeadingNarrativeBlock(block(
        item(1, 1, "親", "親"),
        item(2, 2, "子", "子"))));

    assertFalse(NarrativeStructure.isSectionHeadingNarrativeBlock(block(
        item(1, 1, "一行目", "一行目"),
        item(2, 1, "二行目", "二行目"))));
  }

  @Test
  void rendersCalendarLikeNarrativeRowsWithCellBoundariesPreserved() {
    final SheetMarkdown.NarrativeBlock block = block(
        item(1, 1, "2021年1月", "2021年1月"),
        item(2, 1, "日 月 火 水 木 金 土", "日", "月", "火", "水", "木", "金", "土"),
        item(3, 1, "2021-01-03 2021-01-04 2021-01-05 2021-01-06 2021-01-07 2021-01-08 2021-01-09",
            "2021-01-03", "2021-01-04", "2021-01-05", "2021-01-06", "2021-01-07", "2021-01-08", "2021-01-09"),
        item(4, 1, "仕事 私用 その他", "仕事", "私用", "その他", "", "", "", ""));
    final String markdown = NarrativeStructure.renderNarrativeBlock(block);

    assertTrue(NarrativeStructure.isCalendarLikeNarrativeBlock(block));
    assertTrue(markdown.contains("2021年1月"));
    assertTrue(markdown.contains("### 日 月 火 水 木 金 土"));
    assertTrue(markdown.contains("2021-01-03 | 2021-01-04 | 2021-01-05 | 2021-01-06 | 2021-01-07 | 2021-01-08 | 2021-01-09"));
    assertTrue(markdown.contains("仕事 | 私用 | その他"));
  }

  private static SheetMarkdown.NarrativeBlock block(final SheetMarkdown.NarrativeItem... items) {
    final SheetMarkdown.NarrativeBlock block = new SheetMarkdown.NarrativeBlock(items[0]);
    for (int index = 1; index < items.length; index += 1) {
      block.append(items[index]);
    }
    return block;
  }

  private static SheetMarkdown.NarrativeItem item(
      final int row,
      final int startCol,
      final String text,
      final String... cellValues) {
    return new SheetMarkdown.NarrativeItem(row, startCol, text, Arrays.asList(cellValues));
  }
}
