package org.openforis.collect.io.metadata.codelist;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.ReferenceDataImportTask;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.codelistimport.CodeListCSVReader;
import org.openforis.collect.manager.codelistimport.CodeListLine;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.LanguageSpecificText;

/**
 * 
 * @author S. Ricci
 * 
 */
public class CodeListImportTask extends ReferenceDataImportTask<ParsingError> {

	private static final Logger LOG = LogManager.getLogger(CodeListImportTask.class);

	private static final String DIFFERENT_LABEL_MESSAGE_KEY = "survey.code_list.import_data.error.different_label";
	
	//input
	private CodeListManager codeListManager;
	private String entryName;
	private InputStream inputStream;
	private CodeList codeList;
	private boolean overwriteData;
	private CSVFileOptions csvFileOptions;
	
	//internal variables
	private CodeListCSVReader reader;
	private List<String> levels;
	private Map<String, CodeListItem> codeToRootItem;

	public CodeListImportTask() {
		csvFileOptions = new CSVFileOptions();
	}
	
	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		codeToRootItem = new LinkedHashMap<String, CodeListItem>();
	}
	
	@Override
	protected void execute() throws Throwable {
		parseCSVLines();
		if (hasErrors()) {
			changeStatus(Status.FAILED);
		} else if (isRunning()) {
			saveData();
		}
	}

	private void saveData() {
		if ( overwriteData ) {
			codeList.removeAllLevels();
		}
		addLevelsToCodeList();
		
		codeListManager.deleteAllItems(codeList);
		List<CodeListItem> rootItems = new ArrayList<CodeListItem>(codeToRootItem.values());
		codeListManager.saveItemsAndDescendants(rootItems);
	}

	private void parseCSVLines() {
		long currentRowNumber = 0;
		try {
			CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
			List<String> languages = survey.getLanguages();
			String defaultLanguage = survey.getDefaultLanguage();
			File file = OpenForisIOUtils.copyToTempFile(inputStream);
			reader = new CodeListCSVReader(file, csvFileOptions, languages, defaultLanguage);
			reader.init();
			levels = reader.getLevels();
			addProcessedRow(1);
			currentRowNumber = 2;
			while ( isRunning() ) {
				try {
					CodeListLine line = reader.readNextLine();
					if ( line != null ) {
						CodeListItem currentParentItem = null;
						List<String> levelCodes = line.getLevelCodes();
						for (int levelIdx = 0; levelIdx < levelCodes.size(); levelIdx++) {
							boolean lastLevel = levelIdx == levelCodes.size() - 1;
							CodeListItem item = processLevel(currentParentItem, line, levelIdx, lastLevel);
							currentParentItem = item;
						}
						addProcessedRow(currentRowNumber);
					}
					if ( ! reader.isReady() ) {
						break;
					}
				} catch (ParsingException e) {
					addParsingError(currentRowNumber, e.getError());
				} finally {
					currentRowNumber ++;
				}
			}
		} catch (ParsingException e) {
			changeStatus(Status.FAILED);
			addParsingError(1, e.getError());
		} catch (Exception e) {
			changeStatus(Status.FAILED);
			addParsingError(currentRowNumber, new ParsingError(ErrorType.IOERROR, e.toString()));
			LOG.error("Error importing code list CSV file", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	protected CodeListItem processLevel(CodeListItem parent, CodeListLine line, int levelIdx, boolean lastLevel) {
		CodeListItem result;
		//validate code
		List<String> codes = line.getLevelCodes();
		if ( codes.isEmpty() ) {
			addEmptyCodeColumnError(line, levelIdx);
		}
		String code = codes.get(levelIdx);
		if ( lastLevel && isDuplicate(code, parent) ) {
			addDuplicateCodeError(line, levelIdx);
		}
		//validate labels
		List<LanguageSpecificText> labels = line.getLabelItems(levelIdx);
		for (LanguageSpecificText label : labels) {
			if ( hasDifferentLabel(code, label, parent)) {
				addDifferentLabelError(line, levelIdx, label.getLanguage());
			}
		}
		result = getChildItem(parent, code);
		if ( result == null ) {
			result = codeList.createItem(levelIdx + 1);
			List<LanguageSpecificText> descriptions = line.getDescriptionItems(levelIdx);
			boolean qualifiable = line.isQualifiable(levelIdx);
			fillItem(result, code, labels, descriptions, qualifiable);
			if ( parent == null ) {
				codeToRootItem.put(code, result);
			} else {
				parent.addChildItem(result);
			}
		}
		return result;
	}

	/**
	 * 
	 * Returns when:
	 * not is leaf but has different label than existing node with same code
	 * or is leaf and:
	 * - LOCAL scope and exist item with same code at the same level
	 * - SCHEME scope and exist item with same code in some level
	 * @param code
	 * @param parentItem
	 * @param lastLevel
	 * @return
	 */
	protected boolean isDuplicate(String code, CodeListItem parentItem) {
		CodeListItem duplicateItem = getChildItem(parentItem, code);
		return duplicateItem != null;
	}
	
	protected boolean hasDifferentLabel(String code, LanguageSpecificText item, CodeListItem parentItem) {
		CodeListItem existingItem = getChildItem(parentItem, code);
		if ( existingItem == null ) {
			return false;
		} else {
			String lang = item.getLanguage();
			String label = item.getText();
			String existingItemLabel = existingItem.getLabel(lang);
			return ! existingItemLabel.equals(label);
		}
	}

	protected CodeListItem getChildItem(CodeListItem parentItem, String code) {
		CodeListItem duplicateItem;
		if ( parentItem == null ) {
			duplicateItem = codeToRootItem.get(code);
		} else {
			duplicateItem = parentItem.getChildItem(code);
		}
		return duplicateItem;
	}
	
	private void addEmptyCodeColumnError(CodeListLine line, int levelIdx) {
		String level = levels.get(levelIdx);
		String column = level + CodeListCSVReader.CODE_COLUMN_SUFFIX;
		long lineNumber = line.getLineNumber();
		ParsingError error = new ParsingError(ErrorType.EMPTY, lineNumber, column);
		addParsingError(lineNumber, error);
	}
	
	private void addDuplicateCodeError(CodeListLine line, int levelIdx) {
		String level = levels.get(levelIdx);
		String column = level + CodeListCSVReader.CODE_COLUMN_SUFFIX;
		long lineNumber = line.getLineNumber();
		ParsingError error = new ParsingError(ErrorType.DUPLICATE_VALUE, lineNumber, column);
		addParsingError(lineNumber, error);
	}
	
	private void addDifferentLabelError(CodeListLine line, int levelIdx, String lang) {
		String level = levels.get(levelIdx);
		String column = level + CodeListCSVReader.LABEL_COLUMN_SUFFIX + "_" + lang;
		long lineNumber = line.getLineNumber();
		ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, lineNumber, column, DIFFERENT_LABEL_MESSAGE_KEY);
		addParsingError(lineNumber, error);
	}

	protected CodeListItem getCodeListItemInDescendants(String code) {
		Deque<CodeListItem> stack = new LinkedList<CodeListItem>();
		stack.addAll(codeToRootItem.values());
		while ( ! stack.isEmpty() ) {
			CodeListItem item = stack.pop();
			if ( item.matchCode(code) ) {
				return item;
			} else {
				stack.addAll(item.getChildItems());
			}
		}
		return null;
	}
	private void fillItem(CodeListItem item, String code, List<LanguageSpecificText> labelItems,
			List<LanguageSpecificText> descriptionItems, boolean qualifiable) {
		item.setCode(code);
		for (LanguageSpecificText labelItem : labelItems) {
			item.setLabel(labelItem.getLanguage(), labelItem.getText());
		}
		for (LanguageSpecificText textItem : descriptionItems) {
			item.setDescription(textItem.getLanguage(), textItem.getText());
		}
		item.setQualifiable(qualifiable);
	}

	private void addLevelsToCodeList() {
		if ( levels != null && levels.size() > 1 ) {
			for (String levelName : levels) {
				CodeListLevel level = new CodeListLevel();
				level.setName(levelName);
				codeList.addLevel(level);
			}
		}
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void setCodeList(CodeList codeList) {
		this.codeList = codeList;
	}
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public void setOverwriteData(boolean overwriteData) {
		this.overwriteData = overwriteData;
	}

	public String getEntryName() {
		return entryName;
	}
	
	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}
	
}
