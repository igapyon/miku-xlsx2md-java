/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sharedstrings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SharedStringsTest {
  @Test
  void returnsAnEmptyListWhenSharedStringsXmlIsMissing() {
    assertEquals(Arrays.asList(), SharedStrings.parseSharedStrings(new LinkedHashMap<String, byte[]>()));
  }

  @Test
  void collectsPlainAndRichTextRuns() {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"2\" uniqueCount=\"2\">"
        + "<si><t>通常のテキスト</t></si>"
        + "<si><r><t>分割</t></r><r><t>テキスト</t></r></si>"
        + "</sst>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/sharedStrings.xml", xml.getBytes(StandardCharsets.UTF_8));

    assertEquals(Arrays.asList(
        new SharedStrings.SharedStringEntry("通常のテキスト", null),
        new SharedStrings.SharedStringEntry("分割テキスト", null)),
        SharedStrings.parseSharedStrings(files));
  }

  @Test
  void skipsPhoneticTextNodesAndNormalizesCrLfToLf() {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"1\" uniqueCount=\"1\">"
        + "<si><t>line1&#13;&#10;line2</t><rPh sb=\"0\" eb=\"1\"><t>phonetic</t></rPh><phoneticPr fontId=\"1\"/></si>"
        + "</sst>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/sharedStrings.xml", xml.getBytes(StandardCharsets.UTF_8));

    assertEquals(Arrays.asList(new SharedStrings.SharedStringEntry("line1\nline2", null)), SharedStrings.parseSharedStrings(files));
  }

  @Test
  void preservesSupportedRichTextEmphasisFlags() {
    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"1\" uniqueCount=\"1\">"
        + "<si>"
        + "<r><rPr><b/></rPr><t>Bold</t></r>"
        + "<r><rPr><i/></rPr><t>Italic</t></r>"
        + "<r><rPr><strike/></rPr><t>Strike</t></r>"
        + "<r><rPr><u/></rPr><t>Under</t></r>"
        + "</si>"
        + "</sst>";
    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    files.put("xl/sharedStrings.xml", xml.getBytes(StandardCharsets.UTF_8));

    final List<SharedStrings.RichTextRun> runs = Arrays.asList(
        new SharedStrings.RichTextRun("Bold", true, false, false, false),
        new SharedStrings.RichTextRun("Italic", false, true, false, false),
        new SharedStrings.RichTextRun("Strike", false, false, true, false),
        new SharedStrings.RichTextRun("Under", false, false, false, true));
    assertEquals(Arrays.asList(new SharedStrings.SharedStringEntry("BoldItalicStrikeUnder", runs)), SharedStrings.parseSharedStrings(files));
  }
}
