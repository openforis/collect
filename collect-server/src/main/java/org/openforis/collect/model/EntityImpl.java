/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.model.ModelObject;

/**
 * @author M. Togna
 * 
 */
@Entity
@Table(name = "entity")
public class EntityImpl extends AbstractModelObject<EntityDefinition> implements org.openforis.idm.model.Entity {

	private Map<String, List<ModelObject<? extends ModelObjectDefinition>>> children;

	public EntityImpl() {
		this.children = new HashMap<String, List<ModelObject<? extends ModelObjectDefinition>>>();
	}

	@Override
	public ModelObject<? extends ModelObjectDefinition> get(String name, int index) {
		List<ModelObject<? extends ModelObjectDefinition>> list = this.children.get(name);
		if (list != null) {
			return list.get(index);
		}
		return null;
	}

	@Override
	public void add(ModelObject<? extends ModelObjectDefinition> o) {
		this.beforeUpdate(o);

		String name = o.getDefinition().getName();
		List<ModelObject<? extends ModelObjectDefinition>> list = this.children.get(name);
		if (list == null) {
			list = new ArrayList<ModelObject<? extends ModelObjectDefinition>>();
			this.children.put(name, list);
		}
		list.add(o);

		this.updateList(list.size() - 1, name);
	}

	@Override
	public void add(ModelObject<? extends ModelObjectDefinition> o, int index) {
		this.beforeUpdate(o);

		String name = o.getDefinition().getName();
		List<ModelObject<? extends ModelObjectDefinition>> list = this.children.get(name);
		list.add(index, o);

		this.updateList(index, name);
	}

	@Override
	public int getCount(String name) {
		List<ModelObject<? extends ModelObjectDefinition>> list = this.children.get(name);
		return list != null ? list.size() : 0;
	}

	@Override
	public void move(String name, int oldIndex, int newIndex) {
		List<ModelObject<? extends ModelObjectDefinition>> list = this.children.get(name);
		ModelObject<? extends ModelObjectDefinition> obj = list.remove(oldIndex);
		list.add(newIndex, obj);
		updateList(newIndex, name);
	}

	@Override
	public ModelObject<? extends ModelObjectDefinition> remove(String name, int index) {
		List<ModelObject<? extends ModelObjectDefinition>> list = this.children.get(name);
		ModelObject<? extends ModelObjectDefinition> modelObject = list.remove(index);

		this.updateList(index, name);

		return modelObject;
	}

	@Override
	public Set<String> getChildNames() {
		Set<String> childNames = children.keySet();
		return Collections.unmodifiableSet(childNames);
	}

	// @Override
	// public void clear(String name) {
	// this.children.remove(name);
	// }
	//
	// @Override
	// public void clear() {
	// this.children.clear();
	// }

	// @Override
	// public ModelObject<? extends ModelObjectDefinition> set(ModelObject<? extends ModelObjectDefinition> o, int index) {
	// this.beforeUpdate(o);
	// String name = o.getDefinition().getName();
	// List<ModelObject<? extends ModelObjectDefinition>> list = this.get(name);
	// ModelObject<? extends ModelObjectDefinition> object = list.set(index, o);
	//
	// this.updateList(index, name);
	//
	// return object;
	// }

	private void beforeUpdate(ModelObject<? extends ModelObjectDefinition> o) {
		((AbstractModelObject<? extends ModelObjectDefinition>) o).setRecord(this.getRecord());
	}

	private void updateList(int fromIndex, String name) {
		List<ModelObject<? extends ModelObjectDefinition>> list = this.children.get(name);
		for (int i = fromIndex; i < list.size(); i++) {
			AbstractModelObject<? extends ModelObjectDefinition> modelObject = (AbstractModelObject<? extends ModelObjectDefinition>) list.get(i);
			modelObject.setPath(this.getPath() + "/" + name + "[" + i + "]");
		}
	}

}
