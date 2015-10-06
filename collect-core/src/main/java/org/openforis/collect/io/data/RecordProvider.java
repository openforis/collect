package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;

public interface RecordProvider {
	
	CollectRecord provideRecord(int entryId, Step step) throws IOException, RecordParsingException;
	
	ParseRecordResult provideRecordParsingResult(int entryId, Step step) throws IOException, RecordParsingException;
	
	List<Integer> findEntryIds();
	
}