# miku-xlsx2md-java

`miku-xlsx2md-java` is the Java port of [`igapyon/miku-xlsx2md`](https://github.com/igapyon/miku-xlsx2md).

This repository was created as an experimental generative-AI-driven development effort to port the original Node / browser implementation to Java. The generative AI prompts used during development are recorded in the commit log.

Links:

- Original web app: <https://igapyon.github.io/miku-xlsx2md/>
- Original repository: <https://github.com/igapyon/miku-xlsx2md>

## What is this?

`miku-xlsx2md-java` converts Excel (`.xlsx`) workbooks into Markdown from Java.

- Runs as a Java CLI jar
- Converts a whole workbook without sheet-by-sheet manual work
- Extracts prose, tables, images, charts, shapes, links, rich text, and formula-derived values where supported
- Can write Markdown or ZIP output from the CLI
- Includes a Maven plugin for build-time conversion

The goal is the same as the Node / browser version: extract workbook content as meaningful Markdown, not reproduce Excel's visual appearance exactly.

## Features

- Reads `.xlsx` files from the local filesystem
- Converts all sheets in a workbook in one pass
- Converts workbook content without sheet-by-sheet manual copy-and-paste work
- Extracts prose and table-like regions
- Detects table-like regions by using borders and value groupings as cues
- Handles spreadsheet-grid-style sheets with balanced, border, or planner-aware table detection
- Preserves supported Excel rich text in `github` formatting mode
- Preserves external links and workbook-internal links as Markdown links when supported
- Prefers cached formula values and parses formulas when needed
- Extracts chart configuration data
- Extracts shape source data as text and outputs SVG when supported
- Extracts images as Markdown plus assets in ZIP output
- Supports batch conversion from the Java CLI
- Supports Maven plugin goals for single-file and directory conversion

## Feature Support Overview

| Item | Java version status | Notes |
| --- | --- | --- |
| Read `.xlsx` files | Supported | Runs from the local Java runtime |
| Convert a whole workbook in one pass | Supported | Processes all sheets together |
| Convert without manual copy-and-paste work | Supported | Does not assume sheet-by-sheet manual handling |
| Extract prose | Supported | Targets descriptive text as well as tables |
| Extract tables | Supported | Detects table-like regions using borders and value groupings |
| Handle spreadsheet-grid-style sheets | Supported | `balanced`, `border`, and `planner-aware` table detection modes are available |
| Extract images | Supported | Available through Markdown plus assets, especially ZIP export |
| Preserve rich text | Partially supported | `github` formatting mode preserves supported rich text output |
| Preserve hyperlinks | Partially supported | External links and workbook-internal links are emitted as Markdown links when supported |
| Handle formula cells | Supported | Prefers cached values and derives results by parsing formulas when needed |
| Handle charts | Supported | Extracts semantic information rather than reproducing chart images |
| Handle shapes | Partially supported | Extracts source-oriented data and outputs SVG when supported |
| Save output as ZIP | Supported | CLI can write Markdown and assets together |
| Run batch conversion from CLI | Supported | Directory conversion is available from the Java CLI |
| Run from Maven | Supported | Maven plugin provides `convert` and `convert-directory` goals |
| Reproduce Excel appearance exactly | Not supported | The goal is meaningful Markdown conversion, not visual fidelity |

## Use Cases

- Convert Excel workbooks into Markdown for generative AI input
- Extract prose, tables, and images into a reusable text-based format
- Process an entire workbook without manual work on each sheet
- Run conversion locally from Java-based workflows
- Convert `.xlsx` files during Maven builds or release processes

## Requirements

- Java 8 or later
- Maven, when building from source or using the Maven plugin locally

## Build

```bash
mvn package
```

The executable CLI jar is produced under `miku-xlsx2md/target/`.

## Java CLI

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar <input.xlsx> --out output.md
```

ZIP export is also available.

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar <input.xlsx> --zip output.zip
```

Directory batch conversion is available as a Java-side CLI extension.

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  --input-directory path/to/xlsx \
  --output-directory path/to/markdown \
  --recursive \
  --verbose
```

When `--output-directory` is omitted, Markdown files are written next to the input `.xlsx` files. `--out` and `--zip` are not available with `--input-directory`. `--verbose` prints the workbook path being processed to stderr.

### CLI Options

Defaults are aligned with the original GUI: `display`, `github`, `balanced`, and shape details `exclude`.

- `--input-directory <dir>`: Convert `.xlsx` files under this directory
- `--output-directory <dir>`: Write directory conversion output under this directory
- `--recursive`: Scan input directory recursively
- `--out <file>`: Write combined Markdown to a file
- `--zip <file>`: Write ZIP export to a file
- `--output-mode <mode>`: `display`, `raw`, or `both`
- `--formatting-mode <mode>`: `plain` or `github`
- `--table-detection-mode <mode>`: `balanced`, `border`, or `planner-aware`
- `--encoding <value>`: `utf-8`, `shift_jis`, `utf-16le`, `utf-16be`, `utf-32le`, or `utf-32be`
- `--bom <value>`: `off` or `on`
- `--shape-details <mode>`: `include` or `exclude`
- `--include-shape-details`: Alias for `--shape-details include`
- `--no-header-row`: Do not treat the first row as a table header
- `--no-trim-text`: Preserve surrounding whitespace
- `--keep-empty-rows`: Keep empty rows
- `--keep-empty-columns`: Keep empty columns
- `--summary`: Print per-sheet summary to stdout
- `--verbose`: Print processing file paths to stderr
- `--help`: Show help and exit

Exit codes:

- `0`: Success
- `1`: Error

You can switch the Markdown output mode or include shape source details.

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/shape-workbook.xlsx \
  --out shape.md \
  --output-mode both \
  --shape-details include
```

You can switch how Excel text emphasis is rendered. `github` formatting mode preserves supported rich text output and emits hyperlinks as Markdown links when supported.

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/rich-text-workbook.xlsx \
  --out rich.md \
  --formatting-mode github
```

You can also switch table detection behavior. `balanced` keeps the generic heuristic, `border` detects tables from bordered regions and suppresses borderless fallback detection, and `planner-aware` adds planner/calendar-specific suppression heuristics for layout-heavy sheets.

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/table-workbook.xlsx \
  --out table.md \
  --table-detection-mode border
```

You can control output encoding and BOM. `shift_jis` does not allow BOM.

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/input.xlsx \
  --out xlsx2md-utf16be.md \
  --encoding utf-16be \
  --bom on
```

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/input.xlsx \
  --out xlsx2md-sjis.md \
  --encoding shift_jis
```

## Maven Plugin

The Maven plugin provides a `convert` goal for one workbook and a `convert-directory` goal for batch conversion.

The plugin commands below require `miku-xlsx2md-maven-plugin` to be available from the local Maven repository or another configured Maven repository.

Single-file conversion:

```bash
mvn -N jp.igapyon:miku-xlsx2md-maven-plugin:0.9.0:convert \
  -Dmiku-xlsx2md.inputFile=path/to/input.xlsx \
  -Dmiku-xlsx2md.outputFile=path/to/output.md
```

Directory conversion:

```bash
mvn -N jp.igapyon:miku-xlsx2md-maven-plugin:0.9.0:convert-directory \
  -Dmiku-xlsx2md.inputDirectory=path/to/xlsx \
  -Dmiku-xlsx2md.outputDirectory=path/to/markdown \
  -Dmiku-xlsx2md.recursive=false \
  -Dmiku-xlsx2md.verbose=true
```

When `outputDirectory` is omitted, Markdown files are written next to the input `.xlsx` files. The directory goal does not support ZIP output.

### Maven Plugin Parameters

- `miku-xlsx2md.inputFile`: Input workbook for `convert`
- `miku-xlsx2md.outputFile`: Output Markdown file for `convert`
- `miku-xlsx2md.inputDirectory`: Input directory for `convert-directory`
- `miku-xlsx2md.outputDirectory`: Output directory for `convert-directory`
- `miku-xlsx2md.recursive`: Recursively scan directories for `convert-directory`
- `miku-xlsx2md.outputMode`: `display`, `raw`, or `both`
- `miku-xlsx2md.formattingMode`: `plain` or `github`
- `miku-xlsx2md.tableDetectionMode`: `balanced`, `border`, or `planner-aware`
- `miku-xlsx2md.encoding`: `utf-8`, `shift_jis`, `utf-16le`, `utf-16be`, `utf-32le`, or `utf-32be`
- `miku-xlsx2md.bom`: `off` or `on`
- `miku-xlsx2md.skip`: Skip plugin execution
- `miku-xlsx2md.verbose`: Log workbook paths being processed

## Development

Run tests:

```bash
mvn test
```

Run the Maven plugin smoke check:

```bash
sh scripts/smoke-maven-plugin.sh
```

Node / Java Markdown byte-level comparison can be run after `mvn package` and upstream `npm install`:

```bash
scripts/compare-node-java-markdown.sh
```

By default, the comparison starts with `xlsx2md-basic-sample01.xlsx` and `link/hyperlink-basic-sample01.xlsx`. Additional fixture paths can be passed relative to upstream `tests/fixtures/`.

For implementation status and porting notes, see [docs/development-status.md](docs/development-status.md).

## Documents

- [docs/development-status.md](docs/development-status.md)
- [docs/miku-straight-conversion-guide.md](docs/miku-straight-conversion-guide.md)
- [docs/upstream-class-mapping.md](docs/upstream-class-mapping.md)
- [docs/upstream-test-mapping.md](docs/upstream-test-mapping.md)
- [docs/remaining-items.md](docs/remaining-items.md)
- [docs/follow-up-log.md](docs/follow-up-log.md)
- [docs/generative-ai-prompt-records.md](docs/generative-ai-prompt-records.md)
- [TODO.md](TODO.md)

## License

- Released under the Apache License 2.0
- See [LICENSE](LICENSE) for the full license text

--------------------------------------------------------------------------------

# miku-xlsx2md-java

`miku-xlsx2md-java` は [`igapyon/miku-xlsx2md`](https://github.com/igapyon/miku-xlsx2md) の Java 移植版です。

この repository は、元の Node / browser 実装を Java へ移植する過程で、実験的に生成AI駆動開発を行って作成しました。開発時に与えた生成AIプロンプトは、すべてコミットログに記録しています。

リンク:

- 元の Web app: <https://igapyon.github.io/miku-xlsx2md/>
- 元の Repository: <https://github.com/igapyon/miku-xlsx2md>

## これは何?

`miku-xlsx2md-java` は、Java から Excel (`.xlsx`) ブックを Markdown に変換するツールです。

- Java CLI jar として実行できます
- Excel ブック全体を、シートごとの手作業なしで変換します
- 対応範囲内で、地の文・表・画像・グラフ・図形・リンク・rich text・数式由来の値を抽出します
- CLI から Markdown または ZIP を出力できます
- Maven plugin によりビルド時変換もできます

目的は Node / browser 版と同じく、Excel の見た目を完全再現することではなく、ブック内の情報を意味のある Markdown として取り出すことです。

## 機能

- ローカルファイルシステム上の `.xlsx` ファイルを読み込み
- 全シートをまとめて一括変換
- シートごとの手作業やコピペを前提にせず変換
- 地の文と表らしい領域を抽出
- 罫線や値のまとまりを手がかりに表らしい領域を検知
- `balanced` / `border` / `planner-aware` の table detection mode に対応
- `github` formatting mode で対応する Excel rich text を反映
- 外部リンクやブック内リンクを、対応できる範囲で Markdown リンクとして出力
- 数式は保存済みの値を優先し、必要に応じて数式も解析
- グラフ設定情報を抽出
- 図形の元データをテキストとして抽出し、対応できるものは SVG も出力
- ZIP 出力で画像を Markdown と assets の組み合わせとして出力
- Java CLI からのディレクトリ一括変換に対応
- Maven plugin の single-file / directory conversion に対応

## 主な機能の対応状況

| 項目 | Java 版の状況 | 補足 |
| --- | --- | --- |
| `.xlsx` を読み込める | 対応 | ローカル Java runtime から実行 |
| ブック全体を一括変換できる | 対応 | 全シートをまとめて処理 |
| 人手を介さずに変換できる | 対応 | シートごとの手作業やコピペを前提にしない |
| 地の文を抽出できる | 対応 | 表だけでなく説明文や本文も対象 |
| 表を抽出できる | 対応 | 罫線や値のまとまりを手がかりに表らしい領域を検知 |
| Excel 方眼紙っぽいシートを扱える | 対応 | `balanced` / `border` / `planner-aware` を選択可能 |
| 画像を抽出できる | 対応 | Markdown と assets の組み合わせ、特に ZIP 出力で利用 |
| rich text を反映できる | 一部対応 | `github` formatting mode で対応する rich text 出力を反映 |
| ハイパーリンクを反映できる | 一部対応 | 外部リンクやブック内リンクを Markdown リンクとして出力 |
| 数式セルを扱える | 対応 | 保存済みの値を優先し、値がない場合は可能な範囲で解析 |
| グラフを扱える | 対応 | 画像再現ではなく、意味情報として抽出 |
| 図形を扱える | 一部対応 | raw 寄りの情報を抽出し、対応できるものは SVG として出力 |
| ZIP でまとめて保存できる | 対応 | CLI から Markdown と assets をまとめて保存 |
| CLI から一括変換できる | 対応 | Java CLI でディレクトリ変換が可能 |
| Maven から実行できる | 対応 | `convert` / `convert-directory` goal を提供 |
| Excel の見た目を完全再現できる | 非対応 | 目的は見た目再現ではなく、意味のある Markdown 化 |

## 用途

- Excel ブックの内容を、生成AI に渡しやすい Markdown に変換したい
- 地の文・表・画像をまとめて抽出し、再利用しやすい形にしたい
- シートごとの手作業なしで、ブック全体を一括処理したい
- Java ベースの処理やバッチからローカル変換したい
- Maven build や release process の中で `.xlsx` を Markdown 化したい

## 必要環境

- Java 8 以降
- ソースから build する場合、または Maven plugin をローカル利用する場合は Maven

## Build

```bash
mvn package
```

実行可能な CLI jar は `miku-xlsx2md/target/` 以下に生成されます。

## Java CLI

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar <input.xlsx> --out output.md
```

ZIP 出力も利用できます。

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar <input.xlsx> --zip output.zip
```

Java CLI 独自拡張として、ディレクトリ一括変換も利用できます。

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  --input-directory path/to/xlsx \
  --output-directory path/to/markdown \
  --recursive \
  --verbose
```

`--output-directory` を省略した場合、Markdown ファイルは入力 `.xlsx` ファイルの隣に出力されます。`--input-directory` 指定時は `--out` と `--zip` は利用できません。`--verbose` は処理中の workbook path を stderr に出力します。

### CLI オプション

既定値は元の GUI と揃えてあり、`display` / `github` / `balanced` / shape details `exclude` です。

- `--input-directory <dir>`: 指定ディレクトリ以下の `.xlsx` ファイルを変換
- `--output-directory <dir>`: ディレクトリ変換の出力先
- `--recursive`: 入力ディレクトリを再帰的に走査
- `--out <file>`: 結合済み Markdown をファイルへ出力
- `--zip <file>`: ZIP をファイルへ出力
- `--output-mode <mode>`: `display` / `raw` / `both`
- `--formatting-mode <mode>`: `plain` / `github`
- `--table-detection-mode <mode>`: `balanced` / `border` / `planner-aware`
- `--encoding <value>`: `utf-8` / `shift_jis` / `utf-16le` / `utf-16be` / `utf-32le` / `utf-32be`
- `--bom <value>`: `off` / `on`
- `--shape-details <mode>`: `include` / `exclude`
- `--include-shape-details`: `--shape-details include` の alias
- `--no-header-row`: 先頭行を表ヘッダーとして扱わない
- `--no-trim-text`: 前後の空白を維持する
- `--keep-empty-rows`: 空行を維持する
- `--keep-empty-columns`: 空列を維持する
- `--summary`: シートごとのサマリーを標準出力に表示
- `--verbose`: 処理中の file path を stderr に出力
- `--help`: ヘルプを表示して終了

終了コード:

- `0`: 成功
- `1`: エラー

Markdown output mode の切り替えや shape source details の出力もできます。

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/shape-workbook.xlsx \
  --out shape.md \
  --output-mode both \
  --shape-details include
```

Excel text emphasis の出力方法も切り替えられます。`github` formatting mode では、対応する rich text 出力を反映し、対応できる hyperlink は Markdown link として出力します。

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/rich-text-workbook.xlsx \
  --out rich.md \
  --formatting-mode github
```

table detection behavior も切り替えられます。`balanced` は汎用 heuristic、`border` は罫線領域からの検出、`planner-aware` は planner / calendar 系 layout-heavy sheet 向けの抑制 heuristic を追加します。

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/table-workbook.xlsx \
  --out table.md \
  --table-detection-mode border
```

出力 encoding と BOM も制御できます。`shift_jis` は BOM を許可しません。

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/input.xlsx \
  --out xlsx2md-utf16be.md \
  --encoding utf-16be \
  --bom on
```

```bash
java -jar miku-xlsx2md/target/miku-xlsx2md-0.9.0.jar \
  path/to/input.xlsx \
  --out xlsx2md-sjis.md \
  --encoding shift_jis
```

## Maven Plugin

Maven plugin は、1 workbook 用の `convert` goal と、ディレクトリ一括変換用の `convert-directory` goal を提供します。

以下の plugin command は、`miku-xlsx2md-maven-plugin` が local Maven repository または設定済み Maven repository から解決できることを前提とします。

単一ファイル変換:

```bash
mvn -N jp.igapyon:miku-xlsx2md-maven-plugin:0.9.0:convert \
  -Dmiku-xlsx2md.inputFile=path/to/input.xlsx \
  -Dmiku-xlsx2md.outputFile=path/to/output.md
```

ディレクトリ一括変換:

```bash
mvn -N jp.igapyon:miku-xlsx2md-maven-plugin:0.9.0:convert-directory \
  -Dmiku-xlsx2md.inputDirectory=path/to/xlsx \
  -Dmiku-xlsx2md.outputDirectory=path/to/markdown \
  -Dmiku-xlsx2md.recursive=false \
  -Dmiku-xlsx2md.verbose=true
```

`outputDirectory` を省略した場合、Markdown ファイルは入力 `.xlsx` ファイルの隣に出力されます。directory goal は ZIP 出力に対応していません。

### Maven Plugin Parameters

- `miku-xlsx2md.inputFile`: `convert` の入力 workbook
- `miku-xlsx2md.outputFile`: `convert` の出力 Markdown file
- `miku-xlsx2md.inputDirectory`: `convert-directory` の入力 directory
- `miku-xlsx2md.outputDirectory`: `convert-directory` の出力 directory
- `miku-xlsx2md.recursive`: `convert-directory` で再帰的に走査
- `miku-xlsx2md.outputMode`: `display` / `raw` / `both`
- `miku-xlsx2md.formattingMode`: `plain` / `github`
- `miku-xlsx2md.tableDetectionMode`: `balanced` / `border` / `planner-aware`
- `miku-xlsx2md.encoding`: `utf-8` / `shift_jis` / `utf-16le` / `utf-16be` / `utf-32le` / `utf-32be`
- `miku-xlsx2md.bom`: `off` / `on`
- `miku-xlsx2md.skip`: plugin execution を skip
- `miku-xlsx2md.verbose`: 処理中の workbook path を log 出力

## 開発

test 実行:

```bash
mvn test
```

Maven plugin smoke check:

```bash
sh scripts/smoke-maven-plugin.sh
```

Node / Java Markdown byte-level comparison は、`mvn package` と upstream 側の `npm install` 後に実行できます。

```bash
scripts/compare-node-java-markdown.sh
```

既定では `xlsx2md-basic-sample01.xlsx` と `link/hyperlink-basic-sample01.xlsx` から比較を開始します。追加 fixture path は upstream `tests/fixtures/` からの相対 path で指定できます。

実装状況や移植メモは [docs/development-status.md](docs/development-status.md) を参照してください。

## Documents

- [docs/development-status.md](docs/development-status.md)
- [docs/miku-straight-conversion-guide.md](docs/miku-straight-conversion-guide.md)
- [docs/upstream-class-mapping.md](docs/upstream-class-mapping.md)
- [docs/upstream-test-mapping.md](docs/upstream-test-mapping.md)
- [docs/remaining-items.md](docs/remaining-items.md)
- [docs/follow-up-log.md](docs/follow-up-log.md)
- [docs/generative-ai-prompt-records.md](docs/generative-ai-prompt-records.md)
- [TODO.md](TODO.md)

## License

- Apache License 2.0 で公開
- 詳細は [LICENSE](LICENSE) を参照
