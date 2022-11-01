package org.openforis.collect.service;

import java.io.File;

import org.openforis.collect.manager.RecordFileManager;
import org.openforis.idm.metamodel.RecordFileService;
import org.openforis.idm.model.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;

public class CollectRecordFileService implements RecordFileService {

	@Autowired
	private RecordFileManager recordFileManager;
	
	@Override
	public File getRepositoryFile(FileAttribute fileAttribute) {
		return recordFileManager.getRepositoryFile(fileAttribute);
	}
	
}
