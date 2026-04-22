# Upstream Class Mapping

Document version: `2026-04-22`

## Current Coverage

### upstream file:
`src/ts/address-utils.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.addressutils.AddressUtils`
- `jp.igapyon.mikuxlsx2md.addressutils.AddressUtils.MergeRange`
- `jp.igapyon.mikuxlsx2md.addressutils.AddressUtils.RangeAddress`
- `jp.igapyon.mikuxlsx2md.addressutils.AddressUtils.CellAddress`

notes:
- facade: static utility methods
- helper split: none
- Java-side extension: immutable value objects for testable equality

### upstream file:
`src/ts/markdown-normalize.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalize`

notes:
- facade: static utility methods
- helper split: compiled `Pattern` constants
- Java-side extension: none

### upstream file:
`src/ts/markdown-escape.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.markdownescape.MarkdownEscape`
- `jp.igapyon.mikuxlsx2md.markdownescape.MarkdownEscape.MarkdownLiteralPart`

notes:
- facade: static utility methods
- helper split: line-oriented escaping kept inside same class
- Java-side extension: value object for literal parts

### upstream file:
`src/ts/markdown-table-escape.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.markdowntableescape.MarkdownTableEscape`

notes:
- facade: static utility methods
- helper split: delegates to `MarkdownNormalize`
- Java-side extension: none

### upstream file:
`src/ts/markdown-options.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions`
- `jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptions.ResolvedMarkdownOptions`

notes:
- facade: static normalization methods plus immutable option holders
- helper split: none
- Java-side extension: immutable builders replaced optional object literals

### upstream file:
`src/ts/text-encoding.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.textencoding.TextEncoding`
- `jp.igapyon.mikuxlsx2md.textencoding.TextEncoding.MarkdownEncodingOptions`

notes:
- facade: static encoding helpers
- helper split: UTF-16 / UTF-32 byte encoders kept in same class
- Java-side extension: Java runtime charset lookup for `shift_jis`

### upstream file:
`src/ts/xml-utils.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils`

notes:
- facade: static DOM helper methods
- helper split: secure parser setup kept inside same class
- Java-side extension: JAXP-based DOM parsing with secure processing flags

### upstream file:
`src/ts/zip-io.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.zipio.ZipIo`
- `jp.igapyon.mikuxlsx2md.zipio.ZipIo.ExportEntry`
- `jp.igapyon.mikuxlsx2md.zipio.ZipIo.ZipEntryTimestamp`

notes:
- facade: static ZIP read/write helpers
- helper split: little-endian readers and raw inflate kept inside same class
- Java-side extension: Java `Inflater(true)` for deflate-raw support

### upstream file:
`src/ts/rels-parser.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.relsparser.RelsParser`
- `jp.igapyon.mikuxlsx2md.relsparser.RelsParser.RelationshipEntry`

notes:
- facade: static relationship parsing helpers
- helper split: none
- Java-side extension: XML utils are called directly instead of module-registry injection

### upstream file:
`src/ts/workbook-loader.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader`
- `jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader.WorkbookLoaderDependencies`
- `jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader.ParsedWorkbook`
- `jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader.DefinedName`

notes:
- facade: static workbook loading helpers with dependency injection
- helper split: nested contract/value types kept in the same class
- Java-side extension: minimum workbook entrypoint currently uses simplified placeholder value objects

### upstream file:
`src/ts/shared-strings.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings`
- `jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings.SharedStringEntry`
- `jp.igapyon.mikuxlsx2md.sharedstrings.SharedStrings.RichTextRun`

notes:
- facade: static shared string parsing helpers
- helper split: rich-text run extraction kept inside same class
- Java-side extension: XML utils are called directly instead of runtime-env indirection

### upstream file:
`src/ts/styles-parser.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.stylesparser.StylesParser`
- `jp.igapyon.mikuxlsx2md.stylesparser.StylesParser.BorderFlags`
- `jp.igapyon.mikuxlsx2md.stylesparser.StylesParser.TextStyle`
- `jp.igapyon.mikuxlsx2md.stylesparser.StylesParser.CellStyleInfo`

notes:
- facade: static style parsing helpers
- helper split: builtin format code registry kept in same class
- Java-side extension: XML utils are called directly instead of runtime-env indirection

### upstream file:
`src/ts/worksheet-parser.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser.ParsedSheet`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser.ParsedCell`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser.Hyperlink`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser.ExtractedCellOutput`

notes:
- facade: static worksheet parsing helpers
- helper split: hyperlink parsing, shared-formula translation, inline rich text run extraction, and styled run merging kept in same class
- Java-side extension: parsed cell getters expose formula metadata needed by downstream diagnostics/tests

### upstream file:
`src/ts/core.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.core.Core`

notes:
- facade: minimum public runtime entrypoint
- helper split: workbook loading delegates to `WorkbookLoader`
- Java-side extension: current scope exposes only the connected workbook parsing path

### upstream file:
`src/ts/markdown-export.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.FormulaDiagnostic`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.TableScoreDetail`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.MarkdownSummary`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.MarkdownFile`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.ExportWorkbook`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.ExportSheet`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.ExportImage`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.ExportShape`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.CombinedMarkdownExportFile`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExport.CombinedMarkdownExportPayload`

notes:
- facade: static markdown export helper methods
- helper split: archive entry creation is delegated to `ZipIo`, text encoding is delegated to `TextEncoding`
- Java-side extension: export-side workbook and markdown summary shapes are expressed as immutable value objects

### upstream file:
`src/ts/cell-format.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.cellformat.CellFormat`
- `jp.igapyon.mikuxlsx2md.cellformat.CellFormat.DateParts`
- `jp.igapyon.mikuxlsx2md.cellformat.CellFormat.ResolvedCellLike`

