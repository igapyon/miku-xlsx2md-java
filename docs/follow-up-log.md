# Follow-up Log

Document version: `2026-04-23`

## 2026-04-23 Release Asset Workflow and Fixture Follow-up

upstream file:
- `tests/fixtures/named-range/named-range-sample01.xlsx`
- `tests/fixtures/narrative/narrative-vs-table-sample01.xlsx`
- `tests/fixtures/chart/chart-basic-sample01.xlsx`
- `tests/fixtures/table/table-border-priority-sample01.xlsx`
- `tests/fixtures/rich/rich-text-github-sample01.xlsx`
- `tests/fixtures/merge/merge-pattern-sample01.xlsx`

java classes:
- none

tests:
- `SheetMarkdownTest`
- `MikuXlsx2mdCliTest`
- `MikuXlsx2mdMojoTest`

release workflow:
- `.github/workflows/release.yml`

diff summary:
- 挙動差分:
  - runtime 実装差分はなし
  - `SheetMarkdownTest` の upstream fixture parity coverage を named-range / narrative / chart-basic / table-border-priority へ拡張
  - CLI / Maven plugin fixture conversion coverage を rich-text-github / merge-pattern へ拡張
  - GitHub Actions release workflow を追加し、`v*` tag push または manual dispatch で shaded runtime jar を GitHub Release asset へ添付
- 命名差分:
  - release asset は `miku-xlsx2md/target/miku-xlsx2md-*.jar` を対象にし、`original-*.jar` は除外
- 未移植差分:
  - upstream に対応する release workflow は未確認
  - actual GitHub tag 上での release workflow 実行は未確認

follow-up:
- 実施した確認:
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest,MikuXlsx2mdCliTest test` pass
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass
  - `mvn package` pass

## 2026-04-23 Verbose Processing Diagnostics and 0.9.0 Version

upstream file:
- none

java classes:
- `CliOptions`
- `MikuXlsx2mdCli`
- `DirectoryConverter`
- `MikuXlsx2mdMojo`
- `ConvertDirectoryMojo`

tests:
- `MikuXlsx2mdCliTest`
- `DirectoryConverterTest`
- `MikuXlsx2mdMojoTest`
- `ConvertDirectoryMojoTest`

diff summary:
- 挙動差分:
  - Java CLI に `--verbose` を追加し、処理中 workbook path を stderr へ出力
  - `DirectoryConverter` に progress listener を追加し、directory batch conversion の処理中 workbook path を通知
  - Maven plugin の `convert` / `convert-directory` goal に `miku-xlsx2md.verbose` を追加し、処理中 workbook path を Maven log へ出力
  - Maven project version と README / smoke script の利用例を `0.9.0` へ更新
- 命名差分:
  - CLI は `--verbose`、Maven plugin は `miku-xlsx2md.verbose` を使用
- 未移植差分:
  - upstream に対応する verbose option は未確認

follow-up:
- 実施した確認:
  - `mvn -pl miku-xlsx2md,miku-xlsx2md-maven-plugin -am -Dtest=DirectoryConverterTest,MikuXlsx2mdCliTest,ConvertDirectoryMojoTest,MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass
  - `sh scripts/smoke-maven-plugin.sh` pass
  - `mvn test` pass

## 2026-04-23 Fixture Parity Expansion

upstream file:
- `tests/fixtures/display/display-format-sample01.xlsx`
- `tests/fixtures/link/hyperlink-basic-sample01.xlsx`
- `tests/fixtures/rich/rich-usecase-sample01.xlsx`
- `tests/fixtures/rich/rich-markdown-escape-sample01.xlsx`
- `tests/fixtures/merge/merge-multiline-sample01.xlsx`
- `tests/fixtures/merge/merge-pattern-sample01.xlsx`
- `tests/fixtures/formula/formula-basic-sample01.xlsx`
- `tests/fixtures/formula/formula-spill-sample01.xlsx`
- `tests/fixtures/chart/chart-basic-sample01.xlsx`
- `tests/fixtures/chart/chart-mixed-sample01.xlsx`

