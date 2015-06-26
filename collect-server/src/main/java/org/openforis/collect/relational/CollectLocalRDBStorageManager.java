package org.openforis.collect.relational;

import java.io.File;

import javax.annotation.PostConstruct;

import org.openforis.collect.manager.BaseStorageManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
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

	public boolean existsRDBFile(CollectSurvey survey, Step step) {
		File rdbFile = getRDBFile(survey, step);
		return rdbFile.exists();
	}
	
	public File getRDBFile(CollectSurvey survey, Step step) {
		return new File(storageDirectory, survey.getName() + "_" + step.getStepNumber() + ".db");
	}

}
