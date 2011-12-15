/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.model.ModelObject;

/**
 * @author M. Togna
 * 
 */
public class ModelObjectListener {

	private List<ModelObject<? extends ModelObjectDefinition>> changedObjects;

	public ModelObjectListener() {
		super();
		this.changedObjects = new ArrayList<ModelObject<? extends ModelObjectDefinition>>();
	}

	public void onStateChange(ModelObject<? extends ModelObjectDefinition> modelObject) {
		this.changedObjects.add(modelObject);
	}

	public void clear() {
		this.changedObjects = new ArrayList<ModelObject<? extends ModelObjectDefinition>>();
	}

}
