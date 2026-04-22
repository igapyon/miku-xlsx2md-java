/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdownnormalize;

import java.util.regex.Pattern;

public final class MarkdownNormalize {
  private static final Pattern MARKDOWN_UNSAFE_UNICODE_PATTERN =
      Pattern.compile("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F-\\u009F\\u00AD\\u200B-\\u200F"
          + "\\u2028\\u2029\\u202A-\\u202E\\u2060-\\u206F\\uFEFF\\uFDD0-\\uFDEF\\uFFFE\\uFFFF]");
  private static final Pattern MARKDOWN_LINE_BREAK_PATTERN = Pattern.compile("\\r\\n?|\\n");

  private MarkdownNormalize() {
  }

  public static String normalizeMarkdownNewlines(final String text) {
    return normalizeMarkdownNewlines(text, "\n");
  }

  public static String normalizeMarkdownNewlines(final String text, final String replacement) {
    return MARKDOWN_LINE_BREAK_PATTERN.matcher(text == null ? "" : text).replaceAll(replacement);
  }

  public static String normalizeMarkdownText(final String text) {
    return MARKDOWN_UNSAFE_UNICODE_PATTERN
        .matcher(normalizeMarkdownNewlines(text == null ? "" : text, " "))
        .replaceAll(" ")
        .replace('\t', ' ');
  }

  public static String escapeMarkdownPipes(final String text) {
    return (text == null ? "" : text).replaceAll("(?<!\\\\)\\|", "\\\\|");
  }

  public static String normalizeMarkdownTableCell(final String text) {
    return escapeMarkdownPipes(normalizeMarkdownText(text));
  }

  public static String normalizeMarkdownHeadingText(final String text) {
    return normalizeMarkdownText(text).replaceFirst("^#+\\s*", "");
  }

  public static String normalizeMarkdownListItemText(final String text) {
    return normalizeMarkdownText(text).replaceFirst("^([-*+]|\\d+\\.)\\s+", "");
  }
}
