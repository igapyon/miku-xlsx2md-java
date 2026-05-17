/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.zipio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import org.junit.jupiter.api.Test;

class ZipIoTest {
  @Test
  void roundTripsStoredZipEntries() {
    final byte[] zipBytes = ZipIo.createStoredZip(new ZipIo.ExportEntry[] {
        new ZipIo.ExportEntry("output/test.md", "# Test\n".getBytes(StandardCharsets.UTF_8)),
        new ZipIo.ExportEntry("output/assets/icon.txt", "asset".getBytes(StandardCharsets.UTF_8))
    });

    final Map<String, byte[]> extracted = ZipIo.unzipEntries(zipBytes);
    final List<String> names = new ArrayList<String>(extracted.keySet());
    Collections.sort(names);

    assertEquals(2, names.size());
    assertEquals("output/assets/icon.txt", names.get(0));
    assertEquals("output/test.md", names.get(1));
    assertEquals("# Test\n", new String(extracted.get("output/test.md"), StandardCharsets.UTF_8));
    assertEquals("asset", new String(extracted.get("output/assets/icon.txt"), StandardCharsets.UTF_8));
  }

  @Test
  void supportsEmptyFilePayloads() {
    final byte[] zipBytes = ZipIo.createStoredZip(new ZipIo.ExportEntry[] {
        new ZipIo.ExportEntry("empty.txt", new byte[0])
    });

    final Map<String, byte[]> extracted = ZipIo.unzipEntries(zipBytes);
    assertTrue(extracted.containsKey("empty.txt"));
    assertArrayEquals(new byte[0], extracted.get("empty.txt"));
  }

  @Test
  void writesAFixedReproducibleZipEntryTimestamp() {
    final byte[] zipBytes = ZipIo.createStoredZip(new ZipIo.ExportEntry[] {
        new ZipIo.ExportEntry("output/test.md", "# Test\n".getBytes(StandardCharsets.UTF_8))
    });

    assertEquals(0x04034b50L, readUint32Le(zipBytes, 0));
    assertEquals(ZipIo.FIXED_ZIP_ENTRY_TIMESTAMP.getDosTime(), readUint16Le(zipBytes, 10));
    assertEquals(ZipIo.FIXED_ZIP_ENTRY_TIMESTAMP.getDosDate(), readUint16Le(zipBytes, 12));

    final int localNameLength = readUint16Le(zipBytes, 26);
    final int centralOffset = 30 + localNameLength + "# Test\n".getBytes(StandardCharsets.UTF_8).length;
    assertEquals(0x02014b50L, readUint32Le(zipBytes, centralOffset));
    assertEquals(ZipIo.FIXED_ZIP_ENTRY_TIMESTAMP.getDosTime(), readUint16Le(zipBytes, centralOffset + 12));
    assertEquals(ZipIo.FIXED_ZIP_ENTRY_TIMESTAMP.getDosDate(), readUint16Le(zipBytes, centralOffset + 14));
  }

  @Test
  void doesNotMarkAsciiOnlyFileNamesWithTheUtf8Flag() {
    final byte[] zipBytes = ZipIo.createStoredZip(new ZipIo.ExportEntry[] {
        new ZipIo.ExportEntry("output/test.md", "# Test\n".getBytes(StandardCharsets.UTF_8))
    });

    assertEquals(0, readUint16Le(zipBytes, 6));

    final int localNameLength = readUint16Le(zipBytes, 26);
    final int centralOffset = 30 + localNameLength + "# Test\n".getBytes(StandardCharsets.UTF_8).length;
    assertEquals(0, readUint16Le(zipBytes, centralOffset + 8));
  }

  @Test
  void marksUtf8FileNamesSoNonAsciiEntriesUnzipCorrectly() {
    final byte[] zipBytes = ZipIo.createStoredZip(new ZipIo.ExportEntry[] {
        new ZipIo.ExportEntry("output/日本語.md", "# 日本語\n".getBytes(StandardCharsets.UTF_8))
    });

    assertEquals(0x0800, readUint16Le(zipBytes, 6));

    final int localNameLength = readUint16Le(zipBytes, 26);
    final int centralOffset = 30 + localNameLength + "# 日本語\n".getBytes(StandardCharsets.UTF_8).length;
    assertEquals(0x0800, readUint16Le(zipBytes, centralOffset + 8));

    final Map<String, byte[]> extracted = ZipIo.unzipEntries(zipBytes);
    final List<String> names = new ArrayList<String>(extracted.keySet());
    assertEquals(1, names.size());
    assertEquals("output/日本語.md", names.get(0));
    assertEquals("# 日本語\n", new String(extracted.get("output/日本語.md"), StandardCharsets.UTF_8));
  }

  @Test
  void throwsForInvalidZipInput() {
    final IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> ZipIo.unzipEntries(new byte[] {1, 2, 3, 4}));

