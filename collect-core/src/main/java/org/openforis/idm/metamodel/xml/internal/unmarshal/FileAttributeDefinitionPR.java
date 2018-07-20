package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.EXTENSIONS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.FILE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MAX_SIZE;

import java.io.IOException;

import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 */
class FileAttributeDefinitionPR extends AttributeDefinitionPR {

	public FileAttributeDefinitionPR() {
		super(FILE);
	}

	@Override
	protected NodeDefinition createDefinition(int id) {
		Schema schema = getSchema();
		return schema.createFileAttributeDefinition(id);
	}
	
	@Override
	protected void onStartDefinition() throws XmlParseException, XmlPullParserException, IOException {
		super.onStartDefinition();
		String extensions = getAttribute(EXTENSIONS, false);
		Integer maxSize = getIntegerAttribute(MAX_SIZE, false);
		FileAttributeDefinition defn = (FileAttributeDefinition) getDefinition();
		defn.setMaxSize(maxSize);
		if ( extensions != null ) {
			String[] exts = extensions.split(" ");
			for (String ext : exts) {
				defn.addExtension(ext);
			}
		}
	}
}