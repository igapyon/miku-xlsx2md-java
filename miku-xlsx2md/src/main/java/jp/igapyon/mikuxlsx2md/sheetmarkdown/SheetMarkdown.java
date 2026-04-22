/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sheetmarkdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport;
import jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions;
import jp.igapyon.mikuxlsx2md.narrativestructure.NarrativeStructure;
import jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRenderer;
import jp.igapyon.mikuxlsx2md.sheetassets.SheetAssets;
import jp.igapyon.mikuxlsx2md.tabledetector.TableDetector;
import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;
import jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser;

public final class SheetMarkdown {
  private static final Pattern ISO_DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
  private static final Pattern MONTH_TITLE_PATTERN = Pattern.compile("^\\d{4}年\\d{1,2}月$");

  private SheetMarkdown() {
  }

  public static Map<String, WorksheetParser.ParsedCell> buildCellMap(final WorksheetParser.ParsedSheet sheet) {
    return TableDetector.buildCellMap(sheet);
  }

  public static String formatCellForMarkdown(final WorksheetParser.ParsedCell cell, final MarkdownOptions options) {
    return formatCellForMarkdown(cell, options, null, null);
  }

  public static String formatCellForMarkdown(
      final WorksheetParser.ParsedCell cell,
      final MarkdownOptions options,
      final WorkbookLoader.ParsedWorkbook workbook,
      final WorksheetParser.ParsedSheet sheet) {
    if (cell == null) {
      return "";
    }
    final MarkdownOptions.ResolvedMarkdownOptions resolvedOptions = MarkdownOptions.resolveMarkdownOptions(options);
    final String displayValue = RichTextRenderer.compactText(cell.getOutputValue());
    final String rawValue = RichTextRenderer.compactText(cell.getRawValue());
    final String displayMarkdown = renderCellDisplayText(cell, resolvedOptions.getFormattingMode());
    if ("raw".equals(resolvedOptions.getOutputMode())) {
      return renderCellWithHyperlink(cell, rawValue.isEmpty() ? displayValue : rawValue, workbook, sheet, options);
    }
    if ("both".equals(resolvedOptions.getOutputMode())) {
      if (!rawValue.isEmpty() && !rawValue.equals(displayValue)) {
        if (!displayMarkdown.isEmpty()) {
          return renderCellWithHyperlink(cell, displayMarkdown, workbook, sheet, options) + " [raw=" + rawValue + "]";
        }
        return "[raw=" + rawValue + "]";
      }
      return renderCellWithHyperlink(cell, displayMarkdown.isEmpty() ? rawValue : displayMarkdown, workbook, sheet, options);
    }
    return renderCellWithHyperlink(cell, displayMarkdown, workbook, sheet, options);
  }

  public static boolean isCellInAnyTable(final int row, final int col, final List<TableCandidate> tables) {
    for (final TableCandidate table : safeTableCandidates(tables)) {
      if (row >= table.getStartRow() && row <= table.getEndRow() && col >= table.getStartCol() && col <= table.getEndCol()) {
        return true;
      }
    }
    return false;
  }

  public static List<NarrativeRowSegment> splitNarrativeRowSegments(
      final List<WorksheetParser.ParsedCell> cells,
      final MarkdownOptions options,
      final WorkbookLoader.ParsedWorkbook workbook,
      final WorksheetParser.ParsedSheet sheet) {
    final List<NarrativeRowSegment> segments = new ArrayList<NarrativeRowSegment>();
    NarrativeRowSegment current = null;
    int currentLastCol = -1;
    final List<WorksheetParser.ParsedCell> sortedCells = new ArrayList<WorksheetParser.ParsedCell>(cells == null
        ? Collections.<WorksheetParser.ParsedCell>emptyList() : cells);
    Collections.sort(sortedCells, new Comparator<WorksheetParser.ParsedCell>() {
      @Override
      public int compare(final WorksheetParser.ParsedCell left, final WorksheetParser.ParsedCell right) {
        return Integer.compare(left.getCol(), right.getCol());
      }
    });
    for (final WorksheetParser.ParsedCell cell : sortedCells) {
      final String value = formatCellForMarkdown(cell, options, workbook, sheet).trim();
      if (value.isEmpty()) {
        continue;
      }
      if (current == null || cell.getCol() - currentLastCol > 4) {
        current = new NarrativeRowSegment(cell.getCol(), new ArrayList<String>());
        segments.add(current);
      }
      current.getValues().add(value);
      currentLastCol = cell.getCol();
    }
    return segments;
  }

