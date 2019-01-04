package org.openforis.collect.io.data.csv;

import org.openforis.collect.io.data.NodeFilter;
import org.openforis.collect.model.RecordFilter;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVDataExportParameters {
	
	public enum HeadingSource {
		ATTRIBUTE_NAME, INSTANCE_LABEL, REPORTING_LABEL;
	}
	public enum OutputFormat {
		CSV, XLSX
	}
	private RecordFilter recordFilter;
	private NodeFilter nodeFilter;
	private Integer entityId;
	private boolean alwaysGenerateZipFile = false;
	private String multipleAttributeValueSeparator = ", ";
	private String fieldHeadingSeparator = "_";
	private boolean includeAllAncestorAttributes = false;
	private boolean includeCodeItemPositionColumn = false;
	private boolean includeKMLColumnForCoordinates = false;
	private boolean includeEnumeratedEntities = true;
	private boolean includeCompositeAttributeMergedColumn = false;
	private boolean includeCodeItemLabelColumn = false;
	private boolean includeGroupingLabels = true;
	private boolean includeCreatedByUserColumn = false;
	private boolean codeAttributeExpanded = false;
	private int maxMultipleAttributeValues = 10;
	private int maxExpandedCodeAttributeItems = 30;
	private HeadingSource headingSource = HeadingSource.ATTRIBUTE_NAME;
	private String languageCode = null;
	private OutputFormat outputFormat = OutputFormat.CSV;
	
	public RecordFilter getRecordFilter() {
		return recordFilter;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}
	
	public boolean isAlwaysGenerateZipFile() {
		return alwaysGenerateZipFile;
	}

	public void setAlwaysGenerateZipFile(boolean alwaysGenerateZipFile) {
		this.alwaysGenerateZipFile = alwaysGenerateZipFile;
	}

	public NodeFilter getNodeFilter() {
		return nodeFilter;
	}
	
	public void setNodeFilter(NodeFilter nodeFilter) {
		this.nodeFilter = nodeFilter;
	}
	
	public String getMultipleAttributeValueSeparator() {
		return multipleAttributeValueSeparator;
	}
	
	public void setMultipleAttributeValueSeparator(
			String multipleAttributeValueSeparator) {
		this.multipleAttributeValueSeparator = multipleAttributeValueSeparator;
	}
	
	public String getFieldHeadingSeparator() {
		return fieldHeadingSeparator;
	}
	
	public void setFieldHeadingSeparator(String fieldHeadingSeparator) {
		this.fieldHeadingSeparator = fieldHeadingSeparator;
	}
	
	public boolean isIncludeCodeItemPositionColumn() {
		return includeCodeItemPositionColumn;
	}

	public void setIncludeCodeItemPositionColumn(
			boolean includeCodeItemPositionColumn) {
		this.includeCodeItemPositionColumn = includeCodeItemPositionColumn;
	}

	public boolean isIncludeKMLColumnForCoordinates() {
		return includeKMLColumnForCoordinates;
	}

	public void setIncludeKMLColumnForCoordinates(
			boolean includeKMLColumnForCoordinates) {
		this.includeKMLColumnForCoordinates = includeKMLColumnForCoordinates;
	}

	public boolean isIncludeEnumeratedEntities() {
		return includeEnumeratedEntities;
	}

	public void setIncludeEnumeratedEntities(boolean includeEnumeratedEntities) {
		this.includeEnumeratedEntities = includeEnumeratedEntities;
	}

	public boolean isIncludeAllAncestorAttributes() {
		return includeAllAncestorAttributes;
	}

	public void setIncludeAllAncestorAttributes(boolean includeAllAncestorAttributes) {
		this.includeAllAncestorAttributes = includeAllAncestorAttributes;
	}
	
	public boolean isIncludeCodeItemLabelColumn() {
		return includeCodeItemLabelColumn;
	}
	
	public void setIncludeCodeItemLabelColumn(boolean includeCodeItemLabelColumn) {
		this.includeCodeItemLabelColumn = includeCodeItemLabelColumn;
	}
	
	public int getMaxMultipleAttributeValues() {
		return maxMultipleAttributeValues;
	}
	
	public void setMaxMultipleAttributeValues(int maxMultipleAttributeValues) {
		this.maxMultipleAttributeValues = maxMultipleAttributeValues;
	}
	
	public boolean isCodeAttributeExpanded() {
		return codeAttributeExpanded;
	}
	
	public void setCodeAttributeExpanded(boolean codeAttributeExpanded) {
		this.codeAttributeExpanded = codeAttributeExpanded;
	}
	
	public boolean isIncludeCompositeAttributeMergedColumn() {
		return includeCompositeAttributeMergedColumn;
	}
	
	public void setIncludeCompositeAttributeMergedColumn(
			boolean includeCompositeAttributeMergedColumn) {
		this.includeCompositeAttributeMergedColumn = includeCompositeAttributeMergedColumn;
	}
	
	public int getMaxExpandedCodeAttributeItems() {
		return maxExpandedCodeAttributeItems;
	}
	
	public void setMaxExpandedCodeAttributeItems(
			int maxExpandedCodeAttributeItems) {
		this.maxExpandedCodeAttributeItems = maxExpandedCodeAttributeItems;
	}
	
	public HeadingSource getHeadingSource() {
		return headingSource;
	}
	
	public void setHeadingSource(HeadingSource headingSource) {
		this.headingSource = headingSource;
	}
	
	public String getLanguageCode() {
		return languageCode;
	}
	
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	public boolean isIncludeGroupingLabels() {
		return includeGroupingLabels;
	}
	
	public void setIncludeGroupingLabels(boolean includeGroupingLabels) {
		this.includeGroupingLabels = includeGroupingLabels;
	}
	
	public boolean isIncludeCreatedByUserColumn() {
		return includeCreatedByUserColumn;
	}
	
	public void setIncludeCreatedByUserColumn(boolean includeCreatedByUserColumn) {
		this.includeCreatedByUserColumn = includeCreatedByUserColumn;
	}
	
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}
	
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}
}
