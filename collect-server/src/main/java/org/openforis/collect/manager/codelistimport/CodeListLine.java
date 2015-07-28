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

	private List<String> levelCodes;
	private Map<Integer, List<LanguageSpecificText>> levelToLabels;
	private Map<Integer, List<LanguageSpecificText>> levelToDescriptions;
	
	public CodeListLine() {
		levelCodes = new ArrayList<String>();
		levelToLabels = new HashMap<Integer, List<LanguageSpecificText>>();
		levelToDescriptions = new HashMap<Integer, List<LanguageSpecificText>>();
	}
	
	public void addLevelCode(String code) {
		levelCodes.add(code);
	}

	public void addLabel(int levelIdx, String lang, String text) {
		addLocalizedText(levelToLabels, levelIdx, lang, text);
	}

	public void addDescription(int levelIdx, String lang, String text) {
		addLocalizedText(levelToDescriptions, levelIdx, lang, text);
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
	

}