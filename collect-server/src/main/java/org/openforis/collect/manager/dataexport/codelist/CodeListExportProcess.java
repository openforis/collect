package org.openforis.collect.manager.dataexport.codelist;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static Log LOG = LogFactory.getLog(CodeListImportProcess.class);

	private static final String FLAT_LIST_LEVEL_NAME = "item";
	private static final char SEPARATOR = ',';
	private static final char QUOTECHAR = '"';
	
	public void exportToCSV(OutputStream out, CollectSurvey survey, int codeListId) {
		CsvWriter writer = null;
		try {
			OutputStreamWriter osWriter = new OutputStreamWriter(out, Charset.forName("UTF-8"));
			writer = new CsvWriter(osWriter, SEPARATOR, QUOTECHAR);
			CodeList list = survey.getCodeListById(codeListId);
			initHeaders(writer, survey, list);
			List<CodeListItem> rootItems = list.getItems();
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
		}
		writer.writeHeaders(colNames.toArray(new String[0]));
	}

	protected void writeItem(CsvWriter writer, CodeListItem item, List<CodeListItem> ancestors) {
		List<String> lineValues = new ArrayList<String>();
		addAncestorsLineValues(lineValues, ancestors);
		addItemLineValues(lineValues, item);

		writer.writeNext(lineValues.toArray(new String[0]));
		
		List<CodeListItem> children = item.getChildItems();
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
		for (String lang : langs) {
			String label = item.getLabel(lang);
			if ( label == null && lang.equals(survey.getDefaultLanguage()) ) {
				label = item.getLabel(null);
			}
			lineValues.add(label);
		}
	}

	protected void addAncestorsLineValues(List<String> lineValues,
			List<CodeListItem> ancestors) {
		for (CodeListItem item : ancestors) {
			addItemLineValues(lineValues, item);
		}
	}
	
}
