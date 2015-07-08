package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordManager.RecordOperation;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.concurrency.Task;
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

	private RecordManager recordManager;
	private UserManager userManager;

	//input
	private ZipFile zipFile;
	
	private CollectSurvey packagedSurvey;
	private CollectSurvey existingSurvey;
	private List<Integer> entryIdsToImport;
	private boolean overwriteAll;
	
	//temporary instance variables
	private List<Integer> processedRecords;
	private RecordUpdateBuffer updateBuffer;
	private RecordProvider recordProvider;
	
	public DataRestoreTask() {
		super();
		this.processedRecords = new ArrayList<Integer>();
		this.updateBuffer = new RecordUpdateBuffer();
	}

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		this.recordProvider = new XMLParsingRecordProvider(zipFile, packagedSurvey, existingSurvey, userManager);
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
		processedRecords = new ArrayList<Integer>();
		List<Integer> idsToImport = calculateEntryIdsToImport();
		for (Integer entryId : idsToImport) {
			if ( isRunning() && ! processedRecords.contains(entryId) ) {
				importEntries(entryId);
				processedRecords.add(entryId);
				incrementItemsProcessed();
			} else {
				break;
			}
		}
		updateBuffer.flush();
	}
	
	private void importEntries(int entryId) throws IOException, DataImportExeption, RecordPersistenceException {
		RecordOperationGenerator operationGenerator = new RecordOperationGenerator(recordProvider, recordManager, entryId);
		List<RecordOperation> operations = operationGenerator.generate();
		updateBuffer.append(operations);
	}
	
//	private void replaceData(CollectRecord fromRecord, CollectRecord toRecord) {
//		toRecord.setCreatedBy(fromRecord.getCreatedBy());
//		toRecord.setCreationDate(fromRecord.getCreationDate());
//		toRecord.setModifiedBy(fromRecord.getModifiedBy());
//		toRecord.setModifiedDate(fromRecord.getModifiedDate());
//		toRecord.setStep(fromRecord.getStep());
//		toRecord.setState(fromRecord.getState());
//		toRecord.replaceRootEntity(fromRecord.getRootEntity());
//		validateRecord(toRecord);
//	}
	
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

	public ZipFile getZipFile() {
		return zipFile;
	}
	
	public void setZipFile(ZipFile zipFile) {
		this.zipFile = zipFile;
	}
	
	public CollectSurvey getPackagedSurvey() {
		return packagedSurvey;
	}
	
	public void setPackagedSurvey(CollectSurvey packagedSurvey) {
		this.packagedSurvey = packagedSurvey;
	}
	
	public CollectSurvey getExistingSurvey() {
		return existingSurvey;
	}
	
	public void setExistingSurvey(CollectSurvey existingSurvey) {
		this.existingSurvey = existingSurvey;
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

	private class RecordUpdateBuffer {
		
		public static final int BUFFER_SIZE = 50;
		
		private List<RecordOperation> operations = new ArrayList<RecordOperation>();
		
		public void append(List<RecordOperation> opts) {
			this.operations.addAll(opts);
			if (this.operations.size() >= BUFFER_SIZE) {
				flush();
			}
		}

		void flush() {
			recordManager.executeRecordOperations(operations);
			operations.clear();
		}
	}
	
}
