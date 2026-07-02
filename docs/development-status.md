# Development Status

This document keeps the development-oriented status notes that were previously in the top-level README.

## Fixed Baseline

- Java source / target compatibility: `1.8`
- Build tool: `Maven`
- Test framework: `JUnit Jupiter`
- Primary test entrypoint: `mvn test`
- Packaging: single fat jar plus attached sources jar
- Local upstream workspace: `workplace/`
- Low-level Office package dependency: vendored `miku-ms-office-core-java`
  release jar under `vendor/miku-ms-office-core-java/`

## Current Status

- Upstream source / test / CLI inventory completed from `workplace/miku-xlsx2md`
- Java runtime scaffolding is in place at the repository root
- Low-level ZIP package reading delegates through `miku-ms-office-core-java`
  while Markdown/assets ZIP output still uses the local deterministic stored
  ZIP writer.
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
- `WorksheetParserTest` includes legacy note and threaded comment parsing coverage from worksheet relationships
- `SheetMarkdownTest` includes GitHub hyperlink rendering coverage that suppresses underline markup on linked cells
- `SheetMarkdownTest` includes comment section rendering coverage and summary comment counts
- `SheetMarkdownTest` includes shape section spacing coverage when consecutive SVG-backed shape items are rendered
- `SheetMarkdownTest` includes table detection compatibility alias coverage for `border-priority`
- `SheetMarkdownTest` includes planner-aware calendar layout suppression coverage so repeated narrow calendar columns stay narrative instead of becoming small tables
- `SheetMarkdownTest` includes upstream `xlsx2md-basic`, display, hyperlink, rich text / markdown escape, merge, formula, chart, shape, table, grid, image, and weird-sheetname fixture parity coverage
- Focused workbook-to-markdown fixture regression is in place for upstream `display-format`, `hyperlink`, rich text, and merge fixtures
- Java CLI is implemented with Node-compatible option vocabulary, GUI-aligned default formatting mode `github`, initial end-to-end conversion, and Java-side directory batch conversion
- Java CLI supports the upstream metadata command `--version`, the upstream
  `--front-matter include|exclude` option, and simplified workbook-level YAML
  front matter aligned with upstream `miku-xlsx2md` v1.3.0.
- Java conversion reflects upstream `miku-xlsx2md` v1.3.0 legacy note and
  threaded comment output as `### Comments` sections.
- CLI fixture coverage includes upstream `xlsx2md-basic`, `image-basic-sample01`, `image-basic-sample02`, `edge-empty`, weird-sheetname, `shape-flowchart`, `shape-block-arrow`, `shape-callout`, table-basic / grid-layout / table alias, narrative/display/named-range/hyperlink/rich/merge/formula/chart fixtures, shape fixture, and compatibility alias cases
- Maven plugin support has moved to the separated `miku-xlsx2md-java-maven` repository
- GitHub Actions release workflow builds the runtime jar and uploads it to the GitHub Release assets for tag releases
- Advanced sheet-markdown parity and future upstream fixture additions remain follow-up work; the current local upstream fixture inventory is covered by CLI conversion tests.

## Build

```bash
mvn test
```

```bash
mvn package
```

The shaded CLI jar and sources jar are produced under `target/`.

## CLI

Current entrypoint:

```bash
java -jar target/miku-xlsx2md-1.3.0.jar --help
```

The CLI validates the main option set used by the upstream Node.js CLI and can write combined Markdown or ZIP export outputs.

Node / Java Markdown byte-level comparison can be run after `mvn package` and upstream `npm install`:

```bash
scripts/compare-node-java-markdown.sh
```

By default, the comparison starts with `xlsx2md-basic-sample01.xlsx` and `link/hyperlink-basic-sample01.xlsx`. Additional fixture paths can be passed relative to upstream `tests/fixtures/`.

Directory batch conversion is available as a Java-side CLI extension:

```bash
java -jar target/miku-xlsx2md-1.3.0.jar \
  --input-directory docs/xlsx \
  --output-directory docs/md \
  --recursive \
  --verbose
```

When `--output-directory` is omitted, Markdown files are written next to the input `.xlsx` files. `--out` and `--zip` are not available with `--input-directory`. `--verbose` prints the workbook path being processed to stderr.

## Maven Plugin

Maven plugin support has moved to the separated
`miku-xlsx2md-java-maven` repository. This repository now owns the Java
runtime, CLI, reusable conversion APIs, runtime tests, and runtime jar
packaging only.

## Release

GitHub Actions release workflow:

```bash
.github/workflows/release.yml
```

When a `v*` tag is pushed, or when the workflow is started manually with `tag_name`, the workflow checks the tag against `pom.xml`, runs `mvn -B verify`, smoke-tests the runtime jar with Java 8, and attaches the shaded runtime jar plus sources jar from `target/` to the GitHub Release page.