  public static Map<Integer, List<WorksheetParser.ParsedCell>> collectNarrativeCellsByRow(
      final WorksheetParser.ParsedSheet sheet,
      final List<TableCandidate> tables) {
    final Map<Integer, List<WorksheetParser.ParsedCell>> rowMap = new LinkedHashMap<Integer, List<WorksheetParser.ParsedCell>>();
    for (final WorksheetParser.ParsedCell cell : safeCells(sheet)) {
      if (stringValue(cell.getOutputValue()).isEmpty()) {
        continue;
      }
      if (isCellInAnyTable(cell.getRow(), cell.getCol(), tables)) {
        continue;
      }
      List<WorksheetParser.ParsedCell> entries = rowMap.get(Integer.valueOf(cell.getRow()));
      if (entries == null) {
        entries = new ArrayList<WorksheetParser.ParsedCell>();
        rowMap.put(Integer.valueOf(cell.getRow()), entries);
      }
      entries.add(cell);
    }
    return rowMap;
  }

  public static List<NarrativeItem> buildNarrativeItems(
      final WorkbookLoader.ParsedWorkbook workbook,
      final WorksheetParser.ParsedSheet sheet,
      final List<TableCandidate> tables,
      final MarkdownOptions options) {
    final Map<Integer, List<WorksheetParser.ParsedCell>> rowMap = collectNarrativeCellsByRow(sheet, tables);
    final List<Integer> rowNumbers = new ArrayList<Integer>(rowMap.keySet());
    Collections.sort(rowNumbers);
    final List<NarrativeItem> items = new ArrayList<NarrativeItem>();
    for (final Integer rowNumber : rowNumbers) {
      final List<WorksheetParser.ParsedCell> cells = rowMap.get(rowNumber);
      for (final NarrativeRowSegment segment : splitNarrativeRowSegments(cells, options, workbook, sheet)) {
        final String text = joinWithSpace(segment.getValues()).trim();
        if (!text.isEmpty()) {
          items.add(new NarrativeItem(rowNumber.intValue(), segment.getStartCol(), text, segment.getValues()));
        }
      }
    }
    return items;
  }

  public static List<NarrativeBlock> extractNarrativeBlocks(
      final WorkbookLoader.ParsedWorkbook workbook,
      final WorksheetParser.ParsedSheet sheet,
      final List<TableCandidate> tables,
      final MarkdownOptions options) {
    final List<NarrativeItem> items = buildNarrativeItems(workbook, sheet, tables, options);
    final List<NarrativeBlock> blocks = new ArrayList<NarrativeBlock>();
    NarrativeBlock current = null;
    int previousRow = -100;
    for (final NarrativeItem item : items) {
      if (shouldAppendToCalendarNarrativeBlock(current, item, previousRow)) {
        current.append(item);
      } else if (shouldStartNarrativeBlock(current, item.getRow(), previousRow, item.getStartCol())) {
        current = new NarrativeBlock(item);
        blocks.add(current);
      } else {
        current.append(item);
      }
      previousRow = item.getRow();
    }
    return blocks;
  }

  public static List<TableCandidate> detectTableCandidates(
      final WorksheetParser.ParsedSheet sheet,
      final String tableDetectionMode) {
    final List<TableCandidate> candidates = new ArrayList<TableCandidate>();
    for (final TableDetector.TableCandidate candidate : TableDetector.detectTableCandidates(
        sheet,
        TableDetector.DEFAULT_TABLE_SCORE_WEIGHTS,
        tableDetectionMode)) {
      candidates.add(new TableCandidate(
          candidate.getStartRow(),
          candidate.getStartCol(),
          candidate.getEndRow(),
          candidate.getEndCol(),
          candidate.getScore(),
          candidate.getReasonSummary()));
    }
    return candidates;
  }

  public static List<List<String>> matrixFromCandidate(
      final WorksheetParser.ParsedSheet sheet,
      final TableCandidate candidate,
      final MarkdownOptions options,
      final WorkbookLoader.ParsedWorkbook workbook) {
    return TableDetector.matrixFromCandidate(
        sheet,
        new TableDetector.Bounds(candidate.getStartRow(), candidate.getStartCol(), candidate.getEndRow(), candidate.getEndCol()),
        options,
        new TableDetector.CellFormatter() {
          @Override
          public String formatCellForMarkdown(final WorksheetParser.ParsedCell cell, final MarkdownOptions cellOptions) {
            return SheetMarkdown.formatCellForMarkdown(cell, cellOptions, workbook, sheet);
          }
        });
  }

