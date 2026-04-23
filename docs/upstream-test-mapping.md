# Upstream Test Mapping

Document version: `2026-04-23`

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
- `jp.igapyon.mikuxlsx2md.markdownoptions.MarkdownOptionsTest.acceptsPlannerAwareTableDetectionMode`

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
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.parsesSharedFormulasWithCrossSheetAndAbsoluteReferences`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.parsesWorksheetHyperlinksFromLocalRefsAndExternalRelationships`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.expandsHyperlinkRangesAndHashTargetsAcrossCells`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.attachesCellTextStyleToSharedInlineBooleanAndFormattedValues`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.exposesFormulaCachedStateTypeAndSpillRef`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.parsesUpstreamFormulaCrossSheetFixtureWithConcreteFollowerCoverageWhenAvailable`
- `jp.igapyon.mikuxlsx2md.worksheetparser.WorksheetParserTest.parsesUpstreamFormulaSharedFixtureWithExtendedFollowerCoverageWhenAvailable`

fixtures:
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-crosssheet-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-shared-sample01.xlsx`

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=WorksheetParserTest test`

notes:
- Current Java coverage includes shared formula translation with relative / absolute / sheet-qualified references, upstream `formula-crosssheet` / `formula-shared` fixture assertions, value type / raw value / formula type / cached value metadata assertions for those formula fixtures, hyperlink range expansion with hash locations, richTextRuns propagation for styled shared / inline / boolean / formatted values, and formula cached state / type / spill ref metadata.

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
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamRichUsecaseFixtureToGithubMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamRichTextGithubFixtureToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamRichMarkdownEscapeFixtureToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamNarrativeFixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamEdgeEmptyFixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamEdgeWeirdSheetNameFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample01FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample02FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample03FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample11FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample12FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample13FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample14FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample15FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamTableBasicSample16FixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamGridLayoutFixtureWorkbookToMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamMergePatternFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamMergeMultilineFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamFormulaBasicFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamFormulaCrossSheetFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamFormulaSharedFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamFormulaSpillFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamChartBasicFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamChartMixedFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamImageFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamImageFixtureSample02WorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.convertsUpstreamBorderPriorityFixtureDifferentlyBetweenBalancedAndBorderModesWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamShapeFixtureWorkbookWhenAvailable`
- `jp.igapyon.mikuxlsx2md.core.CoreFixtureRegressionTest.parsesUpstreamCalloutShapeFixtureWithoutSvgAssetsWhenAvailable`

fixtures:
- `workplace/miku-xlsx2md/tests/fixtures/named-range/named-range-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/link/hyperlink-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/display/display-format-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/narrative/narrative-vs-table-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/edge/edge-empty-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/edge/edge-weird-sheetname-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample02.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample03.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample11.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample12.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample13.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample14.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample15.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample16.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/grid-layout-sample-01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-text-github-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-markdown-escape-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-usecase-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/merge/merge-multiline-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/merge/merge-pattern-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-crosssheet-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-shared-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-spill-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/chart/chart-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/chart/chart-mixed-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-border-priority-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample02.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-callout-sample01.xlsx`

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
`tests/xlsx2md-border-grid.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.bordergrid.BorderGridTest.returnsFalseWhenNeitherAdjacentCellOwnsTheQueriedEdge`
- `jp.igapyon.mikuxlsx2md.bordergrid.BorderGridTest.normalizesBordersFromAdjacentCells`
- `jp.igapyon.mikuxlsx2md.bordergrid.BorderGridTest.collectsRowStatsWithNormalizedAndRawBorderCountsSeparately`
- `jp.igapyon.mikuxlsx2md.bordergrid.BorderGridTest.countsNormalizedBorderedCellsAcrossACandidateRange`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=BorderGridTest test`

### upstream test / intent:
`tests/xlsx2md-table-detector.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.collectsSeedCellsFromValuesOrBorders`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.trimsBorderedTableBeforeFollowingBorderlessNoteRow`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.normalizesCandidateMatricesWithMergeTokensAndEmptyTrimming`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.detectsBorderedDenseGridAsTableCandidate`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.prunesRedundantAndCalendarLikeCandidates`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.borderModeExcludesDenseBorderlessBlocksButKeepsBorderedTables`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.doesNotTreatMergeHeavyFormBlocksAsTables`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.plannerAwareDropsCalendarLikeColumnCandidatesWhileBorderKeepsThem`
- `jp.igapyon.mikuxlsx2md.tabledetector.TableDetectorTest.plannerAwareDoesNotKeepHugeFallbackCandidateForMergeHeavyMixedLayoutSheet`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=TableDetectorTest test`

