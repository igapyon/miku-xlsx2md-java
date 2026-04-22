# TODO

Document version: `2026-04-22`

## Fixed Direction

- `docs/miku-straight-conversion-guide.md` を正本の共通原則として扱う
- Java 版は Maven multi-module 構成で進める
- runtime / CLI は `miku-xlsx2md` module に置く
- Maven plugin は `miku-xlsx2md-maven-plugin` module に置く
- straight conversion の過程では新機能を追加しない

## Current Queue

- `sheet-markdown.ts` と `worksheet-tables.ts` の最小移植を進める
- markdown export を core facade へ接続する
- `cell-format.ts` を移植して display value の責務を `WorksheetParser` から分離する

## Next Queue

- CLI と Maven plugin の接続先となる core API facade を広げる
- sheet / workbook artifact 組み立てを進める
- worksheet parser の coverage を広げる
- Maven plugin を runtime core に接続する

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
- `core.ts` の最小 facade を Java へ追加した
- `markdown-table-escape.ts` を Java へ移植した
- `markdown-export.ts` を Java へ移植した
- upstream fixture を使う workbook parse focused regression を追加した
- plugin skeleton を追加した
- docs を module 構成へ更新した
- `mvn -o test` を通した

## Notes

- `.mvn/jvm.config` は sibling repo の設定を参考に通信安定化の前提として置く
- plugin 化は runtime 実装を core として呼び出す構成を前提に進める
