/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdownexport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import jp.igapyon.mikuxlsx2md.markdowntableescape.MarkdownTableEscape;
import jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalize;
import jp.igapyon.mikuxlsx2md.textencoding.TextEncoding;
import jp.igapyon.mikuxlsx2md.zipio.ZipIo;

public final class MarkdownExport {
  private static final List<String> FORMULA_STATUSES =
      Collections.unmodifiableList(Arrays.asList("resolved", "fallback_formula", "unsupported_external"));

  private MarkdownExport() {
  }

  public static String normalizeMarkdownLineBreaks(final String text) {
    return MarkdownNormalize.normalizeMarkdownText(text);
  }

  public static String escapeMarkdownCell(final String text) {
    return MarkdownTableEscape.escapeMarkdownTableCell(text);
  }

  public static String renderMarkdownTable(final List<List<String>> rows, final boolean treatFirstRowAsHeader) {
    if (rows == null || rows.isEmpty()) {
      return "";
    }
    final List<List<String>> workingRows = new ArrayList<List<String>>();
    for (final List<String> row : rows) {
      final List<String> convertedRow = new ArrayList<String>();
      for (final String cell : row) {
        convertedRow.add(escapeMarkdownCell(cell));
      }
      workingRows.add(convertedRow);
    }
    if (workingRows.size() == 1 && treatFirstRowAsHeader) {
      workingRows.add(createBlankRow(workingRows.get(0).size()));
    }
    final List<String> header = treatFirstRowAsHeader ? workingRows.get(0) : createBlankRow(workingRows.get(0).size());
    final List<List<String>> body = treatFirstRowAsHeader ? workingRows.subList(1, workingRows.size()) : workingRows;
    final List<String> lines = new ArrayList<String>();
    lines.add("| " + joinCells(header) + " |");
    lines.add("| " + joinCells(createDividerRow(header.size())) + " |");
    for (final List<String> row : body) {
      lines.add("| " + joinCells(row) + " |");
    }
    return joinLines(lines);
  }

  public static String sanitizeFileNameSegment(final String value, final String fallback) {
    final String normalized = String.valueOf(value == null ? "" : value).trim().replaceAll("\\s+", " ");
    final StringBuilder builder = new StringBuilder();
    boolean lastWasUnderscore = false;
    for (int index = 0; index < normalized.length(); index += 1) {
      final char ch = normalized.charAt(index);
      final boolean isUnsafe =
          ch == '\\' || ch == '/' || ch == ':' || ch == '*' || ch == '?' || ch == '"' || ch == '<' || ch == '>' || ch == '|';
      final boolean isWhitespace = Character.isWhitespace(ch);
      final boolean isAllowed =
          Character.isLetterOrDigit(ch) || ch == '.' || ch == '_' || ch == '-' || isWhitespace || isUnsafe;
      final char output = (isWhitespace || isUnsafe || !isAllowed) ? '_' : ch;
      if (output == '_') {
        if (lastWasUnderscore) {
          continue;
        }
        builder.append(output);
        lastWasUnderscore = true;
      } else {
        builder.append(output);
        lastWasUnderscore = false;
      }
    }
    final String sanitized = builder.toString().replaceAll("^[_ .-]+|[_ .-]+$", "");
    return sanitized.isEmpty() ? fallback : sanitized;
  }

  public static String stripWorkbookExtension(final String workbookName) {
    return String.valueOf(workbookName == null ? "" : workbookName).replaceFirst("(?i)\\.xlsx$", "");
  }

  public static String createCombinedMarkdownFileName(final String workbookName) {
    final String baseName = stripWorkbookExtension(String.valueOf(workbookName == null ? "workbook" : workbookName));
    return (baseName.isEmpty() ? "workbook" : baseName) + ".md";
  }

  public static String createExportEntryName(final String relativePath) {
    return "output/" + String.valueOf(relativePath == null ? "" : relativePath);
  }

  public static String createOutputFileName(
      final String workbookName,
      final int sheetIndex,
      final String sheetName) {
    return createOutputFileName(workbookName, sheetIndex, sheetName, "display", "plain");
  }

