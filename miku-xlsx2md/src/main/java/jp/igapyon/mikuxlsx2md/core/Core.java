/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.core;

import java.util.List;

import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;

public final class Core {
  private Core() {
  }

  public static WorkbookLoader.ParsedWorkbook parseWorkbook(final byte[] workbookBytes, final String workbookName) {
    return WorkbookLoader.parseWorkbook(workbookBytes, workbookName);
  }

  public static List<MarkdownExport.MarkdownFile> convertWorkbookToMarkdownFiles(
      final WorkbookLoader.ParsedWorkbook workbook,
      final MarkdownOptions options) {
    return SheetMarkdown.convertWorkbookToMarkdownFiles(workbook, options);
  }

  public static MarkdownExport.MarkdownFile convertSheetToMarkdown(
      final WorkbookLoader.ParsedWorkbook workbook,
      final jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser.ParsedSheet sheet,
      final MarkdownOptions options) {
    return SheetMarkdown.convertSheetToMarkdown(workbook, sheet, options);
  }

  public static MarkdownExport.ExportWorkbook toExportWorkbook(final WorkbookLoader.ParsedWorkbook workbook) {
    return SheetMarkdown.toExportWorkbook(workbook);
  }
}