### upstream test / intent:
`tests/xlsx2md-sheet-assets.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssetsTest.sanitizesSheetAssetDirectories`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssetsTest.rendersHierarchicalRawEntries`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssetsTest.groupsNearbyShapesIntoShapeBlocks`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssetsTest.rendersImageChartAndShapeSections`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssetsTest.rendersUngroupedShapesAfterGroupedShapeBlocks`
- `jp.igapyon.mikuxlsx2md.sheetassets.SheetAssetsTest.parsesDrawingImagesChartsAndShapes`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=SheetAssetsTest test`

notes:
- Current Java coverage ports asset drawing parsing, rendering, shape block grouping, and parsed shape SVG helper connection intent.

### upstream test / intent:
`tests/xlsx2md-office-drawing.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.officedrawing.OfficeDrawingTest.rendersTextboxShapesAsSvgAssetsWithSanitizedSheetDirectories`
- `jp.igapyon.mikuxlsx2md.officedrawing.OfficeDrawingTest.rendersConnectorShapesWithArrowMarkers`
- `jp.igapyon.mikuxlsx2md.officedrawing.OfficeDrawingTest.returnsNullForUnsupportedShapeKinds`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=OfficeDrawingTest test`

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
`tests/xlsx2md-rich-text-parser.test.js`
`tests/xlsx2md-rich-text-plain-formatter.test.js`
`tests/xlsx2md-rich-text-github-formatter.test.js`
`tests/xlsx2md-rich-text-renderer.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.splitsRawTextIntoTextAndLineBreakTokens`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.tokenizesPlainModeIntoASingleEscapedTextToken`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.tokenizesGithubFallbackCellsIntoStyledTextAndLineBreakTokens`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.rendersTokenizedRichTextWithGithubFormatting`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.fallsBackToPlainTokensInPlainMode`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.rendersEscapedMarkdownLinkLikeTextSafelyInsideStyledRuns`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.rendersConsecutiveLineBreaksAcrossStyledRichRuns`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.showsPlainVsGithubDifferencesForTheSameRichTextInput`
- `jp.igapyon.mikuxlsx2md.richtextrenderer.RichTextRendererTest.suppressesUnderlineWhenRequestedForHyperlinkRendering`

fixtures:
- none

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=RichTextRendererTest test`

### upstream test / intent:
`tests/xlsx2md-sheet-markdown.test.js`

java tests:
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.extractsNarrativeBlocksOutsideTables`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsSheetToMarkdownWithDetectedTableAndSummary`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.formatsHyperlinksRawAndBothModes`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsWorkbookThroughCoreFacadeShape`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsSheetWithShapeBlocks`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.omitsShapeSectionsWhenIncludeShapeDetailsIsDisabled`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.keepsNearbyCalendarRowsInOneNarrativeBlock`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.reordersCalendarLikeSectionsWithSidebar`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.plannerAwareDoesNotTurnRepeatedNarrowCalendarColumnsIntoTables`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.createsEmptyBodyFallbackSummary`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.normalizesTableDetectionCompatibilityAliasInCoreConversion`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.preservesPlainAndGithubLineBreakDifferences`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.keepsMarkdownMarkersLiteralInNarrativeOutput`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.rendersExternalAndWorkbookHyperlinksAsMarkdownLinks`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.suppressesUnderlineMarkupForHyperlinkCellsInGithubMode`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.keepsBlankLineBetweenShapeItemsWhenSvgOutputIsPresent`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.preservesHyperlinksInRawModeAndAppendsRawOnlyWhenValuesDifferInBothMode`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamDisplayFixtureIntoDisplayRawAndBothMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamHyperlinkFixtureIntoGithubMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamRichUsecaseFixtureIntoPlainAndGithubMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamMergeMultilineFixtureIntoMergedTableMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamRichMarkdownEscapeFixtureIntoPlainAndGithubMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamMergePatternFixtureIntoMergeTokenMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamFormulaBasicFixtureIntoFormulaMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamFormulaSpillFixtureIntoSpillMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamChartMixedFixtureIntoCombinedChartMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamBasicFixtureIntoPlainRawAndBothMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamBasicShapeFixtureIntoSvgBackedShapeBlockMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamCalloutShapeFixtureIntoRawDetailShapeBlockMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamFlowchartShapeFixtureIntoShapeBlockMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamBlockArrowShapeFixtureIntoShapeBlockMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample01FixtureIntoSeparatedVerticalTablesWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample02FixtureIntoSeparatedHorizontalTablesWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample03FixtureIntoSeparatedQuadTablesWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample11FixtureIntoGridHeavyMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample12FixtureIntoTwoSectionGridMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample13FixtureIntoDenseMultiTableMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample14FixtureIntoSparseMergeMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample15FixtureIntoMergedGridMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamTableBasicSample16FixtureIntoMultiValueMergeMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamGridLayoutFixtureIntoWideGridMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamImageFixtureSample02IntoImageAndChartMarkdownWhenAvailable`
- `jp.igapyon.mikuxlsx2md.sheetmarkdown.SheetMarkdownTest.convertsUpstreamWeirdSheetNameFixtureIntoSanitizedMarkdownFileWhenAvailable`

