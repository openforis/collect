package org.openforis.idm.metamodel;

import java.io.File;

import org.openforis.idm.model.FileAttribute;

public interface RecordFileService {

	File getRepositoryFile(FileAttribute fileAttribute);
	
}
