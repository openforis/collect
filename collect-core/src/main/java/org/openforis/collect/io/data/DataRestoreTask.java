package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.EventQueue;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.event.RecordStep;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.collect.io.data.RecordImportError.Level;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordManager.RecordOperations;
import org.openforis.collect.manager.RecordManager.RecordStepOperation;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;
import org.openforis.collect.utils.Consumer;
import org.openforis.commons.collection.Predicate;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DataRestoreTask extends Task {

	@Autowired
	private EventQueue eventQueue;
	
	private RecordManager recordManager;
	private UserManager userManager;

	//input
	private RecordProvider recordProvider;
	
	private List<Integer> entryIdsToImport;
	private boolean overwriteAll;
	
	//output
	private final List<RecordImportError> errors;
	
	//temporary instance variables
	private final List<Integer> processedRecords;
	private final RecordUpdateBuffer updateBuffer;

	private Predicate<CollectRecord> includeRecordPredicate;

	public DataRestoreTask() {
		super();
		this.processedRecords = new ArrayList<Integer>();
		this.updateBuffer = new RecordUpdateBuffer();
		this.errors = new ArrayList<RecordImportError>();
	}

	@Override
	protected long countTotalItems() {
		List<Integer> idsToImport = calculateEntryIdsToImport();
		return idsToImport.size();
	}

	private List<Integer> calculateEntryIdsToImport() {
		if ( entryIdsToImport != null ) {
			return entryIdsToImport;
		} 
		if ( ! overwriteAll ) {
			throw new IllegalArgumentException("No entries to import specified and overwriteAll parameter is 'false'");
		}
		return recordProvider.findEntryIds();
	}
	
	@Override
	protected void execute() throws Throwable {
		List<Integer> idsToImport = calculateEntryIdsToImport();
		for (Integer entryId : idsToImport) {
			if ( isRunning() && ! processedRecords.contains(entryId) ) {
				importEntries(entryId);
				processedRecords.add(entryId);
				incrementProcessedItems();
			} else {
				break;
			}
		}
		updateBuffer.flush();
	}
	
	private void importEntries(int entryId) throws IOException, MissingStepsException {
		try {
			RecordOperationGenerator operationGenerator = new RecordOperationGenerator(recordProvider, recordManager, 
					entryId, includeRecordPredicate);
			RecordOperations recordOperations = operationGenerator.generate();
			if (! recordOperations.isEmpty()) {
				updateBuffer.append(recordOperations);
			}
		} catch(MissingStepsException e) {
			reportMissingStepsErrors(entryId, e);
		} catch (RecordParsingException e) {
			reportRecordParsingErrors(entryId, e);
		}
	}

	private void reportMissingStepsErrors(int entryId, MissingStepsException e) {
		Step originalStep = e.getOperations().getOriginalStep();
		String entryName = recordProvider.getEntryName(entryId, originalStep);
		errors.add(new RecordImportError(entryId, entryName, originalStep, 
				"Missing data for step", Level.ERROR));
	}

	private void reportRecordParsingErrors(int entryId, RecordParsingException e) {
		Step recordStep = e.getRecordStep();
		String entryName = recordProvider.getEntryName(entryId, recordStep);
		ParseRecordResult parseResult = e.getParseRecordResult();
		for (NodeUnmarshallingError failure : parseResult.getFailures()) {
			errors.add(new RecordImportError(entryId, entryName, recordStep, 
					String.format("%s : %s", failure.getPath(), failure.getMessage()), Level.ERROR));
		}
		for (NodeUnmarshallingError warn : parseResult.getWarnings()) {
			errors.add(new RecordImportError(entryId, entryName, recordStep, 
					String.format("%s : %s", warn.getPath(), warn.getMessage()), Level.WARNING));
		}
	}

	public RecordManager getRecordManager() {
		return recordManager;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public UserManager getUserManager() {
		return userManager;
	}
	
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public boolean isOverwriteAll() {
		return overwriteAll;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}

	public List<Integer> getEntryIdsToImport() {
		return entryIdsToImport;
	}

	public void setEntryIdsToImport(List<Integer> entryIdsToImport) {
		this.entryIdsToImport = entryIdsToImport;
	}
	
	public List<RecordImportError> getErrors() {
		return errors;
	}
	
	public void setRecordProvider(RecordProvider recordProvider) {
		this.recordProvider = recordProvider;
	}
	
	public void setIncludeRecordPredicate(Predicate<CollectRecord> includeRecordPredicate) {
		this.includeRecordPredicate = includeRecordPredicate;
	}
	
	private class RecordUpdateBuffer {
		
		public static final int BUFFER_SIZE = 100;
		/**
		 * limits the total size of the operations to be executed in batch to 30,000 record nodes
		 * in order to avoid a too big heap size growing during the database update process.
		 */
		public static final int MAX_OPERATIONS_SIZE = 30000; 
		
		private List<RecordOperations> operations = new ArrayList<RecordOperations>();
		private int operationsSize = 0;
		
		public void append(RecordOperations opts) {
			this.operations.add(opts);
			this.operationsSize += opts.getOperationsSize();
			
			if (this.operations.size() >= BUFFER_SIZE || 
					this.operationsSize >= MAX_OPERATIONS_SIZE) {
				flush();
			}
		}

		void flush() {
			if (eventQueue.isEnabled()) {
				recordManager.executeRecordOperations(operations, new EventPublisher());
			} else {
				recordManager.executeRecordOperations(operations);
			}
			operations.clear();
			operationsSize = 0;
		}
	}

	private final class EventPublisher implements Consumer<RecordStepOperation> {
		
		final EventProducer eventProducer = new EventProducer();
		
		@Override
		public void consume(RecordStepOperation operation) {
			CollectRecord record = operation.getRecord();
			String surveyName = record.getSurvey().getName();
			int recordId = record.getId();
			RecordStep recordStep = record.getStep().toRecordStep();
			
			String userName = record.getModifiedBy().getUsername();
			List<RecordEvent> events = eventProducer.produceFor(record, userName);
			
			if (! operation.isInsert()) {
				events.add(0, new RecordDeletedEvent(surveyName, recordId, new Date(), userName));
			}
			eventQueue.publish(new RecordTransaction(surveyName, 
					recordId, recordStep, events));
		}
	}

}
