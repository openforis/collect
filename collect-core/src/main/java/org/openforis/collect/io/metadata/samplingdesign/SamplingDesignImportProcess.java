package org.openforis.collect.io.metadata.samplingdesign;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignLine.SamplingDesignLineCodeKey;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;

/**
 * 
 * @author S. Ricci
 * 
 */
public class SamplingDesignImportProcess extends AbstractProcess<Void, SamplingDesignImportStatus> {

	private static final String SURVEY_NOT_FOUND_ERROR_MESSAGE_KEY = "samplingDesignImport.error.surveyNotFound";
	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "samplingDesignImport.error.internalErrorImportingFile";
	
	private static Logger LOG = LogManager.getLogger(SamplingDesignImportProcess.class);
	
	private SamplingDesignManager samplingDesignManager;
	private SurveyManager surveyManager;
	private File file;
	private boolean overwriteAll;
	
	private SamplingDesignCSVReader reader;
	private Map<SamplingDesignLineCodeKey, List<SamplingDesignLine>> linesByKey;

	private CollectSurvey survey;
	
	public SamplingDesignImportProcess(SamplingDesignManager samplingDesignManager, SurveyManager surveyManager, 
			CollectSurvey survey, File file, boolean overwriteAll) {
		super();
		this.samplingDesignManager = samplingDesignManager;
		this.surveyManager = surveyManager;
		this.survey = survey;
		this.file = file;
		this.overwriteAll = overwriteAll;
	}
	
	@Override
	public void init() {
		super.init();
		linesByKey = new TreeMap<SamplingDesignLineCodeKey, List<SamplingDesignLine>>();
		validateParameters();
	}

	protected void validateParameters() {
		if ( ! file.exists() && ! file.canRead() ) {
			status.error();
			status.setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
		} else if ( survey == null ) {
			status.error();
			status.setErrorMessage(SURVEY_NOT_FOUND_ERROR_MESSAGE_KEY);
		}
	}
	
	@Override
	protected void initStatus() {
		status = new SamplingDesignImportStatus();
	}

	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		parseCSVLines(file);

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
		long currentRowNumber = 0;
		try {
			reader = new SamplingDesignCSVReader(file);
			reader.init();
			status.addProcessedRow(1);
			status.setTotal(reader.size());
			currentRowNumber = 2;
			while ( status.isRunning() ) {
				try {
					SamplingDesignLine line = reader.readNextLine();
					if ( line != null ) {
						List<SamplingDesignLine> lines = linesByKey.get(line.getKey());
						if (lines == null) {
							lines = new ArrayList<SamplingDesignLine>();
							linesByKey.put(line.getKey(), lines);
						}
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
			LOG.error("Error importing sampling design CSV file", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	protected void processLines() {
		for (List<SamplingDesignLine> lines : linesByKey.values()) {
			for (SamplingDesignLine line : lines) {
				long lineNumber = line.getLineNumber();
				if ( status.isRunning() && ! status.isRowProcessed(lineNumber) && ! status.isRowInError(lineNumber) ) {
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
	}
	
	protected boolean processLine(SamplingDesignLine line) throws ParsingException {
		SamplingDesignLineValidator validator = SamplingDesignLineValidator.createInstance(survey);
		validator.validate(line);
		List<ParsingError> errors = validator.getErrors();
		for (ParsingError error : errors) {
			status.addParsingError(error);
		}
		checkDuplicateLine(line);
		return true;
	}

	protected void checkDuplicateLine(SamplingDesignLine line) throws ParsingException {
		List<SamplingDesignLine> lines = linesByKey.get(line.getKey());
		for (SamplingDesignLine existingLine : lines) {
			if (existingLine != null && existingLine.getLineNumber() != line.getLineNumber()) {
				SamplingDesignFileColumn lastLevelCol = SamplingDesignFileColumn.LEVEL_COLUMNS[line.getKey().getLevelCodes().size() - 1];
				throwDuplicateLineException(line, existingLine, new SamplingDesignFileColumn[]{lastLevelCol});
			}
		}
//		for (SamplingDesignLine currentLine : lines) {
//			if ( currentLine.getLineNumber() != line.getLineNumber() ) {
//				if ( line.getLevelCodes().equals(currentLine.getLevelCodes()) ) {
//					SamplingDesignFileColumn lastLevelCol = SamplingDesignFileColumn.LEVEL_COLUMNS[line.getLevelCodes().size() - 1];
//					throwDuplicateLineException(line, currentLine, new SamplingDesignFileColumn[]{lastLevelCol});
//				}
//			}
//		}
	}
	
	protected void throwDuplicateLineException(SamplingDesignLine line, SamplingDesignLine duplicateLine, 
			SamplingDesignFileColumn[] columns) throws ParsingException {
		String[] colNames = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			SamplingDesignFileColumn column = columns[i];
			colNames[i] = column.getColumnName();
		}
		ParsingError error = new ParsingError(
			ErrorType.DUPLICATE_VALUE, 
			line.getLineNumber(), 
			colNames);
		String duplicateLineNumber = Long.toString(duplicateLine.getLineNumber());
		error.setMessageArgs(new String[] {duplicateLineNumber});
		throw new ParsingException(error);
	}

	protected void persistSamplingDesign() throws SurveyStoreException {
		List<String> infoColumnNames = reader.getInfoColumnNames();
		List<ReferenceDataDefinition.Attribute> attributes = ReferenceDataDefinition.Attribute.fromNames(infoColumnNames);
		SamplingPointDefinition samplingPoint;
		if ( attributes.isEmpty() ) {
			samplingPoint = null;
		} else {
			samplingPoint = new SamplingPointDefinition();
			samplingPoint.setAttributes(attributes);
		}
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		if ( referenceDataSchema == null ) {
			referenceDataSchema = new ReferenceDataSchema();
			survey.setReferenceDataSchema(referenceDataSchema);
		}
		referenceDataSchema.setSamplingPointDefinition(samplingPoint);
		saveSurvey();

		List<SamplingDesignItem> items = createItemsFromLines();
		samplingDesignManager.insert(survey, items, overwriteAll);
	}

	@SuppressWarnings("deprecation")
	private void saveSurvey() throws SurveyStoreException {
		if ( survey.isTemporary() ) {
			surveyManager.save(survey);
		} else {
			surveyManager.updateModel(survey);
		}
	}

	protected List<SamplingDesignItem> createItemsFromLines() {
		List<SamplingDesignItem> items = new ArrayList<SamplingDesignItem>();
		for (List<SamplingDesignLine> lines : linesByKey.values()) {
			for (SamplingDesignLine line : lines) {
				SamplingDesignItem item = line.toSamplingDesignItem(survey, reader.getInfoColumnNames());
				items.add(item);
			}
		}
		return items;
	}
	
	public List<String> getInfoColumnNames() {
		return reader.getInfoColumnNames();
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
}
