# Remaining Items

Document version: `2026-04-22`

## Current Position

Java port scaffolding is ready as a Maven multi-module project, workbook parsing minimum path is connected, markdown export helper functions are straight-converted, and an initial sheet-to-markdown conversion layer is connected to the core facade. The project is not yet able to run end-to-end conversion from the CLI or Maven plugin.

## Status

- 対応済み
  - Maven / JUnit / Java 8 baseline
  - `workplace/` workspace rule
  - upstream source / test inventory
  - `address-utils`
  - `markdown-normalize`
  - `markdown-escape`
  - `markdown-options`
  - `text-encoding`
  - `xml-utils`
  - `zip-io`
  - `rels-parser`
  - workbook loader minimum entrypoint
  - `shared-strings`
  - `styles-parser`
  - worksheet parser minimum scope
  - connected workbook parse facade
  - workbook parse fixture regression
  - `markdown-table-escape`
  - `markdown-export` helper layer
  - `cell-format`
  - `worksheet-tables`
  - `sheet-markdown` minimum conversion layer
  - core markdown conversion facade
  - CLI option vocabulary skeleton
  - Maven plugin skeleton
- 保守確認
  - help text compatibility
  - focused regression command layout
- 保留
  - advanced `sheet-markdown` parity coverage
  - workbook-to-markdown fixture regression
  - end-to-end CLI conversion

## Focused Regression

- `mvn test`
- `mvn -pl miku-xlsx2md -Dtest=AddressUtilsTest test`
- `mvn -pl miku-xlsx2md -Dtest=MarkdownNormalizeTest test`
- `mvn -pl miku-xlsx2md -Dtest=MarkdownEscapeTest test`
- `mvn -pl miku-xlsx2md -Dtest=MarkdownOptionsTest test`
- `mvn -pl miku-xlsx2md -Dtest=TextEncodingTest test`
- `mvn -pl miku-xlsx2md -Dtest=XmlUtilsTest test`
- `mvn -pl miku-xlsx2md -Dtest=ZipIoTest test`
- `mvn -pl miku-xlsx2md -Dtest=RelsParserTest test`
- `mvn -pl miku-xlsx2md -Dtest=WorkbookLoaderTest test`
- `mvn -pl miku-xlsx2md -Dtest=SharedStringsTest test`
- `mvn -pl miku-xlsx2md -Dtest=StylesParserTest test`
- `mvn -pl miku-xlsx2md -Dtest=WorksheetParserTest test`
- `mvn -pl miku-xlsx2md -Dtest=CoreTest test`
- `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test`
- `mvn -pl miku-xlsx2md -Dtest=MarkdownExportTest test`
- `mvn -pl miku-xlsx2md -Dtest=CellFormatTest test`
- `mvn -pl miku-xlsx2md -Dtest=WorksheetTablesTest test`
- `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test`
- `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test`
- `mvn -pl miku-xlsx2md-maven-plugin -Dtest=MikuXlsx2mdMojoTest test`

## Last Known Result

- `mvn -o test` passed on `2026-04-22`

## Next Unit

- Add workbook-to-markdown fixture regression through the runtime core facade
- Connect CLI to runtime core conversion
- Connect Maven plugin to runtime core API once workbook conversion becomes available
