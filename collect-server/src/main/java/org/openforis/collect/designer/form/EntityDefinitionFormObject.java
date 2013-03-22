/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Disposition;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class EntityDefinitionFormObject<T extends EntityDefinition> extends NodeDefinitionFormObject<T> {

	private boolean showRowNumbers;
	private boolean countInRecordSummary;
	private String disposition;
	
	//layout
	private String layoutType;

	EntityDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		UIOptions uiOptions = getUIOptions(dest);
		Layout layout = Layout.valueOf(layoutType);
		uiOptions.setLayout(dest, layout);
		uiOptions.setCountInSummaryListValue(dest, countInRecordSummary);
		uiOptions.setShowRowNumbersValue(dest, showRowNumbers);
		Disposition dispositionEnum = super.isMultiple() && layout == Layout.TABLE &&
				Disposition.BY_COLUMNS.getValue().equals(this.disposition) ? Disposition.BY_COLUMNS: null;
		uiOptions.setDisposition(dest, dispositionEnum);
	}
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		UIOptions uiOptions = getUIOptions(source);
		Layout layout = uiOptions.getLayout(source);
		layoutType = layout.name();
		countInRecordSummary = uiOptions.getCountInSumamryListValue(source);
		showRowNumbers = uiOptions.getShowRowNumbersValue(source);
		disposition = uiOptions.getDisposition(source).getValue();
	}

	@Override
	protected void reset() {
		super.reset();
		layoutType = null;
	}
	
	public String getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
	}

	public boolean isShowRowNumbers() {
		return showRowNumbers;
	}

	public void setShowRowNumbers(boolean showRowNumbers) {
		this.showRowNumbers = showRowNumbers;
	}
	
	public boolean isCountInRecordSummary() {
		return countInRecordSummary;
	}

	public void setCountInRecordSummary(boolean countInRecordSummary) {
		this.countInRecordSummary = countInRecordSummary;
	}

	public String getDisposition() {
		return disposition;
	}
	
	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}
	
}