fixtures:
- `workplace/miku-xlsx2md/tests/fixtures/xlsx2md-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/display/display-format-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/link/hyperlink-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-usecase-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-markdown-escape-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/merge/merge-multiline-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/merge/merge-pattern-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-spill-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/chart/chart-mixed-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-flowchart-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-block-arrow-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-callout-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample02.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample03.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample11.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample12.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample13.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample14.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample15.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-basic-sample16.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/grid-layout-sample-01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample02.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/edge/edge-weird-sheetname-sample01.xlsx`

focused regression:
- `mvn -pl miku-xlsx2md -Dtest=SheetMarkdownTest test`

notes:
- Current Java coverage includes the core facade path, shape block rendering connection, shape details toggle, calendar narrative grouping and sidebar ordering, empty-body fallback, table detection compatibility alias normalization, plain/GitHub line break behavior, Markdown literal escaping, hyperlink output modes, GitHub hyperlink underline suppression, SVG-backed shape item spacing, and fixture-backed narrative / sparse / border-priority / broader table-basic / grid-layout / xlsx2md-basic / display / hyperlink / rich / merge / formula / chart / shape / image-basic-sample02 / weird-sheetname parity checks.
- Current Java coverage also includes planner-aware suppression for repeated narrow calendar layouts so those bands remain narrative instead of becoming small tables.
- More advanced upstream sheet-markdown cases remain follow-up coverage.

### upstream test / intent:
Node CLI option compatibility, help text shape, and initial conversion I/O

java tests:
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.printsHelpAndExitsSuccessfully`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.failsForUnknownOption`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.acceptsKnownOptionsAndWritesConvertedMarkdown`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.writesZipOnlyWhenZipPathIsSpecified`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.printsProcessingFileToStderrWhenVerbose`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsDirectoryInputsAndPreservesRelativeDirectoriesWhenRecursive`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.printsDirectoryProcessingFilesToStderrWhenVerbose`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.rejectsZipWhenConvertingDirectoryInputs`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.rejectsShiftJisBomCombination`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamShapeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamDisplayFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamNamedRangeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamNarrativeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamRichUsecaseFixtureInGithubModeWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamMergeMultilineFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamChartBasicFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamRichMarkdownEscapeFixtureInGithubModeWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamFormulaBasicFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamFormulaSpillFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamChartMixedFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamFormulaCrossSheetFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamFormulaSharedFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamImageFixtureSample01WhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamEdgeEmptyFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.keepsBorderPriorityAsCompatibilityAliasWhenUsingUpstreamTableFixture`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.keepsIncludeShapeDetailsAsCompatibilityAliasWhenUsingUpstreamShapeFixture`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamBasicFixtureInBothModeWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamImageFixtureSample02WhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamFlowchartShapeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamBlockArrowShapeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamCalloutShapeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.cli.MikuXlsx2mdCliTest.convertsUpstreamWeirdSheetNameFixtureWhenAvailable`

fixtures:
- `workplace/miku-xlsx2md/tests/fixtures/display/display-format-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/named-range/named-range-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/narrative/narrative-vs-table-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-usecase-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-markdown-escape-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/merge/merge-multiline-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-crosssheet-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-shared-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-spill-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/chart/chart-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/chart/chart-mixed-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-border-priority-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/xlsx2md-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample02.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-flowchart-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-block-arrow-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-callout-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/edge/edge-empty-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/edge/edge-weird-sheetname-sample01.xlsx`

