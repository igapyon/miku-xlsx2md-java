#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
WORK_DIR="$ROOT_DIR/target/maven-plugin-smoke"
XLSX_DIR="$WORK_DIR/xlsx"
INPUT_FILE="$WORK_DIR/smoke.xlsx"
OUTPUT_FILE="$WORK_DIR/smoke.md"

mkdir -p "$WORK_DIR"
rm -f "$INPUT_FILE" "$OUTPUT_FILE"
rm -rf "$XLSX_DIR"
mkdir -p "$XLSX_DIR/xl/_rels" "$XLSX_DIR/xl/worksheets"

printf '%s' '<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Sheet1" r:id="rId1"/></sheets></workbook>' > "$XLSX_DIR/xl/workbook.xml"
printf '%s' '<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Target="worksheets/sheet1.xml"/></Relationships>' > "$XLSX_DIR/xl/_rels/workbook.xml.rels"
printf '%s' '<?xml version="1.0" encoding="UTF-8"?><sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><si><t>Hello</t></si></sst>' > "$XLSX_DIR/xl/sharedStrings.xml"
printf '%s' '<?xml version="1.0" encoding="UTF-8"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><borders count="1"><border><left/><right/><top/><bottom/></border></borders><cellXfs count="1"><xf numFmtId="0" borderId="0" fontId="0"/></cellXfs></styleSheet>' > "$XLSX_DIR/xl/styles.xml"
printf '%s' '<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData><row r="1"><c r="A1" t="s"><v>0</v></c></row></sheetData></worksheet>' > "$XLSX_DIR/xl/worksheets/sheet1.xml"
( cd "$XLSX_DIR" && jar cf "$INPUT_FILE" xl )

( cd "$ROOT_DIR" && mvn -DskipTests install )
( cd "$ROOT_DIR" && mvn -N jp.igapyon:miku-xlsx2md-maven-plugin:0.9.0:convert \
  -Dmiku-xlsx2md.inputFile="$INPUT_FILE" \
  -Dmiku-xlsx2md.outputFile="$OUTPUT_FILE" \
  -Dmiku-xlsx2md.outputMode=both \
  -Dmiku-xlsx2md.formattingMode=github \
  -Dmiku-xlsx2md.tableDetectionMode=border \
  -Dmiku-xlsx2md.encoding=utf-8 \
  -Dmiku-xlsx2md.bom=off )

test -f "$OUTPUT_FILE"
grep -Fq "# Book: smoke.xlsx" "$OUTPUT_FILE"
grep -Fq "Hello [raw=0]" "$OUTPUT_FILE"

printf '%s\n' "Maven plugin smoke passed: target/maven-plugin-smoke/smoke.md"
