package org.openforis.collect.manager.samplingDesignImport;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.referenceDataImport.ParsingError;
import org.openforis.collect.manager.referenceDataImport.ParsingException;
import org.openforis.collect.manager.referenceDataImport.ParsingError.ErrorType;
import org.openforis.collect.model.SamplingDesignItem;

/**
 * 
 * @author S. Ricci
 * 
 */
public class SamplingDesignImportProcess extends AbstractProcess<Void, SamplingDesignImportStatus> {

	private static Log LOG = LogFactory.getLog(SamplingDesignImportProcess.class);

	private static final String CSV = "csv";

	private SamplingDesignManager samplingDesignManager;
	private File file;
	private boolean overwriteAll;
	
	private SamplingDesignCSVReader reader;
	private String errorMessage;
	private List<SamplingDesignLine> lines;

	private int surveyId;
	private boolean work;
	
	public SamplingDesignImportProcess(SamplingDesignManager samplingDesignManager, int surveyId, boolean work, File file, boolean overwriteAll) {
		super();
		this.surveyId = surveyId;
		this.work = work;
		this.samplingDesignManager = samplingDesignManager;
		this.file = file;
		this.overwriteAll = overwriteAll;
	}
	
	@Override
	public void init() {
		super.init();
		lines = new ArrayList<SamplingDesignLine>();
	}
	
	@Override
	protected void initStatus() {
		status = new SamplingDesignImportStatus();
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
//		} else if (ZIP.equals(extension) ) {
//			processPackagedFile();
//			status.complete();
		} else {
			errorMessage = "File type not supported" + extension;
			status.setErrorMessage(errorMessage);
			status.error();
			LOG.error("Species import: " + errorMessage);
		}
		if ( status.isRunning() ) {
			processLines();
		}
		if ( status.isRunning() && ! status.hasErrors() ) {
			persistSamplingDesign();
		} else {
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
			
			reader = new SamplingDesignCSVReader(isReader);
			status.addProcessedRow(1);
			currentRowNumber = 2;
			while ( status.isRunning() ) {
				try {
					SamplingDesignLine line = reader.readNextLine();
					if ( line != null ) {
						lines.add(line);
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
	
	protected void processLines() {
		for (SamplingDesignLine line : lines) {
			long lineNumber = line.getLineNumber();
			if ( ! status.isRowProcessed(lineNumber) && ! status.isRowInError(lineNumber) ) {
				try {
					boolean processed = processLine(line);
					if (processed ) {
						status.addProcessedRow(lineNumber);
					}
				} catch (ParsingException e) {
					status.addParsingError(lineNumber, e.getError());
				}
			}
		}
	}
	
	protected boolean processLine(SamplingDesignLine line) throws ParsingException {
		checkDuplicateLine(line);
		return true;
	}

	protected void checkDuplicateLine(SamplingDesignLine line) throws ParsingException {
		for (SamplingDesignLine currentLine : lines) {
			if ( currentLine.getLineNumber() != line.getLineNumber() ) {
				if ( isDuplicateLocation(line, currentLine) ) {
					throwDuplicateLineException(line, currentLine, SamplingDesignFileColumn.LOCATION);
				} else if ( line.getLevelCodes().equals(currentLine.getLevelCodes()) ) {
					SamplingDesignFileColumn lastLevelCol = SamplingDesignCSVReader.LEVEL_COLUMNS[line.getLevelCodes().size() - 1];
					throwDuplicateLineException(line, currentLine, lastLevelCol);
				}
			}
		}
	}
	
	protected boolean isDuplicateLocation(SamplingDesignLine line1, SamplingDesignLine line2) throws ParsingException {
		List<String> line1LevelCodes = line1.getLevelCodes();
		List<String> line2LevelCodes = line2.getLevelCodes();
		if ( line2.getLocation().equals(line1.getLocation()) ) {
			if ( line2LevelCodes.size() == line1LevelCodes.size()) {
				return true;
			} else {
				int minLevelPosition = Math.min(line1LevelCodes.size(), line2LevelCodes.size());
				List<String> firstLevelCodes1 = line1LevelCodes.subList(0, minLevelPosition);
				List<String> firstLevelCodes2 = line2LevelCodes.subList(0, minLevelPosition);
				if ( ! firstLevelCodes1.equals(firstLevelCodes2) ) {
					return true;
				}
			}
		}
		return false;
	}

	protected void throwDuplicateLineException(SamplingDesignLine line, SamplingDesignLine duplicateLine, 
			SamplingDesignFileColumn column) throws ParsingException {
		ParsingError error = new ParsingError(
			ErrorType.DUPLICATE_VALUE, 
			line.getLineNumber(), 
			column.getName());
		String duplicateLineNumber = Long.toString(duplicateLine.getLineNumber());
		error.setMessageArgs(new String[] {duplicateLineNumber});
		throw new ParsingException(error);
	}

	protected void persistSamplingDesign() {
		if ( overwriteAll ) {
			if ( work ) {
				samplingDesignManager.deleteBySurveyWork(surveyId);
			} else {
				samplingDesignManager.deleteBySurvey(surveyId);
			}
		}
		for (SamplingDesignLine line : lines) {
			persistLine(line);
		}
	}

	protected void persistLine(SamplingDesignLine line) {
		SamplingDesignItem item = new SamplingDesignItem();
		if ( work ) {
			item.setSurveyWorkId(surveyId);
		} else {
			item.setSurveyId(surveyId);
		}
		item.setLocation(line.getLocation());
		item.setLevelCodes(line.getLevelCodes());
		samplingDesignManager.save(item);
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
