/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.textencoding.TextEncoding;

final class CliOptions {
  private static final String[] SHAPE_DETAILS_MODES = {"include", "exclude"};

  private String inputPath;
  private String outPath;
  private String zipPath;
  private boolean treatFirstRowAsHeader = true;
  private boolean trimText = true;
  private boolean removeEmptyRows = true;
  private boolean removeEmptyColumns = true;
  private boolean includeShapeDetails;
  private boolean summary;
  private boolean help;
  private String outputMode = "display";
  private String formattingMode = "plain";
  private String tableDetectionMode = "balanced";
  private String encoding = "utf-8";
  private String bom = "off";

  static CliOptions parse(final String[] args) {
    final CliOptions options = new CliOptions();
    final Map<String, String> aliases = new HashMap<String, String>();
    aliases.put("border-priority", "border");

    int positionalCount = 0;
    for (int index = 0; index < args.length; index += 1) {
      final String arg = args[index];
      if (!arg.startsWith("--")) {
        positionalCount += 1;
        if (positionalCount > 1) {
          throw new IllegalArgumentException("Specify exactly one input workbook.");
        }
        options.inputPath = arg;
        continue;
      }

      if ("--help".equals(arg)) {
        options.help = true;
        continue;
      }
      if ("--include-shape-details".equals(arg)) {
        options.includeShapeDetails = true;
        continue;
      }
      if ("--no-header-row".equals(arg)) {
        options.treatFirstRowAsHeader = false;
        continue;
      }
      if ("--no-trim-text".equals(arg)) {
        options.trimText = false;
        continue;
      }
      if ("--keep-empty-rows".equals(arg)) {
        options.removeEmptyRows = false;
        continue;
      }
      if ("--keep-empty-columns".equals(arg)) {
        options.removeEmptyColumns = false;
        continue;
      }
      if ("--summary".equals(arg)) {
        options.summary = true;
        continue;
      }

      if ("--out".equals(arg)) {
        final String value = nextValue(args, arg, index);
        index += 1;
        options.outPath = value;
      } else if ("--zip".equals(arg)) {
        final String value = nextValue(args, arg, index);
        index += 1;
        options.zipPath = value;
      } else if ("--output-mode".equals(arg)) {
        final String value = nextValue(args, arg, index);
        index += 1;
        options.outputMode = normalizeEnumOption(value, MarkdownOptions.OUTPUT_MODES.toArray(new String[0]), "output mode", null);
      } else if ("--formatting-mode".equals(arg)) {
        final String value = nextValue(args, arg, index);
        index += 1;
        options.formattingMode = normalizeEnumOption(value, MarkdownOptions.FORMATTING_MODES.toArray(new String[0]), "formatting mode", null);
      } else if ("--table-detection-mode".equals(arg)) {
        final String value = nextValue(args, arg, index);
        index += 1;
        options.tableDetectionMode =
            normalizeEnumOption(value, MarkdownOptions.TABLE_DETECTION_MODES.toArray(new String[0]), "table detection mode", aliases);
      } else if ("--shape-details".equals(arg)) {
        final String value = nextValue(args, arg, index);
        index += 1;
        options.includeShapeDetails = "include".equals(normalizeEnumOption(value, SHAPE_DETAILS_MODES, "shape details mode", null));
      } else if ("--encoding".equals(arg)) {
        final String value = nextValue(args, arg, index);
        index += 1;
        options.encoding = normalizeEnumOption(value, TextEncoding.ENCODINGS.toArray(new String[0]), "encoding", null);
      } else if ("--bom".equals(arg)) {
        final String value = nextValue(args, arg, index);
        index += 1;
        options.bom = normalizeEnumOption(value, TextEncoding.BOM_MODES.toArray(new String[0]), "BOM mode", null);
      } else {
        throw new IllegalArgumentException("Unknown option: " + arg);
      }
    }

    if ("shift_jis".equals(options.encoding) && "on".equals(options.bom)) {
      throw new IllegalArgumentException("BOM cannot be enabled for shift_jis.");
    }

    return options;
  }

  private static String nextValue(final String[] args, final String arg, final int index) {
    if (index + 1 >= args.length) {
      throw new IllegalArgumentException("Missing value for " + arg);
    }
    return args[index + 1];
  }

  private static String normalizeEnumOption(
      final String value,
      final String[] allowedValues,
      final String label,
      final Map<String, String> aliases) {
    final String normalized = aliases != null && aliases.containsKey(value) ? aliases.get(value) : value;
    if (!Arrays.asList(allowedValues).contains(normalized)) {
      throw new IllegalArgumentException("Invalid " + label + ": " + value);
    }
    return normalized;
  }

  String getInputPath() {
    return inputPath;
  }

  boolean isHelp() {
    return help;
  }

  boolean isSummary() {
    return summary;
  }

  String getOutPath() {
    return outPath;
  }

  String getZipPath() {
    return zipPath;
  }

  boolean isTreatFirstRowAsHeader() {
    return treatFirstRowAsHeader;
  }

  boolean isTrimText() {
    return trimText;
  }

  boolean isRemoveEmptyRows() {
    return removeEmptyRows;
  }

  boolean isRemoveEmptyColumns() {
    return removeEmptyColumns;
  }

  boolean isIncludeShapeDetails() {
    return includeShapeDetails;
  }

  String getOutputMode() {
    return outputMode;
  }

  String getFormattingMode() {
    return formattingMode;
  }

  String getTableDetectionMode() {
    return tableDetectionMode;
  }

  String getEncoding() {
    return encoding;
  }

  String getBom() {
    return bom;
  }
}
