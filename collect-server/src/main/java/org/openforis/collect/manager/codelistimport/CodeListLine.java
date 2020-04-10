package org.openforis.collect.manager.codelistimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.io.metadata.parsing.Line;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.LanguageSpecificText;


/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListLine extends Line {

	private List<String> levelCodes = new ArrayList<String>();
	private Map<Integer, List<LanguageSpecificText>> levelToLabels = new HashMap<Integer, List<LanguageSpecificText>>();
	private Map<Integer, List<LanguageSpecificText>> levelToDescriptions = new HashMap<Integer, List<LanguageSpecificText>>();
	private Map<Integer, Boolean> levelToQualifiable = new HashMap<Integer, Boolean>();
	
	public void addLevelCode(String code) {
		levelCodes.add(code);
	}

	public void addLabel(int levelIdx, String lang, String text) {
		addLocalizedText(levelToLabels, levelIdx, lang, text);
	}

	public void addDescription(int levelIdx, String lang, String text) {
		addLocalizedText(levelToDescriptions, levelIdx, lang, text);
	}

	public void setQualifiable(int levelIdx, boolean qualifiable) {
		levelToQualifiable.put(levelIdx, qualifiable);
	}
	
	private void addLocalizedText(
			Map<Integer, List<LanguageSpecificText>> levelToLocalizedTexts,
			int levelIdx, String lang, String label) {
		List<LanguageSpecificText> labelItems = levelToLocalizedTexts.get(levelIdx);
		LanguageSpecificText item = new LanguageSpecificText(lang, label);
		if ( labelItems == null ) {
			labelItems = new ArrayList<LanguageSpecificText>();
			levelToLocalizedTexts.put(levelIdx, labelItems);
		}
		labelItems.add(item);
	}
	
	public List<LanguageSpecificText> getLabelItems(int levelIdx) {
		return CollectionUtils.unmodifiableList(levelToLabels.get(levelIdx));
	}
	
	public List<LanguageSpecificText> getDescriptionItems(int levelIdx) {
		return CollectionUtils.unmodifiableList(levelToDescriptions.get(levelIdx));
	}

	public List<String> getLevelCodes() {
		return levelCodes;
	}
	
	public boolean isQualifiable(int levelIdx) {
		Boolean qualifiable = levelToQualifiable.get(levelIdx);
		return qualifiable != null && qualifiable.booleanValue();
	}
	

}