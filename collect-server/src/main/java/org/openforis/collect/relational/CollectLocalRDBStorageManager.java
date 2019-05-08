package org.openforis.collect.relational;

import java.io.File;
import java.util.Date;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.manager.BaseStorageManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.utils.Dates;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CollectLocalRDBStorageManager extends BaseStorageManager implements InitializingBean {
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_STORAGE_SUBFOLDER = "rdb";
	
	public CollectLocalRDBStorageManager() {
		super(DEFAULT_STORAGE_SUBFOLDER);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}
	
	public void init() {
		initStorageDirectory();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(ConfigurationItem.RDB_PATH);
	}

	public boolean existsRDBFile(String surveyName, RecordStep step) {
		File rdbFile = getRDBFile(surveyName, step);
		return rdbFile.exists() && rdbFile.length() > 0;
	}
	
	public Date getRDBFileDate(String surveyName, RecordStep step) {
		File rdbFile = getRDBFile(surveyName, step);
		if (rdbFile.exists()) {
			return Dates.millisToDate(rdbFile.lastModified());
		} else {
			return null;
		}
	}
	
	public File getRDBFile(String surveyName, RecordStep step) {
		return new File(storageDirectory, getRDBFileName(surveyName, step));
	}
	
	public boolean deleteRDBFile(String surveyName, RecordStep step) {
		File rdbFile = getRDBFile(surveyName, step);
		File rdbJournalFile = new File(getRDBJournalFileName(surveyName, step));
		rdbJournalFile.delete(); //don't care if it exists or not
		return rdbFile.delete();
	}

	private String getRDBFileName(String surveyName, RecordStep step) {
		return String.format("%s_%s.db", surveyName, step.nameLowerCase());
	}

	private String getRDBJournalFileName(String surveyName, RecordStep step) {
		return String.format("%s_%s.db-journal", surveyName, step.nameLowerCase());
	}
}
