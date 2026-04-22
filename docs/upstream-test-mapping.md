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
`src/ts/markdown-table-escape.ts` escaping behavior consumed from markdown export

java tests:
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.escapesMarkdownCellsAndKeepsSpacingRules`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.rendersMarkdownTablesWithEscapedCellContent`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=MarkdownExportTest test`

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
`tests/xlsx2md-xml-utils.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.xmlutils.XmlUtilsTest.findsElementsByLocalNameAcrossNamespaces`
- `jp.igapyon.mikuxlsx2md.xmlutils.XmlUtilsTest.findsOnlyDirectChildrenForALocalName`
- `jp.igapyon.mikuxlsx2md.xmlutils.XmlUtilsTest.decodesUtf8BytesAndNormalizesCrLfTextContent`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=XmlUtilsTest test`

### upstream test / intent:
`tests/xlsx2md-zip-io.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.zipio.ZipIoTest.roundTripsStoredZipEntries`
- `jp.igapyon.mikuxlsx2md.zipio.ZipIoTest.supportsEmptyFilePayloads`
- `jp.igapyon.mikuxlsx2md.zipio.ZipIoTest.writesAFixedReproducibleZipEntryTimestamp`
- `jp.igapyon.mikuxlsx2md.zipio.ZipIoTest.doesNotMarkAsciiOnlyFileNamesWithTheUtf8Flag`
- `jp.igapyon.mikuxlsx2md.zipio.ZipIoTest.marksUtf8FileNamesSoNonAsciiEntriesUnzipCorrectly`
- `jp.igapyon.mikuxlsx2md.zipio.ZipIoTest.throwsForInvalidZipInput`
- `jp.igapyon.mikuxlsx2md.zipio.ZipIoTest.inflatesDeflatedEntries`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=ZipIoTest test`

### upstream test / intent:
`tests/xlsx2md-rels-parser.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.relsparser.RelsParserTest.normalizesZipPathsRelativeToTheSourcePath`
- `jp.igapyon.mikuxlsx2md.relsparser.RelsParserTest.buildsRelsPathsNextToTheSourceFile`
- `jp.igapyon.mikuxlsx2md.relsparser.RelsParserTest.parsesRelationshipTargetsIntoAMap`
- `jp.igapyon.mikuxlsx2md.relsparser.RelsParserTest.keepsExternalRelationshipTargetsAsIs`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=RelsParserTest test`

### upstream test / intent:
`tests/xlsx2md-workbook-loader.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoaderTest.parsesDefinedNamesAndSkipsXlnmEntries`
- `jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoaderTest.loadsWorkbookPartsAndInvokesWorksheetParsingAndPostProcessing`
- `jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoaderTest.throwsWhenWorkbookXmlIsMissing`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=WorkbookLoaderTest test`

### upstream test / intent:
`tests/xlsx2md-shared-strings.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.sharedstrings.SharedStringsTest.returnsAnEmptyListWhenSharedStringsXmlIsMissing`
- `jp.igapyon.mikuxlsx2md.sharedstrings.SharedStringsTest.collectsPlainAndRichTextRuns`
- `jp.igapyon.mikuxlsx2md.sharedstrings.SharedStringsTest.skipsPhoneticTextNodesAndNormalizesCrLfToLf`
- `jp.igapyon.mikuxlsx2md.sharedstrings.SharedStringsTest.preservesSupportedRichTextEmphasisFlags`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=SharedStringsTest test`

### upstream test / intent:
`tests/xlsx2md-styles-parser.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.stylesparser.StylesParserTest.returnsAGeneralDefaultStyleWhenStylesXmlIsMissing`
- `jp.igapyon.mikuxlsx2md.stylesparser.StylesParserTest.detectsBorderSidesFromStyleAttributesOrChildNodes`
- `jp.igapyon.mikuxlsx2md.stylesparser.StylesParserTest.parsesBordersAndCustomNumberFormatsFromStylesXml`
- `jp.igapyon.mikuxlsx2md.stylesparser.StylesParserTest.fallsBackToBuiltInFormatCodesWhenNoCustomNumFmtExists`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=StylesParserTest test`

### upstream test / intent:
`tests/xlsx2md-worksheet-parser.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.extractsSharedStringAndBooleanCellValues`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.translatesSharedFormulasAcrossRelativeReferences`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.parsesWorksheetCellsMergesAndSharedFormulas`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.parsesWorksheetHyperlinksFromLocalRefsAndExternalRelationships`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=WorksheetParserTest test`

### upstream test / intent:
connected workbook parsing path through the Java core facade

java tests:
- `jp.igapyon.mikuxlsx2md.core.CoreTest.parsesWorkbookThroughConnectedLoader`

fixtures:
- synthetic stored ZIP built in test

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=CoreTest test`

### upstream test / intent:
`tests/xlsx2md-main.test.js` fixture-based workbook parsing and workbook-to-markdown expectations

