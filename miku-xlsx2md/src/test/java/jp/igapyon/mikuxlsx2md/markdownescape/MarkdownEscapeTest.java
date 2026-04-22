/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdownescape;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class MarkdownEscapeTest {
  @Test
  void escapesInlineMarkdownControlCharacters() {
    assertEquals(
        "a\\*b \\_c\\_ \\[x\\]\\(y\\) \\!\\[z\\]\\(w\\) &lt;tag&gt; a \\| b \\`q\\` \\~",
        MarkdownEscape.escapeMarkdownLiteralText("a*b _c_ [x](y) ![z](w) <tag> a | b `q` ~"));
    assertEquals(
        Arrays.asList(
            new MarkdownEscape.MarkdownLiteralPart("text", "a", "a"),
            new MarkdownEscape.MarkdownLiteralPart("escaped", "\\*", "*"),
            new MarkdownEscape.MarkdownLiteralPart("text", "b ", "b "),
            new MarkdownEscape.MarkdownLiteralPart("escaped", "&lt;", "<"),
            new MarkdownEscape.MarkdownLiteralPart("text", "tag", "tag"),
            new MarkdownEscape.MarkdownLiteralPart("escaped", "&gt;", ">")),
        MarkdownEscape.escapeMarkdownLiteralParts("a*b <tag>"));
  }

  @Test
  void escapesLineStartMarkdownMarkers() {
    assertEquals("\\# h\n\\- item\n1\\. num\n\\> quote",
        MarkdownEscape.escapeMarkdownLineStart("# h\n- item\r\n1. num\r> quote"));
    assertEquals("\\# h\n\\- item\n1\\. num\n&gt; quote",
        MarkdownEscape.escapeMarkdownLiteralText("# h\n- item\n1. num\n> quote"));
  }

  @Test
  void escapesAdditionalListMarkersAndAmpersands() {
    assertEquals("\\+ plus\n\\* star\na &amp; b",
        MarkdownEscape.escapeMarkdownLiteralText("+ plus\n* star\na & b"));
    assertEquals(
        Arrays.asList(
            new MarkdownEscape.MarkdownLiteralPart("text", "a ", "a "),
            new MarkdownEscape.MarkdownLiteralPart("escaped", "&amp;", "&"),
            new MarkdownEscape.MarkdownLiteralPart("text", " b", " b")),
        MarkdownEscape.escapeMarkdownLiteralParts("a & b"));
  }
}
