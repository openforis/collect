package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

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
			AbstractTreeNode<T> treeNode = (AbstractTreeNode<T>) getChild(selectionPath);
			AbstractTreeNode<T> parentTreeNode = (AbstractTreeNode<T>) treeNode.getParent();
			Set<TreeNode<T>> openObjects = getOpenObjects();
			parentTreeNode.remove(treeNode);
			setOpenObjects(openObjects);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void appendNodeToSelected(T data) {
		AbstractTreeNode<T> parentNode = getSelectedNode();
		if( parentNode == null ) {
			parentNode = (AbstractTreeNode<T>) getRoot();
		} else if ( parentNode.isLeaf() ) {
			parentNode = recreateNode(parentNode);
		}
		AbstractTreeNode<T> node = getNode(data);
		if ( node == null ) {
			node = createNode(data);
			parentNode.add(node);
		}
		addOpenObject(parentNode);
		setSelection(Arrays.asList(node));
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
	public void select(T data) {
		Collection<? extends TreeNode<T>> selection;
		if ( data == null ) {
			selection = Collections.emptyList();
		} else {
			AbstractTreeNode<T> treeNode = getNode(data);
			selection = Arrays.asList(treeNode);
		}
		setSelection(selection);
	}
	
	protected AbstractTreeNode<T> getNode(T data) {
		if ( data == null ) {
			return null;
		} else {
			int[] path = getNodePath(data);
			if ( path == null ) {
				return null;
			} else {
				return (AbstractTreeNode<T>) getChild(path);
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

	protected AbstractTreeNode<T> recreateNode(AbstractTreeNode<T> node) {
		AbstractTreeNode<T> parent = (AbstractTreeNode<T>) node.getParent();
		T data = node.getData();
		AbstractTreeNode<T> newNode = createNode(data, true);
		parent.replace(node, newNode);
		return newNode;
	}

	protected AbstractTreeNode<T> createNode(T data) {
		return createNode(data, false);
	}
	
	protected abstract AbstractTreeNode<T> createNode(
			T data, boolean defineEmptyChildrenForLeaves);

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
