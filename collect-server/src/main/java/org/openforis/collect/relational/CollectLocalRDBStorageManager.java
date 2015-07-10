package org.openforis.collect.relational;

import java.io.File;

import javax.annotation.PostConstruct;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.manager.BaseStorageManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CollectLocalRDBStorageManager extends BaseStorageManager {
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_STORAGE_SUBFOLDER = "rdb";
	
	public CollectLocalRDBStorageManager() {
		super(DEFAULT_STORAGE_SUBFOLDER);
	}
	
	@PostConstruct
	public void init() {
		initStorageDirectory();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(ConfigurationItem.LOCAL_RDB_PATH);
	}

	public boolean existsRDBFile(String surveyName, RecordStep step) {
		File rdbFile = getRDBFile(surveyName, step);
		return rdbFile.exists();
	}
	
	public File getRDBFile(String surveyName, RecordStep step) {
		return new File(storageDirectory, getRDBFileName(surveyName, step));
	}

	private String getRDBFileName(String surveyName, RecordStep step) {
		return String.format("%s_%s.db", surveyName, step.name().toLowerCase());
	}

}
