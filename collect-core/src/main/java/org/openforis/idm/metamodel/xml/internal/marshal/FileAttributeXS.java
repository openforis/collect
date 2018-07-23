package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.EXTENSIONS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.FILE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MAX_SIZE;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.FileAttributeDefinition;

/**
 * 
 * @author G. Miceli
 *
 */
class FileAttributeXS extends AttributeDefinitionXS<FileAttributeDefinition> {

	FileAttributeXS() {
		super(FILE);
	}
	
	@Override
	protected void attributes(FileAttributeDefinition defn) throws IOException {
		super.attributes(defn);
		List<String> extensions = defn.getExtensions();
		String extensionsStr = StringUtils.join(extensions, " ");
		attribute(MAX_SIZE, defn.getMaxSize());
		attribute(EXTENSIONS, extensionsStr);
	}
}
