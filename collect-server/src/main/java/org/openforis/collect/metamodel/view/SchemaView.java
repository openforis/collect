package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.List;

public class SchemaView {

	private List<EntityDefView> rootEntities = new ArrayList<EntityDefView>();

	public List<EntityDefView> getRootEntities() {
		return rootEntities;
	}

	public void addRootEntity(EntityDefView view) {
		this.rootEntities.add(view);
	}
	
}
