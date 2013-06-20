package org.openforis.collect.manager.codelistimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.codelistimport.CodeListLine.CodeLabelItem;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * 
 * @author S. Ricci
 * 
 */
public class CodeListImportProcess extends AbstractProcess<Void, CodeListImportStatus> {

	private static final String CSV = "csv";

	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "codeListImport.error.internalErrorImportingFile";
	
	private static Log LOG = LogFactory.getLog(CodeListImportProcess.class);
	//parameters
	private CodeListManager codeListManager;
	private File file;
	private CodeList codeList;
	private CodeScope codeScope;
	private String langCode;
	
	//internal variables
	private CodeListCSVReader reader;
	private List<String> levels;
	private Map<String, CodeListItem> codeToRootItem;
	private boolean overwriteData;

	public CodeListImportProcess(CodeListManager codeListManager,
			CodeList codeList, CodeScope codeScope, String langCode, File file,
			boolean overwriteData) {
		this.codeListManager = codeListManager;
		this.codeList = codeList;
		this.codeScope = codeScope;
		this.langCode = langCode;
		this.file = file;
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
		String fileName = file.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if ( CSV.equalsIgnoreCase(extension) ) {
			parseCSVLines(file);
		} else {
			String errorMessage = "File type not supported" + extension;
			status.setErrorMessage(errorMessage);
			status.error();
			LOG.error("Species import: " + errorMessage);
		}
		if ( status.hasErrors() ) {
			status.error();
		}
		if ( status.isRunning() ) {
			saveData();
			status.complete();
		}
	}

	protected void saveData() {
		codeList.setCodeScope(codeScope);
		if ( overwriteData ) {
			codeList.removeAllLevels();
		}
		addLevelsToCodeList();
		
		codeListManager.deleteAllItems(codeList);
		List<CodeListItem> rootItems = new ArrayList<CodeListItem>(codeToRootItem.values());
		codeListManager.saveItemsAndDescendants(rootItems);
		//saveItemsAndDescendants(rootItems, null);
	}

//	protected void saveItemsAndDescendants(Collection<CodeListItem> items,
//			Integer parentItemId) {
//		for (CodeListItem item : items) {
//			PersistedCodeListItem persistedChildItem = PersistedCodeListItem.fromItem(item);
//			persistedChildItem.setParentId(parentItemId);
//			codeListManager.save(persistedChildItem);
//			saveItemsAndDescendants(item.getChildItems(), persistedChildItem.getSystemId());
//		}
//	}

	protected void parseCSVLines(File file) {
		InputStreamReader isReader = null;
		FileInputStream is = null;
		long currentRowNumber = 0;
		try {
			is = new FileInputStream(file);
			isReader = new InputStreamReader(is);
			reader = new CodeListCSVReader(isReader);
			levels = reader.getLevels();
			adjustCodeScope();
			status.addProcessedRow(1);
			currentRowNumber = 2;
			while ( status.isRunning() ) {
				try {
					CodeListLine line = reader.readNextLine();
					if ( line != null ) {
						CodeListItem parentItem = null;
						for (int levelIdx = 0; levelIdx < levels.size(); levelIdx++) {
							boolean leafNode = levelIdx == levels.size() - 1;
							CodeListItem item = processLevel(parentItem, line, levelIdx, leafNode);
							parentItem = item;
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
			status.addParsingError(currentRowNumber, new ParsingError(ErrorType.IOERROR, e.getMessage()));
			LOG.error("Error importing species CSV file", e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LOG.error("Error closing reader", e);
			}
		}
	}

	protected CodeListItem processLevel(CodeListItem parent, CodeListLine line, int levelIdx, boolean leafNode) {
		CodeListItem result;
		List<CodeLabelItem> codeLabelItems = line.getCodeLabelItems();
		CodeLabelItem codeLabelItem = codeLabelItems.get(levelIdx);
		if ( isDuplicate(codeLabelItem, parent, levelIdx, leafNode) ) {
			addDuplicateCodeError(line, levelIdx);
		} 
		String code = codeLabelItem.getCode();
		result = getChildItem(parent, code);
		if ( result == null ) {
			result = codeList.createItem();
			fillItem(codeLabelItem, result);
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
	 * @param codeLabelItem
	 * @param parentItem
	 * @param levelIdx
	 * @param leafNode
	 * @return
	 */
	protected boolean isDuplicate(CodeLabelItem codeLabelItem, CodeListItem parentItem, int levelIdx, boolean leafNode) {
		String code = codeLabelItem.getCode();
		if ( leafNode ) {
			CodeListItem duplicateItem;
			if ( codeScope == CodeScope.LOCAL ) {
				duplicateItem = getChildItem(parentItem, code);
			} else {
				duplicateItem = getCodeListItemInDescendants(code);
			}
			return duplicateItem != null;
		} else {
			CodeListItem existingItem = getChildItem(parentItem, code);
			String label = codeLabelItem.getLabel();
			return existingItem != null && ! existingItem.getLabel(langCode).equals(label);
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
	
	protected void addDuplicateCodeError(CodeListLine line,	int levelIdx) {
		String level = levels.get(levelIdx);
		String column = level + CodeListCSVReader.CODE_COLUMN_SUFFIX;
		long lineNumber = line.getLineNumber();
		ParsingError error = new ParsingError(ErrorType.DUPLICATE_VALUE, lineNumber, column);
		status.addParsingError(lineNumber, error);
	}

	protected CodeListItem getCodeListItemInDescendants(String code) {
		Stack<CodeListItem> stack = new Stack<CodeListItem>();
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
	protected void fillItem(CodeLabelItem codeLabelItem, CodeListItem item) {
		item.setCode(codeLabelItem.getCode());
		item.setLabel(langCode, codeLabelItem.getLabel());
	}

	protected void addLevelsToCodeList() {
		for (String levelName : levels) {
			CodeListLevel level = new CodeListLevel();
			level.setName(levelName);
			codeList.addLevel(level);
		}
	}
	
	protected void adjustCodeScope() {
		if ( levels.size() == 1 ) {
			codeScope = CodeScope.SCHEME;
		}
	}

}
