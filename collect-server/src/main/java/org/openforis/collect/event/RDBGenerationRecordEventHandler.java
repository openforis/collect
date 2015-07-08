package org.openforis.collect.event;

import org.fao.foris.simpleeventbroker.AbstractEventHandler;
import org.fao.foris.simpleeventbroker.EventHandlerMonitor;
import org.openforis.collect.manager.CollectRDBGenerator;

public class RDBGenerationRecordEventHandler extends AbstractEventHandler {

	private static final int TIEMOUT_MILLIS = 60000;
	private CollectRDBGenerator rdbGenerator;

	protected RDBGenerationRecordEventHandler(CollectRDBGenerator rdbGenerator) {
		super(RDBGenerationRecordEventHandler.class.getSimpleName(), TIEMOUT_MILLIS);
		this.rdbGenerator = rdbGenerator;
	}

	@Override
	public void handle(Object recordTransaction, EventHandlerMonitor eventHandlerMonitor) {
		rdbGenerator.process((RecordTransaction) recordTransaction);
	}

	
}
