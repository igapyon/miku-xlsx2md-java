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
`scripts/miku-xlsx2md-cli.mjs`

java classes:
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCli`
- `jp.igapyon.mikuxlsx2md.cli.CliOptions`

notes:
- facade: `main(String[] args)` delegates to `run(String[] args, PrintStream out, PrintStream err)`
- helper split: option parsing isolated in `CliOptions`
- Java-side extension: current implementation stops after validation because workbook conversion is still pending

## Next Candidates

- `src/ts/markdown-options.ts`
- `src/ts/text-encoding.ts`
- `src/ts/xml-utils.ts`
- `src/ts/zip-io.ts`
- `src/ts/shared-strings.ts`