java classes:
- none

tests:
- `SheetMarkdownTest`
- `MikuXlsx2mdCliTest`
- `MikuXlsx2mdMojoTest`

diff summary:
- 挙動差分:
  - runtime 実装差分はなし
  - upstream fixture parity coverage を `SheetMarkdownTest` へ display / hyperlink / rich / merge / formula / chart 方向に拡張
  - CLI / Maven plugin fixture conversion coverage を rich markdown escape / formula basic / formula spill / chart mixed 方向へ拡張
- 命名差分:
  - `rich-markdown-escape-sample01.xlsx` の sheet name `rich_escape` に合わせ、Java 側 sanitized markdown filename は `rich-markdown-escape-sample01_001_rich_escape.md`
- 未移植差分:
  - formula-crosssheet / formula-shared / image-basic-sample01 / edge-empty の CLI / Maven plugin 横展開は後続の `Formula and Edge Fixture Follow-up` で対応済み

follow-up:
- 実施した確認:
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest,MikuXlsx2mdCliTest test` pass
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass
  - `mvn test` pass

## 2026-04-23 Formula and Edge Fixture Follow-up

upstream file:
- `tests/fixtures/formula/formula-crosssheet-sample01.xlsx`
- `tests/fixtures/formula/formula-shared-sample01.xlsx`
- `tests/fixtures/image/image-basic-sample01.xlsx`
- `tests/fixtures/edge/edge-empty-sample01.xlsx`

java classes:
- none

tests:
- `WorksheetParserTest`
- `MikuXlsx2mdCliTest`
- `MikuXlsx2mdMojoTest`

diff summary:
- 挙動差分:
  - runtime 実装差分はなし
  - `WorksheetParserTest` の upstream formula fixture coverage に value type / raw value / formula type / cached value metadata assertions を追加
  - CLI / Maven plugin fixture conversion coverage を formula-crosssheet / formula-shared / image-basic-sample01 / edge-empty へ拡張
- 命名差分:
  - なし
- 未移植差分:
  - 追加の CLI / Maven plugin fixture 横展開候補は未確認

follow-up:
- 実施した確認:
  - `mvn -pl miku-xlsx2md -Dtest=WorksheetParserTest,MikuXlsx2mdCliTest test` pass
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass
  - `mvn test` pass

## 2026-04-23 Sheet Markdown and Smoke Follow-up

upstream file:
- `tests/fixtures/formula/formula-crosssheet-sample01.xlsx`
- `tests/fixtures/formula/formula-shared-sample01.xlsx`
- `tests/fixtures/image/image-basic-sample01.xlsx`
- `tests/fixtures/edge/edge-empty-sample01.xlsx`

java classes:
- none

tests:
- `SheetMarkdownTest`
- `scripts/smoke-maven-plugin.sh`

diff summary:
- 挙動差分:
  - runtime 実装差分はなし
  - `SheetMarkdownTest` の upstream fixture parity coverage を formula-crosssheet / formula-shared / image-basic-sample01 / edge-empty へ拡張
  - Maven plugin smoke script に full-coordinate `convert-directory` coverage を追加
  - local upstream fixture inventory を確認し、この時点の追加 fixture 候補は未確認
- 命名差分:
  - なし
- 未移植差分:
  - 追加の fixture 横展開候補は未確認

follow-up:
- 実施した確認:
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass
  - `sh scripts/smoke-maven-plugin.sh` pass
  - `mvn test` pass

## 2026-04-23 Shared Directory Batch Conversion

upstream file:
- none

java classes:
- `DirectoryConverter`
- `CliOptions`
- `MikuXlsx2mdCli`
- `ConvertDirectoryMojo`

tests:
- `DirectoryConverterTest`
- `MikuXlsx2mdCliTest`
- `ConvertDirectoryMojoTest`
- `MikuXlsx2mdMojoTest`

diff summary:
- 挙動差分:
  - directory batch conversion を runtime helper `DirectoryConverter` へ共通化
  - Java CLI に `--input-directory`, `--output-directory`, `--recursive` を追加
  - CLI directory mode は `--out` / `--zip` 併用を禁止
  - Maven plugin `convert-directory` goal は `DirectoryConverter` へ委譲
  - `.xlsx` のみを探索し、`outputDirectory` 省略時は入力ディレクトリへ `.md` を出力
- 命名差分:
  - CLI は upstream Node 版にない Java-side extension として directory mode を追加
- 未移植差分:
  - upstream に対応する CLI directory batch option は未確認

follow-up:
- 実施した確認:
  - `mvn -pl miku-xlsx2md,miku-xlsx2md-maven-plugin -am -Dtest=DirectoryConverterTest,MikuXlsx2mdCliTest,ConvertDirectoryMojoTest,MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass

