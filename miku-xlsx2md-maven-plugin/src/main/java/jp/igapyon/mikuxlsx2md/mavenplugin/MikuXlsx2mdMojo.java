/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import jp.igapyon.mikuxlsx2md.core.Core;
import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.textencoding.TextEncoding;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;

@Mojo(name = "convert", threadSafe = true)
public class MikuXlsx2mdMojo extends AbstractMojo {
  @Parameter(property = "miku-xlsx2md.inputFile", required = true)
  private File inputFile;

  @Parameter(property = "miku-xlsx2md.outputFile")
  private File outputFile;

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

    if (inputFile == null) {
      throw new MojoExecutionException("inputFile is required.");
    }

    final String workbookName = inputFile.getName();
    final WorkbookLoader.ParsedWorkbook workbook;
    try {
      workbook = Core.parseWorkbook(Files.readAllBytes(inputFile.toPath()), workbookName);
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

    final File actualOutputFile = outputFile == null ? new File(payload.getFileName()) : outputFile;
    try {
      final File parent = actualOutputFile.getParentFile();
      if (parent != null) {
        Files.createDirectories(parent.toPath());
      }
      Files.write(actualOutputFile.toPath(), payload.getData());
    } catch (final IOException ex) {
      throw new MojoExecutionException(formatWorkbookError(workbookName, "markdown write failed", ex), ex);
    }
    getLog().info("miku-xlsx2md wrote " + actualOutputFile.getPath());
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

  void setInputFile(final File inputFile) {
    this.inputFile = inputFile;
  }

  void setOutputFile(final File outputFile) {
    this.outputFile = outputFile;
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

  File getInputFile() {
    return inputFile;
  }

  File getOutputFile() {
    return outputFile;
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