  public static String renderNarrativeBlock(final NarrativeBlock block) {
    return NarrativeStructure.renderNarrativeBlock(block);
  }

  public static MarkdownExport.MarkdownFile convertSheetToMarkdown(
      final WorkbookLoader.ParsedWorkbook workbook,
      final WorksheetParser.ParsedSheet sheet,
      final MarkdownOptions options) {
    final SheetRenderState state = collectSheetRenderState(workbook, sheet, options);
    final String fileName = MarkdownExport.createOutputFileName(
        workbook.getName(),
        sheet.getIndex(),
        sheet.getName(),
        state.getResolvedOptions().getOutputMode(),
        state.getResolvedOptions().getFormattingMode());
    return new MarkdownExport.MarkdownFile(
        fileName,
        sheet.getName(),
        createSheetMarkdownText(workbook, sheet, state),
        createSheetSummary(sheet, state));
  }

  public static List<MarkdownExport.MarkdownFile> convertWorkbookToMarkdownFiles(
      final WorkbookLoader.ParsedWorkbook workbook,
      final MarkdownOptions options) {
    final List<MarkdownExport.MarkdownFile> files = new ArrayList<MarkdownExport.MarkdownFile>();
    for (final WorksheetParser.ParsedSheet sheet : workbook.getSheets()) {
      files.add(convertSheetToMarkdown(workbook, sheet, options));
    }
    return files;
  }

  public static SheetRenderState collectSheetRenderState(
      final WorkbookLoader.ParsedWorkbook workbook,
      final WorksheetParser.ParsedSheet sheet,
      final MarkdownOptions options) {
    final MarkdownOptions.ResolvedMarkdownOptions resolvedOptions = MarkdownOptions.resolveMarkdownOptions(options);
    final List<TableCandidate> tables = detectTableCandidates(sheet, resolvedOptions.getTableDetectionMode());
    final List<NarrativeBlock> narrativeBlocks = extractNarrativeBlocks(workbook, sheet, tables, options);
    final List<MarkdownExport.FormulaDiagnostic> formulaDiagnostics = createFormulaDiagnostics(sheet);
    final List<ContentSection> sections = new ArrayList<ContentSection>();
    for (final NarrativeBlock block : narrativeBlocks) {
      sections.add(new ContentSection(block.getStartRow(), block.getStartCol(), renderNarrativeBlock(block) + "\n", "narrative", block));
    }
    int tableCounter = 1;
    for (final TableCandidate table : tables) {
      final List<List<String>> rows = matrixFromCandidate(sheet, table, options, workbook);
      if (rows.isEmpty() || rows.get(0).isEmpty()) {
        continue;
      }
      final String range = AddressUtils.formatRange(table.getStartRow(), table.getStartCol(), table.getEndRow(), table.getEndCol());
      final String markdown = "### Table: " + leftPad(tableCounter, 3) + " (" + range + ")\n\n"
          + MarkdownExport.renderMarkdownTable(rows, resolvedOptions.isTreatFirstRowAsHeader()) + "\n";
      sections.add(new ContentSection(table.getStartRow(), table.getStartCol(), markdown, "table", null));
      tableCounter += 1;
    }
    Collections.sort(sections);
    final String body = renderGroupedSectionBody(sections);
    return new SheetRenderState(
        resolvedOptions,
        tables,
        narrativeBlocks,
        formulaDiagnostics,
        sections,
        body,
        SheetAssets.renderImageSection(sheet),
        SheetAssets.renderChartSection(safeCharts(sheet)),
        SheetAssets.renderShapeSection(safeShapes(sheet), resolvedOptions.isIncludeShapeDetails()));
  }

  public static String createSheetMarkdownText(
      final WorkbookLoader.ParsedWorkbook workbook,
      final WorksheetParser.ParsedSheet sheet,
      final SheetRenderState state) {
    return joinLinesWithEmptyPreserved(asList(
        "# Book: " + workbook.getName(),
        "",
        "## Sheet: " + sheet.getName(),
        "",
        state.getBody().isEmpty() ? "_No extractable body content was found._" : state.getBody(),
        state.getChartSection(),
        state.getShapeSection(),
        state.getImageSection()));
  }

