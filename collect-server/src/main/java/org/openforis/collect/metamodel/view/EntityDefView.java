package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.metamodel.NodeType;

public class EntityDefView extends NodeDefView {

	private List<NodeDefView> children;
	private boolean root;
	private boolean enumerate;
	
	public EntityDefView(boolean root, int id, String name, String label, 
			boolean multiple) {
		super(id, name, label, NodeType.ENTITY, false, multiple);
		this.root = root;
		children = new ArrayList<NodeDefView>();
	}

	public boolean isRoot() {
		return root;
	}
	
	public List<NodeDefView> getChildren() {
		return children;
	}

	public void addChild(NodeDefView child) {
		children.add(child);
	}
	
	public void setChildren(List<NodeDefView> children) {
		this.children = children;
	}

	public boolean isEnumerate() {
		return enumerate;
	}
	
	public void setEnumerate(boolean enumerate) {
		this.enumerate = enumerate;
	}
	
}