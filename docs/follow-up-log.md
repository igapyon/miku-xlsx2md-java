# Follow-up Log

Document version: `2026-04-22`

## 2026-04-22 Initial Straight Conversion Setup

upstream file:
- `src/ts/address-utils.ts`
- `src/ts/markdown-normalize.ts`
- `src/ts/markdown-escape.ts`
- `src/ts/markdown-options.ts`
- `src/ts/text-encoding.ts`
- `src/ts/xml-utils.ts`
- `src/ts/zip-io.ts`
- `src/ts/rels-parser.ts`
- `src/ts/workbook-loader.ts`
- `src/ts/shared-strings.ts`
- `src/ts/styles-parser.ts`
- `src/ts/worksheet-parser.ts`
- `src/ts/core.ts`
- `src/ts/markdown-table-escape.ts`
- `src/ts/markdown-export.ts`
- `src/ts/cell-format.ts`
- `src/ts/worksheet-tables.ts`
- `src/ts/sheet-markdown.ts`
- `scripts/miku-xlsx2md-cli.mjs`

java classes:
- `AddressUtils`
- `MarkdownNormalize`
- `MarkdownEscape`
- `MarkdownOptions`
- `TextEncoding`
- `XmlUtils`
- `ZipIo`
- `RelsParser`
- `WorkbookLoader`
- `SharedStrings`
- `StylesParser`
- `WorksheetParser`
- `Core`
- `MarkdownTableEscape`
- `MarkdownExport`
- `CellFormat`
- `WorksheetTables`
- `SheetMarkdown`
- `CliOptions`
- `MikuXlsx2mdCli`
- `MikuXlsx2mdMojo`

tests:
- `AddressUtilsTest`
- `MarkdownNormalizeTest`
- `MarkdownEscapeTest`
- `MarkdownOptionsTest`
- `TextEncodingTest`
- `XmlUtilsTest`
- `ZipIoTest`
- `RelsParserTest`
- `WorkbookLoaderTest`
- `SharedStringsTest`
- `StylesParserTest`
- `WorksheetParserTest`
- `CoreTest`
- `CoreFixtureRegressionTest`
- `MarkdownExportTest`
- `CellFormatTest`
- `WorksheetTablesTest`
- `SheetMarkdownTest`
- `MikuXlsx2mdCliTest`
- `MikuXlsx2mdMojoTest`

diff summary:
- 挙動差分:
  - CLI は option validation / help / initial workbook conversion を実装
  - Maven plugin は runtime core conversion へ接続済み
  - `sheet-markdown` は最小変換導線を実装し、advanced table detector / rich text split / shape block grouping は未移植
- 命名差分:
  - module registry 方式を Java static facade へ読み替え
- 未移植差分:
  - worksheet parse coverage expansion
  - advanced `sheet-markdown` parity coverage
  - broader CLI / Maven plugin fixture coverage
  - Maven plugin smoke execution command
- Java 側独自拡張:
  - immutable value objects for equality-based tests

follow-up:
- 実施した確認:
  - upstream source/test inventory
  - sibling repo の multi-module / Maven plugin 構成
  - `.mvn/jvm.config` による Maven 通信前提の固定
  - Java 17 + Maven 3.9 on source/target 1.8
  - `mvn -o test` pass
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test` pass
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass
  - `mvn test` pass
- fixture:
  - `workplace/miku-xlsx2md/tests/fixtures/named-range/named-range-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/link/hyperlink-basic-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/display/display-format-sample01.xlsx`
- 次回の確認観点:
  - advanced `sheet-markdown` parity coverage を広げる
  - table detector / narrative structure / rich text / sheet assets helper の分割移植を検討する
  - CLI / Maven plugin の fixture coverage を広げる
