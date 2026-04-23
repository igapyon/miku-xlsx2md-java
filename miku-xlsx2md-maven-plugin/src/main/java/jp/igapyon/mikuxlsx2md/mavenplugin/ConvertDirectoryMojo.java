/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import jp.igapyon.mikuxlsx2md.directoryconverter.DirectoryConverter;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.textencoding.TextEncoding;

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

    final List<DirectoryConverter.DirectoryConversionResult> results;
    try {
      results = DirectoryConverter.convertDirectory(new DirectoryConverter.DirectoryConversionOptions(
          inputDirectory == null ? null : inputDirectory.toPath(),
          outputDirectory == null ? null : outputDirectory.toPath(),
          recursive,
          createMarkdownOptions(),
          new TextEncoding.MarkdownEncodingOptions(encoding, bom)));
    } catch (final IllegalArgumentException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    } catch (final IOException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
    if (results.isEmpty()) {
      final Path inputPath = inputDirectory == null ? null : inputDirectory.toPath().toAbsolutePath().normalize();
      getLog().info("No .xlsx files found under " + (inputPath == null ? "inputDirectory" : inputPath.getFileName()));
      return;
    }
    for (final DirectoryConverter.DirectoryConversionResult result : results) {
      getLog().info("miku-xlsx2md wrote " + result.getOutputPath().toString());
    }
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
