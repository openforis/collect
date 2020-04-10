package org.openforis.collect.manager.codelistimport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.codelist.CodeListImportJob;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.LanguageSpecificText;

/**
 * 
 * @author S. Ricci
 * @deprecated Use {@link CodeListImportJob} instead
 */
public class CodeListImportProcess extends AbstractProcess<Void, CodeListImportStatus> {

	private static final Logger LOG = LogManager.getLogger(CodeListImportProcess.class);

	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "codeListImport.error.internalErrorImportingFile";
	private static final String DIFFERENT_LABEL_MESSAGE_KEY = "codeListImport.parsingError.differentLabel";
	
	//parameters
	private CodeListManager codeListManager;
	private File file;
	private CSVFileOptions csvFileOptions;
	private CodeList codeList;
	
	//internal variables
	private CodeListCSVReader reader;
	private List<String> levels;
	private Map<String, CodeListItem> codeToRootItem;
	private boolean overwriteData;

	public CodeListImportProcess(CodeListManager codeListManager,
			CodeList codeList, String langCode, File file, boolean overwriteData) {
		this(codeListManager, codeList, langCode, file, new CSVFileOptions(), overwriteData);
	}

	public CodeListImportProcess(CodeListManager codeListManager,
			CodeList codeList, String langCode, File file,
			CSVFileOptions csvFileOptions, boolean overwriteData) {
		this.codeListManager = codeListManager;
		this.codeList = codeList;
		this.file = file;
		this.csvFileOptions = csvFileOptions;
		this.overwriteData = overwriteData;
	}
	
	@Override
	public void init() {
		super.init();
		validateParameters();
		codeToRootItem = new LinkedHashMap<String, CodeListItem>();
	}
	
	protected void validateParameters() {
		if ( ! file.exists() && ! file.canRead() ) {
			status.error();
			status.setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
		}
	}
	
	@Override
	protected void initStatus() {
		status = new CodeListImportStatus();
	}
	
	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		processFile();
	}

	protected void processFile() throws IOException {
		parseCSVLines();
		
		if ( status.hasErrors() ) {
			status.error();
		}
		if ( status.isRunning() ) {
			saveData();
			status.complete();
		}
	}

	protected void saveData() {
		if ( overwriteData ) {
			codeList.removeAllLevels();
		}
		addLevelsToCodeList();
		
		codeListManager.deleteAllItems(codeList);
		List<CodeListItem> rootItems = new ArrayList<CodeListItem>(codeToRootItem.values());
		codeListManager.saveItemsAndDescendants(rootItems);
	}

	protected void parseCSVLines() {
		long currentRowNumber = 0;
		try {
			CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
			List<String> languages = survey.getLanguages();
			String defaultLanguage = survey.getDefaultLanguage();
			reader = new CodeListCSVReader(file, csvFileOptions, languages, defaultLanguage);
			reader.init();
			levels = reader.getLevels();
			status.addProcessedRow(1);
			currentRowNumber = 2;
			while ( status.isRunning() ) {
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
						status.addProcessedRow(currentRowNumber);
					}
					if ( ! reader.isReady() ) {
						break;
					}
				} catch (ParsingException e) {
					status.addParsingError(currentRowNumber, e.getError());
				} finally {
					currentRowNumber ++;
				}
			}
			status.setTotal(reader.getLinesRead() + 1);
		} catch (ParsingException e) {
			status.error();
			status.addParsingError(1, e.getError());
		} catch (Exception e) {
			status.error();
			status.addParsingError(currentRowNumber, new ParsingError(ErrorType.IOERROR, e.toString()));
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
		if ( CollectionUtils.isEmpty(labels) ) {
			addMissingDefaultLanguageLabelError(line, levelIdx);
		} else {
			for (LanguageSpecificText label : labels) {
				if ( hasDifferentLabel(code, label, parent)) {
					addDifferentLabelError(line, levelIdx, label.getLanguage());
				}
			}
		}
		result = getChildItem(parent, code);
		if ( result == null ) {
			result = codeList.createItem(levelIdx + 1);
			List<LanguageSpecificText> descriptions = line.getDescriptionItems(levelIdx);
			fillItem(result, code, labels, descriptions);
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
		CodeListItem duplicateItem;
		duplicateItem = getChildItem(parentItem, code);
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
	
	protected void addEmptyCodeColumnError(CodeListLine line, int levelIdx) {
		String level = levels.get(levelIdx);
		String column = level + CodeListCSVReader.CODE_COLUMN_SUFFIX;
		long lineNumber = line.getLineNumber();
		ParsingError error = new ParsingError(ErrorType.EMPTY, lineNumber, column);
		status.addParsingError(lineNumber, error);
	}
	
	protected void addDuplicateCodeError(CodeListLine line,	int levelIdx) {
		String level = levels.get(levelIdx);
		String column = level + CodeListCSVReader.CODE_COLUMN_SUFFIX;
		long lineNumber = line.getLineNumber();
		ParsingError error = new ParsingError(ErrorType.DUPLICATE_VALUE, lineNumber, column);
		status.addParsingError(lineNumber, error);
	}
	
	protected void addDifferentLabelError(CodeListLine line, int levelIdx, String lang) {
		String level = levels.get(levelIdx);
		String column = level + CodeListCSVReader.LABEL_COLUMN_SUFFIX + "_" + lang;
		long lineNumber = line.getLineNumber();
		ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, lineNumber, column, DIFFERENT_LABEL_MESSAGE_KEY);
		status.addParsingError(lineNumber, error);
	}

	protected void addMissingDefaultLanguageLabelError(CodeListLine line, int levelIdx) {
		String level = levels.get(levelIdx);
		String column = level + CodeListCSVReader.LABEL_COLUMN_SUFFIX;
		long lineNumber = line.getLineNumber();
		ParsingError error = new ParsingError(ErrorType.EMPTY, lineNumber, column);
		status.addParsingError(lineNumber, error);
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
	protected void fillItem(CodeListItem item, String code, List<LanguageSpecificText> labelItems, 
			List<LanguageSpecificText> descriptionItems) {
		item.setCode(code);
		for (LanguageSpecificText textItem : labelItems) {
			item.setLabel(textItem.getLanguage(), textItem.getText());
		}
		for (LanguageSpecificText textItem : descriptionItems) {
			item.setDescription(textItem.getLanguage(), textItem.getText());
		}
	}

	protected void addLevelsToCodeList() {
		if ( levels != null && levels.size() > 1 ) {
			for (String levelName : levels) {
				CodeListLevel level = new CodeListLevel();
				level.setName(levelName);
				codeList.addLevel(level);
			}
		}
	}
	
}