  public static String createOutputFileName(
      final String workbookName,
      final int sheetIndex,
      final String sheetName,
      final String outputMode) {
    return createOutputFileName(workbookName, sheetIndex, sheetName, outputMode, "plain");
  }

  public static String createOutputFileName(
      final String workbookName,
      final int sheetIndex,
      final String sheetName,
      final String outputMode,
      final String formattingMode) {
    final String bookBase = sanitizeFileNameSegment(stripWorkbookExtension(workbookName), "workbook");
    final String safeSheetName = sanitizeFileNameSegment(sheetName, "Sheet" + sheetIndex);
    final String normalizedOutputMode = outputMode == null ? "display" : outputMode.toLowerCase(Locale.ROOT);
    final String normalizedFormattingMode = formattingMode == null ? "plain" : formattingMode.toLowerCase(Locale.ROOT);
    if (!"display".equals(normalizedOutputMode) && !"raw".equals(normalizedOutputMode) && !"both".equals(normalizedOutputMode)) {
      throw new IllegalArgumentException("Unsupported output mode: " + String.valueOf(outputMode));
    }
    if (!"plain".equals(normalizedFormattingMode) && !"github".equals(normalizedFormattingMode)) {
      throw new IllegalArgumentException("Unsupported formatting mode: " + String.valueOf(formattingMode));
    }
    return bookBase + "_" + leftPad(sheetIndex, 3) + "_" + safeSheetName + ".md";
  }

  public static String createSummaryText(final MarkdownFile markdownFile) {
    final Map<String, Integer> formulaCounts = countFormulaStatuses(markdownFile.getSummary().getFormulaDiagnostics());
    final List<String> lines = new ArrayList<String>();
    lines.add("Output file: " + markdownFile.getFileName());
    lines.add("Output mode: " + markdownFile.getSummary().getOutputMode());
    lines.add("Formatting mode: " + markdownFile.getSummary().getFormattingMode());
    lines.add("Table detection mode: " + markdownFile.getSummary().getTableDetectionMode());
    lines.add("Sections: " + markdownFile.getSummary().getSections());
    lines.add("Tables: " + markdownFile.getSummary().getTables());
    lines.add("Narrative blocks: " + markdownFile.getSummary().getNarrativeBlocks());
    lines.add("Merged ranges: " + markdownFile.getSummary().getMerges());
    lines.add("Images: " + markdownFile.getSummary().getImages());
    lines.add("Charts: " + markdownFile.getSummary().getCharts());
    lines.add("Analyzed cells: " + markdownFile.getSummary().getCells());
    for (final String status : FORMULA_STATUSES) {
      lines.add("Formula " + status + ": " + formulaCounts.get(status));
    }
    for (final TableScoreDetail detail : markdownFile.getSummary().getTableScores()) {
      lines.add("Table candidate " + detail.getRange() + ": score " + detail.getScore() + " / " + joinReasons(detail.getReasons()));
    }
    return joinLines(lines);
  }

  public static CombinedMarkdownExportFile createCombinedMarkdownExportFile(
      final ExportWorkbook workbook,
      final List<MarkdownFile> markdownFiles) {
    final String fileName = createCombinedMarkdownFileName(workbook.getName());
    final String bookHeading = "# Book: " + String.valueOf(workbook.getName() == null ? "workbook.xlsx" : workbook.getName());
    final List<String> parts = new ArrayList<String>();
    parts.add(bookHeading);
    for (final MarkdownFile markdownFile : markdownFiles) {
      final String stripped = stripBookHeading(markdownFile.getMarkdown(), bookHeading);
      if (!stripped.trim().isEmpty()) {
        parts.add(stripped);
      }
    }
    return new CombinedMarkdownExportFile(fileName, joinParagraphs(parts));
  }

  public static byte[] encodeMarkdownText(final String text, final TextEncoding.MarkdownEncodingOptions options) {
    return TextEncoding.encodeText(text, options);
  }

