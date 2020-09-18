package org.openforis.collect.command.handler;

import org.openforis.collect.command.CreateRecordPreviewCommand;
import org.openforis.collect.manager.CachedRecordProvider;

public class CreateRecordPreviewHandler extends CreateRecordHandler<CreateRecordPreviewCommand> {
	
	@Override
	protected void persistRecord(RecordCommandResult result) {
		if (recordProvider instanceof CachedRecordProvider) {
			((CachedRecordProvider) recordProvider).putRecord(result.getRecord());
		}
	}

}
