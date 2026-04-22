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
`src/ts/markdown-options.ts` option normalization behavior

java tests:
- `jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptionsTest.normalizesModesAndBooleanDefaults`
- `jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptionsTest.keepsCompatibilityAliasForBorderPriority`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=MarkdownOptionsTest test`

### upstream test / intent:
`tests/xlsx2md-text-encoding.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.textencoding.TextEncodingTest.encodesUtf8WithoutBomByDefault`
- `jp.igapyon.mikuxlsx2md.textencoding.TextEncodingTest.encodesUtf16LeWithBomWhenRequested`
- `jp.igapyon.mikuxlsx2md.textencoding.TextEncodingTest.encodesUtf32BeWithoutBom`
- `jp.igapyon.mikuxlsx2md.textencoding.TextEncodingTest.encodesShiftJisWhenAvailable`
- `jp.igapyon.mikuxlsx2md.textencoding.TextEncodingTest.rejectsBomForShiftJis`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=TextEncodingTest test`

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

### upstream test / intent:
Maven plugin skeleton option mapping and skip behavior

java tests:
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.skipsWhenRequested`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.failsFastUntilCoreConversionIsImplemented`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md-maven-plugin -Dtest=MikuXlsx2mdMojoTest test`
