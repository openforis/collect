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
	private Map<Integer, List<LanguageSpecificText>> levelToLocalizedText;
	
	public CodeListLine() {
		levelCodes = new ArrayList<String>();
		levelToLocalizedText = new HashMap<Integer, List<LanguageSpecificText>>();
	}
	
	public void addLevelCode(String code) {
		levelCodes.add(code);
	}

	public void addLabel(int levelIdx, String lang, String label) {
		LanguageSpecificText item = new LanguageSpecificText(lang, label);
		List<LanguageSpecificText> labelItems = levelToLocalizedText.get(levelIdx);
		if ( labelItems == null ) {
			labelItems = new ArrayList<LanguageSpecificText>();
			levelToLocalizedText.put(levelIdx, labelItems);
		}
		labelItems.add(item);
	}
	
	public List<LanguageSpecificText> getLabelItems(int levelIdx) {
		return CollectionUtils.unmodifiableList(levelToLocalizedText.get(levelIdx));
	}
	
	public List<String> getLevelCodes() {
		return levelCodes;
	}
	

}