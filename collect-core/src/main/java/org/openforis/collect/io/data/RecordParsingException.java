package org.openforis.collect.io.data;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;

public class RecordParsingException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private ParseRecordResult parseRecordResult;
	private Step recordStep;
	
	public RecordParsingException(ParseRecordResult parseRecordResult, Step recordStep) {
		super("Error parsing record " + parseRecordResult);
		this.parseRecordResult = parseRecordResult;
		this.recordStep = recordStep;
	}
	
	public ParseRecordResult getParseRecordResult() {
		return parseRecordResult;
	}
	
	public Step getRecordStep() {
		return recordStep;
	}

}