  public static MarkdownExport.MarkdownSummary createSheetSummary(
      final WorksheetParser.ParsedSheet sheet,
      final SheetRenderState state) {
    final List<MarkdownExport.TableScoreDetail> tableScores = new ArrayList<MarkdownExport.TableScoreDetail>();
    for (final TableCandidate table : state.getTables()) {
      tableScores.add(new MarkdownExport.TableScoreDetail(
          AddressUtils.formatRange(table.getStartRow(), table.getStartCol(), table.getEndRow(), table.getEndCol()),
          table.getScore(),
          table.getReasonSummary()));
    }
    return new MarkdownExport.MarkdownSummary(
        state.getResolvedOptions().getOutputMode(),
        state.getResolvedOptions().getFormattingMode(),
        state.getResolvedOptions().getTableDetectionMode(),
        state.getSections().isEmpty() ? 0 : state.getSections().size(),
        state.getTables().size(),
        state.getNarrativeBlocks().size(),
        safeMerges(sheet).size(),
        safeImages(sheet).size(),
        safeCharts(sheet).size(),
        safeCells(sheet).size(),
        tableScores,
        state.getFormulaDiagnostics());
  }

  public static MarkdownExport.ExportWorkbook toExportWorkbook(final WorkbookLoader.ParsedWorkbook workbook) {
    final List<MarkdownExport.ExportSheet> sheets = new ArrayList<MarkdownExport.ExportSheet>();
    for (final WorksheetParser.ParsedSheet sheet : workbook.getSheets()) {
      final List<MarkdownExport.ExportImage> images = new ArrayList<MarkdownExport.ExportImage>();
      for (final WorksheetParser.ParsedImageAsset image : safeImages(sheet)) {
        images.add(new MarkdownExport.ExportImage(image.getPath(), image.getData()));
      }
      final List<MarkdownExport.ExportShape> shapes = new ArrayList<MarkdownExport.ExportShape>();
      for (final WorksheetParser.ParsedShapeAsset shape : safeShapes(sheet)) {
        shapes.add(new MarkdownExport.ExportShape(shape.getSvgPath(), shape.getSvgData()));
      }
      sheets.add(new MarkdownExport.ExportSheet(images, shapes));
    }
    return new MarkdownExport.ExportWorkbook(workbook.getName(), sheets);
  }

  private static List<MarkdownExport.FormulaDiagnostic> createFormulaDiagnostics(final WorksheetParser.ParsedSheet sheet) {
    final List<MarkdownExport.FormulaDiagnostic> diagnostics = new ArrayList<MarkdownExport.FormulaDiagnostic>();
    for (final WorksheetParser.ParsedCell cell : safeCells(sheet)) {
      if (!stringValue(cell.getFormulaText()).isEmpty() && cell.getResolutionStatus() != null) {
        diagnostics.add(new MarkdownExport.FormulaDiagnostic(
            cell.getAddress(),
            cell.getFormulaText(),
            cell.getResolutionStatus(),
            cell.getResolutionSource(),
            cell.getOutputValue()));
      }
    }
    return diagnostics;
  }

  private static String renderCellDisplayText(final WorksheetParser.ParsedCell cell, final String formattingMode) {
    return RichTextRenderer.renderCellDisplayText(cell, formattingMode, cell.getHyperlink() != null);
  }

  private static String renderCellWithHyperlink(
      final WorksheetParser.ParsedCell cell,
      final String text,
      final WorkbookLoader.ParsedWorkbook workbook,
      final WorksheetParser.ParsedSheet sheet,
      final MarkdownOptions options) {
    final WorksheetParser.Hyperlink hyperlink = cell.getHyperlink();
    final String label = stringValue(text).trim();
    if (hyperlink == null || label.isEmpty()) {
      return text;
    }
    if ("external".equals(hyperlink.getKind())) {
      final String href = stringValue(hyperlink.getTarget()).trim();
      return href.isEmpty() ? label : "[" + label + "](" + href + ")";
    }
    final String currentSheetName = sheet == null ? "" : sheet.getName();
    final InternalHyperlinkLocation location = parseInternalHyperlinkLocation(
        stringValue(hyperlink.getLocation()).isEmpty() ? hyperlink.getTarget() : hyperlink.getLocation(),
        currentSheetName);
    final String traceText = joinNonEmpty("!", location.getSheetName(), location.getRefText());
    final WorksheetParser.ParsedSheet targetSheet = findSheet(workbook, location.getSheetName());
    if (targetSheet == null || workbook == null) {
      return traceText.isEmpty() ? label : label + " (" + traceText + ")";
    }
    final String href = "#" + createHeadingFragment(targetSheet.getName());
    return !traceText.isEmpty() && !traceText.equals(targetSheet.getName())
        ? "[" + label + "](" + href + ") (" + traceText + ")"
        : "[" + label + "](" + href + ")";
  }

