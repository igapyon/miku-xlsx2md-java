# Follow-up Log

Document version: `2026-04-22`

## 2026-04-22 Initial Straight Conversion Setup

upstream file:
- `src/ts/address-utils.ts`
- `src/ts/markdown-normalize.ts`
- `src/ts/markdown-escape.ts`
- `scripts/miku-xlsx2md-cli.mjs`

java classes:
- `AddressUtils`
- `MarkdownNormalize`
- `MarkdownEscape`
- `CliOptions`
- `MikuXlsx2mdCli`

tests:
- `AddressUtilsTest`
- `MarkdownNormalizeTest`
- `MarkdownEscapeTest`
- `MikuXlsx2mdCliTest`

diff summary:
- 挙動差分:
  - CLI は option validation と help まで実装し、workbook conversion は未移植
- 命名差分:
  - module registry 方式を Java static facade へ読み替え
- 未移植差分:
  - workbook parse
  - markdown export
  - zip export
  - summary output
- Java 側独自拡張:
  - immutable value objects for equality-based tests

follow-up:
- 実施した確認:
  - upstream source/test inventory
  - Java 17 + Maven 3.9 on source/target 1.8
  - `mvn -o test` pass
- fixture:
  - none
- 次回の確認観点:
  - `markdown-options.ts` を Java enum/constant へ移す
  - workbook parsing の最小入口を作る
