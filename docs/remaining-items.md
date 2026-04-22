# Remaining Items

Document version: `2026-04-22`

## Current Position

Java port scaffolding is ready as a Maven multi-module project, and the first low-dependency utility files are straight-converted. The project is not yet able to read `.xlsx` files.

## Status

- 対応済み
  - Maven / JUnit / Java 8 baseline
  - `workplace/` workspace rule
  - upstream source / test inventory
  - `address-utils`
  - `markdown-normalize`
  - `markdown-escape`
  - `markdown-options`
  - `text-encoding`
  - CLI option vocabulary skeleton
  - Maven plugin skeleton
- 保守確認
  - help text compatibility
  - focused regression command layout
- 保留
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
- `mvn -pl miku-xlsx2md -Dtest=AddressUtilsTest test`
- `mvn -pl miku-xlsx2md -Dtest=MarkdownNormalizeTest test`
- `mvn -pl miku-xlsx2md -Dtest=MarkdownEscapeTest test`
- `mvn -pl miku-xlsx2md -Dtest=MarkdownOptionsTest test`
- `mvn -pl miku-xlsx2md -Dtest=TextEncodingTest test`
- `mvn -pl miku-xlsx2md -Dtest=MikuXlsx2mdCliTest test`
- `mvn -pl miku-xlsx2md-maven-plugin -Dtest=MikuXlsx2mdMojoTest test`

## Last Known Result

- `mvn -o test` passed on `2026-04-22`

## Next Unit

- Start workbook parsing from ZIP / XML primitives
- Connect Maven plugin to runtime core API once workbook conversion becomes available
