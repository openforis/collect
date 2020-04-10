package org.openforis.collect.manager.dataexport.codelist;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.codelistimport.CodeListCSVReader;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.io.excel.ExcelFlatValuesWriter;
import org.openforis.commons.io.flat.FlatDataWriter;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListExportProcess {

	private static final String FLAT_LIST_LEVEL_NAME = "item";
	private static final char SEPARATOR = ',';
	private static final char QUOTECHAR = '"';
	
	public enum OutputFormat {
		CSV, EXCEL
	}
	
	private CodeListManager codeListManager;

	
	public CodeListExportProcess(CodeListManager codeListManager) {
		super();
		this.codeListManager = codeListManager;
	}

	public void exportToCSV(OutputStream out, CollectSurvey survey, int codeListId) {
		export(out, survey, codeListId, OutputFormat.CSV);
	}

	public void export(OutputStream out, CollectSurvey survey, int codeListId, OutputFormat outputFormat) {
		CodeList list = survey.getCodeListById(codeListId);
		Map<Integer, Boolean> qualifiableByLevel = codeListManager.hasQualifiableItemsByLevel(list);
		FlatDataWriter writer = null;
		try {
			if (outputFormat == OutputFormat.CSV) {
				OutputStreamWriter osWriter = new OutputStreamWriter(out, Charset.forName("UTF-8"));
				writer = new CsvWriter(osWriter, SEPARATOR, QUOTECHAR);
			} else {
				writer = new ExcelFlatValuesWriter(out);
			}
			initHeaders(writer, survey, list, qualifiableByLevel);
			List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
			for (CodeListItem item : rootItems) {
				List<CodeListItem> ancestors = Collections.emptyList();
				writeItem(writer, qualifiableByLevel, item, ancestors);
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error exporting code list %s: %s", list.getName(), e.getMessage()), e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private void initHeaders(FlatDataWriter writer, CollectSurvey survey,
			CodeList list, Map<Integer, Boolean> qualifiableByLevel) {
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
		int levelIdx = 0;
		for (String levelName : levelNames) {
			colNames.add(levelName + CodeListCSVReader.CODE_COLUMN_SUFFIX);
			List<String> langs = survey.getLanguages();
			for (String lang : langs) {
				colNames.add(levelName + CodeListCSVReader.LABEL_COLUMN_SUFFIX + "_" + lang);
			}
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

	protected void writeItem(FlatDataWriter writer, Map<Integer, Boolean> qualifiableByLevel, 
			CodeListItem item, List<CodeListItem> ancestors) {
		List<String> lineValues = new ArrayList<String>();
		lineValues.addAll(getAncestorsLineValues(qualifiableByLevel, ancestors));
		lineValues.addAll(getItemLineValues(qualifiableByLevel, item));

		writer.writeNext(lineValues);
		
		List<CodeListItem> children = codeListManager.loadChildItems(item);
		List<CodeListItem> childAncestors = new ArrayList<CodeListItem>(ancestors);
		childAncestors.add(item);
		for (CodeListItem child : children) {
			writeItem(writer, qualifiableByLevel, child, childAncestors);
		}
	}

	protected List<String> getItemLineValues(Map<Integer, Boolean> qualifiableByLevel, CodeListItem item) {
		List<String> lineValues = new ArrayList<String>();
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
		if (qualifiableByLevel.get(item.getLevel() - 1)) {
			lineValues.add(String.valueOf(item.isQualifiable()));
		}
		return lineValues;
	}

	protected List<String> getAncestorsLineValues(Map<Integer, Boolean> qualifiableByLevel, List<CodeListItem> ancestors) {
		List<String> lineValues = new ArrayList<String>();
		for (CodeListItem item : ancestors) {
			lineValues.addAll(getItemLineValues(qualifiableByLevel, item));
		}
		return lineValues;
	}
	
}
