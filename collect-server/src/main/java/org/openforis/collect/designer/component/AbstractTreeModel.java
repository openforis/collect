package org.openforis.collect.designer.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractTreeModel<T> extends DefaultTreeModel<T> {
	
	private static final long serialVersionUID = 1L;
	
	AbstractTreeModel(AbstractTreeNode<T> root) {
		super(root);
	}
	
	public void deselect() {
		Collection<AbstractTreeNode<T>> emptySelection = Collections.emptyList();
		setSelection(emptySelection);
	}

	public void removeSelectedNode() {
		int[] selectionPath = getSelectionPath();
		if ( selectionPath != null ) {
			AbstractTreeNode<T> treeNode = (AbstractTreeNode<T>) getChild(selectionPath);
			AbstractTreeNode<T> parentTreeNode = (AbstractTreeNode<T>) treeNode.getParent();
			parentTreeNode.remove(treeNode);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void appendNodeToSelected(T item) {
		AbstractTreeNode<T> parentNode = getSelectedNode();
		AbstractTreeNode<T> nodeToSelect;
		if ( parentNode != null && parentNode.isLeaf() ) {
			parentNode = recreateNode(parentNode);
			nodeToSelect = getNode(item);
		} else {
			if ( parentNode == null) {
				parentNode = (AbstractTreeNode<T>) getRoot();
			}
			AbstractTreeNode<T> newNode = createNode(item);
			parentNode.add(newNode);
			nodeToSelect = newNode;
		}
		addOpenObject(parentNode);
		setSelection(Arrays.asList(nodeToSelect));
	}
	
	protected AbstractTreeNode<T> getSelectedNode() {
		int[] selectionPath = getSelectionPath();
		return selectionPath != null ? (AbstractTreeNode<T>) getChild(selectionPath): null;
	}

	protected AbstractTreeNode<T> getParentNode(T item) {
		AbstractTreeNode<T> node = getNode(item);
		AbstractTreeNode<T> parent = (AbstractTreeNode<T>) node.getParent();
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
	
	@SuppressWarnings("unchecked")
	public void select(T defn) {
		Collection<? extends TreeNode<T>> selection;
		if ( defn == null ) {
			selection = Collections.emptyList();
		} else {
			AbstractTreeNode<T> treeNode = getNode(defn);
			selection = Arrays.asList(treeNode);
		}
		setSelection(selection);
	}
	
	protected AbstractTreeNode<T> getNode(T item) {
		if ( item == null ) {
			return null;
		} else {
			int[] path = getNodePath(item);
			return (AbstractTreeNode<T>) getChild(path);
		}
	}
	
	protected abstract int[] getNodePath(T item);

	protected AbstractTreeNode<T> recreateNode(AbstractTreeNode<T> node) {
		AbstractTreeNode<T> parent = (AbstractTreeNode<T>) node.getParent();
		T data = node.getData();
		AbstractTreeNode<T> newNode = createNode(data);
		parent.replace(node, newNode);
		return newNode;
	}

	protected abstract AbstractTreeNode<T> createNode(T data);

	public void moveSelectedNode(int toIndex) {
		int[] selectionPath = getSelectionPath();
		AbstractTreeNode<T> treeNode = (AbstractTreeNode<T>) getChild(selectionPath);
		AbstractTreeNode<T> parentTreeNode = (AbstractTreeNode<T>) treeNode.getParent();
		Set<TreeNode<T>> openObjects = getOpenObjects();
		parentTreeNode.insert(treeNode, toIndex);
		setOpenObjects(openObjects);
		@SuppressWarnings("unchecked")
		List<AbstractTreeNode<T>> selection = Arrays.asList(treeNode);
		setSelection(selection);
	}

	static abstract class AbstractTreeNode<T> extends DefaultTreeNode<T> {
		
		private static final long serialVersionUID = 1L;
		
		AbstractTreeNode(T data) {
			super(data);
		}
		
		AbstractTreeNode(T data, Collection<AbstractTreeNode<T>> children) {
			super(data, children);
		}
		
		void replace(TreeNode<T> oldNode, TreeNode<T> newNode) {
			int index = this.getIndex(oldNode);
			this.remove(index);
			this.insert(newNode, index);
		}
		
	}
}
