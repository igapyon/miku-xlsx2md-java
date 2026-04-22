# Remaining Items

Document version: `2026-04-22`

## Current Position

Java port scaffolding is ready, and the first low-dependency utility files are straight-converted. The project is not yet able to read `.xlsx` files.

## Status

- 対応済み
  - Maven / JUnit / Java 8 baseline
  - `workplace/` workspace rule
  - upstream source / test inventory
  - `address-utils`
  - `markdown-normalize`
  - `markdown-escape`
  - CLI option vocabulary skeleton
- 保守確認
  - help text compatibility
  - focused regression command layout
- 保留
  - `markdown-options`
  - `text-encoding`
  - `zip-io`
  - `xml-utils`
  - workbook loader
  - worksheet parser
  - shared strings
  - styles parser
  - markdown export
  - end-to-end CLI conversion

## Focused Regression

- `mvn test`
- `mvn -Dtest=AddressUtilsTest test`
- `mvn -Dtest=MarkdownNormalizeTest test`
- `mvn -Dtest=MarkdownEscapeTest test`
- `mvn -Dtest=MikuXlsx2mdCliTest test`

## Last Known Result

- `mvn -o test` passed on `2026-04-22`

## Next Unit

- Port `src/ts/markdown-options.ts`
- Port `src/ts/text-encoding.ts`
- Start workbook parsing from ZIP / XML primitives
