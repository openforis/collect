package org.openforis.collect.manager.codeListImport;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.codeListImport.CodeListLine.CodeLabelItem;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.referenceDataImport.ParsingError;
import org.openforis.collect.manager.referenceDataImport.ParsingError.ErrorType;
import org.openforis.collect.manager.referenceDataImport.ParsingException;
import org.openforis.idm.metamodel.CodeList;
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

	private File file;
	
	private CodeListCSVReader reader;
	private CodeList codeList;
	private String langCode;
	private String errorMessage;
	private boolean overwriteData;
	
	public CodeListImportProcess(CodeList codeList, String langCode, File file, boolean overwriteData) {
		this.codeList = codeList;
		this.langCode = langCode;
		this.file = file;
		this.overwriteData = overwriteData;
	}
	
	@Override
	public void init() {
		super.init();
		validateParameters();
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
		if ( overwriteData ) {
			codeList.removeAllItems();
		}
		processFile();
	}

	protected void processFile() throws IOException {
		String fileName = file.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if ( CSV.equalsIgnoreCase(extension) ) {
			parseCSVLines(file);
		} else {
			errorMessage = "File type not supported" + extension;
			status.setErrorMessage(errorMessage);
			status.error();
			LOG.error("Species import: " + errorMessage);
		}
		if ( status.hasErrors() ) {
			status.error();
		}
		if ( status.isRunning() ) {
			status.complete();
		}
	}

	protected void parseCSVLines(File file) {
		InputStreamReader isReader = null;
		FileInputStream is = null;
		long currentRowNumber = 0;
		try {
			is = new FileInputStream(file);
			isReader = new InputStreamReader(is);
			reader = new CodeListCSVReader(isReader);
			status.addProcessedRow(1);
			currentRowNumber = 2;
			addLevels(codeList);
			while ( status.isRunning() ) {
				try {
					CodeListLine line = reader.readNextLine();
					if ( line != null ) {
						List<String> levels = reader.getLevels();
						CodeListItem parentItem = null;
						for (int levelIdx = 0; levelIdx < levels.size(); levelIdx++) {
							CodeListItem item = processLevel(codeList, parentItem, line, levelIdx);
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
			close(isReader);
		}
	}

	protected CodeListItem processLevel(CodeList codeList,
			CodeListItem parentItem, CodeListLine line, int levelIdx) {
		List<CodeLabelItem> codeLabelItems = line.getCodeLabelItems();
		CodeLabelItem codeLabelItem = codeLabelItems.get(levelIdx);
		String code = codeLabelItem.getCode();
		CodeListItem item;
		if ( levelIdx == 0 ) {
			item = codeList.getItem(code);
			if ( item == null ) {
				item = codeList.createItem();
				fillItem(codeLabelItem, item);
				codeList.addItem(item);
			}
		} else {
			item = parentItem.getChildItem(code);
			if ( item == null ) {
				item = codeList.createItem();
				fillItem(codeLabelItem, item);
				parentItem.addChildItem(item);
			}
		}
		return item;
	}

	protected void fillItem(CodeLabelItem codeLabelItem, CodeListItem item) {
		item.setCode(codeLabelItem.getCode());
		item.setLabel(langCode, codeLabelItem.getLabel());
	}

	protected void addLevels(CodeList codeList) {
		List<String> levels = reader.getLevels();
		for (String levelName : levels) {
			CodeListLevel level = new CodeListLevel();
			level.setName(levelName);
			codeList.addLevel(level);
		}
	}
	
	private void close(Closeable closeable) {
		if ( closeable != null ) {
			try {
				closeable.close();
			} catch (IOException e) {
				LOG.error("Error closing stream: ", e);
			}
		}
	}
	
}