  public static CombinedMarkdownExportPayload createCombinedMarkdownExportPayload(
      final ExportWorkbook workbook,
      final List<MarkdownFile> markdownFiles,
      final TextEncoding.MarkdownEncodingOptions options) {
    final CombinedMarkdownExportFile combined = createCombinedMarkdownExportFile(workbook, markdownFiles);
    return new CombinedMarkdownExportPayload(
        combined.getFileName(),
        combined.getContent(),
        encodeMarkdownText(combined.getContent() + "\n", options),
        TextEncoding.createTextMimeType(options));
  }

  public static ZipIo.ExportEntry createMarkdownExportEntry(
      final ExportWorkbook workbook,
      final List<MarkdownFile> markdownFiles,
      final TextEncoding.MarkdownEncodingOptions options) {
    if (markdownFiles == null || markdownFiles.isEmpty()) {
      return null;
    }
    final CombinedMarkdownExportPayload combined = createCombinedMarkdownExportPayload(workbook, markdownFiles, options);
    return new ZipIo.ExportEntry(createExportEntryName(combined.getFileName()), combined.getData());
  }

  public static List<ZipIo.ExportEntry> createAssetExportEntries(final ExportWorkbook workbook) {
    final List<ZipIo.ExportEntry> entries = new ArrayList<ZipIo.ExportEntry>();
    for (final ExportSheet sheet : workbook.getSheets()) {
      for (final ExportImage image : sheet.getImages()) {
        entries.add(new ZipIo.ExportEntry(createExportEntryName(image.getPath()), image.getData()));
      }
      for (final ExportShape shape : sheet.getShapes()) {
        if (shape.getSvgPath() == null || shape.getSvgData() == null) {
          continue;
        }
        entries.add(new ZipIo.ExportEntry(createExportEntryName(shape.getSvgPath()), shape.getSvgData()));
      }
    }
    return entries;
  }

  public static List<ZipIo.ExportEntry> createExportEntries(
      final ExportWorkbook workbook,
      final List<MarkdownFile> markdownFiles,
      final TextEncoding.MarkdownEncodingOptions options) {
    final List<ZipIo.ExportEntry> entries = new ArrayList<ZipIo.ExportEntry>(createAssetExportEntries(workbook));
    final ZipIo.ExportEntry markdownEntry = createMarkdownExportEntry(workbook, markdownFiles, options);
    if (markdownEntry != null) {
      entries.add(0, markdownEntry);
    }
    return entries;
  }

  public static byte[] createWorkbookExportArchive(
      final ExportWorkbook workbook,
      final List<MarkdownFile> markdownFiles,
      final TextEncoding.MarkdownEncodingOptions options) {
    final List<ZipIo.ExportEntry> entries = createExportEntries(workbook, markdownFiles, options);
    return ZipIo.createStoredZip(entries.toArray(new ZipIo.ExportEntry[entries.size()]));
  }

  private static List<String> createBlankRow(final int columnCount) {
    final List<String> result = new ArrayList<String>();
    for (int index = 0; index < columnCount; index += 1) {
      result.add("");
    }
    return result;
  }

  private static List<String> createDividerRow(final int columnCount) {
    final List<String> result = new ArrayList<String>();
    for (int index = 0; index < columnCount; index += 1) {
      result.add("---");
    }
    return result;
  }

