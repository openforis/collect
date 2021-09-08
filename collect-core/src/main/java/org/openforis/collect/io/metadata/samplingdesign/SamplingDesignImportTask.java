package org.openforis.collect.io.metadata.samplingdesign;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.ReferenceDataImportTask;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignLine.SamplingDesignLineCodeKey;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 * 
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SamplingDesignImportTask extends ReferenceDataImportTask<ParsingError> {

	private static final String SURVEY_NOT_FOUND_ERROR_MESSAGE_KEY = "samplingDesignImport.error.surveyNotFound";
	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "samplingDesignImport.error.internalErrorImportingFile";
	
	private transient SamplingDesignManager samplingDesignManager;
	
	//input
	private transient CollectSurvey survey;
	private transient File file;
	private boolean overwriteAll;
	
	//internal variables
	private transient SamplingDesignCSVReader reader;
	private Map<SamplingDesignLineCodeKey, List<SamplingDesignLine>> linesByKey;
	private boolean skipValidation;

	public SamplingDesignImportTask() {
		this.skipValidation = false;
	}
	
	@Override
	protected void createInternalVariables() throws Throwable {
		linesByKey = new TreeMap<SamplingDesignLineCodeKey, List<SamplingDesignLine>>();
		reader = new SamplingDesignCSVReader(file);
		super.createInternalVariables();
	}
	
	@Override
	protected long countTotalItems() {
		try {
			return reader.size();
		} catch (IOException e) {
			throw new RuntimeException("Error calculating total items count: " + e.getMessage(), e);
		}
	}
	
	protected void validateParameters() {
		if ( ! file.exists() && ! file.canRead() ) {
			setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
			changeStatus(Status.FAILED);
		} else if ( survey == null ) {
			setErrorMessage(SURVEY_NOT_FOUND_ERROR_MESSAGE_KEY);
			changeStatus(Status.FAILED);
		}
	}
	
	@Override
	protected void execute() throws Throwable {
		validateParameters();
		if ( ! isFailed() ) {
			parseCSVLines();
			if ( isRunning() ) {
				processLines();
			}
			if ( isRunning() && ! hasErrors() ) {
				persistSamplingDesign();
			} else {
				changeStatus(Status.FAILED);
			}
			if ( isRunning() ) {
				changeStatus(Status.COMPLETED);
			}
		}
	}

	protected void parseCSVLines() {
		long currentRowNumber = 0;
		try {
			reader.init();
			addProcessedRow(1);
			currentRowNumber = 2;
			while ( isRunning() ) {
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
					addParsingError(currentRowNumber, e.getError());
				} finally {
					currentRowNumber ++;
				}
			}
		} catch (ParsingException e) {
			addParsingError(1, e.getError());
			changeStatus(Status.FAILED);
		} catch (Exception e) {
			addParsingError(currentRowNumber, new ParsingError(ErrorType.IOERROR, e.getMessage()));
			changeStatus(Status.FAILED);
			logError("Error importing species CSV file", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	protected void processLines() {
		for (List<SamplingDesignLine> lines : linesByKey.values()) {
			for (SamplingDesignLine line : lines) {
				long lineNumber = line.getLineNumber();
				if ( isRunning() && ! isRowProcessed(lineNumber) && ! isRowInError(lineNumber) ) {
					try {
						boolean valid = skipValidation || validateLine(line);
						if (valid ) {
							addProcessedRow(lineNumber);
						}
					} catch (ParsingException e) {
						addParsingError(lineNumber, e.getError());
					}
				}
			}
		}
	}
	
	protected boolean validateLine(SamplingDesignLine line) throws ParsingException {
		SamplingDesignLineValidator validator = SamplingDesignLineValidator.createInstance(survey);
		validator.validate(line);
		List<ParsingError> errors = validator.getErrors();
		for (ParsingError error : errors) {
			addParsingError(error);
		}
		checkDuplicateLine(line);
		return true;
	}

	protected void checkDuplicateLine(SamplingDesignLine line) throws ParsingException {
		List<SamplingDesignLine> lines = linesByKey.get(line.getKey());
		for (SamplingDesignLine existingLine : lines) {
			if (existingLine != null && existingLine.getLineNumber() != line.getLineNumber()) {
				if ( isDuplicateLocation(line, existingLine) ) {
					throwDuplicateLineException(line, existingLine, SamplingDesignFileColumn.LOCATION_COLUMNS);
				} else if ( line.getKey().getLevelCodes().equals(existingLine.getKey().getLevelCodes()) ) {
					SamplingDesignFileColumn lastLevelCol = SamplingDesignFileColumn.LEVEL_COLUMNS[line.getKey().getLevelCodes().size() - 1];
					throwDuplicateLineException(line, existingLine, new SamplingDesignFileColumn[]{lastLevelCol});
				}
			}
		}
	}
	
	protected boolean isDuplicateLocation(SamplingDesignLine line1, SamplingDesignLine line2) throws ParsingException {
		List<String> line1LevelCodes = line1.getKey().getLevelCodes();
		List<String> line2LevelCodes = line2.getKey().getLevelCodes();
		if ( line1.hasEqualLocation(line2) ) {
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

	protected void persistSamplingDesign() throws SurveyImportException {
		List<String> infoColumnNames = reader.getInfoColumnNames();
		List<ReferenceDataDefinition.Attribute> attributes = ReferenceDataDefinition.Attribute.fromNames(infoColumnNames);
		SamplingPointDefinition samplingPoint = new SamplingPointDefinition();
		samplingPoint.setAttributes(attributes);
		
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		if ( referenceDataSchema == null ) {
			referenceDataSchema = new ReferenceDataSchema();
			survey.setReferenceDataSchema(referenceDataSchema);
		}
		referenceDataSchema.setSamplingPointDefinition(samplingPoint);

		List<SamplingDesignItem> items = createItemsFromLines();
		samplingDesignManager.insert(survey, items, overwriteAll);
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
	
	public void setSamplingDesignManager(SamplingDesignManager samplingDesignManager) {
		this.samplingDesignManager = samplingDesignManager;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public boolean isOverwriteAll() {
		return overwriteAll;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}
	
	public boolean isSkipValidation() {
		return skipValidation;
	}
	
	public void setSkipValidation(boolean skipValidation) {
		this.skipValidation = skipValidation;
	}

}
