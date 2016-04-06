package org.openforis.idm.path;

import static org.openforis.idm.path.Path.PARENT_ALIASES;
import static org.openforis.idm.path.Path.THIS_ALIASES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public class PathElement implements Axis {

	private static final Pattern PATTERN = Pattern.compile("(\\w+)(?:\\[(\\d+)\\])?");
	
	private String name;
	private Integer index;
	
	public PathElement(String name) {
		this.name = name;
		this.index = null;
	}
	
	public PathElement(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}
	
	public Integer getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		if ( index == null ) {
			return name;
		} else {
			return name + "[" + index + "]";			
		}
	}
	
	@Override
	public List<Node<?>> evaluate(Node<?> context) {
		if ( isParentFunction(name) ) {
			Entity parent = context.getParent();
			if ( parent == null ) { 
				return Collections.emptyList();
			} else {
				return Collections.<Node<?>>singletonList(parent);
			}
		} else if ( isThisFunction(name) ) {
			return Collections.<Node<?>>singletonList(context);
		} else if ( context instanceof Entity ) {
			return evaluateInternal((Entity) context);
		} if ( context instanceof Attribute ) {
			return evaluateInternal((Attribute<?, ?>) context);
		} else {			//  /cluster[0]/plot[2]/location[1]/x/y  (invalid)
			throw new IllegalArgumentException("Node path element parent must be either an entity or an attribute");
		}
	}

	private List<Node<?>> evaluateInternal(Entity parent) {
		List<Node<?>> children = parent.getChildren(name);
		if ( index == null ) {					//  /cluster[1]/plot[2]/location
			return children;
		} else {								//  /cluster[1]/plot[2]/location[1]
			if ( children.size() >= index ) {
				List<Node<?>> result = new ArrayList<Node<?>>(1);
				Node<?> node = children.get(index-1);
				result.add(node);
				return result;
			} else {
				return Collections.emptyList();
			}
		} 
	}

	@Override
	public List<Node<?>> evaluate(Record context) {
		Entity root = context.getRootEntity();
		if ( root.getName().equals(name) && (index == null || index == 1) ) {
			List<Node<?>> nodes = new ArrayList<Node<?>>(1);
			nodes.add(root);
			return Collections.unmodifiableList(nodes);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public NodeDefinition evaluate(Schema context) {
		return context.getRootEntityDefinition(name);
	}

	private List<Node<?>> evaluateInternal(Attribute<?, ?> parentAttribute) {
		if ( index == null || index == 1) {					// /cluster[0]/plot[2]/location[1]/x[1]
			if (parentAttribute.getDefinition().hasField(name)) {
				return Collections.<Node<?>>singletonList(parentAttribute.getField(name));
			} else {
				return null;
			}
		} else {								// /cluster[1]/plot[2]/location[1]/x[2] NO 
			throw new IllegalArgumentException("Index "+index+" out of bound; fields are always single");
		}
	}

	@Override
	public NodeDefinition evaluate(NodeDefinition context) {
		if ( isParentFunction(name) ) {
			return context.getParentDefinition();
		} else if ( isThisFunction(name) ) {
			return context;
		} else if ( context instanceof EntityDefinition ) {
			return evaluateInternal((EntityDefinition) context);
		} else if ( context instanceof AttributeDefinition ) {
			return evaluateInternal((AttributeDefinition) context);
		} else {
			throw new IllegalArgumentException("Parent definition must be Entity or Attribute");
		}
	}

	private NodeDefinition evaluateInternal(EntityDefinition context) {
		return context.getChildDefinition(name);
	}

	private NodeDefinition evaluateInternal(AttributeDefinition context) {
		return context.getFieldDefinition(name);
	}
	
	private static boolean isThisFunction(String name) {
		return THIS_ALIASES.contains(name);
	}

	private static boolean isParentFunction(String name) {
		return PARENT_ALIASES.contains(name);
	}

	public static PathElement parseElement(String s) throws InvalidPathException {
		if ( isParentFunction(s) || isThisFunction(s) ) {
			return new PathElement(s);
		}
		Matcher m = PATTERN.matcher(s);
		if ( ! m.matches() ) {
			throw new InvalidPathException(s);
		}
		
		try {
			String name = m.group(1);
			String sIdx = m.group(2);
			if ( sIdx == null ) {
				return new PathElement(name);
			} else {
				Integer idx = Integer.parseInt(sIdx);
				return new PathElement(name, idx);
			}
		} catch ( NumberFormatException e ) {
			throw new InvalidPathException(s);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		PathElement other = (PathElement) obj;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
