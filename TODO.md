# TODO

Document version: `2026-04-22`

## Fixed Direction

- `docs/miku-straight-conversion-guide.md` を正本の共通原則として扱う
- Java 版は Maven multi-module 構成で進める
- runtime / CLI は `miku-xlsx2md` module に置く
- Maven plugin は `miku-xlsx2md-maven-plugin` module に置く
- straight conversion の過程では新機能を追加しない

## Current Queue

- advanced `sheet-markdown` parity coverage を fixture 側へさらに広げる
  - rich text / markdown escape fixture coverage は追加済み
  - merge multiline / weird sheet name / image+chart sample02 fixture coverage は追加済み
  - xlsx2md-basic / shape-flowchart / shape-block-arrow fixture coverage は追加済み
- worksheet parser の shared / cross-sheet formula fixture coverage を upstream fixture focused regression からさらに広げる
  - formula-crosssheet / formula-shared fixture assertion expansion は追加済み

## Next Queue

- CLI / Maven plugin の fixture coverage をさらに広げる
- Maven plugin smoke coverage を必要に応じて広げる

## Done In This Step

- Maven multi-module 構成へ移行した
- `miku-xlsx2md` runtime module を作成した
- `miku-xlsx2md-maven-plugin` module を追加した
- `.mvn/jvm.config` を追加した
- `markdown-options.ts` を Java へ移植した
- `text-encoding.ts` を Java へ移植した
- `xml-utils.ts` を Java へ移植した
- `zip-io.ts` を Java へ移植した
- `rels-parser.ts` を Java へ移植した
- workbook loader の最小入口を追加した
- `shared-strings.ts` を Java へ移植した
- `styles-parser.ts` を Java へ移植した
- `worksheet-parser.ts` の最小範囲を Java へ移植した
- `WorksheetParser` の richTextRuns / formula metadata coverage を広げた
- `WorksheetParserTest` に shared formula の sheet-qualified / absolute reference translation coverage を追加した
- `core.ts` の最小 facade を Java へ追加した
- `markdown-table-escape.ts` を Java へ移植した
- `markdown-export.ts` を Java へ移植した
- `cell-format.ts` を Java へ移植した
- `worksheet-tables.ts` を Java へ移植した
- `WorksheetParser` の display value formatting を `CellFormat` へ委譲した
- `sheet-markdown.ts` の最小範囲を Java へ移植した
- `narrative-structure.ts` を Java へ移植した
- `SheetMarkdown` の narrative rendering を `NarrativeStructure` へ委譲した
- `border-grid.ts` を Java へ移植した
- `table-detector.ts` を Java へ移植した
- `SheetMarkdown` の table detection / table matrix rendering を `TableDetector` へ委譲した
- `sheet-assets.ts` の rendering / shape block grouping 範囲を Java へ移植した
- `SheetMarkdown` の asset section rendering を `SheetAssets` へ委譲した
- `SheetMarkdown` の shape block rendering を `SheetAssets` の block helper へ接続した
- advanced `sheet-markdown` parity coverage に calendar / empty fallback / line break / literal escaping / hyperlink output mode / shape details toggle cases を追加した
- `sheet-assets.ts` の `parseDrawingImages` / `parseDrawingCharts` / `parseDrawingShapes` を Java へ移植した
- `WorksheetParser` の parsed sheet 生成を sheet assets parse helper へ接続した
- `office-drawing.ts` の shape SVG rendering helper を Java へ移植した
- `SheetAssets` の shape parsing を office drawing SVG helper へ接続した
- upstream image / shape fixture を使う workbook parse focused regression を追加した
- `rich-text-*` helper 群を Java へ移植した
- `SheetMarkdown` の cell display rendering を `RichTextRenderer` へ委譲した
- table cell pipe escaping を rich-text renderer 経由でも二重エスケープしないよう調整した
- `SheetMarkdownTest` に GitHub hyperlink rendering 時の underline suppression coverage を追加した
- `SheetMarkdownTest` に consecutive SVG-backed shape items の spacing coverage を追加した
- `SheetMarkdownTest` に table detection compatibility alias coverage を追加した
- `SheetMarkdownTest` に upstream xlsx2md-basic / shape-flowchart / shape-block-arrow fixture coverage を追加した
- `WorksheetParserTest` に upstream formula-crosssheet / formula-shared fixture coverage を追加した
- markdown conversion を core facade へ接続した
- workbook-to-markdown conversion の focused fixture regression を追加した
- formula basic / chart basic fixture を使う workbook-to-markdown focused regression を追加した
- formula cross-sheet / shared fixture を使う workbook-to-markdown focused regression を追加した
- formula spill / chart mixed fixture を使う workbook-to-markdown focused regression を追加した
- rich usecase / merge pattern fixture を使う workbook-to-markdown focused regression を追加した
- rich text github / markdown escape fixture を使う workbook-to-markdown focused regression を追加した
- merge multiline fixture を使う workbook-to-markdown focused regression を追加した
- narrative / edge-empty / table border-priority fixture を使う workbook-to-markdown focused regression を追加した
- edge weird-sheetname fixture を使う workbook-to-markdown focused regression を追加した
- table-basic / grid-layout fixture を使う workbook-to-markdown focused regression を追加した
- image-basic-sample02 fixture を使う workbook parse / markdown focused regression を追加した
- CLI を runtime core conversion に接続した
- Maven plugin を runtime core conversion に接続した
- CLI / Maven plugin の upstream fixture conversion coverage を追加した
- CLI に upstream table fixture を使う `border-priority` alias coverage を追加した
- CLI に upstream shape fixture を使う `--include-shape-details` alias coverage を追加した
- CLI に upstream display / named-range / narrative fixture conversion coverage を追加した
- Maven plugin に upstream shape fixture conversion coverage を追加した
- Maven plugin に upstream table fixture を使う `border-priority` coverage を追加した
- Maven plugin に upstream display / named-range / narrative fixture conversion coverage を追加した
- Maven plugin smoke 実行方法を full-coordinate script として固定した
- `WorksheetParser` の hyperlink range / hash location coverage を追加した
- upstream fixture を使う workbook parse focused regression を追加した
- plugin skeleton を追加した
- docs を module 構成へ更新した
- `mvn -o test` を通した

## Notes

- `.mvn/jvm.config` は sibling repo の設定を参考に通信安定化の前提として置く
- plugin 化は runtime 実装を core として呼び出す構成を前提に進める
