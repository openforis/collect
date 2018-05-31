package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.openforis.collect.designer.component.BasicTreeModel.SimpleNodeData;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BasicTreeModel<T extends SimpleNodeData> extends DefaultTreeModel<T> {
	
	private static final long serialVersionUID = 1L;
	
	BasicTreeModel(AbstractNode<T> root) {
		super(root);
	}
	
	public void deselect() {
		Collection<AbstractNode<T>> emptySelection = Collections.emptyList();
		setSelection(emptySelection);
	}

	public Collection<TreeNode<T>> getAllItems() {
		Collection<TreeNode<T>> result = new ArrayList<TreeNode<T>>();
		Stack<TreeNode<T>> stack = new Stack<TreeNode<T>>();
		stack.push(getRoot());
		while ( ! stack.isEmpty() ) {
			TreeNode<T> treeNode = stack.pop();
			result.add(treeNode);
			List<TreeNode<T>> children = treeNode.getChildren();
			if ( children != null && ! children.isEmpty() ) {
				stack.addAll(children);
			}
		}
		return result;
	}
	
	public void openAllItems() {
		Collection<TreeNode<T>> allItems = getAllItems();
		setOpenObjects(allItems);
	}
	
	public void removeSelectedNode() {
		int[] selectionPath = getSelectionPath();
		if ( selectionPath != null ) {
			AbstractNode<T> treeNode = (AbstractNode<T>) getChild(selectionPath);
			AbstractNode<T> parentTreeNode = (AbstractNode<T>) treeNode.getParent();
			Set<TreeNode<T>> openObjects = getOpenObjects();
			parentTreeNode.remove(treeNode);
			setOpenObjects(openObjects);
		}
	}
	
	public void appendNodeToSelected(T data) {
		AbstractNode<T> parentNode = getSelectedNode();
		if( parentNode == null ) {
			parentNode = (AbstractNode<T>) getRoot();
		} else if ( parentNode.isLeaf() ) {
			parentNode = recreateNode(parentNode);
		}
		AbstractNode<T> node = getNode(data);
		if ( node == null ) {
			node = createNode(data);
			parentNode.add(node);
		}
		addOpenObject(parentNode);
		setSelection(Arrays.asList(node));
	}
	
	protected AbstractNode<T> getSelectedNode() {
		int[] selectionPath = getSelectionPath();
		return selectionPath != null ? (AbstractNode<T>) getChild(selectionPath): null;
	}

	protected AbstractNode<T> getParentNode(T item) {
		AbstractNode<T> node = getNode(item);
		AbstractNode<T> parent = (AbstractNode<T>) node.getParent();
		return parent;
	}

	protected int[] toArray(List<Integer> temp) {
		int[] result = new int[temp.size()];
		for (int i = 0; i < temp.size(); i++) {
			int value = temp.get(i).intValue();
			result[i] = value;
		}
		return result;
	}
	
	public void select(T data) {
		Collection<? extends TreeNode<T>> selection;
		if ( data == null ) {
			selection = Collections.emptyList();
		} else {
			AbstractNode<T> treeNode = getNode(data);
			selection = Arrays.asList(treeNode);
		}
		setSelection(selection);
	}
	
	protected AbstractNode<T> getNode(T data) {
		if ( data == null ) {
			return null;
		} else {
			int[] path = getNodePath(data);
			if ( path == null ) {
				return null;
			} else {
				return (AbstractNode<T>) getChild(path);
			}
		}
	}
	
	protected int[] getNodePath(T data) {
		 TreeNode<T> treeNode = getTreeNode(data);
		 if ( treeNode == null ) {
			 return null;
		 } else {
			 int[] result = getPath(treeNode);
			 return result;
		 }
	}

	protected TreeNode<T> getTreeNode(T data) {
		TreeNode<T> root = getRoot();
		Stack<TreeNode<T>> treeNodesStack = new Stack<TreeNode<T>>();
		treeNodesStack.push(root);
		while ( ! treeNodesStack.isEmpty() ) {
			TreeNode<T> treeNode = treeNodesStack.pop();
			T treeNodeData = treeNode.getData();
			if ( treeNodeData != null && treeNodeData.equals(data) ) {
				return treeNode;
			}
			List<TreeNode<T>> children = treeNode.getChildren();
			if ( children != null && children.size() > 0 ) {
				treeNodesStack.addAll(children);
			}
		}
		return null;
	}

	protected AbstractNode<T> recreateNode(AbstractNode<T> node) {
		AbstractNode<T> parent = (AbstractNode<T>) node.getParent();
		T data = node.getData();
		AbstractNode<T> newNode = createNode(data, true);
		parent.replace(node, newNode);
		return newNode;
	}

	protected AbstractNode<T> createNode(T data) {
		return createNode(data, false);
	}
	
	protected abstract AbstractNode<T> createNode(
			T data, boolean defineEmptyChildrenForLeaves);

	public void moveSelectedNode(int toIndex) {
		int[] selectionPath = getSelectionPath();
		AbstractNode<T> treeNode = (AbstractNode<T>) getChild(selectionPath);
		AbstractNode<T> parentTreeNode = (AbstractNode<T>) treeNode.getParent();
		Set<TreeNode<T>> openObjects = getOpenObjects();
		parentTreeNode.insert(treeNode, toIndex);
		setOpenObjects(openObjects);
		List<AbstractNode<T>> selection = Arrays.asList(treeNode);
		setSelection(selection);
	}

	public static abstract class AbstractNode<T> extends DefaultTreeNode<T> {
		
		private static final long serialVersionUID = 1L;
		
		AbstractNode(T data) {
			super(data);
		}
		
		AbstractNode(T data, Collection<? extends AbstractNode<T>> children) {
			super(data, children);
		}
		
		void replace(TreeNode<T> oldNode, TreeNode<T> newNode) {
			int index = this.getIndex(oldNode);
			this.remove(index);
			this.insert(newNode, index);
		}
		
		public String getIcon() {
			return null;
		}
		
	}
	
	public static abstract class SimpleNodeData {
		
		protected boolean detached;
		protected boolean root;
		protected String label;
		protected String icon;
		
		public SimpleNodeData(String label, boolean root, boolean detached) {
			super();
			this.detached = detached;
			this.root = root;
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
		
		public void setLabel(String label) {
			this.label = label;
		}
		
		public boolean isDetached() {
			return detached;
		}

		public void setDetached(boolean detached) {
			this.detached = detached;
		}
		
		public String getIcon() {
			return icon;
		}
		
		public void setIcon(String icon) {
			this.icon = icon;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (detached ? 1231 : 1237);
			result = prime * result + ((icon == null) ? 0 : icon.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			result = prime * result + (root ? 1231 : 1237);
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
			SimpleNodeData other = (SimpleNodeData) obj;
			if (detached != other.detached)
				return false;
			if (icon == null) {
				if (other.icon != null)
					return false;
			} else if (!icon.equals(other.icon))
				return false;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			if (root != other.root)
				return false;
			return true;
		}

		/*
		public void markAsDetached(boolean root) {
			if ( label == null ) {
				label = getDetachedLabel(nodeDefinition, root);
			}
		}
		*/
		
	}

}
