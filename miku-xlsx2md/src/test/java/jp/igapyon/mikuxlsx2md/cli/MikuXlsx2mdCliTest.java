/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class MikuXlsx2mdCliTest {
  @Test
  void printsHelpAndExitsSuccessfully() {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    final int exitCode = MikuXlsx2mdCli.run(new String[] {"--help"}, asPrintStream(stdout), asPrintStream(stderr));

    assertEquals(0, exitCode);
    assertTrue(asString(stdout).contains("Usage:"));
    assertTrue(asString(stdout).contains("--shape-details"));
    assertTrue(asString(stdout).contains("--include-shape-details"));
    assertTrue(asString(stdout).contains("--encoding"));
    assertTrue(asString(stdout).contains("--bom"));
    assertTrue(asString(stdout).contains("--formatting-mode"));
    assertTrue(asString(stdout).contains("--table-detection-mode"));
    assertTrue(asString(stdout).contains("Exit codes:"));
    assertEquals("", asString(stderr));
  }

  @Test
  void failsForUnknownOption() {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {"sample.xlsx", "--unknown-option"},
        asPrintStream(stdout),
        asPrintStream(stderr));

    assertEquals(1, exitCode);
    assertTrue(asString(stderr).contains("Unknown option: --unknown-option"));
  }

  @Test
  void acceptsKnownOptionsButReportsUnimplementedConversion() {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {
            "sample.xlsx",
            "--out", "/tmp/out.md",
            "--output-mode", "both",
            "--formatting-mode", "github",
            "--table-detection-mode", "border-priority",
            "--shape-details", "include",
            "--keep-empty-rows",
            "--keep-empty-columns",
            "--summary"
        },
        asPrintStream(stdout),
        asPrintStream(stderr));

    assertEquals(1, exitCode);
    assertTrue(asString(stderr).contains("Workbook conversion is not implemented yet"));
    assertTrue(asString(stderr).contains("Validated input: sample.xlsx"));
  }

  @Test
  void rejectsShiftJisBomCombination() {
    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

    final int exitCode = MikuXlsx2mdCli.run(
        new String[] {"sample.xlsx", "--encoding", "shift_jis", "--bom", "on"},
        asPrintStream(stdout),
        asPrintStream(stderr));

    assertEquals(1, exitCode);
    assertTrue(asString(stderr).contains("BOM cannot be enabled for shift_jis."));
  }

  private static PrintStream asPrintStream(final ByteArrayOutputStream buffer) {
    try {
      return new PrintStream(buffer, true, StandardCharsets.UTF_8.name());
    } catch (final java.io.UnsupportedEncodingException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static String asString(final ByteArrayOutputStream buffer) {
    return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
  }
}
