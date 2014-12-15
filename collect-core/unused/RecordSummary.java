/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class RecordSummary extends CollectRecord {

	public RecordSummary(Survey survey, String rootEntity, String versionName) {
		super(survey, rootEntity, versionName);
	}

	@Override
	public Entity getRootEntity() {
		throw new RuntimeException("Method not supported in Summary class");
	}

	@Override
	public Node<? extends NodeDefinition> getNodeById(int id) {
		throw new RuntimeException("Method not supported in Summary class");
	}
	
	
	
}
