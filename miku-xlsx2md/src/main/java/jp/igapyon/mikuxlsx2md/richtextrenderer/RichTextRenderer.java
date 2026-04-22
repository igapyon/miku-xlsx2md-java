/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.richtextrenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jp.igapyon.mikuxlsx2md.markdownescape.MarkdownEscape;
import jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalize;
import jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings;
import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

public final class RichTextRenderer {
  private RichTextRenderer() {
  }

  public static String compactText(final String text) {
    return MarkdownNormalize.normalizeMarkdownText(MarkdownEscape.escapeMarkdownLiteralText(text)).replaceAll("\\s+", " ").trim();
  }

  public static List<RichTextToken> tokenizeCellDisplayText(
      final WorksheetParser.ParsedCell cell,
      final String formattingMode) {
    return tokenizeCellDisplayText(cell, formattingMode, false);
  }

  public static List<RichTextToken> tokenizeCellDisplayText(
      final WorksheetParser.ParsedCell cell,
      final String formattingMode,
      final boolean suppressUnderline) {
    if (cell == null) {
      return Collections.emptyList();
    }
    if (!"github".equals(formattingMode)) {
      return tokenizePlainCellText(cell.getOutputValue());
    }
    final String displayValue = compactText(cell.getOutputValue());
    if (cell.getRichTextRuns() != null
        && displayValue.equals(compactText(joinRunText(cell.getRichTextRuns())))) {
      return tokenizeGithubRichTextRuns(cell.getRichTextRuns(), suppressUnderline);
    }
    return tokenizeGithubCellText(cell.getOutputValue(), createTextStyle(cell.getTextStyle(), suppressUnderline));
  }

