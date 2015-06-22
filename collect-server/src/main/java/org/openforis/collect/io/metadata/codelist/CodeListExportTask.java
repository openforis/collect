package org.openforis.collect.io.metadata.codelist;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	
	private OutputStream out;
	private CodeList list;
	private CodeListManager codeListManager;

	//transient
	private CsvWriter writer;

	@Override
	protected void initalizeInternalVariables() throws Throwable {
		super.initalizeInternalVariables();
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
		for (String levelName : levelNames) {
			colNames.add(levelName + CodeListCSVReader.CODE_COLUMN_SUFFIX);
			List<String> langs = list.getSurvey().getLanguages();
			for (String lang : langs) {
				colNames.add(levelName + CodeListCSVReader.LABEL_COLUMN_SUFFIX + "_" + lang);
			}
		}
		writer.writeHeaders(colNames.toArray(new String[colNames.size()]));
	}

	protected void writeItem(CodeListItem item, List<CodeListItem> ancestors) {
		List<String> lineValues = new ArrayList<String>();
		addAncestorsLineValues(lineValues, ancestors);
		addItemLineValues(lineValues, item);

		writer.writeNext(lineValues.toArray(new String[lineValues.size()]));
		
		List<CodeListItem> children = codeListManager.loadChildItems(item);
		List<CodeListItem> childAncestors = new ArrayList<CodeListItem>(ancestors);
		childAncestors.add(item);
		for (CodeListItem child : children) {
			writeItem(child, childAncestors);
		}
	}

	protected void addItemLineValues(List<String> lineValues, CodeListItem item) {
		lineValues.add(item.getCode());
		CollectSurvey survey = (CollectSurvey) item.getSurvey();
		List<String> langs = survey.getLanguages();
		for (String lang : langs) {
			String label = item.getLabel(lang);
			lineValues.add(label);
		}
	}

	protected void addAncestorsLineValues(List<String> lineValues,
			List<CodeListItem> ancestors) {
		for (CodeListItem item : ancestors) {
			addItemLineValues(lineValues, item);
		}
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
