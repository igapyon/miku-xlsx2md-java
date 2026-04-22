# Upstream Test Mapping

Document version: `2026-04-22`

## Current Coverage

### upstream test / intent:
`tests/xlsx2md-address-utils.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.addressutils.AddressUtilsTest.convertsBetweenColumnNumbersAndLetters`
- `jp.igapyon.mikuxlsx2md.addressutils.AddressUtilsTest.parsesAndNormalizesCellAndRangeAddresses`
- `jp.igapyon.mikuxlsx2md.addressutils.AddressUtilsTest.formatsDisplayRangesAndMergedRangeRefs`

fixtures:
- none

focused regression:
- `mvn -Dtest=AddressUtilsTest test`

### upstream test / intent:
`tests/xlsx2md-markdown-normalize.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalizeTest.replacesLineBreaksTabsAndControls`
- `jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalizeTest.normalizesMarkdownNewlines`
- `jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalizeTest.replacesUnsafeUnicode`
- `jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalizeTest.escapesPipesInTableCells`
- `jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalizeTest.removesHeadingAndListMarkers`

fixtures:
- none

focused regression:
- `mvn -Dtest=MarkdownNormalizeTest test`

### upstream test / intent:
`tests/xlsx2md-markdown-escape.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.markdownescape.MarkdownEscapeTest.escapesInlineMarkdownControlCharacters`
- `jp.igapyon.mikuxlsx2md.markdownescape.MarkdownEscapeTest.escapesLineStartMarkdownMarkers`
- `jp.igapyon.mikuxlsx2md.markdownescape.MarkdownEscapeTest.escapesAdditionalListMarkersAndAmpersands`

fixtures:
- none

focused regression:
- `mvn -Dtest=MarkdownEscapeTest test`

### upstream test / intent:
Node CLI option compatibility and help text shape

java tests:
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.printsHelpAndExitsSuccessfully`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.failsForUnknownOption`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.acceptsKnownOptionsButReportsUnimplementedConversion`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.rejectsShiftJisBomCombination`

fixtures:
- none

focused regression:
- `mvn -Dtest=MikuXlsx2mdCliTest test`