## 2026-04-23 Maven Plugin Directory Goal

upstream file:
- none

java classes:
- `ConvertDirectoryMojo`

tests:
- `ConvertDirectoryMojoTest`

diff summary:
- 挙動差分:
  - Maven plugin に `convert-directory` goal を追加
  - `inputDirectory` 配下の `.xlsx` を一括変換し、`outputDirectory` 省略時は入力ディレクトリへ `.md` を出力
  - `recursive` は既定 `false` とし、再帰有効時は入力相対ディレクトリ構造を出力側へ維持
  - directory goal では ZIP 出力を扱わず、combined markdown のみを出力
- 命名差分:
  - 既存の単一ファイル goal `convert` は維持
- 未移植差分:
  - upstream に対応する同名 directory batch goal は未確認

follow-up:
- 実施した確認:
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest,ConvertDirectoryMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass

## 2026-04-23 Upstream Sync (`e67f2bc`)

upstream file:
- `src/ts/markdown-options.ts`
- `src/ts/table-detector.ts`
- `src/ts/sheet-markdown.ts`
- `src/ts/core.ts`
- `src/ts/markdown-export.ts`
- `src/ts/main.ts`
- `src/ts/module-registry-access.ts`
- `scripts/miku-xlsx2md-cli.mjs`
- `tests/xlsx2md-table-detector.test.js`
- `tests/xlsx2md-main.test.js`
- `tests/xlsx2md-cli.test.js`
- `tests/xlsx2md-node-runtime.test.js`

java classes:
- `MarkdownOptions`
- `TableDetector`
- `SheetMarkdown`
- `CliOptions`
- `MikuXlsx2mdCli`

tests:
- `MarkdownOptionsTest`
- `TableDetectorTest`
- `SheetMarkdownTest`
- `MikuXlsx2mdCliTest`

diff summary:
- 挙動差分:
  - upstream `planner-aware` table detection mode を Java 側 `MarkdownOptions` / `TableDetector` / `SheetMarkdown` へ反映
  - planner/calendar 向け抑制 heuristics は `planner-aware` 選択時だけ適用し、`balanced` / `border` 既存挙動は維持
  - CLI help は upstream と同様に `planner-aware` を表示し、GUI-aligned defaults 表記を追加
  - Java CLI 既定の `formattingMode` を `github` へ同期
- 命名差分:
  - `border-priority` compatibility alias は引き続き Java 側で `border` へ正規化
- 未移植差分:
  - upstream GUI HTML / browser runtime 表示変更は Java CLI / runtime の対象外

