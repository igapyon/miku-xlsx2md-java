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
import jp.igapyon.mikuxlsx2md.directoryconverter.DirectoryConverter;
import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;

public final class MikuXlsx2mdCli {
  private static final String FALLBACK_VERSION = "1.2.0";

  private MikuXlsx2mdCli() {
  }

  public static void main(final String[] args) {
    final int exitCode = run(args, System.out, System.err);
    System.exit(exitCode);
  }

  public static int run(final String[] args, final PrintStream out, final PrintStream err) {
    try {
      final CliOptions options = CliOptions.parse(args);
      if (options.isVersion()) {
        out.println(resolveToolVersion());
        return 0;
      }
      if (options.isHelp() || (options.getInputPath() == null && options.getInputDirectory() == null)) {
        printHelp(out);
        return options.isHelp() ? 0 : 1;
      }

      if (options.getInputDirectory() != null) {
        convertDirectory(options, out, err);
      } else {
        convertWorkbook(options, out, err);
      }
      return 0;
    } catch (final IllegalArgumentException ex) {
      err.println(ex.getMessage());
      return 1;
    } catch (final IOException ex) {
      err.println(ex.getMessage());
      return 1;
    }
  }

  private static void convertWorkbook(final CliOptions options, final PrintStream out, final PrintStream err) throws IOException {
    final Path inputPath = Paths.get(options.getInputPath()).toAbsolutePath();
    final String workbookName = inputPath.getFileName() == null ? "workbook.xlsx" : inputPath.getFileName().toString();
    if (options.isVerbose()) {
      err.println("[processing] " + inputPath.toString());
    }
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

    final MarkdownExport.MarkdownExportOptions exportOptions = createMarkdownExportOptions(
        options,
        options.getInputPath());
    if (options.getZipPath() != null) {
      try {
        final byte[] archive = MarkdownExport.createWorkbookExportArchive(Core.toExportWorkbook(workbook), files, exportOptions);
        writeBinaryFile(Paths.get(options.getZipPath()).toAbsolutePath(), archive);
      } catch (final RuntimeException ex) {
        throw new IOException(formatWorkbookError(workbookName, "zip write failed", ex));
      }
    }

    if (options.getZipPath() == null || options.getOutPath() != null) {
      final MarkdownExport.CombinedMarkdownExportPayload combined =
          MarkdownExport.createCombinedMarkdownExportPayload(Core.toExportWorkbook(workbook), files, exportOptions);
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

  private static void convertDirectory(final CliOptions options, final PrintStream out, final PrintStream err) throws IOException {
    final List<DirectoryConverter.DirectoryConversionResult> results;
    try {
      results = DirectoryConverter.convertDirectory(new DirectoryConverter.DirectoryConversionOptions(
          Paths.get(options.getInputDirectory()),
          options.getOutputDirectory() == null ? null : Paths.get(options.getOutputDirectory()),
          options.isRecursive(),
          createMarkdownOptions(options),
          createMarkdownExportOptions(options, null),
          options.isVerbose() ? new DirectoryConverter.ProgressListener() {
            @Override
            public void processing(final Path workbookPath) {
              err.println("[processing] " + workbookPath.toString());
            }
          } : null));
    } catch (final IllegalArgumentException ex) {
      throw ex;
    }
    for (final DirectoryConverter.DirectoryConversionResult result : results) {
      if (options.isSummary()) {
        printWorkbookSummary(out, result.getWorkbookName(), result.getMarkdownFiles());
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

  private static MarkdownExport.MarkdownExportOptions createMarkdownExportOptions(
      final CliOptions options,
      final String sourcePath) {
    return new MarkdownExport.MarkdownExportOptions(
        options.getEncoding(),
        options.getBom(),
        sourcePath,
        resolveToolVersion(),
        options.isIncludeShapeDetails() ? "include" : "exclude",
        null);
  }

  private static String resolveToolVersion() {
    final Package packageInfo = MikuXlsx2mdCli.class.getPackage();
    if (packageInfo != null && packageInfo.getImplementationVersion() != null) {
      return packageInfo.getImplementationVersion();
    }
    return FALLBACK_VERSION;
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
    out.println("  java -jar miku-xlsx2md-java.jar --input-directory <dir> [options]");
    out.println();
    out.println("Purpose:");
    out.println("  Convert one local Excel .xlsx workbook into AI-friendly, human-reviewable Markdown.");
    out.println("  The conversion extracts workbook structure and semantic content; it does not try to");
    out.println("  reproduce the exact Excel visual layout.");
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
    out.println("  --version                     Show version and exit");
    out.println("  --help                        Show help and exit");
    out.println();
    out.println("Java-side directory extension:");
    out.println("  --input-directory <dir>       Convert .xlsx files under this directory");
    out.println("  --output-directory <dir>      Write directory conversion output under this directory");
    out.println("  --recursive                   Scan input directory recursively");
    out.println("  --verbose                     Print processing file paths to stderr");
    out.println();
    out.println("GUI-aligned defaults:");
    out.println("  output-mode=display, formatting-mode=github, table-detection-mode=balanced, shape-details=exclude");
    out.println();
    out.println("Output contract for agents:");
    out.println("  - The primary Markdown output is one workbook-level combined Markdown document.");
    out.println("  - ZIP output contains output/<workbook>.md plus extracted assets under output/assets/.");
    out.println("  - Combined Markdown always starts with YAML front matter, then \"# Book: <workbook>\",");
    out.println("    followed by \"## Sheet: <sheet>\" sections in workbook sheet order.");
    out.println("  - sources[0].path in front matter is the input path passed to this CLI.");
    out.println("  - created and updated are generation dates. Use the core export API generatedDate");
    out.println("    option, not a CLI flag, when deterministic fixture output is required.");
    out.println("  - Use --summary for machine-readable-ish progress logs; use the Markdown front");
    out.println("    matter and body as the durable conversion artifact.");
    out.println();
    out.println("Front matter fields:");
    out.println("  title, description, type, category, topics, status, audience, created, updated,");
    out.println("  sources, conversion");
    out.println();
    out.println("Stable topic values:");
    out.println("  converted, xlsx, markdown, miku-xlsx2md, workbook-conversion");
    out.println();
    out.println("Exit codes:");
    out.println("  0                             Success");
    out.println("  1                             Error");
  }
}
