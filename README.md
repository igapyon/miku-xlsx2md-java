# miku-xlsx2md-java

Java port of [`igapyon/miku-xlsx2md`](https://github.com/igapyon/miku-xlsx2md) following the straight conversion rules in [docs/miku-straight-conversion-guide.md](docs/miku-straight-conversion-guide.md).

## Fixed Baseline

- Java source / target compatibility: `1.8`
- Build tool: `Maven`
- Test framework: `JUnit Jupiter`
- Primary test entrypoint: `mvn test`
- Packaging: single fat jar
- Local upstream workspace: `workplace/`

## Current Status

- Upstream source / test / CLI inventory completed from `workplace/miku-xlsx2md`
- Initial Java multi-module scaffolding is in place
- Straight-converted utility modules implemented:
  - `address-utils.ts`
  - `markdown-normalize.ts`
  - `markdown-escape.ts`
  - `markdown-table-escape.ts`
- Additional option / encoding modules implemented:
  - `markdown-options.ts`
  - `text-encoding.ts`
- Workbook parsing minimum path is implemented:
  - `xml-utils.ts`
  - `zip-io.ts`
  - `rels-parser.ts`
  - `workbook-loader.ts`
  - `shared-strings.ts`
  - `styles-parser.ts`
  - `worksheet-parser.ts`
  - `core.ts`
- Markdown export helper layer is implemented:
  - `markdown-export.ts`
- Display-value formatting helper is implemented:
  - `cell-format.ts`
- Worksheet table metadata helper is implemented:
  - `worksheet-tables.ts`
- Narrative structure helper is implemented:
  - `narrative-structure.ts`
- Table detection helper layer is implemented:
  - `border-grid.ts`
  - `table-detector.ts`
- Sheet asset parsing / rendering / grouping helper layer is partially implemented:
  - `sheet-assets.ts`
- Office drawing shape SVG helper layer is implemented:
  - `office-drawing.ts`
- Rich text rendering helper layer is implemented:
  - `rich-text-parser.ts`
  - `rich-text-plain-formatter.ts`
  - `rich-text-github-formatter.ts`
  - `rich-text-renderer.ts`
- Initial sheet-to-markdown conversion layer is implemented:
  - `sheet-markdown.ts`
- Focused workbook fixture regression is in place for upstream `named-range` and `hyperlink` fixtures
- Focused workbook fixture regression includes the upstream `display-format` fixture
- Focused workbook fixture regression includes upstream formula and chart fixtures, including cross-sheet, shared, spill, and mixed chart cases
- Focused workbook fixture regression includes upstream rich text and merge fixtures, including `rich-text-github`, `rich-markdown-escape`, and `merge-multiline`
- Focused workbook fixture regression includes upstream image and shape fixtures, including `image-basic-sample02`, `shape-flowchart`, and `shape-block-arrow`
- Focused workbook fixture regression includes upstream narrative, edge-empty, weird-sheetname, table border-priority, full table-basic parity subset, and grid-layout parity cases
- `WorksheetParserTest` includes shared formula translation coverage with sheet-qualified and absolute references, plus upstream `formula-crosssheet` / `formula-shared` fixture assertions
- `SheetMarkdownTest` includes GitHub hyperlink rendering coverage that suppresses underline markup on linked cells
- `SheetMarkdownTest` includes shape section spacing coverage when consecutive SVG-backed shape items are rendered
- `SheetMarkdownTest` includes table detection compatibility alias coverage for `border-priority`
- `SheetMarkdownTest` includes planner-aware calendar layout suppression coverage so repeated narrow calendar columns stay narrative instead of becoming small tables
- `SheetMarkdownTest` includes upstream `xlsx2md-basic`, display, hyperlink, rich text / markdown escape, merge, formula, chart, shape, table, grid, image, and weird-sheetname fixture parity coverage
- Focused workbook-to-markdown fixture regression is in place for upstream `display-format`, `hyperlink`, rich text, and merge fixtures
- Java CLI is implemented with Node-compatible option vocabulary, GUI-aligned default formatting mode `github`, initial end-to-end conversion, and Java-side directory batch conversion
- Maven plugin is connected to runtime core conversion, including a directory batch conversion goal with optional recursive scan shared with the CLI runtime helper
- CLI / Maven plugin fixture coverage includes upstream `xlsx2md-basic`, `image-basic-sample02`, weird-sheetname, `shape-flowchart`, `shape-block-arrow`, `shape-callout`, table alias, narrative/display/named-range/rich/merge/formula/chart fixtures, shape fixture, and compatibility alias cases
- Maven plugin full-coordinate smoke command is fixed in `scripts/smoke-maven-plugin.sh`
- Advanced sheet-markdown parity and broader CLI / Maven plugin fixture coverage are still pending, but the current subset now covers additional rich/merge/formula/chart/table/grid/image edge cases

## Build

```bash
mvn test
```

```bash
mvn package
```

The shaded CLI jar is produced under `miku-xlsx2md/target/`.

## CLI

Current entrypoint:

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar --help
```

The CLI validates the main option set used by the upstream Node.js CLI and can write combined Markdown or ZIP export outputs.

Directory batch conversion is available as a Java-side CLI extension:

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  --input-directory docs/xlsx \
  --output-directory docs/md \
  --recursive \
  --verbose
```

When `--output-directory` is omitted, Markdown files are written next to the input `.xlsx` files. `--out` and `--zip` are not available with `--input-directory`. `--verbose` prints the workbook path being processed to stderr.

## Maven Plugin

Full-coordinate smoke check:

```bash
sh scripts/smoke-maven-plugin.sh
```

The smoke script installs the local artifacts and invokes:

```bash
mvn -N jp.igapyon:miku-xlsx2md-maven-plugin:0.9.0:convert
```

Directory batch conversion is also available:

```bash
mvn -N jp.igapyon:miku-xlsx2md-maven-plugin:0.9.0:convert-directory \
  -Dmiku-xlsx2md.inputDirectory=docs/xlsx \
  -Dmiku-xlsx2md.outputDirectory=docs/md \
  -Dmiku-xlsx2md.recursive=false \
  -Dmiku-xlsx2md.verbose=true
```

When `outputDirectory` is omitted, Markdown files are written next to the input `.xlsx` files. The directory goal does not support ZIP output. `miku-xlsx2md.verbose=true` logs the workbook path being processed.

## Documents

- [docs/upstream-class-mapping.md](docs/upstream-class-mapping.md)
- [docs/upstream-test-mapping.md](docs/upstream-test-mapping.md)
- [docs/remaining-items.md](docs/remaining-items.md)
- [docs/follow-up-log.md](docs/follow-up-log.md)
- [TODO.md](TODO.md)
