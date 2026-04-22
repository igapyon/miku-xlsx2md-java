# TODO

Document version: `2026-04-22`

## Fixed Direction

- `docs/miku-straight-conversion-guide.md` を正本の共通原則として扱う
- Java 版は Maven multi-module 構成で進める
- runtime / CLI は `miku-xlsx2md` module に置く
- Maven plugin は `miku-xlsx2md-maven-plugin` module に置く
- straight conversion の過程では新機能を追加しない

## Current Queue

- `zip-io.ts` を Java へ移植する
- `xml-utils.ts` を Java へ移植する
- workbook loader の最小入口を追加する
- CLI と Maven plugin の接続先となる core API facade を整える
- fixture を使う workbook parse 単位の focused regression を追加する

## Next Queue

- worksheet / workbook XML parsing を段階的に移植する
- shared strings / styles / rels を接続する
- markdown export の最小 round-trip を作る
- Maven plugin を runtime core に接続する

## Done In This Step

- Maven multi-module 構成へ移行した
- `miku-xlsx2md` runtime module を作成した
- `miku-xlsx2md-maven-plugin` module を追加した
- `.mvn/jvm.config` を追加した
- `markdown-options.ts` を Java へ移植した
- `text-encoding.ts` を Java へ移植した
- plugin skeleton を追加した
- docs を module 構成へ更新した
- `mvn -o test` を通した

## Notes

- `.mvn/jvm.config` は sibling repo の設定を参考に通信安定化の前提として置く
- plugin 化は runtime 実装を core として呼び出す構成を前提に進める
