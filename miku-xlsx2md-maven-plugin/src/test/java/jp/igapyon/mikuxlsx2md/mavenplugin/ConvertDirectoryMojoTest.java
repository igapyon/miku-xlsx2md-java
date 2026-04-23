/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.mavenplugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.igapyon.mikuxlsx2md.zipio.ZipIo;

class ConvertDirectoryMojoTest {
  @TempDir
  Path tempDir;

  @Test
  void skipsWhenRequested() {
    final ConvertDirectoryMojo mojo = new ConvertDirectoryMojo();
    mojo.setSkip(true);

    assertDoesNotThrow(() -> mojo.execute());
  }

  @Test
  void writesMarkdownNextToInputFilesWhenOutputDirectoryIsOmitted() throws java.io.IOException {
    final Path inputDirectory = tempDir.resolve("input");
    Files.createDirectories(inputDirectory);
    Files.write(inputDirectory.resolve("sample.xlsx"), createWorkbookBytes("Hello"));
    Files.write(inputDirectory.resolve("note.txt"), "ignore".getBytes(StandardCharsets.UTF_8));

    final ConvertDirectoryMojo mojo = new ConvertDirectoryMojo();
    mojo.setInputDirectory(inputDirectory.toFile());

    assertDoesNotThrow(() -> mojo.execute());

    final Path outputPath = inputDirectory.resolve("sample.md");
    assertEquals(inputDirectory.toFile(), mojo.getInputDirectory());
    assertEquals(null, mojo.getOutputDirectory());
    assertEquals(false, mojo.isRecursive());
    assertTrue(Files.isRegularFile(outputPath));
    assertTrue(new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8).contains("# Book: sample.xlsx"));
  }

  @Test
  void preservesRelativeDirectoriesWhenRecursiveOutputDirectoryIsSpecified() throws java.io.IOException {
    final Path inputDirectory = tempDir.resolve("input");
    final Path nestedDirectory = inputDirectory.resolve("nested").resolve("deep");
    Files.createDirectories(nestedDirectory);
    Files.write(inputDirectory.resolve("root.xlsx"), createWorkbookBytes("Root"));
    Files.write(nestedDirectory.resolve("child.xlsx"), createWorkbookBytes("Child"));
    final Path outputDirectory = tempDir.resolve("out");

    final ConvertDirectoryMojo mojo = new ConvertDirectoryMojo();
    mojo.setInputDirectory(inputDirectory.toFile());
    mojo.setOutputDirectory(outputDirectory.toFile());
    mojo.setRecursive(true);
    mojo.setOutputMode("both");

    assertDoesNotThrow(() -> mojo.execute());

    final Path rootOutput = outputDirectory.resolve("root.md");
    final Path childOutput = outputDirectory.resolve("nested").resolve("deep").resolve("child.md");
    assertEquals(outputDirectory.toFile(), mojo.getOutputDirectory());
    assertEquals(true, mojo.isRecursive());
    assertEquals("both", mojo.getOutputMode());
    assertTrue(Files.isRegularFile(rootOutput));
    assertTrue(Files.isRegularFile(childOutput));
    assertTrue(new String(Files.readAllBytes(rootOutput), StandardCharsets.UTF_8).contains("# Book: root.xlsx"));
    assertTrue(new String(Files.readAllBytes(childOutput), StandardCharsets.UTF_8).contains("# Book: child.xlsx"));
  }

  @Test
  void doesNotDescendIntoSubdirectoriesWhenRecursiveIsDisabled() throws java.io.IOException {
    final Path inputDirectory = tempDir.resolve("input");
    final Path nestedDirectory = inputDirectory.resolve("nested");
    Files.createDirectories(nestedDirectory);
    Files.write(nestedDirectory.resolve("child.xlsx"), createWorkbookBytes("Child"));
    final Path outputDirectory = tempDir.resolve("out");

    final ConvertDirectoryMojo mojo = new ConvertDirectoryMojo();
    mojo.setInputDirectory(inputDirectory.toFile());
    mojo.setOutputDirectory(outputDirectory.toFile());
    mojo.setRecursive(false);

    assertDoesNotThrow(() -> mojo.execute());

    assertTrue(!Files.exists(outputDirectory.resolve("nested").resolve("child.md")));
  }

  private static byte[] createWorkbookBytes(final String text) {
    return ZipIo.createStoredZip(new ZipIo.ExportEntry[] {
        entry("xl/workbook.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"Sheet1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>"),
        entry("xl/_rels/workbook.xml.rels",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Target=\"worksheets/sheet1.xml\"/>"
                + "</Relationships>"),
        entry("xl/sharedStrings.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<si><t>" + text + "</t></si>"
                + "</sst>"),
        entry("xl/styles.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/></border></borders>"
                + "<cellXfs count=\"1\"><xf numFmtId=\"0\" borderId=\"0\" fontId=\"0\"/></cellXfs>"
                + "</styleSheet>"),
        entry("xl/worksheets/sheet1.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<sheetData><row r=\"1\"><c r=\"A1\" t=\"s\"><v>0</v></c></row></sheetData>"
                + "</worksheet>")
    });
  }

  private static ZipIo.ExportEntry entry(final String name, final String text) {
    return new ZipIo.ExportEntry(name, text.getBytes(StandardCharsets.UTF_8));
  }
}
