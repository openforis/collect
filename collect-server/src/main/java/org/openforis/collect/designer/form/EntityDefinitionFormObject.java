/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Annotation;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class EntityDefinitionFormObject<T extends EntityDefinition> extends NodeDefinitionFormObject<T> {

	private boolean showRowNumbers;
	private boolean countInRecordSummary;
	
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
		dest.setAnnotation(Annotation.COUNT_IN_SUMMARY_LIST.getQName(), Boolean.valueOf(countInRecordSummary).toString());
		dest.setAnnotation(Annotation.SHOW_ROW_NUMBERS.getQName(), Boolean.valueOf(showRowNumbers).toString());
	}
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		UIOptions uiOptions = getUIOptions(source);
		Layout layout = uiOptions.getLayout(source);
		layoutType = layout.name();
		countInRecordSummary = Boolean.valueOf(source.getAnnotation(Annotation.COUNT_IN_SUMMARY_LIST.getQName()));
		showRowNumbers = Boolean.valueOf(source.getAnnotation(Annotation.SHOW_ROW_NUMBERS.getQName()));
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

}