  private static InternalHyperlinkLocation parseInternalHyperlinkLocation(final String location, final String currentSheetName) {
    final String normalized = stringValue(location).trim().replaceFirst("^#", "");
    if (normalized.isEmpty()) {
      return new InternalHyperlinkLocation(currentSheetName, "");
    }
    final int bangIndex = normalized.lastIndexOf('!');
    if (bangIndex >= 0) {
      String sheetName = normalized.substring(0, bangIndex);
      if (sheetName.startsWith("'") && sheetName.endsWith("'") && sheetName.length() >= 2) {
        sheetName = sheetName.substring(1, sheetName.length() - 1).replace("''", "'");
      }
      return new InternalHyperlinkLocation(sheetName.isEmpty() ? currentSheetName : sheetName, normalized.substring(bangIndex + 1).trim());
    }
    return new InternalHyperlinkLocation(currentSheetName, normalized);
  }

  private static String createHeadingFragment(final String text) {
    return stringValue(text).trim().toLowerCase().replaceAll("<[^>]+>", "").replaceAll("[^\\p{L}\\p{N}\\s_-]+", "").replaceAll("\\s+", "-");
  }

  private static boolean shouldStartNarrativeBlock(
      final NarrativeBlock current,
      final int rowNumber,
      final int previousRow,
      final int startCol) {
    return current == null || rowNumber - previousRow > 1 || Math.abs(startCol - current.getStartCol()) > 3;
  }

  private static boolean shouldAppendToCalendarNarrativeBlock(
      final NarrativeBlock current,
      final NarrativeItem item,
      final int previousRow) {
    if (current == null || !blockHasCalendarDateItem(current)) {
      return false;
    }
    final int rowGap = item.getRow() - previousRow;
    if (rowGap < 0 || rowGap > 4) {
      return false;
    }
    final int startColDelta = Math.abs(item.getStartCol() - current.getStartCol());
    return startColDelta <= 24 || isCalendarDateItem(item);
  }

