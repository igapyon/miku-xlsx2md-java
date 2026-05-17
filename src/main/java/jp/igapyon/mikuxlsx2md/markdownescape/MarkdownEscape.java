/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdownescape;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalize;

public final class MarkdownEscape {
  private MarkdownEscape() {
  }

  public static String escapeMarkdownLineStart(final String text) {
    final String[] lines = MarkdownNormalize.normalizeMarkdownNewlines(text).split("\n", -1);
    final List<String> escaped = new ArrayList<String>();
    for (final String line : lines) {
      escaped.add(escapeMarkdownLineStartSegment(line));
    }
    return joinLines(escaped);
  }

  public static List<MarkdownLiteralPart> escapeMarkdownLiteralParts(final String text) {
    final String source = text == null ? "" : text;
    final List<MarkdownLiteralPart> parts = new ArrayList<MarkdownLiteralPart>();
    final StringBuilder buffer = new StringBuilder();

    for (int index = 0; index < source.length(); index += 1) {
      final char ch = source.charAt(index);
      final boolean atLineStart = index == 0;
      final char next = index + 1 < source.length() ? source.charAt(index + 1) : '\0';
      final String escapedText = getEscapedMarkdownLiteralText(ch, atLineStart, next);
      if (escapedText != null) {
        pushTextBuffer(parts, buffer);
        parts.add(new MarkdownLiteralPart("escaped", escapedText, String.valueOf(ch)));
        continue;
      }

      final OrderedListMarker marker = parseOrderedListMarker(source, index, atLineStart);
      if (marker != null) {
        pushTextBuffer(parts, buffer);
        parts.add(new MarkdownLiteralPart("text", marker.getDigits(), marker.getDigits()));
        parts.add(new MarkdownLiteralPart("escaped", "\\.", "."));
        index = marker.getDotIndex();
        continue;
      }

      buffer.append(ch);
    }

    pushTextBuffer(parts, buffer);
    return parts;
  }

  public static String escapeMarkdownLiteralText(final String text) {
    final String[] lines = MarkdownNormalize.normalizeMarkdownNewlines(text).split("\n", -1);
    final List<String> escaped = new ArrayList<String>();
    for (final String line : lines) {
      final StringBuilder builder = new StringBuilder();
      for (final MarkdownLiteralPart part : escapeMarkdownLiteralParts(line)) {
        builder.append(part.getText());
      }
      escaped.add(builder.toString());
    }
    return joinLines(escaped);
  }

  private static String escapeMarkdownLineStartSegment(final String text) {
    String result = text == null ? "" : text;
    result = result.replaceFirst("^(\\s*)([#>])", "$1\\\\$2");
    result = result.replaceFirst("^(\\s*)([-+*])(\\s+)", "$1\\\\$2$3");
    result = result.replaceFirst("^(\\s*)(\\d+)\\.(\\s+)", "$1$2\\\\.$3");
    return result;
  }

  private static void pushTextBuffer(final List<MarkdownLiteralPart> parts, final StringBuilder buffer) {
    if (buffer.length() == 0) {
      return;
    }
    final String text = buffer.toString();
    parts.add(new MarkdownLiteralPart("text", text, text));
    buffer.setLength(0);
  }

  private static String getEscapedMarkdownLiteralText(final char ch, final boolean atLineStart, final char nextChar) {
    if (ch == '\\') {
      return "\\\\";
    }
    if (ch == '&') {
      return "&amp;";
    }
    if (ch == '<') {
      return "&lt;";
    }
    if (ch == '>') {
      return "&gt;";
    }
    if ("`*_{}[]()!|~".indexOf(ch) >= 0) {
      return "\\" + ch;
    }
    if (atLineStart && ch == '#') {
      return "\\" + ch;
    }
    if (atLineStart && "-+*".indexOf(ch) >= 0 && Character.isWhitespace(nextChar)) {
      return "\\" + ch;
    }
    return null;
  }

  private static OrderedListMarker parseOrderedListMarker(final String source, final int index, final boolean atLineStart) {
    if (!atLineStart || index >= source.length() || !Character.isDigit(source.charAt(index))) {
      return null;
    }
    final StringBuilder digits = new StringBuilder();
    int cursor = index;
    while (cursor < source.length() && Character.isDigit(source.charAt(cursor))) {
      digits.append(source.charAt(cursor));
      cursor += 1;
    }
    if (cursor >= source.length() || source.charAt(cursor) != '.') {
      return null;
    }
    if (cursor + 1 >= source.length() || !Character.isWhitespace(source.charAt(cursor + 1))) {
      return null;
    }
    return new OrderedListMarker(digits.toString(), cursor);
  }

  private static String joinLines(final List<String> lines) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < lines.size(); index += 1) {
      if (index > 0) {
        builder.append('\n');
      }
      builder.append(lines.get(index));
    }
    return builder.toString();
  }

  private static final class OrderedListMarker {
    private final String digits;
    private final int dotIndex;

    private OrderedListMarker(final String digits, final int dotIndex) {
      this.digits = digits;
      this.dotIndex = dotIndex;
    }

    private String getDigits() {
      return digits;
    }

    private int getDotIndex() {
      return dotIndex;
    }
  }

  public static final class MarkdownLiteralPart {
    private final String kind;
    private final String text;
    private final String rawText;

    public MarkdownLiteralPart(final String kind, final String text, final String rawText) {
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
      if (!(other instanceof MarkdownLiteralPart)) {
        return false;
      }
      final MarkdownLiteralPart that = (MarkdownLiteralPart) other;
      return Objects.equals(kind, that.kind)
          && Objects.equals(text, that.text)
          && Objects.equals(rawText, that.rawText);
    }

    @Override
    public int hashCode() {
      return Objects.hash(kind, text, rawText);
    }
  }
}
