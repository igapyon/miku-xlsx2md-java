/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.narrativestructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalize;
import jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown;

public final class NarrativeStructure {
  private static final Pattern ISO_DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

  private NarrativeStructure() {
  }

  public static String renderNarrativeBlock(final SheetMarkdown.NarrativeBlock block) {
    if (block == null || block.getItems().isEmpty()) {
      return block == null ? "" : joinLines(normalizeLines(block.getLines()));
    }
    if (isCalendarLikeNarrativeBlock(block)) {
      return renderCalendarLikeNarrativeBlock(block);
    }
    final List<String> parts = new ArrayList<String>();
    int index = 0;
    while (index < block.getItems().size()) {
      final SheetMarkdown.NarrativeItem current = block.getItems().get(index);
      final SheetMarkdown.NarrativeItem next = index + 1 < block.getItems().size() ? block.getItems().get(index + 1) : null;
      if (isIndentedChildItem(current, next)) {
        int childEnd = index + 1;
        while (childEnd < block.getItems().size() && isIndentedChildItem(current, block.getItems().get(childEnd))) {
          childEnd += 1;
        }
        final List<String> childLines = new ArrayList<String>();
        for (int childIndex = index + 1; childIndex < childEnd; childIndex += 1) {
          childLines.add(formatNarrativeBullet(block.getItems().get(childIndex).getText()));
        }
        parts.add(formatNarrativeHeading(current.getText()));
        if (!childLines.isEmpty()) {
          parts.add(joinLines(childLines));
        }
        index = childEnd;
      } else {
        parts.add(normalizeNarrativeText(current.getText()));
        index += 1;
      }
    }
    return joinParagraphs(parts);
  }

  public static boolean isSectionHeadingNarrativeBlock(final SheetMarkdown.NarrativeBlock block) {
    if (block == null || block.getItems().size() < 2) {
      return false;
    }
    return isIndentedChildItem(block.getItems().get(0), block.getItems().get(1));
  }

  public static boolean isCalendarLikeNarrativeBlock(final SheetMarkdown.NarrativeBlock block) {
    if (block == null || block.getItems().size() < 2) {
      return false;
    }
    int calendarLikeCount = 0;
    for (final SheetMarkdown.NarrativeItem item : block.getItems()) {
      if (isCalendarLikeItem(item)) {
        calendarLikeCount += 1;
      }
    }
    return calendarLikeCount >= 2;
  }

  private static String renderCalendarLikeNarrativeBlock(final SheetMarkdown.NarrativeBlock block) {
    final List<String> parts = new ArrayList<String>();
    for (final SheetMarkdown.NarrativeItem item : block.getItems()) {
      final List<String> values = nonEmptyTrimmed(item.getCellValues());
      if (isCalendarLikeItem(item) || values.size() >= 2) {
        parts.add(renderCalendarLikeItem(item));
      } else {
        parts.add(normalizeNarrativeText(item.getText()));
      }
    }
    return joinParagraphs(parts);
  }

  private static String renderCalendarLikeItem(final SheetMarkdown.NarrativeItem item) {
    final List<String> values = nonEmptyTrimmed(item.getCellValues());
    if (values.isEmpty()) {
      return normalizeNarrativeText(item.getText());
    }
    boolean allWeekdays = true;
    boolean allDatesOrWeekdays = true;
    for (final String value : values) {
      allWeekdays = allWeekdays && isWeekdayToken(value);
      allDatesOrWeekdays = allDatesOrWeekdays && (isIsoDateToken(value) || isWeekdayToken(value));
    }
    if (allWeekdays) {
      return formatNarrativeHeading(join(values, " "));
    }
    if (allDatesOrWeekdays) {
      return join(values, " | ");
    }
    return join(values, " | ");
  }

  private static boolean isCalendarLikeItem(final SheetMarkdown.NarrativeItem item) {
    if (item == null) {
      return false;
    }
    final List<String> values = nonEmptyTrimmed(item.getCellValues());
    if (values.size() < 5) {
      return false;
    }
    int weekdayCount = 0;
    int dateCount = 0;
    for (final String value : values) {
      if (isWeekdayToken(value)) {
        weekdayCount += 1;
      }
      if (isIsoDateToken(value)) {
        dateCount += 1;
      }
    }
    return weekdayCount >= 5 || dateCount >= 5 || values.size() >= 7;
  }

  private static String normalizeNarrativeText(final String text) {
    return MarkdownNormalize.normalizeMarkdownText(text);
  }

  private static String formatNarrativeHeading(final String text) {
    return "### " + MarkdownNormalize.normalizeMarkdownHeadingText(text);
  }

  private static String formatNarrativeBullet(final String text) {
    return "- " + MarkdownNormalize.normalizeMarkdownListItemText(text);
  }

  private static boolean isIndentedChildItem(final SheetMarkdown.NarrativeItem parent, final SheetMarkdown.NarrativeItem child) {
    return parent != null && child != null && child.getStartCol() > parent.getStartCol();
  }

  private static boolean isWeekdayToken(final String value) {
    final String normalized = stringValue(value).trim();
    return "日".equals(normalized) || "月".equals(normalized) || "火".equals(normalized) || "水".equals(normalized)
        || "木".equals(normalized) || "金".equals(normalized) || "土".equals(normalized)
        || "日曜日".equals(normalized) || "月曜日".equals(normalized) || "火曜日".equals(normalized) || "水曜日".equals(normalized)
        || "木曜日".equals(normalized) || "金曜日".equals(normalized) || "土曜日".equals(normalized);
  }

  private static boolean isIsoDateToken(final String value) {
    return ISO_DATE_PATTERN.matcher(stringValue(value).trim()).matches();
  }

  private static List<String> nonEmptyTrimmed(final List<String> input) {
    final List<String> values = new ArrayList<String>();
    for (final String value : input == null ? Collections.<String>emptyList() : input) {
      final String normalized = stringValue(value).trim();
      if (!normalized.isEmpty()) {
        values.add(normalized);
      }
    }
    return values;
  }

  private static List<String> normalizeLines(final List<String> lines) {
    final List<String> result = new ArrayList<String>();
    for (final String line : lines == null ? Collections.<String>emptyList() : lines) {
      result.add(normalizeNarrativeText(line));
    }
    return result;
  }

  private static String joinLines(final List<String> values) {
    return join(values, "\n");
  }

  private static String joinParagraphs(final List<String> values) {
    return join(values, "\n\n");
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
}
