/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import jp.igapyon.mikuxlsx2md.core.Core;
import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.textencoding.TextEncoding;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;

@Mojo(name = "convert-directory", threadSafe = true)
public class ConvertDirectoryMojo extends AbstractMojo {
  @Parameter(property = "miku-xlsx2md.inputDirectory", required = true)
  private File inputDirectory;

  @Parameter(property = "miku-xlsx2md.outputDirectory")
  private File outputDirectory;

  @Parameter(property = "miku-xlsx2md.recursive", defaultValue = "false")
  private boolean recursive;

  @Parameter(property = "miku-xlsx2md.outputMode", defaultValue = "display")
  private String outputMode = "display";

  @Parameter(property = "miku-xlsx2md.formattingMode", defaultValue = "plain")
  private String formattingMode = "plain";

  @Parameter(property = "miku-xlsx2md.tableDetectionMode", defaultValue = "balanced")
  private String tableDetectionMode = "balanced";

  @Parameter(property = "miku-xlsx2md.encoding", defaultValue = "utf-8")
  private String encoding = "utf-8";

  @Parameter(property = "miku-xlsx2md.bom", defaultValue = "off")
  private String bom = "off";

  @Parameter(property = "miku-xlsx2md.skip", defaultValue = "false")
  private boolean skip;

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("miku-xlsx2md skipped.");
      return;
    }

    if (inputDirectory == null) {
      throw new MojoExecutionException("inputDirectory is required.");
    }
    if (!inputDirectory.isDirectory()) {
      throw new MojoExecutionException("inputDirectory must be an existing directory.");
    }
    if (outputDirectory != null && outputDirectory.exists() && !outputDirectory.isDirectory()) {
      throw new MojoExecutionException("outputDirectory must be a directory when specified.");
    }

    final Path inputRoot = inputDirectory.toPath().toAbsolutePath().normalize();
    final Path outputRoot = (outputDirectory == null ? inputRoot : outputDirectory.toPath()).toAbsolutePath().normalize();
    final List<Path> workbookPaths = collectWorkbookPaths(inputRoot, recursive);
    if (workbookPaths.isEmpty()) {
      getLog().info("No .xlsx files found under " + inputRoot.getFileName());
      return;
    }

    for (final Path workbookPath : workbookPaths) {
      convertOneWorkbook(inputRoot, outputRoot, workbookPath);
    }
  }

  private void convertOneWorkbook(final Path inputRoot, final Path outputRoot, final Path workbookPath)
      throws MojoExecutionException {
    final String workbookName = workbookPath.getFileName() == null ? "workbook.xlsx" : workbookPath.getFileName().toString();
    final WorkbookLoader.ParsedWorkbook workbook;
    try {
      workbook = Core.parseWorkbook(Files.readAllBytes(workbookPath), workbookName);
    } catch (final IOException ex) {
      throw new MojoExecutionException(formatWorkbookError(workbookName, "read failed", ex), ex);
    } catch (final RuntimeException ex) {
      throw new MojoExecutionException(formatWorkbookError(workbookName, "parse failed", ex), ex);
    }

    final List<MarkdownExport.MarkdownFile> files;
    try {
      files = Core.convertWorkbookToMarkdownFiles(workbook, createMarkdownOptions());
    } catch (final RuntimeException ex) {
      throw new MojoExecutionException(formatWorkbookError(workbookName, "convert failed", ex), ex);
    }

    final MarkdownExport.CombinedMarkdownExportPayload payload;
    try {
      payload = MarkdownExport.createCombinedMarkdownExportPayload(
          Core.toExportWorkbook(workbook),
          files,
          new TextEncoding.MarkdownEncodingOptions(encoding, bom));
    } catch (final RuntimeException ex) {
      throw new MojoExecutionException(formatWorkbookError(workbookName, "encode failed", ex), ex);
    }

    final Path relativeParent = inputRoot.relativize(workbookPath.getParent());
    final Path actualOutputPath = outputRoot.resolve(relativeParent).resolve(payload.getFileName());
    try {
      final Path parent = actualOutputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.write(actualOutputPath, payload.getData());
    } catch (final IOException ex) {
      throw new MojoExecutionException(formatWorkbookError(workbookName, "markdown write failed", ex), ex);
    }
    getLog().info("miku-xlsx2md wrote " + actualOutputPath.toString());
  }

  private static List<Path> collectWorkbookPaths(final Path inputRoot, final boolean recursive) throws MojoExecutionException {
    final List<Path> result = new ArrayList<Path>();
    try {
      final Stream<Path> stream = recursive ? Files.walk(inputRoot) : Files.list(inputRoot);
      try {
        result.addAll(stream
            .filter(Files::isRegularFile)
            .filter(ConvertDirectoryMojo::isXlsxFile)
            .sorted(Comparator.comparing(Path::toString))
            .collect(Collectors.toList()));
      } finally {
        stream.close();
      }
    } catch (final IOException ex) {
      throw new MojoExecutionException("Directory scan failed: " + ex.getMessage(), ex);
    }
    return result;
  }

  private static boolean isXlsxFile(final Path path) {
    final String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(Locale.ROOT);
    return name.endsWith(".xlsx");
  }

  private MarkdownOptions createMarkdownOptions() {
    return new MarkdownOptions(
        null,
        null,
        null,
        null,
        null,
        outputMode,
        formattingMode,
        tableDetectionMode);
  }

  private static String formatWorkbookError(final String workbookName, final String stage, final Throwable error) {
    final String message = error.getMessage() == null ? String.valueOf(error) : error.getMessage();
    return "[" + workbookName + "] " + stage + ": " + message;
  }

  void setInputDirectory(final File inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  void setOutputDirectory(final File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  void setRecursive(final boolean recursive) {
    this.recursive = recursive;
  }

  void setOutputMode(final String outputMode) {
    this.outputMode = outputMode;
  }

  void setFormattingMode(final String formattingMode) {
    this.formattingMode = formattingMode;
  }

  void setTableDetectionMode(final String tableDetectionMode) {
    this.tableDetectionMode = tableDetectionMode;
  }

  void setEncoding(final String encoding) {
    this.encoding = encoding;
  }

  void setBom(final String bom) {
    this.bom = bom;
  }

  void setSkip(final boolean skip) {
    this.skip = skip;
  }

  File getInputDirectory() {
    return inputDirectory;
  }

  File getOutputDirectory() {
    return outputDirectory;
  }

  boolean isRecursive() {
    return recursive;
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
