# Remaining Items

Document version: `2026-04-23`

## Current Position

Java port scaffolding is ready as a Maven multi-module project, workbook parsing minimum path is connected, markdown export helper functions are straight-converted, and an initial sheet-to-markdown conversion layer is connected to the core facade. Table detection, rich text rendering, sheet asset parsing/rendering/grouping helpers, and office drawing shape SVG helper are now split out of `SheetMarkdown` / `WorksheetParser`, initial end-to-end conversion is connected from both the CLI and Maven plugin, Maven plugin full-coordinate smoke execution is fixed as a script, upstream `planner-aware` table detection mode plus GUI-aligned CLI defaults are now reflected on the Java side, both the Java CLI and Maven plugin now share directory batch conversion, and verbose processing diagnostics are available for CLI and Maven plugin execution.

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
  - `WorksheetParser` richTextRuns / formula metadata / hyperlink range / sheet-qualified shared formula translation coverage subset
  - connected workbook parse facade
  - workbook parse fixture regression
  - `markdown-table-escape`
  - `markdown-export` helper layer
  - `cell-format`
  - `worksheet-tables`
  - `narrative-structure`
  - `border-grid`
  - `table-detector`
  - `planner-aware` table detection mode with planner/calendar-specific suppression heuristics
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
  - advanced `sheet-markdown` parity coverage subset for calendar narrative, calendar sidebar ordering, empty fallback, line breaks, literal escaping, hyperlink output modes, shape details toggle, and fixture-backed narrative / sparse / border-priority / broader table-basic / grid-layout / xlsx2md-basic / named-range / display / hyperlink / rich / merge / formula / chart / shape / image-basic-sample01 / image-basic-sample02 / edge-empty / weird-sheetname cases
  - idempotent table pipe escaping for rich text rendered cells
  - core markdown conversion facade
  - workbook-to-markdown fixture regression
  - formula basic / chart basic workbook-to-markdown fixture regression
  - formula cross-sheet / shared workbook-to-markdown fixture regression
  - formula spill / chart mixed workbook-to-markdown fixture regression
  - rich usecase / rich-text-github / rich-markdown-escape / merge pattern / merge-multiline workbook-to-markdown fixture regression
  - image fixture regression now includes `image-basic-sample02`
  - edge fixture regression now includes weird sheet name filename sanitization coverage
  - CLI option vocabulary and initial conversion
  - CLI GUI-aligned default formatting mode `github` and help text sync
  - CLI directory batch conversion backed by shared runtime helper
  - CLI `--verbose` processing diagnostics
  - Maven plugin initial conversion
  - Maven plugin directory batch conversion goal with optional recursive scan and output-directory mirroring backed by shared runtime helper
  - Maven plugin `miku-xlsx2md.verbose` processing diagnostics
  - release version updated to `0.9.0`
  - CLI / Maven plugin upstream fixture conversion coverage for the current local fixture inventory, including table-basic / grid-layout / table alias / shape details compatibility aliases and display / named-range / narrative / hyperlink / rich / rich-text-github / merge / merge-pattern / formula / chart / xlsx2md-basic / image-basic-sample01 / image-basic-sample02 / edge-empty / weird-sheetname / shape-flowchart / shape-block-arrow / shape-callout
  - Maven plugin full-coordinate smoke execution command, including `convert` and `convert-directory`
  - GitHub Actions release workflow that attaches the shaded runtime jar to GitHub Release assets
- 保守確認
  - help text compatibility
  - focused regression command layout
- 保留
  - advanced `sheet-markdown` fixture parity coverage beyond the current subset
  - worksheet parser shared / cross-sheet formula fixture coverage now includes broader upstream `formula-crosssheet` / `formula-shared` assertions plus value type / raw value / formula type / cached value metadata assertions
  - broader CLI / Maven plugin fixture coverage for future upstream fixture additions
  - broader Maven plugin smoke coverage beyond the fixed `convert` / `convert-directory` commands
  - actual GitHub tag 上での release workflow 実行は未確認

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
- `sh scripts/smoke-maven-plugin.sh`

## Last Known Result

- `mvn test` passed on `2026-04-23`

## Next Unit

- Expand advanced `sheet-markdown` fixture parity coverage beyond the current subset
- Expand worksheet parser shared / cross-sheet formula fixture coverage further if upstream adds new cases
- Add broader CLI / Maven plugin fixture coverage when future upstream fixture additions appear
