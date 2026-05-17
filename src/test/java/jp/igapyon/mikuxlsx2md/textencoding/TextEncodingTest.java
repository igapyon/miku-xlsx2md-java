/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.textencoding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TextEncodingTest {
  @Test
  void encodesUtf8WithoutBomByDefault() {
    assertArrayEquals(new byte[] {0x41}, TextEncoding.encodeText("A"));
  }

  @Test
  void encodesUtf16LeWithBomWhenRequested() {
    assertArrayEquals(
        new byte[] {(byte) 0xff, (byte) 0xfe, 0x41, 0x00},
        TextEncoding.encodeText("A", new TextEncoding.MarkdownEncodingOptions("utf-16le", "on")));
  }

  @Test
  void encodesUtf32BeWithoutBom() {
    assertArrayEquals(
        new byte[] {0x00, 0x00, 0x00, 0x41},
        TextEncoding.encodeText("A", new TextEncoding.MarkdownEncodingOptions("utf-32be", "off")));
  }

  @Test
  void encodesShiftJisWhenAvailable() {
    assertArrayEquals(
        new byte[] {(byte) 0x82, (byte) 0xa0, 0x41},
        TextEncoding.encodeText("あA", new TextEncoding.MarkdownEncodingOptions("shift_jis", "off")));
  }

  @Test
  void rejectsBomForShiftJis() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TextEncoding.encodeText("A", new TextEncoding.MarkdownEncodingOptions("shift_jis", "on")));
  }
}
