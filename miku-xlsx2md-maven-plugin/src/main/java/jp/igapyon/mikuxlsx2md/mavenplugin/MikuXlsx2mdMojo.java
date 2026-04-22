/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.mavenplugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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

    throw new MojoExecutionException(
        "miku-xlsx2md Maven plugin is not connected to workbook conversion yet.");
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
