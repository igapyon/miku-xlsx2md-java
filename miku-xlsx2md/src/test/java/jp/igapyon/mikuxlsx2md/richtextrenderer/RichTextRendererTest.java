/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.richtextrenderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings;
import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

class RichTextRendererTest {
  @Test
  void splitsRawTextIntoTextAndLineBreakTokens() {
    assertEquals(
        Arrays.asList(
            RichTextRenderer.RawLineToken.text("a"),
            RichTextRenderer.RawLineToken.lineBreak(),
            RichTextRenderer.RawLineToken.lineBreak(),
            RichTextRenderer.RawLineToken.text("b c")),
        RichTextRenderer.splitRawTextWithLineBreaks("a\r\n\nb\tc"));
  }

  @Test
  void tokenizesPlainModeIntoASingleEscapedTextToken() {
    assertEquals(
        Arrays.asList(RichTextRenderer.RichTextToken.text("\\# a\\*b c")),
        RichTextRenderer.tokenizeCellDisplayText(cell("# a*b\nc", style(false, false, false, false), null), "plain"));
  }

  @Test
  void tokenizesGithubFallbackCellsIntoStyledTextAndLineBreakTokens() {
    assertEquals(
        Arrays.asList(
            styled("line1", style(true, false, false, true)),
            RichTextRenderer.RichTextToken.lineBreak(),
            styled("line2", style(true, false, false, true))),
        RichTextRenderer.tokenizeCellDisplayText(cell("line1\nline2", style(true, false, false, true), null), "github"));
  }

  @Test
  void rendersTokenizedRichTextWithGithubFormatting() {
    final WorksheetParser.ParsedCell cell = cell(
        "plain bold\nnext",
        style(false, false, false, false),
        Arrays.asList(
            run("plain ", false, false, false, false),
            run("bold\nnext", true, false, false, false)));

    assertEquals("plain **bold**<br>**next**", RichTextRenderer.renderCellDisplayText(cell, "github"));
  }

  @Test
  void fallsBackToPlainTokensInPlainMode() {
    final WorksheetParser.ParsedCell cell = cell("# head\nline2", style(true, false, false, false), null);

    assertEquals("\\# head line2", RichTextRenderer.renderCellDisplayText(cell, "plain"));
  }

  @Test
  void rendersEscapedMarkdownLinkLikeTextSafelyInsideStyledRuns() {
    final WorksheetParser.ParsedCell cell = cell(
        "[x](y) `code` <tag>",
        style(false, false, false, false),
        Arrays.asList(
            run("[x](y) ", true, false, false, false),
            run("`code` ", false, true, false, false),
            run("<tag>", false, false, false, true)));

    assertEquals("**\\[x\\]\\(y\\) ***\\`code\\` *<ins>&lt;tag&gt;</ins>", RichTextRenderer.renderCellDisplayText(cell, "github"));
  }

  @Test
  void rendersConsecutiveLineBreaksAcrossStyledRichRuns() {
    final WorksheetParser.ParsedCell cell = cell(
        "top\n\nnext",
        style(false, false, false, false),
        Arrays.asList(
            run("top\n", true, false, false, false),
            run("\nnext", false, true, false, true)));

    assertEquals("**top**<br><br>*<ins>next</ins>*", RichTextRenderer.renderCellDisplayText(cell, "github"));
  }

  @Test
  void showsPlainVsGithubDifferencesForTheSameRichTextInput() {
    final WorksheetParser.ParsedCell cell = cell("a*b\nnext", style(true, false, false, false), null);

    assertEquals("a\\*b next", RichTextRenderer.renderCellDisplayText(cell, "plain"));
    assertEquals("**a\\*b**<br>**next**", RichTextRenderer.renderCellDisplayText(cell, "github"));
  }

  @Test
  void suppressesUnderlineWhenRequestedForHyperlinkRendering() {
    final WorksheetParser.ParsedCell cell = cell("Under", style(false, false, false, true), null);

    assertEquals("<ins>Under</ins>", RichTextRenderer.renderCellDisplayText(cell, "github"));
    assertEquals("Under", RichTextRenderer.renderCellDisplayText(cell, "github", true));
  }

  private static RichTextRenderer.RichTextToken styled(
      final String text,
      final RichTextRenderer.RichTextStyle style) {
    return RichTextRenderer.RichTextToken.styledText(
        Arrays.asList(new RichTextRenderer.StyledTextPart("text", text, text)),
        style);
  }

  private static RichTextRenderer.RichTextStyle style(
      final boolean bold,
      final boolean italic,
      final boolean strike,
      final boolean underline) {
    return new RichTextRenderer.RichTextStyle(bold, italic, strike, underline);
  }

  private static SharedStrings.RichTextRun run(
      final String text,
      final boolean bold,
      final boolean italic,
      final boolean strike,
      final boolean underline) {
    return new SharedStrings.RichTextRun(text, bold, italic, strike, underline);
  }

  private static WorksheetParser.ParsedCell cell(
      final String outputValue,
      final RichTextRenderer.RichTextStyle style,
      final java.util.List<SharedStrings.RichTextRun> runs) {
    return new WorksheetParser.ParsedCell(
        "A1",
        1,
        1,
        "s",
        outputValue,
        outputValue,
        "",
        null,
        null,
        "none",
        0,
        new StylesParser.BorderFlags(false, false, false, false),
        0,
        "General",
        new StylesParser.TextStyle(style.isBold(), style.isItalic(), style.isStrike(), style.isUnderline()),
        runs == null ? null : Collections.unmodifiableList(runs),
        "",
        "",
        null);
  }
}