  private static boolean blockHasCalendarDateItem(final NarrativeBlock block) {
    for (final NarrativeItem item : block.getItems()) {
      if (isCalendarDateItem(item)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isCalendarDateItem(final NarrativeItem item) {
    final List<String> values = nonEmptyTrimmed(item.getCellValues());
    if (values.size() < 5) {
      return false;
    }
    for (final String value : values) {
      if (!isIsoDateToken(value) && !isWeekdayToken(value)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isCalendarHeaderSection(final ContentSection section) {
    if (!"narrative".equals(section.getKind()) || section.getNarrativeBlock() == null) {
      return false;
    }
    final List<String> values = getNarrativeValues(section);
    if (values.isEmpty() || values.size() > 10) {
      return false;
    }
    boolean allWeekdays = values.size() >= 5;
    boolean hasMonthTitle = false;
    boolean hasPlannerLabel = false;
    for (final String value : values) {
      allWeekdays = allWeekdays && isWeekdayToken(value);
      hasMonthTitle = hasMonthTitle || MONTH_TITLE_PATTERN.matcher(value).matches();
      hasPlannerLabel = hasPlannerLabel || "目標と優先事項".equals(value) || "その他".equals(value);
    }
    return allWeekdays || hasMonthTitle || hasPlannerLabel;
  }

  private static boolean isCalendarBodySection(final ContentSection section) {
    if (!"narrative".equals(section.getKind()) || section.getNarrativeBlock() == null) {
      return false;
    }
    for (final NarrativeItem item : section.getNarrativeBlock().getItems()) {
      if (isCalendarDateItem(item)) {
        return true;
      }
    }
    return false;
  }

  private static List<ContentSection> createCalendarAwareSectionEntries(final List<ContentSection> entries) {
    final List<ContentSection> mainCalendarEntries = new ArrayList<ContentSection>();
    for (final ContentSection entry : entries) {
      if (isCalendarBodySection(entry)) {
        mainCalendarEntries.add(entry);
      }
    }
    Collections.sort(mainCalendarEntries, new Comparator<ContentSection>() {
      @Override
      public int compare(final ContentSection left, final ContentSection right) {
        if (left.getSortCol() != right.getSortCol()) {
          return Double.compare(left.getSortCol(), right.getSortCol());
        }
        return Double.compare(left.getSortRow(), right.getSortRow());
      }
    });
    if (mainCalendarEntries.isEmpty()) {
      return entries;
    }
    final ContentSection main = mainCalendarEntries.get(0);
    final List<ContentSection> reordered = new ArrayList<ContentSection>();
    final List<ContentSection> headerEntries = new ArrayList<ContentSection>();
    final List<ContentSection> sidebarEntries = new ArrayList<ContentSection>();
    final List<ContentSection> remainingEntries = new ArrayList<ContentSection>();
    for (final ContentSection entry : entries) {
      if (entry == main) {
        continue;
      }
      if (isCalendarHeaderSection(entry) && entry.getSortRow() <= main.getSortRow() + 1) {
        headerEntries.add(entry);
      } else if (isCalendarBodySection(entry) && entry.getSortCol() >= main.getSortCol() + 10) {
        sidebarEntries.add(entry);
      } else {
        remainingEntries.add(entry);
      }
    }
    if (headerEntries.isEmpty() && sidebarEntries.isEmpty()) {
      return entries;
    }
    reordered.addAll(headerEntries);
    reordered.add(main);
    if (!sidebarEntries.isEmpty()) {
      final ContentSection firstSidebar = sidebarEntries.get(0);
      reordered.add(new ContentSection(firstSidebar.getSortRow() - 0.1d, firstSidebar.getSortCol(), "### Sidebar\n", "narrative", null));
      reordered.addAll(sidebarEntries);
    }
    reordered.addAll(remainingEntries);
    return reordered;
  }

  private static String renderGroupedSectionBody(final List<ContentSection> sections) {
    final List<String> entries = new ArrayList<String>();
    for (final ContentSection section : createCalendarAwareSectionEntries(sections)) {
      final String value = trimRight(section.getMarkdown());
      if (!value.isEmpty()) {
        entries.add(value);
      }
    }
    return joinParagraphs(entries).trim();
  }

  private static boolean isIsoDateToken(final String value) {
    return ISO_DATE_PATTERN.matcher(stringValue(value).trim()).matches();
  }

  private static boolean isWeekdayToken(final String value) {
    final String normalized = stringValue(value).trim();
    return "日".equals(normalized) || "月".equals(normalized) || "火".equals(normalized) || "水".equals(normalized)
        || "木".equals(normalized) || "金".equals(normalized) || "土".equals(normalized)
        || "日曜日".equals(normalized) || "月曜日".equals(normalized) || "火曜日".equals(normalized) || "水曜日".equals(normalized)
        || "木曜日".equals(normalized) || "金曜日".equals(normalized) || "土曜日".equals(normalized);
  }

  private static WorksheetParser.ParsedSheet findSheet(final WorkbookLoader.ParsedWorkbook workbook, final String sheetName) {
    if (workbook == null) {
      return null;
    }
    for (final WorksheetParser.ParsedSheet sheet : workbook.getSheets()) {
      if (sheet.getName().equals(sheetName)) {
        return sheet;
      }
    }
    return null;
  }

  private static List<String> getNarrativeValues(final ContentSection section) {
    final List<String> values = new ArrayList<String>();
    if (section.getNarrativeBlock() == null) {
      return values;
    }
    for (final NarrativeItem item : section.getNarrativeBlock().getItems()) {
      values.addAll(nonEmptyTrimmed(item.getCellValues()));
    }
    return values;
  }

  private static List<String> nonEmptyTrimmed(final List<String> input) {
    final List<String> values = new ArrayList<String>();
    for (final String value : input == null ? Collections.<String>emptyList() : input) {
      final String normalized = stringValue(value).trim();
      if (!normalized.isEmpty()) {
        values.add(normalized);
      }
    }
    return values;
  }

  private static List<WorksheetParser.ParsedCell> safeCells(final WorksheetParser.ParsedSheet sheet) {
    return sheet == null || sheet.getCells() == null ? Collections.<WorksheetParser.ParsedCell>emptyList() : sheet.getCells();
  }

  private static List<AddressUtils.MergeRange> safeMerges(final WorksheetParser.ParsedSheet sheet) {
    return sheet == null || sheet.getMerges() == null ? Collections.<AddressUtils.MergeRange>emptyList() : sheet.getMerges();
  }

  private static List<WorksheetParser.ParsedImageAsset> safeImages(final WorksheetParser.ParsedSheet sheet) {
    return sheet == null || sheet.getImages() == null ? Collections.<WorksheetParser.ParsedImageAsset>emptyList() : sheet.getImages();
  }

  private static List<WorksheetParser.ParsedChartAsset> safeCharts(final WorksheetParser.ParsedSheet sheet) {
    return sheet == null || sheet.getCharts() == null ? Collections.<WorksheetParser.ParsedChartAsset>emptyList() : sheet.getCharts();
  }

  private static List<WorksheetParser.ParsedShapeAsset> safeShapes(final WorksheetParser.ParsedSheet sheet) {
    return sheet == null || sheet.getShapes() == null ? Collections.<WorksheetParser.ParsedShapeAsset>emptyList() : sheet.getShapes();
  }

  private static List<TableCandidate> safeTableCandidates(final List<TableCandidate> tables) {
    return tables == null ? Collections.<TableCandidate>emptyList() : tables;
  }

  private static String stringValue(final String value) {
    return value == null ? "" : value;
  }

  private static String joinWithSpace(final List<String> values) {
    return join(values, " ");
  }

  private static String joinNonEmpty(final String delimiter, final String left, final String right) {
    final String normalizedLeft = stringValue(left);
    final String normalizedRight = stringValue(right);
    if (normalizedLeft.isEmpty()) {
      return normalizedRight;
    }
    if (normalizedRight.isEmpty()) {
      return normalizedLeft;
    }
    return normalizedLeft + delimiter + normalizedRight;
  }

  private static String joinLines(final List<String> values) {
    return join(values, "\n");
  }

  private static String joinParagraphs(final List<String> values) {
    return join(values, "\n\n");
  }

  private static String join(final List<String> values, final String delimiter) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < values.size(); index += 1) {
      if (index > 0) {
        builder.append(delimiter);
      }
      builder.append(values.get(index));
    }
    return builder.toString();
  }

  private static String joinLinesWithEmptyPreserved(final List<String> values) {
    return join(values, "\n");
  }

  private static List<String> asList(final String... values) {
    final List<String> result = new ArrayList<String>();
    Collections.addAll(result, values);
    return result;
  }

  private static String leftPad(final int value, final int width) {
    final String text = String.valueOf(value);
    if (text.length() >= width) {
      return text;
    }
    final StringBuilder builder = new StringBuilder();
    for (int index = text.length(); index < width; index += 1) {
      builder.append('0');
    }
    return builder.append(text).toString();
  }

  private static String trimRight(final String value) {
    return stringValue(value).replaceFirst("\\s+$", "");
  }

  public static final class TableCandidate {
    private final int startRow;
    private final int startCol;
    private final int endRow;
    private final int endCol;
    private final int score;
    private final List<String> reasonSummary;

    public TableCandidate(
        final int startRow,
        final int startCol,
        final int endRow,
        final int endCol,
        final int score,
        final List<String> reasonSummary) {
      this.startRow = startRow;
      this.startCol = startCol;
      this.endRow = endRow;
      this.endCol = endCol;
      this.score = score;
      this.reasonSummary = reasonSummary == null ? Collections.<String>emptyList() : reasonSummary;
    }

    public int getStartRow() {
      return startRow;
    }

    public int getStartCol() {
      return startCol;
    }

    public int getEndRow() {
      return endRow;
    }

    public int getEndCol() {
      return endCol;
    }

    public int getScore() {
      return score;
    }

    public List<String> getReasonSummary() {
      return reasonSummary;
    }
  }

  public static final class NarrativeRowSegment {
    private final int startCol;
    private final List<String> values;

    public NarrativeRowSegment(final int startCol, final List<String> values) {
      this.startCol = startCol;
      this.values = values;
    }

    public int getStartCol() {
      return startCol;
    }

    public List<String> getValues() {
      return values;
    }
  }

  public static final class NarrativeItem {
    private final int row;
    private final int startCol;
    private final String text;
    private final List<String> cellValues;

    public NarrativeItem(final int row, final int startCol, final String text, final List<String> cellValues) {
      this.row = row;
      this.startCol = startCol;
      this.text = text;
      this.cellValues = cellValues == null ? Collections.<String>emptyList() : cellValues;
    }

    public int getRow() {
      return row;
    }

    public int getStartCol() {
      return startCol;
    }

    public String getText() {
      return text;
    }

    public List<String> getCellValues() {
      return cellValues;
    }
  }

  public static final class NarrativeBlock {
    private final int startRow;
    private final int startCol;
    private int endRow;
    private final List<String> lines;
    private final List<NarrativeItem> items;

    public NarrativeBlock(final NarrativeItem item) {
      this.startRow = item.getRow();
      this.startCol = item.getStartCol();
      this.endRow = item.getRow();
      this.lines = new ArrayList<String>();
      this.items = new ArrayList<NarrativeItem>();
      append(item);
    }

    public void append(final NarrativeItem item) {
      lines.add(item.getText());
      endRow = item.getRow();
      items.add(item);
    }

    public int getStartRow() {
      return startRow;
    }

    public int getStartCol() {
      return startCol;
    }

    public int getEndRow() {
      return endRow;
    }

    public List<String> getLines() {
      return lines;
    }

    public List<NarrativeItem> getItems() {
      return items;
    }
  }

  public static final class SheetRenderState {
    private final MarkdownOptions.ResolvedMarkdownOptions resolvedOptions;
    private final List<TableCandidate> tables;
    private final List<NarrativeBlock> narrativeBlocks;
    private final List<MarkdownExport.FormulaDiagnostic> formulaDiagnostics;
    private final List<ContentSection> sections;
    private final String body;
    private final String imageSection;
    private final String chartSection;
    private final String shapeSection;

    public SheetRenderState(
        final MarkdownOptions.ResolvedMarkdownOptions resolvedOptions,
        final List<TableCandidate> tables,
        final List<NarrativeBlock> narrativeBlocks,
        final List<MarkdownExport.FormulaDiagnostic> formulaDiagnostics,
        final List<ContentSection> sections,
        final String body,
        final String imageSection,
        final String chartSection,
        final String shapeSection) {
      this.resolvedOptions = resolvedOptions;
      this.tables = tables;
      this.narrativeBlocks = narrativeBlocks;
      this.formulaDiagnostics = formulaDiagnostics;
      this.sections = sections;
      this.body = body;
      this.imageSection = imageSection;
      this.chartSection = chartSection;
      this.shapeSection = shapeSection;
    }

    public MarkdownOptions.ResolvedMarkdownOptions getResolvedOptions() {
      return resolvedOptions;
    }

    public List<TableCandidate> getTables() {
      return tables;
    }

    public List<NarrativeBlock> getNarrativeBlocks() {
      return narrativeBlocks;
    }

    public List<MarkdownExport.FormulaDiagnostic> getFormulaDiagnostics() {
      return formulaDiagnostics;
    }

    public List<ContentSection> getSections() {
      return sections;
    }

    public String getBody() {
      return body;
    }

    public String getImageSection() {
      return imageSection;
    }

    public String getChartSection() {
      return chartSection;
    }

    public String getShapeSection() {
      return shapeSection;
    }
  }

  public static final class ContentSection implements Comparable<ContentSection> {
    private final double sortRow;
    private final double sortCol;
    private final String markdown;
    private final String kind;
    private final NarrativeBlock narrativeBlock;

    public ContentSection(
        final double sortRow,
        final double sortCol,
        final String markdown,
        final String kind,
        final NarrativeBlock narrativeBlock) {
      this.sortRow = sortRow;
      this.sortCol = sortCol;
      this.markdown = markdown;
      this.kind = kind;
      this.narrativeBlock = narrativeBlock;
    }

    public double getSortRow() {
      return sortRow;
    }

    public double getSortCol() {
      return sortCol;
    }

    public String getMarkdown() {
      return markdown;
    }

    public String getKind() {
      return kind;
    }

    public NarrativeBlock getNarrativeBlock() {
      return narrativeBlock;
    }

    @Override
    public int compareTo(final ContentSection other) {
      if (sortRow != other.sortRow) {
        return Double.compare(sortRow, other.sortRow);
      }
      return Double.compare(sortCol, other.sortCol);
    }
  }

  private static final class InternalHyperlinkLocation {
    private final String sheetName;
    private final String refText;

    private InternalHyperlinkLocation(final String sheetName, final String refText) {
      this.sheetName = sheetName;
      this.refText = refText;
    }

    public String getSheetName() {
      return sheetName;
    }

    public String getRefText() {
      return refText;
    }
  }
}
