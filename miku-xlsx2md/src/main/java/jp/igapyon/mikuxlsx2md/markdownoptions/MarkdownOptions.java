/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdownoptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class MarkdownOptions {
  public static final List<String> OUTPUT_MODES = Collections.unmodifiableList(Arrays.asList("display", "raw", "both"));
  public static final List<String> FORMATTING_MODES = Collections.unmodifiableList(Arrays.asList("plain", "github"));
  public static final List<String> TABLE_DETECTION_MODES = Collections.unmodifiableList(Arrays.asList("balanced", "border"));
  public static final Map<String, String> TABLE_DETECTION_MODE_ALIASES;

  static {
    final Map<String, String> aliases = new LinkedHashMap<String, String>();
    aliases.put("border-priority", "border");
    TABLE_DETECTION_MODE_ALIASES = Collections.unmodifiableMap(aliases);
  }

  private final Boolean treatFirstRowAsHeader;
  private final Boolean trimText;
  private final Boolean removeEmptyRows;
  private final Boolean removeEmptyColumns;
  private final Boolean includeShapeDetails;
  private final String outputMode;
  private final String formattingMode;
  private final String tableDetectionMode;

  public MarkdownOptions() {
    this(null, null, null, null, null, null, null, null);
  }

  public MarkdownOptions(
      final Boolean treatFirstRowAsHeader,
      final Boolean trimText,
      final Boolean removeEmptyRows,
      final Boolean removeEmptyColumns,
      final Boolean includeShapeDetails,
      final String outputMode,
      final String formattingMode,
      final String tableDetectionMode) {
    this.treatFirstRowAsHeader = treatFirstRowAsHeader;
    this.trimText = trimText;
    this.removeEmptyRows = removeEmptyRows;
    this.removeEmptyColumns = removeEmptyColumns;
    this.includeShapeDetails = includeShapeDetails;
    this.outputMode = outputMode;
    this.formattingMode = formattingMode;
    this.tableDetectionMode = tableDetectionMode;
  }

  public static String normalizeOutputMode(final String value) {
    return normalizeEnum(value, OUTPUT_MODES, "display", Collections.<String, String>emptyMap());
  }

  public static String normalizeFormattingMode(final String value) {
    return normalizeEnum(value, FORMATTING_MODES, "plain", Collections.<String, String>emptyMap());
  }

  public static String normalizeTableDetectionMode(final String value) {
    return normalizeEnum(value, TABLE_DETECTION_MODES, "balanced", TABLE_DETECTION_MODE_ALIASES);
  }

  public static ResolvedMarkdownOptions resolveMarkdownOptions(final MarkdownOptions options) {
    final MarkdownOptions input = options == null ? new MarkdownOptions() : options;
    return new ResolvedMarkdownOptions(
        resolveBoolean(input.treatFirstRowAsHeader, true),
        resolveBoolean(input.trimText, true),
        resolveBoolean(input.removeEmptyRows, true),
        resolveBoolean(input.removeEmptyColumns, true),
        resolveBoolean(input.includeShapeDetails, true),
        normalizeOutputMode(input.outputMode),
        normalizeFormattingMode(input.formattingMode),
        normalizeTableDetectionMode(input.tableDetectionMode));
  }

  private static String normalizeEnum(
      final String value,
      final List<String> allowedValues,
      final String fallback,
      final Map<String, String> aliases) {
    final String normalizedInput = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    if (normalizedInput.isEmpty()) {
      return fallback;
    }
    final String normalizedValue = aliases.containsKey(normalizedInput) ? aliases.get(normalizedInput) : normalizedInput;
    return allowedValues.contains(normalizedValue) ? normalizedValue : fallback;
  }

  private static boolean resolveBoolean(final Boolean value, final boolean fallback) {
    return value == null ? fallback : value.booleanValue();
  }

  public static final class ResolvedMarkdownOptions {
    private final boolean treatFirstRowAsHeader;
    private final boolean trimText;
    private final boolean removeEmptyRows;
    private final boolean removeEmptyColumns;
    private final boolean includeShapeDetails;
    private final String outputMode;
    private final String formattingMode;
    private final String tableDetectionMode;

    public ResolvedMarkdownOptions(
        final boolean treatFirstRowAsHeader,
        final boolean trimText,
        final boolean removeEmptyRows,
        final boolean removeEmptyColumns,
        final boolean includeShapeDetails,
        final String outputMode,
        final String formattingMode,
        final String tableDetectionMode) {
      this.treatFirstRowAsHeader = treatFirstRowAsHeader;
      this.trimText = trimText;
      this.removeEmptyRows = removeEmptyRows;
      this.removeEmptyColumns = removeEmptyColumns;
      this.includeShapeDetails = includeShapeDetails;
      this.outputMode = outputMode;
      this.formattingMode = formattingMode;
      this.tableDetectionMode = tableDetectionMode;
    }

    public boolean isTreatFirstRowAsHeader() {
      return treatFirstRowAsHeader;
    }

    public boolean isTrimText() {
      return trimText;
    }

    public boolean isRemoveEmptyRows() {
      return removeEmptyRows;
    }

    public boolean isRemoveEmptyColumns() {
      return removeEmptyColumns;
    }

    public boolean isIncludeShapeDetails() {
      return includeShapeDetails;
    }

    public String getOutputMode() {
      return outputMode;
    }

    public String getFormattingMode() {
      return formattingMode;
    }

    public String getTableDetectionMode() {
      return tableDetectionMode;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ResolvedMarkdownOptions)) {
        return false;
      }
      final ResolvedMarkdownOptions that = (ResolvedMarkdownOptions) other;
      return treatFirstRowAsHeader == that.treatFirstRowAsHeader
          && trimText == that.trimText
          && removeEmptyRows == that.removeEmptyRows
          && removeEmptyColumns == that.removeEmptyColumns
          && includeShapeDetails == that.includeShapeDetails
          && Objects.equals(outputMode, that.outputMode)
          && Objects.equals(formattingMode, that.formattingMode)
          && Objects.equals(tableDetectionMode, that.tableDetectionMode);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          Boolean.valueOf(treatFirstRowAsHeader),
          Boolean.valueOf(trimText),
          Boolean.valueOf(removeEmptyRows),
          Boolean.valueOf(removeEmptyColumns),
          Boolean.valueOf(includeShapeDetails),
          outputMode,
          formattingMode,
          tableDetectionMode);
    }
  }
}
