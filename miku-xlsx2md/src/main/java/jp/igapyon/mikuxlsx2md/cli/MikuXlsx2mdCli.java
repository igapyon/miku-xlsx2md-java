/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import jp.igapyon.mikuxlsx2md.core.Core;
import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.textencoding.TextEncoding;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;

public final class MikuXlsx2mdCli {
  private MikuXlsx2mdCli() {
  }

  public static void main(final String[] args) {
    final int exitCode = run(args, System.out, System.err);
    System.exit(exitCode);
  }

  public static int run(final String[] args, final PrintStream out, final PrintStream err) {
    try {
      final CliOptions options = CliOptions.parse(args);
      if (options.isHelp() || options.getInputPath() == null) {
        printHelp(out);
        return options.isHelp() ? 0 : 1;
      }

      convertWorkbook(options, out);
      return 0;
    } catch (final IllegalArgumentException ex) {
      err.println(ex.getMessage());
      return 1;
    } catch (final IOException ex) {
      err.println(ex.getMessage());
      return 1;
    }
  }

  private static void convertWorkbook(final CliOptions options, final PrintStream out) throws IOException {
    final Path inputPath = Paths.get(options.getInputPath()).toAbsolutePath();
    final String workbookName = inputPath.getFileName() == null ? "workbook.xlsx" : inputPath.getFileName().toString();
    final byte[] workbookBytes;
    try {
      workbookBytes = Files.readAllBytes(inputPath);
    } catch (final IOException ex) {
      throw new IOException(formatWorkbookError(workbookName, "read failed", ex));
    }

    final WorkbookLoader.ParsedWorkbook workbook;
    try {
      workbook = Core.parseWorkbook(workbookBytes, workbookName);
    } catch (final RuntimeException ex) {
      throw new IOException(formatWorkbookError(workbookName, "parse failed", ex));
    }

    final List<MarkdownExport.MarkdownFile> files;
    try {
      files = Core.convertWorkbookToMarkdownFiles(workbook, createMarkdownOptions(options));
    } catch (final RuntimeException ex) {
      throw new IOException(formatWorkbookError(workbookName, "convert failed", ex));
    }

    if (options.isSummary()) {
      printWorkbookSummary(out, workbookName, files);
    }

    final TextEncoding.MarkdownEncodingOptions encodingOptions =
        new TextEncoding.MarkdownEncodingOptions(options.getEncoding(), options.getBom());
    if (options.getZipPath() != null) {
      try {
        final byte[] archive = MarkdownExport.createWorkbookExportArchive(Core.toExportWorkbook(workbook), files, encodingOptions);
        writeBinaryFile(Paths.get(options.getZipPath()).toAbsolutePath(), archive);
      } catch (final RuntimeException ex) {
        throw new IOException(formatWorkbookError(workbookName, "zip write failed", ex));
      }
    }

    if (options.getZipPath() == null || options.getOutPath() != null) {
      final MarkdownExport.CombinedMarkdownExportPayload combined =
          MarkdownExport.createCombinedMarkdownExportPayload(Core.toExportWorkbook(workbook), files, encodingOptions);
      final Path outputPath = options.getOutPath() == null
          ? Paths.get(combined.getFileName()).toAbsolutePath()
          : Paths.get(options.getOutPath()).toAbsolutePath();
      try {
        writeBinaryFile(outputPath, combined.getData());
      } catch (final IOException ex) {
        throw new IOException(formatWorkbookError(workbookName, "markdown write failed", ex));
      }
    }
  }

  private static MarkdownOptions createMarkdownOptions(final CliOptions options) {
    return new MarkdownOptions(
        Boolean.valueOf(options.isTreatFirstRowAsHeader()),
        Boolean.valueOf(options.isTrimText()),
        Boolean.valueOf(options.isRemoveEmptyRows()),
        Boolean.valueOf(options.isRemoveEmptyColumns()),
        Boolean.valueOf(options.isIncludeShapeDetails()),
        options.getOutputMode(),
        options.getFormattingMode(),
        options.getTableDetectionMode());
  }

  private static void printWorkbookSummary(
      final PrintStream out,
      final String workbookName,
      final List<MarkdownExport.MarkdownFile> files) {
    out.println("[workbook] " + workbookName);
    for (final MarkdownExport.MarkdownFile file : files) {
      out.println(MarkdownExport.createSummaryText(file));
      out.println();
    }
  }

  private static void writeBinaryFile(final Path outputPath, final byte[] data) throws IOException {
    final Path parent = outputPath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.write(outputPath, data);
  }

  private static String formatWorkbookError(final String workbookName, final String stage, final Throwable error) {
    final String message = error.getMessage() == null ? String.valueOf(error) : error.getMessage();
    return "[" + workbookName + "] " + stage + ": " + message;
  }

  static void printHelp(final PrintStream out) {
    out.println("Usage:");
    out.println("  java -jar miku-xlsx2md-java.jar <input.xlsx> [options]");
    out.println();
    out.println("Options:");
    out.println("  --out <file>                  Write combined Markdown to this file");
    out.println("  --zip <file>                  Write ZIP export to this file");
    out.println("  --encoding <value>            utf-8 | shift_jis | utf-16le | utf-16be | utf-32le | utf-32be (default: utf-8)");
    out.println("  --bom <value>                 off | on (default: off; shift_jis does not allow on)");
    out.println("  --output-mode <mode>          display | raw | both (default: display)");
    out.println("  --formatting-mode <mode>      plain | github (default: github)");
    out.println("  --table-detection-mode <mode> balanced | border | planner-aware (default: balanced)");
    out.println("  --shape-details <mode>        include | exclude (default: exclude)");
    out.println("  --include-shape-details       Alias for --shape-details include");
    out.println("  --no-header-row               Do not treat the first row as a table header");
    out.println("  --no-trim-text                Preserve surrounding whitespace");
    out.println("  --keep-empty-rows             Keep empty rows");
    out.println("  --keep-empty-columns          Keep empty columns");
    out.println("  --summary                     Print per-sheet summary to stdout");
    out.println("  --help                        Show help and exit");
    out.println();
    out.println("GUI-aligned defaults:");
    out.println("  output-mode=display, formatting-mode=github, table-detection-mode=balanced, shape-details=exclude");
    out.println();
    out.println("Exit codes:");
    out.println("  0                             Success");
    out.println("  1                             Error");
  }
}
