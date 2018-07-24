package org.openforis.collect.manager.dataexport.codelist;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.codelistimport.CodeListCSVReader;
import org.openforis.collect.manager.codelistimport.CodeListImportProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListExportProcess {

	private static final Logger LOG = LogManager.getLogger(CodeListImportProcess.class);

	private static final String FLAT_LIST_LEVEL_NAME = "item";
	private static final char SEPARATOR = ',';
	private static final char QUOTECHAR = '"';
	
	private CodeListManager codeListManager;
	
	public CodeListExportProcess(CodeListManager codeListManager) {
		super();
		this.codeListManager = codeListManager;
	}

	public void exportToCSV(OutputStream out, CollectSurvey survey, int codeListId) {
		CsvWriter writer = null;
		try {
			OutputStreamWriter osWriter = new OutputStreamWriter(out, Charset.forName("UTF-8"));
			writer = new CsvWriter(osWriter, SEPARATOR, QUOTECHAR);
			CodeList list = survey.getCodeListById(codeListId);
			initHeaders(writer, survey, list);
			List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
			for (CodeListItem item : rootItems) {
				List<CodeListItem> ancestors = Collections.emptyList();
				writeItem(writer, item, ancestors);
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private void initHeaders(CsvWriter writer, CollectSurvey survey,
			CodeList list) {
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
			List<String> langs = survey.getLanguages();
			for (String lang : langs) {
				colNames.add(levelName + CodeListCSVReader.LABEL_COLUMN_SUFFIX + "_" + lang);
			}
			for (String lang : langs) {
				colNames.add(levelName + CodeListCSVReader.DESCRIPTION_COLUMN_SUFFIX + "_" + lang);
			}
		}
		writer.writeHeaders(colNames);
	}

	protected void writeItem(CsvWriter writer, CodeListItem item, List<CodeListItem> ancestors) {
		List<String> lineValues = new ArrayList<String>();
		addAncestorsLineValues(lineValues, ancestors);
		addItemLineValues(lineValues, item);

		writer.writeNext(lineValues);
		
		List<CodeListItem> children = codeListManager.loadChildItems(item);
		List<CodeListItem> childAncestors = new ArrayList<CodeListItem>(ancestors);
		childAncestors.add(item);
		for (CodeListItem child : children) {
			writeItem(writer, child, childAncestors);
		}
	}

	protected void addItemLineValues(List<String> lineValues, CodeListItem item) {
		lineValues.add(item.getCode());
		CollectSurvey survey = (CollectSurvey) item.getSurvey();
		List<String> langs = survey.getLanguages();
		//add labels
		for (String lang : langs) {
			String text = item.getLabel(lang);
			lineValues.add(text);
		}
		//add description
		for (String lang : langs) {
			String text = item.getDescription(lang);
			lineValues.add(text);
		}
	}

	protected void addAncestorsLineValues(List<String> lineValues,
			List<CodeListItem> ancestors) {
		for (CodeListItem item : ancestors) {
			addItemLineValues(lineValues, item);
		}
	}
	
}
