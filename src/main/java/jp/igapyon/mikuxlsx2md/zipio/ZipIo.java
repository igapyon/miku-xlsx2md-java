/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.zipio;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public final class ZipIo {
  private static final int UTF8_FILE_NAME_FLAG = 0x0800;
  public static final ZipEntryTimestamp FIXED_ZIP_ENTRY_TIMESTAMP = toDosDateTime(2025, 1, 1, 0, 0, 0);

  private ZipIo() {
  }

  public static String decodeXmlText(final byte[] bytes) {
    return new String(bytes == null ? new byte[0] : bytes, StandardCharsets.UTF_8);
  }

  public static Map<String, byte[]> unzipEntries(final byte[] zipBytes) {
    final byte[] source = zipBytes == null ? new byte[0] : zipBytes;
    int eocdOffset = -1;
    for (int offset = source.length - 22; offset >= Math.max(0, source.length - 0x10000 - 22); offset -= 1) {
      if (readUint32Le(source, offset) == 0x06054b50L) {
        eocdOffset = offset;
        break;
      }
    }
    if (eocdOffset < 0) {
      throw new IllegalArgumentException("ZIP end-of-central-directory record was not found.");
    }

    final int centralDirectorySize = (int) readUint32Le(source, eocdOffset + 12);
    final int centralDirectoryOffset = (int) readUint32Le(source, eocdOffset + 16);
    final int endOffset = centralDirectoryOffset + centralDirectorySize;
    final Map<String, ZipEntryRecord> entries = new LinkedHashMap<String, ZipEntryRecord>();
    int cursor = centralDirectoryOffset;

    while (cursor < endOffset) {
      if (readUint32Le(source, cursor) != 0x02014b50L) {
        throw new IllegalArgumentException("ZIP central directory format is invalid.");
      }
      final int compressionMethod = readUint16Le(source, cursor + 10);
      final int compressedSize = (int) readUint32Le(source, cursor + 20);
      final int uncompressedSize = (int) readUint32Le(source, cursor + 24);
      final int fileNameLength = readUint16Le(source, cursor + 28);
      final int extraFieldLength = readUint16Le(source, cursor + 30);
      final int fileCommentLength = readUint16Le(source, cursor + 32);
      final int localHeaderOffset = (int) readUint32Le(source, cursor + 42);
      final String name = decodeXmlText(Arrays.copyOfRange(source, cursor + 46, cursor + 46 + fileNameLength));
      entries.put(name, new ZipEntryRecord(name, compressionMethod, compressedSize, uncompressedSize, localHeaderOffset));
      cursor += 46 + fileNameLength + extraFieldLength + fileCommentLength;
    }

    final Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
    for (final ZipEntryRecord entry : entries.values()) {
      final int localOffset = entry.getLocalHeaderOffset();
      if (readUint32Le(source, localOffset) != 0x04034b50L) {
        throw new IllegalArgumentException("ZIP local header is invalid: " + entry.getName());
      }
      final int fileNameLength = readUint16Le(source, localOffset + 26);
      final int extraFieldLength = readUint16Le(source, localOffset + 28);
      final int dataOffset = localOffset + 30 + fileNameLength + extraFieldLength;
      final byte[] compressedData = Arrays.copyOfRange(source, dataOffset, dataOffset + entry.getCompressedSize());

      final byte[] fileData;
      if (entry.getCompressionMethod() == 0) {
        fileData = compressedData;
      } else if (entry.getCompressionMethod() == 8) {
        fileData = inflateRaw(compressedData);
      } else {
        throw new IllegalArgumentException(
            "Unsupported compression method: " + entry.getName() + " (method=" + entry.getCompressionMethod() + ")");
      }
      files.put(entry.getName(), fileData);
    }

    return files;
  }

  public static byte[] createStoredZip(final ExportEntry[] entries) {
    final ByteArrayOutputStream localChunks = new ByteArrayOutputStream();
    final ByteArrayOutputStream centralChunks = new ByteArrayOutputStream();
    int offset = 0;

    for (final ExportEntry entry : entries) {
      final byte[] nameBytes = entry.getName().getBytes(StandardCharsets.UTF_8);
      final byte[] dataBytes = entry.getData();
      final long entryCrc32 = crc32(dataBytes);
      final int generalPurposeBitFlag = hasNonAsciiCharacters(entry.getName()) ? UTF8_FILE_NAME_FLAG : 0;

      final byte[] localHeader = new byte[30 + nameBytes.length];
      writeUint32Le(localHeader, 0, 0x04034b50L);
      writeUint16Le(localHeader, 4, 20);
      writeUint16Le(localHeader, 6, generalPurposeBitFlag);
      writeUint16Le(localHeader, 8, 0);
      writeUint16Le(localHeader, 10, FIXED_ZIP_ENTRY_TIMESTAMP.getDosTime());
      writeUint16Le(localHeader, 12, FIXED_ZIP_ENTRY_TIMESTAMP.getDosDate());
      writeUint32Le(localHeader, 14, entryCrc32);
      writeUint32Le(localHeader, 18, dataBytes.length);
      writeUint32Le(localHeader, 22, dataBytes.length);
      writeUint16Le(localHeader, 26, nameBytes.length);
      writeUint16Le(localHeader, 28, 0);
      System.arraycopy(nameBytes, 0, localHeader, 30, nameBytes.length);
      localChunks.write(localHeader, 0, localHeader.length);
      localChunks.write(dataBytes, 0, dataBytes.length);

      final byte[] centralHeader = new byte[46 + nameBytes.length];
      writeUint32Le(centralHeader, 0, 0x02014b50L);
      writeUint16Le(centralHeader, 4, 20);
      writeUint16Le(centralHeader, 6, 20);
      writeUint16Le(centralHeader, 8, generalPurposeBitFlag);
      writeUint16Le(centralHeader, 10, 0);
      writeUint16Le(centralHeader, 12, FIXED_ZIP_ENTRY_TIMESTAMP.getDosTime());
      writeUint16Le(centralHeader, 14, FIXED_ZIP_ENTRY_TIMESTAMP.getDosDate());
      writeUint32Le(centralHeader, 16, entryCrc32);
      writeUint32Le(centralHeader, 20, dataBytes.length);
      writeUint32Le(centralHeader, 24, dataBytes.length);
      writeUint16Le(centralHeader, 28, nameBytes.length);
      writeUint16Le(centralHeader, 30, 0);
      writeUint16Le(centralHeader, 32, 0);
      writeUint16Le(centralHeader, 34, 0);
      writeUint16Le(centralHeader, 36, 0);
      writeUint32Le(centralHeader, 38, 0);
      writeUint32Le(centralHeader, 42, offset);
      System.arraycopy(nameBytes, 0, centralHeader, 46, nameBytes.length);
      centralChunks.write(centralHeader, 0, centralHeader.length);

      offset += localHeader.length + dataBytes.length;
    }

    final int centralDirectoryStart = offset;
    final byte[] centralBytes = centralChunks.toByteArray();
    final byte[] eocd = new byte[22];
    writeUint32Le(eocd, 0, 0x06054b50L);
    writeUint16Le(eocd, 4, 0);
    writeUint16Le(eocd, 6, 0);
    writeUint16Le(eocd, 8, entries.length);
    writeUint16Le(eocd, 10, entries.length);
    writeUint32Le(eocd, 12, centralBytes.length);
    writeUint32Le(eocd, 16, centralDirectoryStart);
    writeUint16Le(eocd, 20, 0);

    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    final byte[] localBytes = localChunks.toByteArray();
    output.write(localBytes, 0, localBytes.length);
    output.write(centralBytes, 0, centralBytes.length);
    output.write(eocd, 0, eocd.length);
    return output.toByteArray();
  }

  public static ZipEntryTimestamp toDosDateTime(
      final int year,
      final int month,
      final int day,
      final int hour,
      final int minute,
      final int second) {
    final int clampedYear = Math.max(1980, Math.min(2107, year));
    final int dosTime = ((hour & 0x1f) << 11) | ((minute & 0x3f) << 5) | ((second / 2) & 0x1f);
    final int dosDate = (((clampedYear - 1980) & 0x7f) << 9) | ((month & 0x0f) << 5) | (day & 0x1f);
    return new ZipEntryTimestamp(dosTime, dosDate);
  }

  private static boolean hasNonAsciiCharacters(final String value) {
    return value != null && value.matches(".*[^\\x00-\\x7f].*");
  }

  private static long crc32(final byte[] bytes) {
    final CRC32 crc32 = new CRC32();
    crc32.update(bytes == null ? new byte[0] : bytes);
    return crc32.getValue();
  }

  private static byte[] inflateRaw(final byte[] data) {
    final Inflater inflater = new Inflater(true);
    inflater.setInput(data);
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    final byte[] buffer = new byte[1024];
    try {
      while (!inflater.finished()) {
        final int count = inflater.inflate(buffer);
        if (count == 0) {
          if (inflater.needsInput()) {
            break;
          }
        } else {
          output.write(buffer, 0, count);
        }
      }
      return output.toByteArray();
    } catch (final DataFormatException ex) {
      throw new IllegalArgumentException("This environment does not support ZIP deflate decompression.", ex);
    } finally {
      inflater.end();
    }
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

  public static final class ZipEntryTimestamp {
    private final int dosTime;
    private final int dosDate;

    public ZipEntryTimestamp(final int dosTime, final int dosDate) {
      this.dosTime = dosTime;
      this.dosDate = dosDate;
    }

    public int getDosTime() {
      return dosTime;
    }

    public int getDosDate() {
      return dosDate;
    }
  }

  public static final class ExportEntry {
    private final String name;
    private final byte[] data;

    public ExportEntry(final String name, final byte[] data) {
      this.name = name;
      this.data = data == null ? new byte[0] : data;
    }

    public String getName() {
      return name;
    }

    public byte[] getData() {
      return data;
    }
  }

  private static final class ZipEntryRecord {
    private final String name;
    private final int compressionMethod;
    private final int compressedSize;
    private final int uncompressedSize;
    private final int localHeaderOffset;

    private ZipEntryRecord(
        final String name,
        final int compressionMethod,
        final int compressedSize,
        final int uncompressedSize,
        final int localHeaderOffset) {
      this.name = name;
      this.compressionMethod = compressionMethod;
      this.compressedSize = compressedSize;
      this.uncompressedSize = uncompressedSize;
      this.localHeaderOffset = localHeaderOffset;
    }

    private String getName() {
      return name;
    }

    private int getCompressionMethod() {
      return compressionMethod;
    }

    private int getCompressedSize() {
      return compressedSize;
    }

    @SuppressWarnings("unused")
    private int getUncompressedSize() {
      return uncompressedSize;
    }

    private int getLocalHeaderOffset() {
      return localHeaderOffset;
    }
  }
}
