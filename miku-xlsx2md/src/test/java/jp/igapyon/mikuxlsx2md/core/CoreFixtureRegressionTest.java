/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

class CoreFixtureRegressionTest {
  @Test
  void parsesUpstreamNamedRangeFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("named-range", "named-range-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "named-range-sample01.xlsx");

    assertEquals("named-range-sample01.xlsx", workbook.getName());
    assertEquals(Arrays.asList("Summary", "Other"), Arrays.asList(
        workbook.getSheets().get(0).getName(),
        workbook.getSheets().get(1).getName()));
    assertEquals(Arrays.asList(
        new WorkbookLoader.DefinedName("BaseName", "=Summary!$B$3", null),
        new WorkbookLoader.DefinedName("BaseRange", "=Summary!$B$4:$B$5", null),
        new WorkbookLoader.DefinedName("LocalCross", "=Other!$B$2", "Other")),
        workbook.getDefinedNames());
    assertTrue(workbook.getSharedStrings().stream().anyMatch((entry) -> entry.getText().contains("definedNames サンプル")));
    assertEquals("=BaseName", findCell(workbook.getSheets().get(0).getCells(), "D3").getFormulaText());
    assertEquals("Base", findCell(workbook.getSheets().get(0).getCells(), "D3").getOutputValue());
    assertEquals("=SUM(BaseRange)", findCell(workbook.getSheets().get(0).getCells(), "D4").getFormulaText());
    assertEquals("30", findCell(workbook.getSheets().get(0).getCells(), "D4").getOutputValue());
    assertEquals("=LocalCross", findCell(workbook.getSheets().get(1).getCells(), "D2").getFormulaText());
    assertEquals("CrossRef", findCell(workbook.getSheets().get(1).getCells(), "D2").getOutputValue());
  }

  @Test
  void parsesUpstreamHyperlinkFixtureWorkbookWhenAvailable() throws IOException {
    final Path fixturePath = resolveFixturePath("link", "hyperlink-basic-sample01.xlsx");
    Assumptions.assumeTrue(Files.isRegularFile(fixturePath), "upstream fixture is not available in workplace/");

    final WorkbookLoader.ParsedWorkbook workbook = Core.parseWorkbook(Files.readAllBytes(fixturePath), "hyperlink-basic-sample01.xlsx");
    final List<WorksheetParser.ParsedSheet> sheets = workbook.getSheets();
    final WorksheetParser.ParsedSheet summary = sheets.get(0);

    assertEquals(Arrays.asList("Summary", "Other"), Arrays.asList(sheets.get(0).getName(), sheets.get(1).getName()));
    assertEquals(8, workbook.getSharedStrings().size());
    assertEquals("Open example", findCell(summary.getCells(), "A1").getOutputValue());
    assertEquals("Jump to Other", findCell(summary.getCells(), "A2").getOutputValue());
    assertEquals("https://example.com/", findCell(summary.getCells(), "A1").getHyperlink().getTarget());
    assertEquals("external", findCell(summary.getCells(), "A1").getHyperlink().getKind());
    assertEquals("Other!A1", findCell(summary.getCells(), "A2").getHyperlink().getTarget());
    assertEquals("internal", findCell(summary.getCells(), "A2").getHyperlink().getKind());
    assertEquals("https://example.com/docs", findCell(summary.getCells(), "B5").getHyperlink().getTarget());
    assertEquals("Jump to Other", findCell(summary.getCells(), "B6").getHyperlink().getDisplay());
  }

  private static WorksheetParser.ParsedCell findCell(final List<WorksheetParser.ParsedCell> cells, final String address) {
    for (final WorksheetParser.ParsedCell cell : cells) {
      if (address.equals(cell.getAddress())) {
        assertNotNull(cell);
        return cell;
      }
    }
    throw new AssertionError("Cell was not found: " + address);
  }

  private static Path resolveFixturePath(final String group, final String fileName) {
    final Path local = Paths.get("workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
    if (Files.isRegularFile(local)) {
      return local;
    }
    return Paths.get("..", "workplace", "miku-xlsx2md", "tests", "fixtures", group, fileName);
  }
}
