/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.textencoding;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class TextEncoding {
  public static final List<String> ENCODINGS =
      Collections.unmodifiableList(Arrays.asList("utf-8", "shift_jis", "utf-16le", "utf-16be", "utf-32le", "utf-32be"));
  public static final List<String> BOM_MODES = Collections.unmodifiableList(Arrays.asList("off", "on"));

  private TextEncoding() {
  }

  public static String normalizeEncoding(final String value) {
    final String normalized = value == null ? "utf-8" : value.toLowerCase(Locale.ROOT);
    if (ENCODINGS.contains(normalized)) {
      return normalized;
    }
    throw new IllegalArgumentException("Unsupported encoding: " + String.valueOf(value));
  }

  public static String normalizeBom(final String value) {
    final String normalized = value == null ? "off" : value.toLowerCase(Locale.ROOT);
    if (BOM_MODES.contains(normalized)) {
      return normalized;
    }
    throw new IllegalArgumentException("Unsupported BOM mode: " + String.valueOf(value));
  }

  public static byte[] getBomBytes(final String encoding) {
    final String normalized = normalizeEncoding(encoding);
    if ("utf-8".equals(normalized)) {
      return new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
    }
    if ("utf-16le".equals(normalized)) {
      return new byte[] {(byte) 0xff, (byte) 0xfe};
    }
    if ("utf-16be".equals(normalized)) {
      return new byte[] {(byte) 0xfe, (byte) 0xff};
    }
    if ("utf-32le".equals(normalized)) {
      return new byte[] {(byte) 0xff, (byte) 0xfe, 0x00, 0x00};
    }
    if ("utf-32be".equals(normalized)) {
      return new byte[] {0x00, 0x00, (byte) 0xfe, (byte) 0xff};
    }
    throw new IllegalArgumentException("Encoding does not support BOM: " + normalized);
  }

  public static boolean isEncodingAvailable(final String value) {
    final String encoding = normalizeEncoding(value);
    return !"shift_jis".equals(encoding) || Charset.isSupported("Shift_JIS");
  }

  public static byte[] encodeText(final String text) {
    return encodeText(text, new MarkdownEncodingOptions());
  }

  public static byte[] encodeText(final String text, final MarkdownEncodingOptions options) {
    final MarkdownEncodingOptions input = options == null ? new MarkdownEncodingOptions() : options;
    final String encoding = normalizeEncoding(input.getEncoding());
    final String bom = normalizeBom(input.getBom());
    if ("shift_jis".equals(encoding)) {
      if ("on".equals(bom)) {
        throw new IllegalArgumentException("BOM cannot be enabled for shift_jis.");
      }
      if (!Charset.isSupported("Shift_JIS")) {
        throw new IllegalArgumentException("Shift_JIS encoding is not available in this runtime.");
      }
      return (text == null ? "" : text).getBytes(Charset.forName("Shift_JIS"));
    }

    final byte[] body;
    if ("utf-8".equals(encoding)) {
      body = (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
    } else if ("utf-16le".equals(encoding)) {
      body = encodeUtf16(text == null ? "" : text, true);
    } else if ("utf-16be".equals(encoding)) {
      body = encodeUtf16(text == null ? "" : text, false);
    } else if ("utf-32le".equals(encoding)) {
      body = encodeUtf32(text == null ? "" : text, true);
    } else {
      body = encodeUtf32(text == null ? "" : text, false);
    }

    if ("off".equals(bom)) {
      return body;
    }
    return concatBytes(getBomBytes(encoding), body);
  }

  public static String createTextMimeType(final MarkdownEncodingOptions options) {
    final MarkdownEncodingOptions input = options == null ? new MarkdownEncodingOptions() : options;
    return "text/markdown;charset=" + normalizeEncoding(input.getEncoding());
  }

  private static byte[] concatBytes(final byte[] first, final byte[] second) {
    final byte[] result = new byte[first.length + second.length];
    System.arraycopy(first, 0, result, 0, first.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  private static byte[] encodeUtf16(final String text, final boolean littleEndian) {
    final byte[] result = new byte[text.length() * 2];
    for (int index = 0; index < text.length(); index += 1) {
      final char codeUnit = text.charAt(index);
      final int offset = index * 2;
      if (littleEndian) {
        result[offset] = (byte) (codeUnit & 0xff);
        result[offset + 1] = (byte) (codeUnit >>> 8);
      } else {
        result[offset] = (byte) (codeUnit >>> 8);
        result[offset + 1] = (byte) (codeUnit & 0xff);
      }
    }
    return result;
  }

  private static byte[] encodeUtf32(final String text, final boolean littleEndian) {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    for (int index = 0; index < text.length(); index += 1) {
      int codePoint = text.charAt(index);
      if (Character.isHighSurrogate((char) codePoint) && index + 1 < text.length()) {
        final char second = text.charAt(index + 1);
        if (Character.isLowSurrogate(second)) {
          codePoint = Character.toCodePoint((char) codePoint, second);
          index += 1;
        }
      }
      if (littleEndian) {
        output.write(codePoint & 0xff);
        output.write((codePoint >>> 8) & 0xff);
        output.write((codePoint >>> 16) & 0xff);
        output.write((codePoint >>> 24) & 0xff);
      } else {
        output.write((codePoint >>> 24) & 0xff);
        output.write((codePoint >>> 16) & 0xff);
        output.write((codePoint >>> 8) & 0xff);
        output.write(codePoint & 0xff);
      }
    }
    return output.toByteArray();
  }

  public static final class MarkdownEncodingOptions {
    private final String encoding;
    private final String bom;

    public MarkdownEncodingOptions() {
      this(null, null);
    }

    public MarkdownEncodingOptions(final String encoding, final String bom) {
      this.encoding = encoding;
      this.bom = bom;
    }

    public String getEncoding() {
      return encoding;
    }

    public String getBom() {
      return bom;
    }
  }
}