notes:
- facade: static cell display formatting and value parsing helpers
- helper split: date parts and mutable resolved-cell contract are nested types in the same class
- Java-side extension: display formatting is wired from `WorkbookLoader` into `WorksheetParser` through dependency injection

### upstream file:
`src/ts/worksheet-tables.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.worksheettables.WorksheetTables`
- `jp.igapyon.mikuxlsx2md.worksheettables.WorksheetTables.ParsedTable`

notes:
- facade: static worksheet table parsing helpers
- helper split: relationship/path resolution is delegated to existing `RelsParser`, range normalization is delegated to `AddressUtils`
- Java-side extension: currently kept as an independent helper module and not yet connected into sheet-to-markdown conversion

### upstream file:
`src/ts/border-grid.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.bordergrid.BorderGrid`
- `jp.igapyon.mikuxlsx2md.bordergrid.BorderGrid.EdgeStats`

notes:
- facade: static normalized border lookup and table edge statistics helpers
- helper split: consumes worksheet parser cells directly and keeps row edge stats as a nested value object
- Java-side extension: none

### upstream file:
`src/ts/table-detector.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetector`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetector.Bounds`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetector.TableCandidate`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetector.TableScoreWeights`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetector.CellFormatter`

notes:
- facade: static table seed collection, connected component detection, candidate scoring/pruning, bounds trimming, merge token application, and candidate matrix creation
- helper split: normalized border statistics are delegated to `BorderGrid`
- Java-side extension: `SheetMarkdown.detectTableCandidates` and `SheetMarkdown.matrixFromCandidate` delegate to this helper while preserving the existing `SheetMarkdown.TableCandidate` public shape

### upstream file:
`src/ts/sheet-assets.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssets`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssets.BoundingBox`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssets.ShapeBox`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssets.ShapeBlockOptions`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssets.ShapeBlock`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser.ParsedImageAsset`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser.ParsedChartAsset`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParser.ParsedShapeAsset`

notes:
- facade: static sheet asset directory sanitizing, drawing image/chart/shape parsing, asset section rendering, hierarchical raw entry rendering, and shape block grouping helpers
- helper split: image/chart/shape Markdown rendering and shape block section rendering are delegated from `SheetMarkdown`; parsed sheet asset collection is called from `WorksheetParser`; shape SVG rendering is delegated to `OfficeDrawing`
- Java-side extension: parsed shape SVG assets are attached during `SheetAssets.parseDrawingShapes`

### upstream file:
`src/ts/office-drawing.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.officedrawing.OfficeDrawing`
- `jp.igapyon.mikuxlsx2md.officedrawing.OfficeDrawing.SvgRenderResult`

notes:
- facade: static shape SVG rendering helper
- helper split: shape SVG rendering is called from `SheetAssets.parseDrawingShapes`
- Java-side extension: browser `TextEncoder` output is represented as UTF-8 `byte[]`

### upstream file:
`src/ts/narrative-structure.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.narrativestructure.NarrativeStructure`

notes:
- facade: static narrative block rendering and classification helpers
- helper split: consumes `SheetMarkdown.NarrativeBlock` / `NarrativeItem` value types
- Java-side extension: `SheetMarkdown.renderNarrativeBlock` delegates to this helper to preserve upstream responsibility boundaries

### upstream file:
`src/ts/rich-text-parser.ts`
`src/ts/rich-text-plain-formatter.ts`
`src/ts/rich-text-github-formatter.ts`
`src/ts/rich-text-renderer.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRenderer`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRenderer.RichTextStyle`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRenderer.StyledTextPart`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRenderer.RichTextToken`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRenderer.RawLineToken`

notes:
- facade: static rich text tokenization, plain rendering, GitHub rendering, and cell display rendering helpers
- helper split: parser / plain formatter / GitHub formatter / renderer responsibilities are currently kept in one Java class while preserving method-level boundaries
- Java-side extension: `SheetMarkdown.renderCellDisplayText` delegates to this helper, and table pipe escaping is idempotent for already escaped rich-text output

### upstream file:
`src/ts/sheet-markdown.ts`

java classes:
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown.TableCandidate`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown.NarrativeRowSegment`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown.NarrativeItem`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown.NarrativeBlock`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown.SheetRenderState`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdown.ContentSection`

notes:
- facade: static sheet / workbook markdown conversion helpers
- helper split: table detection / matrix rendering is delegated to `TableDetector`; narrative rendering is delegated to `NarrativeStructure`; cell display rendering is delegated to `RichTextRenderer`; asset section rendering and shape block rendering are delegated to `SheetAssets`; hyperlink formatting remains inside the same class
- Java-side extension: `Core` now exposes `convertSheetToMarkdown`, `convertWorkbookToMarkdownFiles`, and parsed-workbook export asset adaptation
- remaining parity gap: advanced fixture parity coverage beyond the current calendar / empty fallback / line break / literal escaping / hyperlink output mode subset is still pending

### upstream file:
`scripts/miku-xlsx2md-cli.mjs`

java classes:
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCli`
- `jp.igapyon.mikuxlsx2md.cli.CliOptions`

notes:
- facade: `main(String[] args)` delegates to `run(String[] args, PrintStream out, PrintStream err)`
- helper split: option parsing isolated in `CliOptions`
- Java-side extension: file I/O and diagnostics are kept in the CLI layer; conversion delegates to the runtime core facade

### upstream file:
Maven plugin implementation target for Java distribution

java classes:
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojo`

notes:
- facade: Maven `convert` goal delegates to runtime core conversion
- helper split: parameter mapping kept inside mojo
- Java-side extension: Maven plugin writes combined Markdown output through the Java runtime core

## Next Candidates

- advanced `sheet-markdown` parity coverage beyond the current subset
