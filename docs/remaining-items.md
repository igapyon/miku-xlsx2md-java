# Remaining Items

Document version: `2026-04-22`

## Current Position

Java port scaffolding is ready as a Maven multi-module project, workbook parsing minimum path is connected, markdown export helper functions are straight-converted, and an initial sheet-to-markdown conversion layer is connected to the core facade. Table detection, rich text rendering, sheet asset parsing/rendering/grouping helpers, and office drawing shape SVG helper are now split out of `SheetMarkdown` / `WorksheetParser`, and initial end-to-end conversion is connected from both the CLI and Maven plugin.

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
  - `narrative-structure`
  - `border-grid`
  - `table-detector`
  - `sheet-assets` rendering / shape block grouping subset
  - `SheetMarkdown` shape block rendering connection
  - `sheet-assets` parseDrawingImages / parseDrawingCharts / parseDrawingShapes subset
  - `WorksheetParser` sheet assets parse helper connection
  - `office-drawing` shape SVG rendering helper
  - `SheetAssets` shape SVG helper connection
  - `rich-text-*`
  - `sheet-markdown` minimum conversion layer
  - `SheetMarkdown` table detection / matrix rendering delegation to `TableDetector`
  - `SheetMarkdown` cell display rendering delegation to `RichTextRenderer`
  - `SheetMarkdown` asset section rendering delegation to `SheetAssets`
  - idempotent table pipe escaping for rich text rendered cells
  - core markdown conversion facade
  - workbook-to-markdown fixture regression
  - image / shape workbook parse fixture regression
  - CLI option vocabulary and initial conversion
  - Maven plugin initial conversion
- 保守確認
  - help text compatibility
  - focused regression command layout
- 保留
  - advanced `sheet-markdown` parity coverage
  - broader CLI / Maven plugin fixture coverage
  - Maven plugin smoke execution command

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
- `mvn -pl miku-xlsx2md -Dtest=NarrativeStructureTest test`
- `mvn -pl miku-xlsx2md -Dtest=BorderGridTest test`
- `mvn -pl miku-xlsx2md -Dtest=TableDetectorTest test`
- `mvn -pl miku-xlsx2md -Dtest=OfficeDrawingTest test`
- `mvn -pl miku-xlsx2md -Dtest=SheetAssetsTest test`
- `mvn -pl miku-xlsx2md -Dtest=RichTextRendererTest test`
- `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test`
- `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test`
- `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test`

## Last Known Result

- `mvn test` passed on `2026-04-22`

## Next Unit

- Expand advanced `sheet-markdown` parity coverage
- Add broader CLI / Maven plugin fixture coverage
