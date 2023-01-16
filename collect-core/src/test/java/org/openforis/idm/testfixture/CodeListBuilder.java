package org.openforis.idm.testfixture;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.Survey;

public class CodeListBuilder {

	private String name;
	private List<String> levels;
	
	public CodeListBuilder(String name) {
		this.name = name;
		this.levels = new ArrayList<String>();
	}
	
	public CodeListBuilder level(String name) {
		this.levels.add(name);
		return this;
	}
	
	public CodeList build(Survey survey) {
		CodeList list = survey.createCodeList();
		list.setName(name);
		for (String levelName: levels) {
			CodeListLevel level = new CodeListLevel();
			level.setName(levelName);
			list.addLevel(level);
		}
		return list;
	}
}