  private static String joinCells(final List<String> values) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < values.size(); index += 1) {
      if (index > 0) {
        builder.append(" | ");
      }
      builder.append(values.get(index));
    }
    return builder.toString();
  }

  private static String joinLines(final List<String> values) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < values.size(); index += 1) {
      if (index > 0) {
        builder.append('\n');
      }
      builder.append(values.get(index));
    }
    return builder.toString();
  }

  private static String joinParagraphs(final List<String> values) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < values.size(); index += 1) {
      if (index > 0) {
        builder.append("\n\n");
      }
      builder.append(values.get(index));
    }
    return builder.toString();
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

  private static String joinReasons(final List<String> reasons) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < reasons.size(); index += 1) {
      if (index > 0) {
        builder.append(", ");
      }
      builder.append(reasons.get(index));
    }
    return builder.toString();
  }

  private static String stripBookHeading(final String markdown, final String bookHeading) {
    final List<String> lines = new ArrayList<String>(Arrays.asList(String.valueOf(markdown == null ? "" : markdown).split("\\n", -1)));
    if (!lines.isEmpty() && bookHeading.equals(lines.get(0))) {
      lines.remove(0);
      while (!lines.isEmpty() && lines.get(0).isEmpty()) {
        lines.remove(0);
      }
    }
    return joinLines(lines);
  }

  private static Map<String, Integer> countFormulaStatuses(final List<FormulaDiagnostic> diagnostics) {
    final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
    for (final String status : FORMULA_STATUSES) {
      counts.put(status, Integer.valueOf(0));
    }
    for (final FormulaDiagnostic item : diagnostics) {
      if (item.getStatus() != null && counts.containsKey(item.getStatus())) {
        counts.put(item.getStatus(), Integer.valueOf(counts.get(item.getStatus()).intValue() + 1));
      }
    }
    return counts;
  }

  public static final class FormulaDiagnostic {
    private final String address;
    private final String formulaText;
    private final String status;
    private final String source;
    private final String outputValue;

    public FormulaDiagnostic(
        final String address,
        final String formulaText,
        final String status,
        final String source,
        final String outputValue) {
      this.address = address;
      this.formulaText = formulaText;
      this.status = status;
      this.source = source;
      this.outputValue = outputValue;
    }

    public String getAddress() {
      return address;
    }

    public String getFormulaText() {
      return formulaText;
    }

    public String getStatus() {
      return status;
    }

    public String getSource() {
      return source;
    }

    public String getOutputValue() {
      return outputValue;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof FormulaDiagnostic)) {
        return false;
      }
      final FormulaDiagnostic that = (FormulaDiagnostic) other;
      return Objects.equals(address, that.address)
          && Objects.equals(formulaText, that.formulaText)
          && Objects.equals(status, that.status)
          && Objects.equals(source, that.source)
          && Objects.equals(outputValue, that.outputValue);
    }

    @Override
    public int hashCode() {
      return Objects.hash(address, formulaText, status, source, outputValue);
    }
  }

  public static final class TableScoreDetail {
    private final String range;
    private final int score;
    private final List<String> reasons;

    public TableScoreDetail(final String range, final int score, final List<String> reasons) {
      this.range = range;
      this.score = score;
      this.reasons = reasons == null ? Collections.<String>emptyList() : reasons;
    }

    public String getRange() {
      return range;
    }

    public int getScore() {
      return score;
    }

    public List<String> getReasons() {
      return reasons;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof TableScoreDetail)) {
        return false;
      }
      final TableScoreDetail that = (TableScoreDetail) other;
      return score == that.score
          && Objects.equals(range, that.range)
          && Objects.equals(reasons, that.reasons);
    }

    @Override
    public int hashCode() {
      return Objects.hash(range, Integer.valueOf(score), reasons);
    }
  }

  public static final class MarkdownSummary {
    private final String outputMode;
    private final String formattingMode;
    private final String tableDetectionMode;
    private final int sections;
    private final int tables;
    private final int narrativeBlocks;
    private final int merges;
    private final int images;
    private final int charts;
    private final int cells;
    private final List<TableScoreDetail> tableScores;
    private final List<FormulaDiagnostic> formulaDiagnostics;

    public MarkdownSummary(
        final String outputMode,
        final String formattingMode,
        final String tableDetectionMode,
        final int sections,
        final int tables,
        final int narrativeBlocks,
        final int merges,
        final int images,
        final int charts,
        final int cells,
        final List<TableScoreDetail> tableScores,
        final List<FormulaDiagnostic> formulaDiagnostics) {
      this.outputMode = outputMode;
      this.formattingMode = formattingMode;
      this.tableDetectionMode = tableDetectionMode;
      this.sections = sections;
      this.tables = tables;
      this.narrativeBlocks = narrativeBlocks;
      this.merges = merges;
      this.images = images;
      this.charts = charts;
      this.cells = cells;
      this.tableScores = tableScores == null ? Collections.<TableScoreDetail>emptyList() : tableScores;
      this.formulaDiagnostics = formulaDiagnostics == null ? Collections.<FormulaDiagnostic>emptyList() : formulaDiagnostics;
    }

    public String getOutputMode() {
      return outputMode;
    }

    public String getFormattingMode() {
      return formattingMode;
    }

    public String getTableDetectionMode() {
      return tableDetectionMode;
    }

    public int getSections() {
      return sections;
    }

    public int getTables() {
      return tables;
    }

    public int getNarrativeBlocks() {
      return narrativeBlocks;
    }

    public int getMerges() {
      return merges;
    }

    public int getImages() {
      return images;
    }

    public int getCharts() {
      return charts;
    }

    public int getCells() {
      return cells;
    }

    public List<TableScoreDetail> getTableScores() {
      return tableScores;
    }

    public List<FormulaDiagnostic> getFormulaDiagnostics() {
      return formulaDiagnostics;
    }
  }

  public static final class MarkdownFile {
    private final String fileName;
    private final String sheetName;
    private final String markdown;
    private final MarkdownSummary summary;

    public MarkdownFile(final String fileName, final String sheetName, final String markdown, final MarkdownSummary summary) {
      this.fileName = fileName;
      this.sheetName = sheetName;
      this.markdown = markdown;
      this.summary = summary;
    }

    public String getFileName() {
      return fileName;
    }

    public String getSheetName() {
      return sheetName;
    }

    public String getMarkdown() {
      return markdown;
    }

    public MarkdownSummary getSummary() {
      return summary;
    }
  }

  public static final class ExportWorkbook {
    private final String name;
    private final List<ExportSheet> sheets;

    public ExportWorkbook(final String name, final List<ExportSheet> sheets) {
      this.name = name;
      this.sheets = sheets == null ? Collections.<ExportSheet>emptyList() : sheets;
    }

    public String getName() {
      return name;
    }

    public List<ExportSheet> getSheets() {
      return sheets;
    }
  }

  public static final class ExportSheet {
    private final List<ExportImage> images;
    private final List<ExportShape> shapes;

    public ExportSheet(final List<ExportImage> images, final List<ExportShape> shapes) {
      this.images = images == null ? Collections.<ExportImage>emptyList() : images;
      this.shapes = shapes == null ? Collections.<ExportShape>emptyList() : shapes;
    }

    public List<ExportImage> getImages() {
      return images;
    }

    public List<ExportShape> getShapes() {
      return shapes;
    }
  }

  public static final class ExportImage {
    private final String path;
    private final byte[] data;

    public ExportImage(final String path, final byte[] data) {
      this.path = path;
      this.data = data;
    }

    public String getPath() {
      return path;
    }

    public byte[] getData() {
      return data;
    }
  }

  public static final class ExportShape {
    private final String svgPath;
    private final byte[] svgData;

    public ExportShape(final String svgPath, final byte[] svgData) {
      this.svgPath = svgPath;
      this.svgData = svgData;
    }

    public String getSvgPath() {
      return svgPath;
    }

    public byte[] getSvgData() {
      return svgData;
    }
  }

  public static final class CombinedMarkdownExportFile {
    private final String fileName;
    private final String content;

    public CombinedMarkdownExportFile(final String fileName, final String content) {
      this.fileName = fileName;
      this.content = content;
    }

    public String getFileName() {
      return fileName;
    }

    public String getContent() {
      return content;
    }
  }

  public static final class CombinedMarkdownExportPayload {
    private final String fileName;
    private final String content;
    private final byte[] data;
    private final String mimeType;

    public CombinedMarkdownExportPayload(
        final String fileName,
        final String content,
        final byte[] data,
        final String mimeType) {
      this.fileName = fileName;
      this.content = content;
      this.data = data;
      this.mimeType = mimeType;
    }

    public String getFileName() {
      return fileName;
    }

    public String getContent() {
      return content;
    }

    public byte[] getData() {
      return data;
    }

    public String getMimeType() {
      return mimeType;
    }
  }
}
