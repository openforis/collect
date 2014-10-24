/**
 * 
 */
package org.openforis.idm.model;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;


/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class Node<D extends NodeDefinition> implements Serializable {

	private static final long serialVersionUID = 1L;

//	private final Log log = LogFactory.getLog(Node.class);
	
	transient D definition;
	transient Record record;
	transient Integer id;
	transient Integer internalId;
	transient Entity parent;
	transient int index;
	
	Integer definitionId;

	protected Node() {
		this.index = 0;
	}
	
	public Node(D definition) {
		if ( definition == null ) {
			throw new NullPointerException("Definition required");
		}
		this.definition = definition;
		this.definitionId = definition.getId();
	}
	
	protected abstract void write(StringWriter sw, int indent);

	public abstract boolean isEmpty();
	
	public abstract  boolean hasData();
	
	public List<Entity> getAncestors() {
		List<Entity> ancestors = new ArrayList<Entity>();
		Entity parent = getParent();
		while (parent != null) {
			ancestors.add(parent);
			parent = parent.getParent();
		}
		return ancestors;
	}
	
	public Entity getNearestAncestorMultipleEntity() {
		Entity currentParent = getParent();
		while ( currentParent != null && ! currentParent.getDefinition().isRoot() && ! currentParent.getDefinition().isMultiple() ) {
			currentParent = currentParent.getParent();
		}
		return currentParent;
	}
	
	public Set<Node<?>> getDescendantsAndSelf() {
		Set<Node<?>> nodesToBeDeleted = new HashSet<Node<?>>();
		nodesToBeDeleted.add(this);
		if ( this instanceof Entity ) {
			nodesToBeDeleted.addAll(((Entity) this).getDescendants());
		}
		return nodesToBeDeleted;
	}

	public String getPath() {
		return getPath(true);
	}
	
	public String getPath(boolean includeRoot) {
		StringBuilder sb = new StringBuilder();
		getPath(sb, includeRoot);
		return sb.toString();
	}
	
	protected void getPath(StringBuilder sb, boolean includeRoot) {
		if ( parent !=null && ( includeRoot || parent.parent != null ) ) {
			parent.getPath(sb, includeRoot);
		}
		String name = getName();
		int idx = getIndex();
		sb.append("/");
		sb.append(name);
		sb.append("[");
		sb.append(idx+1);
		sb.append("]");
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isRelevant() {
		return parent == null ? true : parent.isRelevant(getName());
	}
	
	public Survey getSurvey() {
		return record == null ? null : record.getSurvey();
	}
	
	public Schema getSchema() {
		return getSurvey() == null ? null : getSurvey().getSchema();
	}

	public boolean isDetached() {
		return record == null;
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

	public String getName() {
		return getDefinition().getName();
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
