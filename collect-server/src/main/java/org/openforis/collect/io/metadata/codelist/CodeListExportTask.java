package org.openforis.collect.io.metadata.codelist;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.codelistimport.CodeListCSVReader;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListExportTask extends Task {

	private static final String FLAT_LIST_LEVEL_NAME = "item";
	private static final char SEPARATOR = ',';
	private static final char QUOTECHAR = '"';
	
	// Input
	private CodeListManager codeListManager;
	private CodeList list;
	// Output
	private OutputStream out;

	//transient
	private CsvWriter writer;
	private Map<Integer, Boolean> qualifiableByLevel; // Each entry is true when the code list has qualifiable items for that level

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		qualifiableByLevel = codeListManager.hasQualifiableItemsByLevel(list);
		OutputStreamWriter osWriter = new OutputStreamWriter(out, OpenForisIOUtils.UTF_8);
		writer = new CsvWriter(osWriter, SEPARATOR, QUOTECHAR);
	}
	
	@Override
	protected void execute() throws Throwable {
		initHeaders();
		List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
		for (CodeListItem item : rootItems) {
			writeItem(item, Collections.<CodeListItem>emptyList());
		}
	}

	@Override
	protected void onCompleted() {
		super.onCompleted();
		try {
			writer.flush();
		} catch (IOException e) {
		}
	}
	
	private void initHeaders() {
		ArrayList<String> colNames = new ArrayList<String>();
		List<CodeListLevel> levels = list.getHierarchy();
		List<String> levelNames = new ArrayList<String>();
		if ( levels.isEmpty() ) {
			//fake level for flat list
			levelNames.add(FLAT_LIST_LEVEL_NAME);
		} else {
			for (CodeListLevel level : levels) {
				String levelName = level.getName();
				levelNames.add(levelName);
			}
		}
		List<String> langs = list.getSurvey().getLanguages();
		int levelIdx = 0;
		for (String levelName : levelNames) {
			colNames.add(levelName + CodeListCSVReader.CODE_COLUMN_SUFFIX);
			//add label columns
			for (String lang : langs) {
				colNames.add(levelName + CodeListCSVReader.LABEL_COLUMN_SUFFIX + "_" + lang);
			}
			//add description columns
			for (String lang : langs) {
				colNames.add(levelName + CodeListCSVReader.DESCRIPTION_COLUMN_SUFFIX + "_" + lang);
			}
			if (qualifiableByLevel.get(levelIdx)) {
				colNames.add(levelName + CodeListCSVReader.QUALIFIABLE_COLUMN_SUFFIX);
			}
			levelIdx++;
		}
		
		writer.writeHeaders(colNames);
	}

	protected void writeItem(CodeListItem item, List<CodeListItem> ancestors) {
		List<String> lineValues = new ArrayList<String>();
		lineValues.addAll(extractAncestorsLineValues(ancestors));
		lineValues.addAll(extractItemLineValues(item));

		writer.writeNext(lineValues);
		
		List<CodeListItem> children = codeListManager.loadChildItems(item);
		List<CodeListItem> childAncestors = new ArrayList<CodeListItem>(ancestors);
		childAncestors.add(item);
		for (CodeListItem child : children) {
			writeItem(child, childAncestors);
		}
	}

	protected List<String> extractItemLineValues(CodeListItem item) {
		List<String> lineValues = new ArrayList<String>();
		lineValues.add(item.getCode());
		CollectSurvey survey = (CollectSurvey) item.getSurvey();
		List<String> langs = survey.getLanguages();
		for (String lang : langs) {
			String label = item.getLabel(lang);
			lineValues.add(label);
		}
		for (String lang : langs) {
			String descr = item.getDescription(lang);
			lineValues.add(descr);
		}
		if (qualifiableByLevel.get(item.getLevel() - 1)) {
			lineValues.add(String.valueOf(item.isQualifiable()));
		}
		return lineValues;
	}

	protected List<String> extractAncestorsLineValues(List<CodeListItem> ancestors) {
		List<String> lineValues = new ArrayList<String>();
		for (CodeListItem item : ancestors) {
			lineValues.addAll(extractItemLineValues(item));
		}
		return lineValues;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}
	
	public CodeList getList() {
		return list;
	}
	
	public void setList(CodeList list) {
		this.list = list;
	}

	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
}