    assertEquals("ZIP end-of-central-directory record was not found.", exception.getMessage());
  }

  @Test
  void inflatesDeflatedEntries() {
    final String fileName = "output/test.txt";
    final byte[] original = "fallback".getBytes(StandardCharsets.UTF_8);
    final byte[] compressed = deflateRaw(original);
    final long crc = 0xa87e4381L;
    final byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);

    final byte[] localHeader = new byte[30 + fileNameBytes.length];
    writeUint32Le(localHeader, 0, 0x04034b50L);
    writeUint16Le(localHeader, 4, 20);
    writeUint16Le(localHeader, 6, 0);
    writeUint16Le(localHeader, 8, 8);
    writeUint16Le(localHeader, 10, 0);
    writeUint16Le(localHeader, 12, 0);
    writeUint32Le(localHeader, 14, crc);
    writeUint32Le(localHeader, 18, compressed.length);
    writeUint32Le(localHeader, 22, original.length);
    writeUint16Le(localHeader, 26, fileNameBytes.length);
    writeUint16Le(localHeader, 28, 0);
    System.arraycopy(fileNameBytes, 0, localHeader, 30, fileNameBytes.length);

    final byte[] centralHeader = new byte[46 + fileNameBytes.length];
    writeUint32Le(centralHeader, 0, 0x02014b50L);
    writeUint16Le(centralHeader, 4, 20);
    writeUint16Le(centralHeader, 6, 20);
    writeUint16Le(centralHeader, 8, 0);
    writeUint16Le(centralHeader, 10, 8);
    writeUint16Le(centralHeader, 12, 0);
    writeUint16Le(centralHeader, 14, 0);
    writeUint32Le(centralHeader, 16, crc);
    writeUint32Le(centralHeader, 20, compressed.length);
    writeUint32Le(centralHeader, 24, original.length);
    writeUint16Le(centralHeader, 28, fileNameBytes.length);
    writeUint16Le(centralHeader, 30, 0);
    writeUint16Le(centralHeader, 32, 0);
    writeUint16Le(centralHeader, 34, 0);
    writeUint16Le(centralHeader, 36, 0);
    writeUint32Le(centralHeader, 38, 0);
    writeUint32Le(centralHeader, 42, 0);
    System.arraycopy(fileNameBytes, 0, centralHeader, 46, fileNameBytes.length);

    final byte[] eocd = new byte[22];
    writeUint32Le(eocd, 0, 0x06054b50L);
    writeUint16Le(eocd, 8, 1);
    writeUint16Le(eocd, 10, 1);
    writeUint32Le(eocd, 12, centralHeader.length);
    writeUint32Le(eocd, 16, localHeader.length + compressed.length);

    final byte[] zipBytes = new byte[localHeader.length + compressed.length + centralHeader.length + eocd.length];
    System.arraycopy(localHeader, 0, zipBytes, 0, localHeader.length);
    System.arraycopy(compressed, 0, zipBytes, localHeader.length, compressed.length);
    System.arraycopy(centralHeader, 0, zipBytes, localHeader.length + compressed.length, centralHeader.length);
    System.arraycopy(eocd, 0, zipBytes, localHeader.length + compressed.length + centralHeader.length, eocd.length);

    final Map<String, byte[]> extracted = ZipIo.unzipEntries(zipBytes);
    assertEquals("fallback", new String(extracted.get(fileName), StandardCharsets.UTF_8));
  }

  private static byte[] deflateRaw(final byte[] input) {
    final Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
    deflater.setInput(input);
    deflater.finish();
    final byte[] buffer = new byte[128];
    final java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
    while (!deflater.finished()) {
      final int count = deflater.deflate(buffer);
      output.write(buffer, 0, count);
    }
    deflater.end();
    return output.toByteArray();
  }

  private static int readUint16Le(final byte[] bytes, final int offset) {
    return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
  }

  private static long readUint32Le(final byte[] bytes, final int offset) {
    return (bytes[offset] & 0xffL)
        | ((bytes[offset + 1] & 0xffL) << 8)
        | ((bytes[offset + 2] & 0xffL) << 16)
        | ((bytes[offset + 3] & 0xffL) << 24);
  }

  private static void writeUint16Le(final byte[] bytes, final int offset, final int value) {
    bytes[offset] = (byte) (value & 0xff);
    bytes[offset + 1] = (byte) ((value >>> 8) & 0xff);
  }

  private static void writeUint32Le(final byte[] bytes, final int offset, final long value) {
    bytes[offset] = (byte) (value & 0xff);
    bytes[offset + 1] = (byte) ((value >>> 8) & 0xff);
    bytes[offset + 2] = (byte) ((value >>> 16) & 0xff);
    bytes[offset + 3] = (byte) ((value >>> 24) & 0xff);
  }
}
