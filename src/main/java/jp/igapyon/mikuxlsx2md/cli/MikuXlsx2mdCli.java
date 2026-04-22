/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.cli;

import java.io.PrintStream;

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

      err.println("Workbook conversion is not implemented yet in the Java port.");
      err.println("Validated input: " + options.getInputPath());
      return 1;
    } catch (final IllegalArgumentException ex) {
      err.println(ex.getMessage());
      return 1;
    }
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
    out.println("  --formatting-mode <mode>      plain | github (default: plain)");
    out.println("  --table-detection-mode <mode> balanced | border (default: balanced)");
    out.println("  --shape-details <mode>        include | exclude (default: exclude)");
    out.println("  --include-shape-details       Alias for --shape-details include");
    out.println("  --no-header-row               Do not treat the first row as a table header");
    out.println("  --no-trim-text                Preserve surrounding whitespace");
    out.println("  --keep-empty-rows             Keep empty rows");
    out.println("  --keep-empty-columns          Keep empty columns");
    out.println("  --summary                     Print per-sheet summary to stdout");
    out.println("  --help                        Show help and exit");
    out.println();
    out.println("Exit codes:");
    out.println("  0                             Success");
    out.println("  1                             Error");
  }
}