  public static List<RichTextToken> tokenizePlainCellText(final String text) {
    final String compacted = compactText(text);
    if (compacted.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.<RichTextToken>singletonList(RichTextToken.text(compacted));
  }

  public static List<RichTextToken> tokenizeGithubCellText(final String text, final RichTextStyle style) {
    final List<RawLineToken> tokens = splitRawTextWithLineBreaks(text);
    if (tokens.isEmpty()) {
      return Collections.emptyList();
    }
    final List<RichTextToken> result = new ArrayList<RichTextToken>();
    for (final RawLineToken token : tokens) {
      if ("lineBreak".equals(token.getKind())) {
        result.add(RichTextToken.lineBreak());
      } else {
        result.add(createStyledTextToken(token.getRawText(), style));
      }
    }
    return result;
  }

  public static List<RichTextToken> tokenizeGithubRichTextRuns(final List<SharedStrings.RichTextRun> runs) {
    return tokenizeGithubRichTextRuns(runs, false);
  }

  public static List<RichTextToken> tokenizeGithubRichTextRuns(
      final List<SharedStrings.RichTextRun> runs,
      final boolean suppressUnderline) {
    final List<RichTextToken> result = new ArrayList<RichTextToken>();
    for (final SharedStrings.RichTextRun run : runs == null ? Collections.<SharedStrings.RichTextRun>emptyList() : runs) {
      for (final RawLineToken token : splitRawTextWithLineBreaks(run.getText())) {
        if ("lineBreak".equals(token.getKind())) {
          result.add(RichTextToken.lineBreak());
        } else {
          result.add(createStyledTextToken(token.getRawText(), new RichTextStyle(
              run.isBold(),
              run.isItalic(),
              run.isStrike(),
              !suppressUnderline && run.isUnderline())));
        }
      }
    }
    return result;
  }

  public static List<RawLineToken> splitRawTextWithLineBreaks(final String text) {
    final String normalized = stringValue(text).replaceAll("\\r\\n?|\\n", "\n").replace('\t', ' ');
    if (normalized.isEmpty()) {
      return Collections.emptyList();
    }
    final String[] parts = normalized.split("\n", -1);
    final List<RawLineToken> tokens = new ArrayList<RawLineToken>();
    for (int index = 0; index < parts.length; index += 1) {
      if (!parts[index].isEmpty()) {
        tokens.add(RawLineToken.text(parts[index]));
      }
      if (index < parts.length - 1) {
        tokens.add(RawLineToken.lineBreak());
      }
    }
    return tokens;
  }

  public static List<RichTextToken> splitTextWithLineBreaks(final String text) {
    final List<RichTextToken> result = new ArrayList<RichTextToken>();
    for (final RawLineToken token : splitRawTextWithLineBreaks(text)) {
      if ("lineBreak".equals(token.getKind())) {
        result.add(RichTextToken.lineBreak());
      } else {
        result.add(RichTextToken.text(MarkdownEscape.escapeMarkdownLiteralText(token.getRawText())));
      }
    }
    return result;
  }

  public static RichTextToken createStyledTextToken(final String text, final RichTextStyle style) {
    final List<StyledTextPart> parts = new ArrayList<StyledTextPart>();
    for (final MarkdownEscape.MarkdownLiteralPart part : MarkdownEscape.escapeMarkdownLiteralParts(text)) {
      parts.add(new StyledTextPart(part.getKind(), part.getText(), part.getRawText()));
    }
    return RichTextToken.styledText(parts, style);
  }

  public static String renderCellDisplayText(
      final WorksheetParser.ParsedCell cell,
      final String formattingMode) {
    return renderCellDisplayText(cell, formattingMode, false);
  }

  public static String renderCellDisplayText(
      final WorksheetParser.ParsedCell cell,
      final String formattingMode,
      final boolean suppressUnderline) {
    return renderTokens(tokenizeCellDisplayText(cell, formattingMode, suppressUnderline), formattingMode);
  }

  public static String renderTokens(final List<RichTextToken> tokens, final String formattingMode) {
    if (!"github".equals(formattingMode)) {
      return renderPlainTokens(tokens);
    }
    return renderGithubTokens(tokens);
  }

  public static String renderPlainTokens(final List<RichTextToken> tokens) {
    final List<String> parts = new ArrayList<String>();
    for (final RichTextToken token : tokens == null ? Collections.<RichTextToken>emptyList() : tokens) {
      if ("lineBreak".equals(token.getKind())) {
        parts.add(" ");
      } else if ("styledText".equals(token.getKind())) {
        parts.add(renderStyledTextParts(token.getParts()));
      } else {
        parts.add(token.getText());
      }
    }
    return join(parts, "").replaceAll(" {2,}", " ").trim();
  }

  public static String renderGithubTokens(final List<RichTextToken> tokens) {
    final List<String> parts = new ArrayList<String>();
    for (final RichTextToken token : tokens == null ? Collections.<RichTextToken>emptyList() : tokens) {
      if ("lineBreak".equals(token.getKind())) {
        parts.add("<br>");
      } else if ("styledText".equals(token.getKind())) {
        parts.add(applyTextStyle(renderStyledTextParts(token.getParts()), token.getStyle()));
      } else {
        parts.add(token.getText());
      }
    }
    return join(parts, "").replaceAll(" {2,}", " ").trim();
  }

  public static String renderStyledTextParts(final List<StyledTextPart> parts) {
    final List<String> rendered = new ArrayList<String>();
    for (final StyledTextPart part : parts == null ? Collections.<StyledTextPart>emptyList() : parts) {
      rendered.add(part.getText());
    }
    return join(rendered, "");
  }

  public static String normalizeGithubSegment(final String text) {
    return renderGithubTokens(splitTextWithLineBreaks(text));
  }

  public static String normalizeGithubCellText(final String text) {
    return normalizeGithubSegment(text).replaceAll(" {2,}", " ").trim();
  }

  public static String applyTextStyle(final String text, final RichTextStyle style) {
    String result = stringValue(text);
    if (result.isEmpty()) {
      return "";
    }
    if (style != null && style.isUnderline()) {
      result = "<ins>" + result + "</ins>";
    }
    if (style != null && style.isStrike()) {
      result = "~~" + result + "~~";
    }
    if (style != null && style.isItalic()) {
      result = "*" + result + "*";
    }
    if (style != null && style.isBold()) {
      result = "**" + result + "**";
    }
    return result;
  }

  private static RichTextStyle createTextStyle(final StylesParser.TextStyle style, final boolean suppressUnderline) {
    if (style == null) {
      return new RichTextStyle(false, false, false, false);
    }
    return new RichTextStyle(style.isBold(), style.isItalic(), style.isStrike(), !suppressUnderline && style.isUnderline());
  }

  private static String joinRunText(final List<SharedStrings.RichTextRun> runs) {
    final StringBuilder builder = new StringBuilder();
    for (final SharedStrings.RichTextRun run : runs) {
      builder.append(run.getText());
    }
    return builder.toString();
  }

  private static String join(final List<String> values, final String delimiter) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < values.size(); index += 1) {
      if (index > 0) {
        builder.append(delimiter);
      }
      builder.append(values.get(index));
    }
    return builder.toString();
  }

  private static String stringValue(final String value) {
    return value == null ? "" : value;
  }

  public static final class RichTextStyle {
    private final boolean bold;
    private final boolean italic;
    private final boolean strike;
    private final boolean underline;

    public RichTextStyle(final boolean bold, final boolean italic, final boolean strike, final boolean underline) {
      this.bold = bold;
      this.italic = italic;
      this.strike = strike;
      this.underline = underline;
    }

    public boolean isBold() {
      return bold;
    }

    public boolean isItalic() {
      return italic;
    }

    public boolean isStrike() {
      return strike;
    }

    public boolean isUnderline() {
      return underline;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof RichTextStyle)) {
        return false;
      }
      final RichTextStyle that = (RichTextStyle) other;
      return bold == that.bold && italic == that.italic && strike == that.strike && underline == that.underline;
    }

    @Override
    public int hashCode() {
      return Objects.hash(Boolean.valueOf(bold), Boolean.valueOf(italic), Boolean.valueOf(strike), Boolean.valueOf(underline));
    }
  }

  public static final class StyledTextPart {
    private final String kind;
    private final String text;
    private final String rawText;

    public StyledTextPart(final String kind, final String text, final String rawText) {
      this.kind = kind;
      this.text = text;
      this.rawText = rawText;
    }

    public String getKind() {
      return kind;
    }

    public String getText() {
      return text;
    }

    public String getRawText() {
      return rawText;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof StyledTextPart)) {
        return false;
      }
      final StyledTextPart that = (StyledTextPart) other;
      return Objects.equals(kind, that.kind) && Objects.equals(text, that.text) && Objects.equals(rawText, that.rawText);
    }

    @Override
    public int hashCode() {
      return Objects.hash(kind, text, rawText);
    }
  }

  public static final class RichTextToken {
    private final String kind;
    private final String text;
    private final List<StyledTextPart> parts;
    private final RichTextStyle style;

    private RichTextToken(
        final String kind,
        final String text,
        final List<StyledTextPart> parts,
        final RichTextStyle style) {
      this.kind = kind;
      this.text = text;
      this.parts = parts == null ? Collections.<StyledTextPart>emptyList() : parts;
      this.style = style;
    }

    public static RichTextToken text(final String text) {
      return new RichTextToken("text", text, null, null);
    }

    public static RichTextToken lineBreak() {
      return new RichTextToken("lineBreak", null, null, null);
    }

    public static RichTextToken styledText(final List<StyledTextPart> parts, final RichTextStyle style) {
      return new RichTextToken("styledText", null, parts, style);
    }

    public String getKind() {
      return kind;
    }

    public String getText() {
      return text;
    }

    public List<StyledTextPart> getParts() {
      return parts;
    }

    public RichTextStyle getStyle() {
      return style;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof RichTextToken)) {
        return false;
      }
      final RichTextToken that = (RichTextToken) other;
      return Objects.equals(kind, that.kind)
          && Objects.equals(text, that.text)
          && Objects.equals(parts, that.parts)
          && Objects.equals(style, that.style);
    }

    @Override
    public int hashCode() {
      return Objects.hash(kind, text, parts, style);
    }
  }

  public static final class RawLineToken {
    private final String kind;
    private final String rawText;

    private RawLineToken(final String kind, final String rawText) {
      this.kind = kind;
      this.rawText = rawText;
    }

    public static RawLineToken text(final String rawText) {
      return new RawLineToken("text", rawText);
    }

    public static RawLineToken lineBreak() {
      return new RawLineToken("lineBreak", null);
    }

    public String getKind() {
      return kind;
    }

    public String getRawText() {
      return rawText;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof RawLineToken)) {
        return false;
      }
      final RawLineToken that = (RawLineToken) other;
      return Objects.equals(kind, that.kind) && Objects.equals(rawText, that.rawText);
    }

    @Override
    public int hashCode() {
      return Objects.hash(kind, rawText);
    }
  }
}
