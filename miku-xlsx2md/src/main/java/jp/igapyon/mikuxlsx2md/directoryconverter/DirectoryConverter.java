/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.directoryconverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.igapyon.mikuxlsx2md.core.Core;
import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.textencoding.TextEncoding;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;

public final class DirectoryConverter {
  private DirectoryConverter() {
  }

  public static List<DirectoryConversionResult> convertDirectory(final DirectoryConversionOptions options)
      throws IOException {
    if (options == null) {
      throw new IllegalArgumentException("options is required.");
    }
    final Path inputRoot = requireInputDirectory(options.getInputDirectory());
    final Path outputRoot = resolveOutputDirectory(inputRoot, options.getOutputDirectory());
    final List<Path> workbookPaths = collectWorkbookPaths(inputRoot, options.isRecursive());
    final List<DirectoryConversionResult> results = new ArrayList<DirectoryConversionResult>();
    for (final Path workbookPath : workbookPaths) {
      results.add(convertOneWorkbook(inputRoot, outputRoot, workbookPath, options));
    }
    return results;
  }

  public static List<Path> collectWorkbookPaths(final Path inputRoot, final boolean recursive) throws IOException {
    final List<Path> result = new ArrayList<Path>();
    final Stream<Path> stream = recursive ? Files.walk(inputRoot) : Files.list(inputRoot);
    try {
      result.addAll(stream
          .filter(Files::isRegularFile)
          .filter(DirectoryConverter::isXlsxFile)
          .sorted(Comparator.comparing(Path::toString))
          .collect(Collectors.toList()));
    } finally {
      stream.close();
    }
    return result;
  }

  public static boolean isXlsxFile(final Path path) {
    final String name = path == null || path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(Locale.ROOT);
    return name.endsWith(".xlsx");
  }

  private static DirectoryConversionResult convertOneWorkbook(
      final Path inputRoot,
      final Path outputRoot,
      final Path workbookPath,
      final DirectoryConversionOptions options) throws IOException {
    final String workbookName = workbookPath.getFileName() == null ? "workbook.xlsx" : workbookPath.getFileName().toString();
    final WorkbookLoader.ParsedWorkbook workbook;
    try {
      workbook = Core.parseWorkbook(Files.readAllBytes(workbookPath), workbookName);
    } catch (final RuntimeException ex) {
      throw new IOException(formatWorkbookError(workbookName, "parse failed", ex), ex);
    }

    final List<MarkdownExport.MarkdownFile> files;
    try {
      files = Core.convertWorkbookToMarkdownFiles(workbook, options.getMarkdownOptions());
    } catch (final RuntimeException ex) {
      throw new IOException(formatWorkbookError(workbookName, "convert failed", ex), ex);
    }

    final MarkdownExport.CombinedMarkdownExportPayload payload;
    try {
      payload = MarkdownExport.createCombinedMarkdownExportPayload(
          Core.toExportWorkbook(workbook),
          files,
          options.getEncodingOptions());
    } catch (final RuntimeException ex) {
      throw new IOException(formatWorkbookError(workbookName, "encode failed", ex), ex);
    }

    final Path relativeParent = inputRoot.relativize(workbookPath.getParent());
    final Path outputPath = outputRoot.resolve(relativeParent).resolve(payload.getFileName());
    try {
      final Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.write(outputPath, payload.getData());
    } catch (final IOException ex) {
      throw new IOException(formatWorkbookError(workbookName, "markdown write failed", ex), ex);
    }
    return new DirectoryConversionResult(workbookPath, outputPath, workbookName, files);
  }

  private static Path requireInputDirectory(final Path inputDirectory) {
    if (inputDirectory == null) {
      throw new IllegalArgumentException("inputDirectory is required.");
    }
    final Path inputRoot = inputDirectory.toAbsolutePath().normalize();
    if (!Files.isDirectory(inputRoot)) {
      throw new IllegalArgumentException("inputDirectory must be an existing directory.");
    }
    return inputRoot;
  }

  private static Path resolveOutputDirectory(final Path inputRoot, final Path outputDirectory) {
    if (outputDirectory == null) {
      return inputRoot;
    }
    final Path outputRoot = outputDirectory.toAbsolutePath().normalize();
    if (Files.exists(outputRoot) && !Files.isDirectory(outputRoot)) {
      throw new IllegalArgumentException("outputDirectory must be a directory when specified.");
    }
    return outputRoot;
  }

  private static String formatWorkbookError(final String workbookName, final String stage, final Throwable error) {
    final String message = error.getMessage() == null ? String.valueOf(error) : error.getMessage();
    return "[" + workbookName + "] " + stage + ": " + message;
  }

  public static final class DirectoryConversionOptions {
    private final Path inputDirectory;
    private final Path outputDirectory;
    private final boolean recursive;
    private final MarkdownOptions markdownOptions;
    private final TextEncoding.MarkdownEncodingOptions encodingOptions;

    public DirectoryConversionOptions(
        final Path inputDirectory,
        final Path outputDirectory,
        final boolean recursive,
        final MarkdownOptions markdownOptions,
        final TextEncoding.MarkdownEncodingOptions encodingOptions) {
      this.inputDirectory = inputDirectory;
      this.outputDirectory = outputDirectory;
      this.recursive = recursive;
      this.markdownOptions = markdownOptions == null ? new MarkdownOptions() : markdownOptions;
      this.encodingOptions = encodingOptions == null
          ? new TextEncoding.MarkdownEncodingOptions("utf-8", "off")
          : encodingOptions;
    }

    public Path getInputDirectory() {
      return inputDirectory;
    }

    public Path getOutputDirectory() {
      return outputDirectory;
    }

    public boolean isRecursive() {
      return recursive;
    }

    public MarkdownOptions getMarkdownOptions() {
      return markdownOptions;
    }

    public TextEncoding.MarkdownEncodingOptions getEncodingOptions() {
      return encodingOptions;
    }
  }

  public static final class DirectoryConversionResult {
    private final Path inputPath;
    private final Path outputPath;
    private final String workbookName;
    private final List<MarkdownExport.MarkdownFile> markdownFiles;

    DirectoryConversionResult(
        final Path inputPath,
        final Path outputPath,
        final String workbookName,
        final List<MarkdownExport.MarkdownFile> markdownFiles) {
      this.inputPath = inputPath;
      this.outputPath = outputPath;
      this.workbookName = workbookName;
      this.markdownFiles = markdownFiles;
    }

    public Path getInputPath() {
      return inputPath;
    }

    public Path getOutputPath() {
      return outputPath;
    }

    public String getWorkbookName() {
      return workbookName;
    }

    public List<MarkdownExport.MarkdownFile> getMarkdownFiles() {
      return markdownFiles;
    }
  }
}
