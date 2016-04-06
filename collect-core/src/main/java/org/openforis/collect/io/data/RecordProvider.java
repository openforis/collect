package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.concurrency.ProgressListener;

public interface RecordProvider {
	
	void init() throws Exception;
	
	void init(ProgressListener progressListener) throws Exception;
	
	String getEntryName(int entryId, Step step);
	
	CollectRecord provideRecord(int entryId, Step step) throws IOException, RecordParsingException;
	
	ParseRecordResult provideRecordParsingResult(int entryId, Step step) throws IOException;
	
	List<Integer> findEntryIds();

	
}