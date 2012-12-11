/**
 * 
 */
package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;

/**
 * @author S. Ricci
 *
 */
public class UITabsTreeModel extends AbstractTreeModel<UITabSet> {

	private static final long serialVersionUID = 1L;
	
	UITabsTreeModel(UITabSetTreeNode root) {
		super(root);
	}

	public static UITabsTreeModel createInstance(UITabSet tabSet) {
		List<UITab> tabs = tabSet.getTabs();
		List<AbstractTreeNode<UITabSet>> treeNodes = UITabSetTreeNode.fromList(tabs);
		UITabSetTreeNode root = new UITabSetTreeNode(null, treeNodes);
		UITabsTreeModel result = new UITabsTreeModel(root);
		result.openAllItems();
		return result;
	}

	@Override
	protected AbstractTreeNode<UITabSet> createNode(
			UITabSet data, boolean defineEmptyChildrenForLeaves) {
		UITabSetTreeNode result = UITabSetTreeNode.createNode(data, defineEmptyChildrenForLeaves);
		return result;
	}
	
	@Override
	protected int[] getNodePath(UITabSet item) {
		List<Integer> result = new ArrayList<Integer>();
		UITabSet parent = item.getParent();
		UITabSet currentItem = item;
		while( parent != null ) {
			List<UITab> siblings = parent.getTabs();
			int index = siblings.indexOf(currentItem);
			result.add(0, index);
			currentItem = parent;
			parent = currentItem.getParent();
		}
		return toArray(result);
	}

	static class UITabSetTreeNode extends AbstractTreeNode<UITabSet> {

		private static final long serialVersionUID = 1L;
		
		UITabSetTreeNode(UITabSet data) {
			super(data);
		}
		
		UITabSetTreeNode(UITabSet data, Collection<AbstractTreeNode<UITabSet>> children) {
			super(data, children);
		}
		
		static UITabSetTreeNode createNode(UITabSet item, boolean defineEmptyChildrenForLeaves) {
			UITabSetTreeNode node = null;
			List<UITab> childItems = item.getTabs();
			List<AbstractTreeNode<UITabSet>> childNodes = fromList(childItems);
			if ( childNodes == null || childNodes.isEmpty() ) {
				if ( defineEmptyChildrenForLeaves ) {
					node = new UITabSetTreeNode(item, new ArrayList<AbstractTreeModel.AbstractTreeNode<UITabSet>>());
				} else {
					node = new UITabSetTreeNode(item);
				}
			} else {
				node = new UITabSetTreeNode(item, childNodes);
			}
			return node;
		}
		
		static List<AbstractTreeNode<UITabSet>> fromList(List<? extends UITabSet> items) {
			List<AbstractTreeNode<UITabSet>> result = null;
			if ( items != null ) {
				result = new ArrayList<AbstractTreeNode<UITabSet>>();
				for (UITabSet item : items) {
					UITabSetTreeNode node = createNode(item, false);
					if ( node != null ) {
						result.add(node);
					}
				}
			}
			return result;
		}

	}
	
}
