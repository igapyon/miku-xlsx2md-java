/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.cellformat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.igapyon.mikuxlsx2md.stylesparser.StylesParser;

public final class CellFormat {
  private static final Pattern DATE_TOKEN_PATTERN = Pattern.compile("[ymdhs]");
  private static final Pattern QUOTED_PATTERN = Pattern.compile("\"([^\"]*)\"");
  private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[[^\\]]*\\]");
  private static final Pattern ESCAPED_CHAR_PATTERN = Pattern.compile("\\\\(.)");
  private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_.?");
  private static final Pattern ISO_DATE_TIME_PATTERN =
      Pattern.compile("^(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})(?:[ T](\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?)?$");
  private static final Pattern JAPANESE_DATE_TIME_PATTERN =
      Pattern.compile("^(\\d{4})年(\\d{1,2})月(\\d{1,2})日(?:\\s*(\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?)?$");
  private static final Pattern JAPANESE_YEAR_MONTH_PATTERN = Pattern.compile("^(\\d{4})年(\\d{1,2})月$");
  private static final Pattern JAPANESE_MONTH_DAY_PATTERN = Pattern.compile("^(\\d{1,2})月(\\d{1,2})日$");
  private static final Pattern ISO_YEAR_MONTH_PATTERN = Pattern.compile("^(\\d{4})[-/](\\d{1,2})$");

  private CellFormat() {
  }

  public static boolean isDateFormatCode(final String formatCode) {
    final String normalized = String.valueOf(formatCode == null ? "" : formatCode)
        .toLowerCase(Locale.ROOT)
        .replaceAll("\\[[^\\]]*\\]", "")
        .replaceAll("\"[^\"]*\"", "")
        .replaceAll("\\\\.", "");
    if (normalized.isEmpty() || normalized.contains("general")) {
      return false;
    }
    return DATE_TOKEN_PATTERN.matcher(normalized).find();
  }

  public static String normalizeNumericFormatCode(final String formatCode) {
    String result = String.valueOf(formatCode == null ? "" : formatCode).trim();
    result = BRACKET_PATTERN.matcher(result).replaceAll("");
    result = QUOTED_PATTERN.matcher(result).replaceAll("$1");
    result = ESCAPED_CHAR_PATTERN.matcher(result).replaceAll("$1");
    result = UNDERSCORE_PATTERN.matcher(result).replaceAll("");
    return result.replace("*", "");
  }

  public static String excelSerialToIsoText(final double serial) {
    if (!Double.isFinite(serial)) {
      return String.valueOf(serial);
    }
    final DateParts parts = excelSerialToDateParts(serial);
    if (parts == null) {
      return null;
    }
    if ("00".equals(parts.getHh()) && "00".equals(parts.getMi()) && "00".equals(parts.getSs())) {
      return parts.getYyyy() + "-" + parts.getMm() + "-" + parts.getDd();
    }
    return parts.getYyyy() + "-" + parts.getMm() + "-" + parts.getDd() + " "
        + parts.getHh() + ":" + parts.getMi() + ":" + parts.getSs();
  }

  public static DateParts excelSerialToDateParts(final double serial) {
    if (!Double.isFinite(serial)) {
      return null;
    }
    final long wholeDays = (long) Math.floor(serial);
    final double fractional = serial - wholeDays;
    final long excelEpochOffsetDays = 25569L;
    final long utcDays = wholeDays - excelEpochOffsetDays;
    final long millisPerDay = 24L * 60L * 60L * 1000L;
    final long millis = Math.round(fractional * millisPerDay);
    final LocalDateTime dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(utcDays * millisPerDay + millis), ZoneOffset.UTC);
    return new DateParts(
        dateTime.getYear(),
        dateTime.getMonthValue(),
        dateTime.getDayOfMonth(),
        dateTime.getHour(),
        dateTime.getMinute(),
        dateTime.getSecond());
  }

  public static String formatTextFunctionValue(final String value, final String formatText) {
    final String format = String.valueOf(formatText == null ? "" : formatText).trim();
    if (format.isEmpty()) {
      return null;
    }
    final Double numericValue = parseDoubleOrNull(value);
    final String normalized = format.toLowerCase(Locale.ROOT);

    if (numericValue != null) {
      if (normalized.matches(".*(^|[^a-z])yyyy.*") || normalized.contains("hh:") || normalized.contains("mm/") || normalized.contains("mm-")) {
        final DateParts parts = excelSerialToDateParts(numericValue.doubleValue());
        if (parts == null) {
          return null;
        }
        if ("yyyy-mm-dd".equals(normalized)) {
          return parts.getYyyy() + "-" + parts.getMm() + "-" + parts.getDd();
        }
        if ("yyyy/mm/dd".equals(normalized)) {
          return parts.getYyyy() + "/" + parts.getMm() + "/" + parts.getDd();
        }
        if ("hh:mm:ss".equals(normalized)) {
          return parts.getHh() + ":" + parts.getMi() + ":" + parts.getSs();
        }
        if ("yyyy-mm-dd hh:mm:ss".equals(normalized)) {
          return parts.getYyyy() + "-" + parts.getMm() + "-" + parts.getDd() + " "
              + parts.getHh() + ":" + parts.getMi() + ":" + parts.getSs();
        }
      }
      if (format.matches("^0(?:\\.0+)?$")) {
        final int decimalPlaces = getFractionLength(format);
        return fixed(numericValue.doubleValue(), decimalPlaces);
      }
      if (format.matches("^#,##0(?:\\.0+)?$")) {
        final int decimalPlaces = getFractionLength(format);
        return formatNumberByPattern(numericValue.doubleValue(), decimalPlaces, true);
      }
    }
    return null;
  }

  public static String formatNumberByPattern(final double value, final String pattern) {
    final String normalizedPattern = String.valueOf(pattern == null ? "" : pattern).trim();
    final int decimalPlaces = getPatternDecimalPlaces(normalizedPattern);
    final boolean useGrouping = normalizedPattern.contains(",");
    return formatNumberByPattern(value, decimalPlaces, useGrouping);
  }

  public static String formatDateByPattern(final DateParts parts, final String formatCode) {
    final String normalized = normalizeNumericFormatCode(formatCode).toLowerCase(Locale.ROOT);
    if ("yyyy/m/d".equals(normalized)) {
      return parts.getYear() + "/" + parts.getMonth() + "/" + parts.getDay();
    }
    if ("m月d日".equals(normalized)) {
      return parts.getMonth() + "月" + parts.getDay() + "日";
    }
    if ("yyyy-mm-dd".equals(normalized)) {
      return parts.getYyyy() + "-" + parts.getMm() + "-" + parts.getDd();
    }
    if ("yyyy/mm/dd".equals(normalized)) {
      return parts.getYear() + "/" + parts.getMonth() + "/" + parts.getDay();
    }
    if ("hh:mm:ss".equals(normalized)) {
      return parts.getHh() + ":" + parts.getMi() + ":" + parts.getSs();
    }
    if (normalized.contains("ggge年m月d日")) {
      if (parts.getYear() >= 2019) {
        return "令和" + (parts.getYear() - 2018) + "年" + parts.getMonth() + "月" + parts.getDay() + "日";
      }
      if (parts.getYear() >= 1989) {
        return "平成" + (parts.getYear() - 1988) + "年" + parts.getMonth() + "月" + parts.getDay() + "日";
      }
      return parts.getYear() + "年" + parts.getMonth() + "月" + parts.getDay() + "日";
    }
    return null;
  }

  public static String formatFractionPattern(final double value) {
    if (!Double.isFinite(value)) {
      return null;
    }
    final double tolerance = 1e-9;
    for (int denominator = 1; denominator <= 100; denominator += 1) {
      final int numerator = (int) Math.round(value * denominator);
      if (Math.abs(value - ((double) numerator / (double) denominator)) < tolerance) {
        return numerator + "/" + denominator;
      }
    }
    return null;
  }

  public static String formatDbNum3Pattern(final String rawValue) {
    final String text = String.valueOf(rawValue == null ? "" : rawValue);
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < text.length(); index += 1) {
      if (index > 0) {
        builder.append(' ');
      }
      builder.append(text.charAt(index));
    }
    return builder.toString();
  }

  public static List<String> splitFormatSections(final String formatCode) {
    final List<String> sections = new ArrayList<String>();
    final String source = String.valueOf(formatCode == null ? "" : formatCode);
    final StringBuilder current = new StringBuilder();
    boolean inQuotes = false;
    for (int index = 0; index < source.length(); index += 1) {
      final char ch = source.charAt(index);
      if (ch == '"') {
        inQuotes = !inQuotes;
        current.append(ch);
      } else if (ch == ';' && !inQuotes) {
        sections.add(current.toString());
        current.setLength(0);
      } else {
        current.append(ch);
      }
    }
    sections.add(current.toString());
    return sections;
  }

  public static String formatZeroSection(final String section) {
    final String normalizedSection = String.valueOf(section == null ? "" : section);
    if (normalizedSection.isEmpty()) {
      return null;
    }
    final String compact = normalizedSection.replaceAll("_.|\\\\.|[*?]", "").trim();
    final boolean hasDashLiteral = compact.matches(".*(\"-\"|(^|[^a-zA-Z0-9])-($|[^a-zA-Z0-9])).*");
    if (!hasDashLiteral) {
      return null;
    }
    if (compact.contains("¥")) {
      return "¥ -";
    }
    if (compact.contains("$")) {
      return "$ -";
    }
    return "-";
  }

  public static String formatCellDisplayValue(final String rawValue, final StylesParser.CellStyleInfo cellStyle) {
    if (rawValue == null || rawValue.isEmpty()) {
      return null;
    }
    final Double numericValue = parseDoubleOrNull(rawValue);
    final String formatCode = normalizeNumericFormatCode(cellStyle.getFormatCode());
    final String normalized = formatCode.toLowerCase(Locale.ROOT);
    final List<String> formatSections = splitFormatSections(formatCode);

    if (numericValue != null && isDateFormatCode(formatCode)) {
      final DateParts parts = excelSerialToDateParts(numericValue.doubleValue());
      if (parts == null) {
        return null;
      }
      final String direct = formatDateByPattern(parts, formatCode);
      if (direct != null) {
        return direct;
      }
      final boolean hasDate = normalized.contains("y")
          || normalized.contains("d")
          || normalized.matches(".*(^|[^a-z])m(?:/|-).*")
          || normalized.matches(".*(?:/|-)m(?:[^a-z]|$).*");
      final boolean hasTime = normalized.contains("h") || normalized.contains("s") || normalized.contains(":") || normalized.contains("am/pm");
      if (hasDate && hasTime) {
        return parts.getYyyy() + "-" + parts.getMm() + "-" + parts.getDd() + " "
            + parts.getHh() + ":" + parts.getMi() + ":" + parts.getSs();
      }
      if (hasTime && !hasDate) {
        return parts.getHh() + ":" + parts.getMi() + ":" + parts.getSs();
      }
      return parts.getYyyy() + "-" + parts.getMm() + "-" + parts.getDd();
    }

    if (numericValue == null) {
      return null;
    }

    if (numericValue.doubleValue() == 0.0d && formatSections.size() > 2) {
      final String zeroText = formatZeroSection(formatSections.get(2));
      if (zeroText != null) {
        return zeroText;
      }
    }

    if (normalized.contains("%")) {
      final String percentPattern = normalized.split(";", -1)[0];
      final int decimalPlaces = getPatternDecimalPlaces(percentPattern.replaceAll("[^0#.%]", ""));
      return fixed(numericValue.doubleValue() * 100.0d, decimalPlaces) + "%";
    }

    if (cellStyle.getNumFmtId() == 186 || formatCode.toLowerCase(Locale.ROOT).contains("dbnum3")) {
      return formatDbNum3Pattern(rawValue);
    }

    if (cellStyle.getNumFmtId() == 42) {
      return "¥ " + formatNumberByPattern(Math.abs(numericValue.doubleValue()), "#,##0");
    }

    if (formatCode.matches("(?i).*[#0][^;]*e\\+0+.*")) {
      final String scientificPattern = formatCode.split(";", -1)[0];
      final Matcher decimalMatcher = Pattern.compile("(?i)\\.([0#]+)e\\+").matcher(scientificPattern);
      final int decimalPlaces = decimalMatcher.find() ? decimalMatcher.group(1).length() : 0;
      final Matcher exponentMatcher = Pattern.compile("(?i)e\\+([0#]+)").matcher(scientificPattern);
      final int exponentDigits = exponentMatcher.find() ? exponentMatcher.group(1).length() : 0;
      final String[] parts = String.format(Locale.ROOT, "%." + decimalPlaces + "E", numericValue.doubleValue()).split("E");
      final int exponent = Integer.parseInt(parts[1]);
      final String sign = exponent >= 0 ? "+" : "-";
      final String paddedExponent = leftPad(Math.abs(exponent), exponentDigits);
      return parts[0] + "E" + sign + paddedExponent;
    }

    if (normalized.contains("?/?")) {
      return formatFractionPattern(numericValue.doubleValue());
    }

    if (formatCode.matches("^[^;]*[#0,]+(?:\\.[#0]+)?.*")) {
      final String primaryPattern = formatCode.split(";", -1)[0].trim();
      if (primaryPattern.contains("¥")) {
        final String numericText = formatNumberByPattern(
            numericValue.doubleValue(),
            primaryPattern.replaceAll("[^#0,.\\-]", ""));
        final String withCurrency = primaryPattern.contains("*")
            ? "¥ " + numericText.replaceFirst("^-", "")
            : "¥" + numericText.replaceFirst("^-", "");
        return (numericValue.doubleValue() < 0 ? "-" : "") + withCurrency;
      }
      return formatNumberByPattern(numericValue.doubleValue(), primaryPattern.replaceAll("[^#0,.\\-]", ""));
    }

    return null;
  }

  public static void applyResolvedFormulaValue(
      final ResolvedCellLike cell,
      final String resolvedValue,
      final String resolutionSource) {
    final String rawValue = String.valueOf(resolvedValue == null ? "" : resolvedValue);
    final String formattedValue = formatCellDisplayValue(rawValue, new StylesParser.CellStyleInfo(
        cell.getBorders(),
        cell.getNumFmtId(),
        cell.getFormatCode(),
        StylesParser.EMPTY_TEXT_STYLE));
    cell.setRawValue(rawValue);
    cell.setOutputValue(formattedValue == null ? rawValue : formattedValue);
    cell.setResolutionStatus("resolved");
    cell.setResolutionSource(resolutionSource == null ? "legacy_resolver" : resolutionSource);
  }

  public static DateParts parseDateLikeParts(final String value) {
    final String trimmed = String.valueOf(value == null ? "" : value).trim();
    final Double numericValue = parseDoubleOrNull(trimmed);
    if (numericValue != null) {
      return excelSerialToDateParts(numericValue.doubleValue());
    }
    Matcher matcher = ISO_DATE_TIME_PATTERN.matcher(trimmed);
    if (matcher.matches()) {
      return new DateParts(
          Integer.parseInt(matcher.group(1)),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)),
          parseGroupOrZero(matcher, 4),
          parseGroupOrZero(matcher, 5),
          parseGroupOrZero(matcher, 6));
    }
    matcher = JAPANESE_DATE_TIME_PATTERN.matcher(trimmed);
    if (matcher.matches()) {
      return new DateParts(
          Integer.parseInt(matcher.group(1)),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)),
          parseGroupOrZero(matcher, 4),
          parseGroupOrZero(matcher, 5),
          parseGroupOrZero(matcher, 6));
    }
    matcher = JAPANESE_YEAR_MONTH_PATTERN.matcher(trimmed);
    if (matcher.matches()) {
      return new DateParts(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), 1, 0, 0, 0);
    }
    matcher = JAPANESE_MONTH_DAY_PATTERN.matcher(trimmed);
    if (matcher.matches()) {
      return new DateParts(2000, Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), 0, 0, 0);
    }
    matcher = ISO_YEAR_MONTH_PATTERN.matcher(trimmed);
    if (matcher.matches()) {
      return new DateParts(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), 1, 0, 0, 0);
    }
    return null;
  }

  public static Double datePartsToExcelSerial(
      final int year,
      final int month,
      final int day,
      final int hour,
      final int minute,
      final int second) {
    final long baseUtcMillis = LocalDateTime.of(1899, 12, 31, 0, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
    final long targetUtcMillis = LocalDateTime.of(year, month, day, hour, minute, second).toInstant(ZoneOffset.UTC).toEpochMilli();
    final double serial = (double) (targetUtcMillis - baseUtcMillis) / (24d * 60d * 60d * 1000d);
    return Double.valueOf(serial >= 60d ? serial + 1d : serial);
  }

  public static Double parseValueFunctionText(final String value) {
    final String trimmed = String.valueOf(value == null ? "" : value).trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    final Double numericValue = parseDoubleOrNull(trimmed.replace(",", ""));
    if (numericValue != null) {
      return numericValue;
    }
    final DateParts parts = parseDateLikeParts(trimmed);
    if (parts == null) {
      return null;
    }
    return datePartsToExcelSerial(
        parts.getYear(),
        parts.getMonth(),
        parts.getDay(),
        parts.getHour(),
        parts.getMinute(),
        parts.getSecond());
  }

  private static int parseGroupOrZero(final Matcher matcher, final int groupIndex) {
    return matcher.group(groupIndex) == null ? 0 : Integer.parseInt(matcher.group(groupIndex));
  }

  private static Double parseDoubleOrNull(final String value) {
    try {
      return Double.valueOf(Double.parseDouble(String.valueOf(value)));
    } catch (final NumberFormatException ex) {
      return null;
    }
  }

  private static int getFractionLength(final String format) {
    final String[] parts = format.split("\\.", -1);
    return parts.length < 2 ? 0 : parts[1].length();
  }

  private static int getPatternDecimalPlaces(final String pattern) {
    final String[] parts = pattern.split("\\.", -1);
    return parts.length < 2 ? 0 : parts[1].replaceAll("[^0#]", "").length();
  }

  private static String fixed(final double value, final int decimalPlaces) {
    return BigDecimal.valueOf(value).setScale(decimalPlaces, RoundingMode.HALF_UP).toPlainString();
  }

  private static String formatNumberByPattern(final double value, final int decimalPlaces, final boolean useGrouping) {
    final BigDecimal scaled = BigDecimal.valueOf(value).setScale(decimalPlaces, RoundingMode.HALF_UP);
    final String plain = scaled.abs().toPlainString();
    final String[] parts = plain.split("\\.", -1);
    final String integerPart = useGrouping ? groupThousands(parts[0]) : parts[0];
    final String decimalPart = decimalPlaces > 0 ? "." + (parts.length > 1 ? rightPad(parts[1], decimalPlaces) : repeat('0', decimalPlaces)) : "";
    return (value < 0 ? "-" : "") + integerPart + decimalPart;
  }

  private static String groupThousands(final String digits) {
    final StringBuilder builder = new StringBuilder();
    int count = 0;
    for (int index = digits.length() - 1; index >= 0; index -= 1) {
      if (count == 3) {
        builder.append(',');
        count = 0;
      }
      builder.append(digits.charAt(index));
      count += 1;
    }
    return builder.reverse().toString();
  }

  private static String repeat(final char ch, final int count) {
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < count; index += 1) {
      builder.append(ch);
    }
    return builder.toString();
  }

  private static String rightPad(final String value, final int length) {
    final String text = value == null ? "" : value;
    if (text.length() >= length) {
      return text.substring(0, length);
    }
    return text + repeat('0', length - text.length());
  }

  private static String leftPad(final int value, final int length) {
    final String text = String.valueOf(value);
    if (text.length() >= length) {
      return text;
    }
    return repeat('0', length - text.length()) + text;
  }

  public interface ResolvedCellLike {
    StylesParser.BorderFlags getBorders();
    int getNumFmtId();
    String getFormatCode();
    void setRawValue(String rawValue);
    void setOutputValue(String outputValue);
    void setResolutionStatus(String resolutionStatus);
    void setResolutionSource(String resolutionSource);
  }

  public static final class DateParts {
    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minute;
    private final int second;

    public DateParts(final int year, final int month, final int day, final int hour, final int minute, final int second) {
      this.year = year;
      this.month = month;
      this.day = day;
      this.hour = hour;
      this.minute = minute;
      this.second = second;
    }

    public int getYear() {
      return year;
    }

    public int getMonth() {
      return month;
    }

    public int getDay() {
      return day;
    }

    public int getHour() {
      return hour;
    }

    public int getMinute() {
      return minute;
    }

    public int getSecond() {
      return second;
    }

    public String getYyyy() {
      return leftPad(year, 4);
    }

    public String getMm() {
      return leftPad(month, 2);
    }

    public String getDd() {
      return leftPad(day, 2);
    }

    public String getHh() {
      return leftPad(hour, 2);
    }

    public String getMi() {
      return leftPad(minute, 2);
    }

    public String getSs() {
      return leftPad(second, 2);
    }
  }
}
