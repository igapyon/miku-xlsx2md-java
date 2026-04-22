/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.mavenplugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

class MikuXlsx2mdMojoTest {
  @Test
  void skipsWhenRequested() {
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setSkip(true);

    assertDoesNotThrow(() -> mojo.execute());
  }

  @Test
  void failsFastUntilCoreConversionIsImplemented() {
    final MikuXlsx2mdMojo mojo = new MikuXlsx2mdMojo();
    mojo.setInputFile(new File("sample.xlsx"));
    mojo.setOutputFile(new File("sample.md"));
    mojo.setOutputMode("both");
    mojo.setFormattingMode("github");
    mojo.setTableDetectionMode("border");
    mojo.setEncoding("utf-8");
    mojo.setBom("off");

    final MojoExecutionException exception = assertThrows(MojoExecutionException.class, () -> mojo.execute());
    assertEquals("miku-xlsx2md Maven plugin is not connected to workbook conversion yet.", exception.getMessage());
    assertEquals("sample.xlsx", mojo.getInputFile().getPath());
    assertEquals("sample.md", mojo.getOutputFile().getPath());
    assertEquals("both", mojo.getOutputMode());
    assertEquals("github", mojo.getFormattingMode());
    assertEquals("border", mojo.getTableDetectionMode());
    assertEquals("utf-8", mojo.getEncoding());
    assertEquals("off", mojo.getBom());
  }
}