focused regression:
- `mvn -Dtest=MikuXlsx2mdCliTest test`

notes:
- CLI help now reflects upstream GUI-aligned defaults, including `formatting-mode=github` and the additional `planner-aware` table detection mode.
- Java CLI also includes a Java-side directory batch conversion extension backed by the shared runtime directory converter.
- Java CLI `--verbose` prints processing workbook paths to stderr.

### upstream test / intent:
Maven plugin option mapping, skip behavior, and initial conversion I/O

java tests:
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.skipsWhenRequested`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.writesMarkdownThroughCoreConversion`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamDisplayFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamNamedRangeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamNarrativeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamHyperlinkFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamShapeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamRichUsecaseFixtureInGithubModeWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamMergeMultilineFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamChartBasicFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamRichMarkdownEscapeFixtureInGithubModeWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamFormulaBasicFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamFormulaSpillFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamChartMixedFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamFormulaCrossSheetFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamFormulaSharedFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamImageFixtureSample01WhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamEdgeEmptyFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamBorderPriorityFixtureInBorderModeWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamBasicFixtureInBothModeWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamImageFixtureSample02WhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamFlowchartShapeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamBlockArrowShapeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamCalloutShapeFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.MikuXlsx2mdMojoTest.convertsUpstreamWeirdSheetNameFixtureWhenAvailable`
- `jp.igapyon.mikuxlsx2md.mavenplugin.ConvertDirectoryMojoTest.skipsWhenRequested`
- `jp.igapyon.mikuxlsx2md.mavenplugin.ConvertDirectoryMojoTest.writesMarkdownNextToInputFilesWhenOutputDirectoryIsOmitted`
- `jp.igapyon.mikuxlsx2md.mavenplugin.ConvertDirectoryMojoTest.preservesRelativeDirectoriesWhenRecursiveOutputDirectoryIsSpecified`
- `jp.igapyon.mikuxlsx2md.mavenplugin.ConvertDirectoryMojoTest.doesNotDescendIntoSubdirectoriesWhenRecursiveIsDisabled`
- `jp.igapyon.mikuxlsx2md.directoryconverter.DirectoryConverterTest.writesMarkdownNextToInputFilesWhenOutputDirectoryIsOmitted`
- `jp.igapyon.mikuxlsx2md.directoryconverter.DirectoryConverterTest.preservesRelativeDirectoriesWhenRecursiveOutputDirectoryIsSpecified`
- `jp.igapyon.mikuxlsx2md.directoryconverter.DirectoryConverterTest.doesNotDescendIntoSubdirectoriesWhenRecursiveIsDisabled`
- `jp.igapyon.mikuxlsx2md.directoryconverter.DirectoryConverterTest.notifiesProcessingWorkbookThroughProgressListener`

fixtures:
- `workplace/miku-xlsx2md/tests/fixtures/display/display-format-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/named-range/named-range-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/narrative/narrative-vs-table-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/link/hyperlink-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-usecase-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/rich/rich-markdown-escape-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/merge/merge-multiline-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-crosssheet-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-shared-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/formula/formula-spill-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/chart/chart-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/chart/chart-mixed-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/table/table-border-priority-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/xlsx2md-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/image/image-basic-sample02.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-flowchart-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-block-arrow-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/shape/shape-callout-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/edge/edge-empty-sample01.xlsx`
- `workplace/miku-xlsx2md/tests/fixtures/edge/edge-weird-sheetname-sample01.xlsx`

focused regression:
- `mvn -pl miku-xlsx2md,miku-xlsx2md-maven-plugin -am -Dtest=DirectoryConverterTest,MikuXlsx2mdCliTest,ConvertDirectoryMojoTest,MikuXlsx2mdMojoTest -Dsurefire.failIfNoSpecifiedTests=false test`
- `sh scripts/smoke-maven-plugin.sh`

notes:
- Full-coordinate Maven plugin execution is fixed through `scripts/smoke-maven-plugin.sh`.
- The directory goal uses `inputDirectory`, optional `outputDirectory`, and `recursive=false` by default, scans `.xlsx` files only, and delegates to the same runtime helper used by the Java CLI directory mode.
- Maven plugin goals accept `miku-xlsx2md.verbose` to log processing workbook paths.
