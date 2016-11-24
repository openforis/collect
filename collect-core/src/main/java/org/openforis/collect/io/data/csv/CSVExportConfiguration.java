package org.openforis.collect.io.data.csv;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVExportConfiguration {
	
	public enum HeadingSource {
		ATTRIBUTE_NAME, INSTANCE_LABEL, REPORTING_LABEL;
	}

	private String multipleAttributeValueSeparator = ", ";
	private String fieldHeadingSeparator = "_";
	private boolean includeAllAncestorAttributes = false;
	private boolean includeCodeItemPositionColumn = false;
	private boolean includeKMLColumnForCoordinates = false;
	private boolean includeEnumeratedEntities = true;
	private boolean includeCompositeAttributeMergedColumn = false;
	private boolean includeCodeItemLabelColumn = false;
	private boolean includeGroupingLabels = true;
	private boolean codeAttributeExpanded = false;
	private int maxMultipleAttributeValues = 10;
	private int maxExpandedCodeAttributeItems = 30;
	private HeadingSource headingSource = HeadingSource.ATTRIBUTE_NAME;
	private String languageCode = null;
	
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
}
