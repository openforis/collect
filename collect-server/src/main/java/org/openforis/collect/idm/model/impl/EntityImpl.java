/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;

/**
 * @author M. Togna
 * 
 */
public class EntityImpl extends AbstractModelObject<EntityDefinition> implements Entity {

	private Map<String, List<ModelObject<?>>> children;

	public EntityImpl() {
		this.children = new HashMap<String, List<ModelObject<?>>>();
	}

	@Override
	public ModelObject<?> get(String name, int index) {
		List<ModelObject<?>> list = this.get(name);
		if (list != null) {
			return list.get(index);
		}
		return null;
	}

	@Override
	public List<ModelObject<?>> get(String name) {
		return this.children.get(name);
	}

	@Override
	public void add(ModelObject<? extends ModelObjectDefinition> o) {
		beforeUpdate(o);
		String name = o.getDefinition().getName();
		List<ModelObject<?>> list = this.get(name);
		if (list == null) {
			list = new ArrayList<ModelObject<?>>();
			this.children.put(name, list);
		}
		list.add(o);
	}

	@Override
	public void add(ModelObject<? extends ModelObjectDefinition> o, int index) {
		beforeUpdate(o);
		String name = o.getDefinition().getName();
		List<ModelObject<?>> list = this.get(name);
		list.add(index, o);
	}

	@Override
	public ModelObject<? extends ModelObjectDefinition> remove(String name, int index) {
		List<ModelObject<? extends ModelObjectDefinition>> list = this.get(name);
		ModelObject<? extends ModelObjectDefinition> modelObject = list.remove(index);
		return modelObject;
	}

	@Override
	public void clear(String name) {
		this.children.remove(name);
	}

	@Override
	public void clear() {
		this.children.clear();
	}

	@Override
	public ModelObject<?> set(ModelObject<? extends ModelObjectDefinition> o, int index) {
		beforeUpdate(o);
		String name = o.getDefinition().getName();
		List<ModelObject<? extends ModelObjectDefinition>> list = this.get(name);
		ModelObject<? extends ModelObjectDefinition> object = list.set(index, o);
		return object;
	}

	private void beforeUpdate(ModelObject<? extends ModelObjectDefinition> o) {
		((AbstractModelObject<?>) o).setRecord(this.getRecord());
	}

}