java tests:
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamNamedRangeFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamHyperlinkFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamDisplayFormatFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamDisplayFormatFixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamHyperlinkFixtureWorkbookToMarkdownWhenAvailable`

fixtures:
- `workplace/miku-xlsx2md/tests/fixtures/named-range/named-range-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/link/hyperlink-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/display/display-format-sample01.xlsx`

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=CoreFixtureRegressionTest test`

### upstream test / intent:
`tests/xlsx2md-cell-format.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.cellformat.CellFormatTest.formatsDatePercentFractionAndCurrencyValues`
- `jp.igapyon.mikuxlsx2md.cellformat.CellFormatTest.formatsSpecialTextAndScientificNotationPatterns`
- `jp.igapyon.mikuxlsx2md.cellformat.CellFormatTest.parsesDateLikeTextIntoExcelSerialCompatibleNumbers`
- `jp.igapyon.mikuxlsx2md.cellformat.CellFormatTest.appliesResolvedFormulaFormattingBackOntoACell`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=CellFormatTest test`

### upstream test / intent:
`tests/xlsx2md-worksheet-tables.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.worksheettables.WorksheetTablesTest.normalizesStructuredTableKeys`
- `jp.igapyon.mikuxlsx2md.worksheettables.WorksheetTablesTest.returnsNoTablesWhenNoTablePartsExist`
- `jp.igapyon.mikuxlsx2md.worksheettables.WorksheetTablesTest.resolvesWorksheetTablePartsThroughRelationships`
- `jp.igapyon.mikuxlsx2md.worksheettables.WorksheetTablesTest.ignoresTableDefinitionsWithInvalidRanges`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=WorksheetTablesTest test`

### upstream test / intent:
`tests/xlsx2md-markdown-export.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.normalizesLineBreaksIntoSpaces`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.rendersMarkdownTablesWithEscapedCellContent`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.escapesMarkdownCellsAndKeepsSpacingRules`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.createsSanitizedOutputFileNamesWithoutModeSuffixes`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.summarizesFormulaDiagnosticsAndTableScores`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.createsExportEntriesAndZipArchivesIncludingMarkdownAndAssets`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.createsAssetEntriesWithoutMarkdownAndSkipsIncompleteShapeSvgAssets`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.createsEncodedPayloadBytesForUtf16BeWithBom`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.writesTheBookHeadingOnlyOnceInCombinedMarkdown`
- `jp.igapyon.mikuxlsx2md.markdownexport.MarkdownExportTest.createsMarkdownExportEntryWhenMarkdownExists`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=MarkdownExportTest test`

### upstream test / intent:
`tests/xlsx2md-narrative-structure.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.narrativestructure.NarrativeStructureTest.rendersAnIndentedParentChildBlockAsHeadingPlusBullets`
- `jp.igapyon.mikuxlsx2md.narrativestructure.NarrativeStructureTest.rendersFlatNarrativeRowsAsPlainParagraphs`
- `jp.igapyon.mikuxlsx2md.narrativestructure.NarrativeStructureTest.normalizesHeadingAndListMarkersInsideHierarchicalNarrativeItems`
- `jp.igapyon.mikuxlsx2md.narrativestructure.NarrativeStructureTest.startsANewHeadingWhenIndentationReturnsToTheParentLevel`
- `jp.igapyon.mikuxlsx2md.narrativestructure.NarrativeStructureTest.detectsAHeadingBlockOnlyWhenTheSecondItemIsIndentedDeeper`
- `jp.igapyon.mikuxlsx2md.narrativestructure.NarrativeStructureTest.rendersCalendarLikeNarrativeRowsWithCellBoundariesPreserved`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=NarrativeStructureTest test`

### upstream test / intent:
`tests/xlsx2md-sheet-markdown.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.extractsNarrativeBlocksOutsideTables`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsSheetToMarkdownWithDetectedTableAndSummary`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.formatsHyperlinksRawAndBothModes`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsWorkbookThroughCoreFacadeShape`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test`

notes:
- Current Java coverage is a minimum conversion regression for the new facade.
- Upstream advanced table detection, rich text formatting, calendar grouping, and asset grouping cases remain follow-up coverage.

### upstream test / intent:
Node CLI option compatibility, help text shape, and initial conversion I/O

java tests:
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.printsHelpAndExitsSuccessfully`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.failsForUnknownOption`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.acceptsKnownOptionsAndWritesConvertedMarkdown`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.writesZipOnlyWhenZipPathIsSpecified`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.rejectsShiftJisBomCombination`

fixtures:
- none

focused regression:
- `mvn -Dtest=MikuXlsx2mdCliTest test`

### upstream test / intent:
Maven plugin option mapping, skip behavior, and initial conversion I/O

java tests:
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.skipsWhenRequested`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.writesMarkdownThroughCoreConversion`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md-maven-plugin -am -Dtest=MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test`
