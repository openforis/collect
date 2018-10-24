/**
 * 
 */
package org.openforis.idm.model;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.lang.DeepComparable;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;


/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class Node<D extends NodeDefinition> implements Serializable, DeepComparable {

	private static final long serialVersionUID = 1L;

//	private final Logger log = Logger.getLogger(Node.class);
	
	transient final D definition;
	transient Record record;
	transient Integer id;
	transient Integer internalId;
	transient Entity parent;
	transient int index;
	transient String path;
	transient boolean detached;
	
	public Node(D definition) {
		if ( definition == null ) {
			throw new NullPointerException("Definition required");
		}
		this.definition = definition;
		this.detached = true;
	}
	
	protected abstract void write(StringWriter sw, int indent);

	public abstract boolean isEmpty();
	
	public abstract  boolean hasData();

	public boolean isUserSpecified() {
		return true;
	}
	
	public List<Entity> getAncestors() {
		List<Entity> ancestors = new ArrayList<Entity>();
		Entity parent = getParent();
		while (parent != null) {
			ancestors.add(parent);
			parent = parent.getParent();
		}
		return ancestors;
	}
	
	public Entity getAncestorByDefinition(EntityDefinition def) {
		List<Entity> ancestors = getAncestors();
		for (Entity ancestor : ancestors) {
			if (ancestor.getDefinition().equals(def)) {
				return ancestor;
			}
		}
		return null;
	}
	
	public Entity getNearestMultipleEntityAncestor() {
		Entity currentParent = getParent();
		while ( currentParent != null && ! currentParent.getDefinition().isRoot() && ! currentParent.getDefinition().isMultiple() ) {
			currentParent = currentParent.getParent();
		}
		return currentParent;
	}
	
	public List<Integer> getAncestorIds() {
		List<Entity> ancestors = getAncestors();
		List<Integer> ancestorIds = new ArrayList<Integer>(ancestors.size());
		for (Entity ancestor : ancestors) {
			ancestorIds.add(ancestor.getInternalId());
		}
		return ancestorIds;
	}

	public String getPath() {
		if ( path == null ) {
			updatePath();
		}
		return path;
	}

	protected void resetPath() {
		this.path = null;
	}
	
	private void updatePath() {
		StringBuilder sb = new StringBuilder();
		if ( parent != null ) {
			sb.append(parent.getPath());
		}
		sb.append("/");
		sb.append(getName());
		if (this.definition.isMultiple() && ! (this instanceof Entity && ((Entity) this).isRoot())) {
			sb.append("[");
			sb.append(getIndex() + 1);
			sb.append("]");
		}
		path = sb.toString();
	}

	public <C extends SurveyContext> C getSurveyContext() {
		return getSurvey().getContext();
	}
	
	public int getIndex() {
		return index;
	}
	
	protected void setIndex(int index) {
		this.index = index;
		resetPath();
	}

	public boolean isRelevant() {
		return parent == null ? true : parent.isRelevant(getDefinition());
	}
	
	public boolean isRequired() {
		return parent == null ? true : parent.isRequired(getDefinition());
	}
	
	public Survey getSurvey() {
		return definition.getSurvey();
	}
	
	public Schema getSchema() {
		return getSurvey().getSchema();
	}
	
	public ModelVersion getModelVersion() {
		return record == null ? null: record.getVersion();
	}
	
	public String getName() {
		return getDefinition().getName();
	}

	public Integer getParentId() {
		return parent == null ? null : parent.getInternalId();
	}

	public boolean isDetached() {
		return detached;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getInternalId() {
		return internalId;
	}
	
	public D getDefinition() {
		return this.definition;
	}

	public Record getRecord() {
		return record;
	}
	
	protected void setRecord(Record record) {
		this.record = record;
	}
	
	public Entity getParent() {
		return this.parent;
	}

	protected void setParent(Entity parent) {
		this.parent = parent;
		resetPath();
	}
	
	public boolean deepEquals(Object obj) {
		return equals(obj);
	}
	
	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		write(sw,0);
		return sw.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((internalId == null) ? 0 : internalId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node<?> other = (Node<?>) obj;
		if (internalId == null) {
			if (other.internalId != null)
				return false;
		} else if (!internalId.equals(other.internalId))
			return false;
		return true;
	}
	
}
