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
`scripts/miku-xlsx2md-cli.mjs`

java classes:
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCli`
- `jp.igapyon.mikuxlsx2md.cli.CliOptions`

notes:
- facade: `main(String[] args)` delegates to `run(String[] args, PrintStream out, PrintStream err)`
- helper split: option parsing isolated in `CliOptions`
- Java-side extension: current implementation stops after validation because workbook conversion is still pending

### upstream file:
Maven plugin implementation target for Java distribution

java classes:
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojo`

notes:
- facade: Maven goal skeleton
- helper split: parameter mapping kept inside mojo
- Java-side extension: plugin currently fails fast until workbook conversion core is implemented

## Next Candidates

- `src/ts/shared-strings.ts`
- `src/ts/styles-parser.ts`
- `src/ts/worksheet-parser.ts`
