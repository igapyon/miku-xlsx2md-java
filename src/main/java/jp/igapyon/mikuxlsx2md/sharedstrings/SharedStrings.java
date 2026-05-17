/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.sharedstrings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

public final class SharedStrings {
  private SharedStrings() {
  }

  public static SharedStringEntry parseSharedStringEntry(final Element item) {
    final List<RichTextRun> runs = parseRichTextRuns(item);
    if (runs != null) {
      final StringBuilder text = new StringBuilder();
      for (final RichTextRun run : runs) {
        text.append(run.getText());
      }
      return new SharedStringEntry(text.toString(), runs);
    }

    final List<String> parts = new ArrayList<String>();
    walk(item, parts);
    final StringBuilder text = new StringBuilder();
    for (final String part : parts) {
      text.append(part);
    }
    return new SharedStringEntry(text.toString(), null);
  }

  public static List<SharedStringEntry> parseSharedStrings(final Map<String, byte[]> files) {
    final byte[] sharedStringsBytes = files.get("xl/sharedStrings.xml");
    if (sharedStringsBytes == null) {
      return new ArrayList<SharedStringEntry>();
    }
    final org.w3c.dom.Document doc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(sharedStringsBytes));
    final List<SharedStringEntry> result = new ArrayList<SharedStringEntry>();
    for (final Element item : XmlUtils.getElementsByLocalName(doc, "si")) {
      result.add(parseSharedStringEntry(item));
    }
    return result;
  }

  private static void walk(final Node node, final List<String> parts) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      final Element element = (Element) node;
      if ("rPh".equals(element.getLocalName()) || "phoneticPr".equals(element.getLocalName())) {
        return;
      }
      if ("t".equals(element.getLocalName())) {
        parts.add(XmlUtils.getTextContent(element));
        return;
      }
    }
    for (int index = 0; index < node.getChildNodes().getLength(); index += 1) {
      walk(node.getChildNodes().item(index), parts);
    }
  }

  private static List<RichTextRun> parseRichTextRuns(final Element item) {
    final List<Element> runElements = new ArrayList<Element>();
    for (int index = 0; index < item.getChildNodes().getLength(); index += 1) {
      final Node node = item.getChildNodes().item(index);
      if (node.getNodeType() == Node.ELEMENT_NODE && "r".equals(node.getLocalName())) {
        runElements.add((Element) node);
      }
    }
    if (runElements.isEmpty()) {
      return null;
    }

    final List<RichTextRun> runs = new ArrayList<RichTextRun>();
    for (final Element runElement : runElements) {
      final StringBuilder textBuilder = new StringBuilder();
      for (final Element textNode : XmlUtils.getElementsByLocalName(runElement, "t")) {
        textBuilder.append(XmlUtils.getTextContent(textNode));
      }
      final String text = textBuilder.toString();
      if (text.isEmpty()) {
        continue;
      }
      final Element properties = getFirstElementByLocalName(runElement, "rPr");
      final RichTextRun run = new RichTextRun(
          text,
          hasEnabledBooleanValue(getNestedFirst(properties, "b")),
          hasEnabledBooleanValue(getNestedFirst(properties, "i")),
          hasEnabledBooleanValue(getNestedFirst(properties, "strike")),
          hasEnabledBooleanValue(getNestedFirst(properties, "u")));
      final RichTextRun previous = runs.isEmpty() ? null : runs.get(runs.size() - 1);
      if (previous != null
          && previous.isBold() == run.isBold()
          && previous.isItalic() == run.isItalic()
          && previous.isStrike() == run.isStrike()
          && previous.isUnderline() == run.isUnderline()) {
        previous.appendText(run.getText());
      } else {
        runs.add(run);
      }
    }
    if (runs.isEmpty()) {
      return null;
    }
    boolean emphasized = false;
    for (final RichTextRun run : runs) {
      if (run.isBold() || run.isItalic() || run.isStrike() || run.isUnderline()) {
        emphasized = true;
        break;
      }
    }
    return emphasized ? runs : null;
  }

  private static Element getFirstElementByLocalName(final Element root, final String localName) {
    if (root == null) {
      return null;
    }
    for (int index = 0; index < root.getChildNodes().getLength(); index += 1) {
      final Node node = root.getChildNodes().item(index);
      if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName())) {
        return (Element) node;
      }
    }
    return null;
  }

  private static Element getNestedFirst(final Element root, final String localName) {
    if (root == null) {
      return null;
    }
    final List<Element> elements = XmlUtils.getElementsByLocalName(root, localName);
    return elements.isEmpty() ? null : elements.get(0);
  }

  private static boolean hasEnabledBooleanValue(final Element node) {
    if (node == null) {
      return false;
    }
    final String value = node.getAttribute("val") == null ? "" : node.getAttribute("val").trim().toLowerCase();
    return !"false".equals(value) && !"0".equals(value) && !"none".equals(value);
  }

  public static final class SharedStringEntry {
    private final String text;
    private final List<RichTextRun> runs;

    public SharedStringEntry(final String text, final List<RichTextRun> runs) {
      this.text = text;
      this.runs = runs;
    }

    public String getText() {
      return text;
    }

    public List<RichTextRun> getRuns() {
      return runs;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof SharedStringEntry)) {
        return false;
      }
      final SharedStringEntry that = (SharedStringEntry) other;
      return Objects.equals(text, that.text) && Objects.equals(runs, that.runs);
    }

    @Override
    public int hashCode() {
      return Objects.hash(text, runs);
    }
  }

  public static final class RichTextRun {
    private String text;
    private final boolean bold;
    private final boolean italic;
    private final boolean strike;
    private final boolean underline;

    public RichTextRun(
        final String text,
        final boolean bold,
        final boolean italic,
        final boolean strike,
        final boolean underline) {
      this.text = text;
      this.bold = bold;
      this.italic = italic;
      this.strike = strike;
      this.underline = underline;
    }

    public String getText() {
      return text;
    }

    public boolean isBold() {
      return bold;
    }

    public boolean isItalic() {
      return italic;
    }

    public boolean isStrike() {
      return strike;
    }

    public boolean isUnderline() {
      return underline;
    }

    private void appendText(final String moreText) {
      this.text += moreText;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof RichTextRun)) {
        return false;
      }
      final RichTextRun that = (RichTextRun) other;
      return bold == that.bold
          && italic == that.italic
          && strike == that.strike
          && underline == that.underline
          && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
      return Objects.hash(text, Boolean.valueOf(bold), Boolean.valueOf(italic), Boolean.valueOf(strike), Boolean.valueOf(underline));
    }
  }
}
