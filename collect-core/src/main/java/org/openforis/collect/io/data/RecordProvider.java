package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;

public interface RecordProvider {
	CollectRecord provideRecord(int entryId, Step step) throws IOException;
	
	List<Integer> findEntryIds();
}