follow-up:
- 実施した確認:
  - `workplace/miku-xlsx2md` を `origin/devel` `e67f2bc` へ fast-forward
  - `mvn -pl miku-xlsx2md -Dtest=MarkdownOptionsTest,TableDetectorTest,MikuXlsx2mdCliTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=MarkdownOptionsTest,TableDetectorTest,SheetMarkdownTest,MikuXlsx2mdCliTest test` pass

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
- `src/ts/narrative-structure.ts`
- `src/ts/border-grid.ts`
- `src/ts/table-detector.ts`
- `src/ts/sheet-assets.ts`
- `src/ts/office-drawing.ts`
- `src/ts/rich-text-parser.ts`
- `src/ts/rich-text-plain-formatter.ts`
- `src/ts/rich-text-github-formatter.ts`
- `src/ts/rich-text-renderer.ts`
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
- `NarrativeStructure`
- `BorderGrid`
- `TableDetector`
- `SheetAssets`
- `OfficeDrawing`
- `RichTextRenderer`
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
- `NarrativeStructureTest`
- `BorderGridTest`
- `TableDetectorTest`
- `SheetAssetsTest`
- `OfficeDrawingTest`
- `RichTextRendererTest`
- `SheetMarkdownTest`
- `MikuXlsx2mdCliTest`
- `MikuXlsx2mdMojoTest`

diff summary:
- 挙動差分:
  - CLI は option validation / help / initial workbook conversion を実装
  - Maven plugin は runtime core conversion へ接続済み
  - Maven plugin は full-coordinate smoke 実行を `scripts/smoke-maven-plugin.sh` として固定済み
  - CLI / Maven plugin は upstream fixture conversion coverage subset を追加済みで、`xlsx2md-basic` / `image-basic-sample02` / weird-sheetname / `shape-flowchart` / `shape-block-arrow` / `shape-callout` まで横展開済み
  - core fixture regression は formula basic / formula cross-sheet / formula shared / formula spill / chart basic / chart mixed / rich usecase / rich-text-github / rich-markdown-escape / merge pattern / merge-multiline / narrative / edge-empty / edge-weird-sheetname / border-priority / table-basic / grid-layout / image-basic-sample02 fixture coverage subset を追加済み
  - `sheet-markdown` は最小変換導線を実装し、sheet asset rendering / shape block grouping は `SheetAssets` へ分割・接続済み
  - advanced `sheet-markdown` parity coverage は calendar narrative / calendar sidebar ordering / empty fallback / table detection compatibility alias / line break / literal escaping / hyperlink output mode / GitHub hyperlink underline suppression / SVG-backed shape item spacing / shape details toggle / fixture-backed narrative / sparse / border-priority / broader table-basic / grid-layout / xlsx2md-basic / shape-basic / shape-flowchart / shape-block-arrow / shape-callout / image-basic-sample02 / weird-sheetname cases の subset を追加済み
  - table detection は `TableDetector` に分割し、normalized border 判定は `BorderGrid` に分離
  - `sheet-assets` は Java では rendering / shape block grouping / drawing parse helper 範囲を移植済み
  - `WorksheetParser` は drawing relationships から image / chart / shape assets を収集する導線へ接続済み
  - `WorksheetParser` は cell style / inline rich text 由来の richTextRuns、formula metadata、hyperlink range / hash location、sheet-qualified な shared formula translation、upstream formula-crosssheet / formula-shared fixture assertion expansionの coverage subset を追加済み
  - `office-drawing` は Java では shape SVG rendering helper 範囲を移植済み
  - `SheetAssets` は shape parsing 時に `OfficeDrawing` の SVG asset を接続済み
  - rich text rendering helper は Java では当面 1 class に集約し、parser / plain formatter / github formatter / renderer の責務を同一 class 内に保持
- 命名差分:
  - module registry 方式を Java static facade へ読み替え
- 未移植差分:
  - worksheet parser shared / cross-sheet formula fixture coverage expansion beyond the current focused regression subset
  - advanced `sheet-markdown` fixture parity coverage beyond the current subset
  - broader CLI / Maven plugin fixture coverage beyond the current subset
  - broader Maven plugin smoke coverage beyond the fixed minimum command
- Java 側独自拡張:
  - immutable value objects for equality-based tests

