/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.worksheettables;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.addressutils.AddressUtils;
import jp.igapyon.mikuxlsx2md.relsparser.RelsParser;
import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

public final class WorksheetTables {
  private WorksheetTables() {
  }

  public static String normalizeStructuredTableKey(final String value) {
    return Normalizer.normalize(String.valueOf(value == null ? "" : value), Normalizer.Form.NFKC)
        .trim()
        .toUpperCase(Locale.ROOT)
        .replaceAll("\\s+", " ");
  }

  public static List<ParsedTable> parseWorksheetTables(
      final Map<String, byte[]> files,
      final Document worksheetDoc,
      final String sheetName,
      final String sheetPath) {
    final Map<String, String> sheetRels = RelsParser.parseRelationships(files, RelsParser.buildRelsPath(sheetPath), sheetPath);
    final List<ParsedTable> tables = new ArrayList<ParsedTable>();

    for (final Element tablePartElement : XmlUtils.getElementsByLocalName(worksheetDoc, "tablePart")) {
      String relId = tablePartElement.getAttribute("r:id");
      if (relId == null || relId.isEmpty()) {
        relId = tablePartElement.getAttribute("id");
      }
      if (relId == null || relId.isEmpty()) {
        continue;
      }
      final String tablePath = sheetRels.get(relId);
      if (tablePath == null || tablePath.isEmpty()) {
        continue;
      }
      final byte[] tableBytes = files.get(tablePath);
      if (tableBytes == null) {
        continue;
      }
      final Document tableDoc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(tableBytes));
      final List<Element> tableElements = XmlUtils.getElementsByLocalName(tableDoc, "table");
      if (tableElements.isEmpty()) {
        continue;
      }
      final Element tableElement = tableElements.get(0);
      final AddressUtils.RangeAddress range = AddressUtils.parseRangeAddress(tableElement.getAttribute("ref"));
      if (range == null) {
        continue;
      }
      final List<String> columns = new ArrayList<String>();
      for (final Element columnElement : XmlUtils.getElementsByLocalName(tableElement, "tableColumn")) {
        final String name = columnElement.getAttribute("name") == null ? "" : columnElement.getAttribute("name").trim();
        if (!name.isEmpty()) {
          columns.add(name);
        }
      }
      final String tableName = tableElement.getAttribute("name");
      final String displayName = tableElement.getAttribute("displayName");
      tables.add(new ParsedTable(
          sheetName,
          tableName == null ? "" : tableName,
          displayName == null || displayName.isEmpty() ? (tableName == null ? "" : tableName) : displayName,
          range.getStart(),
          range.getEnd(),
          columns,
          parseInt(tableElement.getAttribute("headerRowCount"), 1),
          parseInt(tableElement.getAttribute("totalsRowCount"), 0)));
    }

    return tables;
  }

  private static int parseInt(final String value, final int fallback) {
    try {
      return value == null || value.isEmpty() ? fallback : Integer.parseInt(value);
    } catch (final NumberFormatException ex) {
      return fallback;
    }
  }

  public static final class ParsedTable {
    private final String sheetName;
    private final String name;
    private final String displayName;
    private final String start;
    private final String end;
    private final List<String> columns;
    private final int headerRowCount;
    private final int totalsRowCount;

    public ParsedTable(
        final String sheetName,
        final String name,
        final String displayName,
        final String start,
        final String end,
        final List<String> columns,
        final int headerRowCount,
        final int totalsRowCount) {
      this.sheetName = sheetName;
      this.name = name;
      this.displayName = displayName;
      this.start = start;
      this.end = end;
      this.columns = columns;
      this.headerRowCount = headerRowCount;
      this.totalsRowCount = totalsRowCount;
    }

    public String getSheetName() {
      return sheetName;
    }

    public String getName() {
      return name;
    }

    public String getDisplayName() {
      return displayName;
    }

    public String getStart() {
      return start;
    }

    public String getEnd() {
      return end;
    }

    public List<String> getColumns() {
      return columns;
    }

    public int getHeaderRowCount() {
      return headerRowCount;
    }

    public int getTotalsRowCount() {
      return totalsRowCount;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ParsedTable)) {
        return false;
      }
      final ParsedTable that = (ParsedTable) other;
      return headerRowCount == that.headerRowCount
          && totalsRowCount == that.totalsRowCount
          && Objects.equals(sheetName, that.sheetName)
          && Objects.equals(name, that.name)
          && Objects.equals(displayName, that.displayName)
          && Objects.equals(start, that.start)
          && Objects.equals(end, that.end)
          && Objects.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
      return Objects.hash(sheetName, name, displayName, start, end, columns, Integer.valueOf(headerRowCount), Integer.valueOf(totalsRowCount));
    }
  }
}