follow-up:
- 実施した確認:
  - upstream source/test inventory
  - sibling repo の multi-module / Maven plugin 構成
  - `.mvn/jvm.config` による Maven 通信前提の固定
  - Java 17 + Maven 3.9 on source/target 1.8
  - `mvn -o test` pass
  - `mvn -pl miku-xlsx2md -Dtest=NarrativeStructureTest,SheetMarkdownTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=BorderGridTest,TableDetectorTest,SheetMarkdownTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=SheetAssetsTest,SheetMarkdownTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=SheetAssetsTest,WorksheetParserTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=OfficeDrawingTest,SheetAssetsTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after image / shape fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=SheetAssetsTest,SheetMarkdownTest,CoreFixtureRegressionTest test` pass after shape block rendering connection
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass after advanced sheet-markdown parity coverage subset expansion
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass after sheet-markdown hyperlink underline suppression coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass after sheet-markdown shape item spacing coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass after sheet-markdown table detection alias coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=WorksheetParserTest test` pass after worksheet parser richTextRuns / formula metadata coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=WorksheetParserTest test` pass after worksheet parser shared formula translation coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=RichTextRendererTest,SheetMarkdownTest,MarkdownNormalizeTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after rich usecase / merge pattern fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after formula basic / chart basic fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after formula cross-sheet / shared fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after formula spill / chart mixed fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after narrative / edge-empty / border-priority fixture parity coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after table-basic / grid-layout fixture parity coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after broader table-basic fixture parity coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test` pass
  - `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test` pass after CLI table fixture alias coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test` pass after CLI shape details alias coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test` pass after CLI display / named-range / narrative fixture coverage expansion
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass after Maven plugin shape fixture coverage expansion
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass after Maven plugin border-priority fixture coverage expansion
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass after Maven plugin display / named-range / narrative fixture coverage expansion
  - `mvn -pl miku-xlsx2md,miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdCliTest,MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass after CLI / Maven plugin fixture coverage expansion
  - `mvn test` pass
  - `sh scripts/smoke-maven-plugin.sh` pass after Maven plugin full-coordinate smoke command fixation
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest,WorksheetParserTest test` pass after sheet-markdown / worksheet parser coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test` pass after rich-text / merge-multiline / weird-sheetname / image-basic-sample02 fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest,WorksheetParserTest test` pass after xlsx2md-basic / shape-flowchart / shape-block-arrow / formula fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest,MikuXlsx2mdCliTest test` pass after broader sheet-markdown / CLI fixture coverage expansion
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass after Maven plugin xlsx2md-basic / image-basic-sample02 / weird-sheetname fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test` pass after CLI shape-flowchart / shape-block-arrow fixture coverage expansion
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass after Maven plugin shape-flowchart / shape-block-arrow fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass after table-basic-sample11 / 12 / 14 / 16 fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test` pass after table-basic-sample01 / 02 / 03 and shape-basic / shape-callout fixture coverage expansion
  - `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test` pass after CLI shape-callout fixture coverage expansion
  - `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test` pass after Maven plugin shape-callout fixture coverage expansion
- fixture:
  - `workplace/miku-xlsx2md/tests/fixtures/named-range/named-range-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/link/hyperlink-basic-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/display/display-format-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/narrative/narrative-vs-table-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/edge/edge-empty-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/edge/edge-weird-sheetname-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample02.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample03.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample11.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample12.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample13.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample14.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample15.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample16.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/grid-layout-sample-01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/rich/rich-text-github-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/rich/rich-markdown-escape-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/formula/formula-basic-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/formula/formula-crosssheet-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/formula/formula-shared-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/formula/formula-spill-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/chart/chart-basic-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/chart/chart-mixed-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/rich/rich-usecase-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/merge/merge-multiline-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/merge/merge-pattern-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample02.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/shape/shape-basic-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/shape/shape-callout-sample01.xlsx`
  - `workplace/miku-xlsx2md/tests/fixtures/table/table-border-priority-sample01.xlsx`
- 次回の確認観点:
  - advanced `sheet-markdown` fixture parity coverage をさらに広げる
  - worksheet parser shared / cross-sheet formula fixture coverage を current focused regression からさらに広げる
  - CLI / Maven plugin の fixture coverage をさらに広げる
