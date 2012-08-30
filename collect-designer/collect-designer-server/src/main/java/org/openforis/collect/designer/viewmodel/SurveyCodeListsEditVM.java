/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLabel.Type;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Versionable;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyCodeListsEditVM extends SurveyItemEditVM<CodeList> {

	private DefaultTreeModel<CodeListItem> treeModel;
	
	private CodeListItem editedChildItem;
	
	@Override
	public BindingListModelList<CodeList> getItems() {
		return new BindingListModelList<CodeList>(survey.getCodeLists(), false);
	}

	@Override
	protected void addNewItemToSurvey() {
		survey.addCodeList(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey() {
		survey.removeCodeList(selectedItem);
	}

	@NotifyChange({"selectedItem","editedItem","editingItem","childItems","itemLabel","itemListLabel","itemDescription"})
	@Command
	public void selectionChanged() {
	}
	
	@NotifyChange({"editingItem","editedItem","items","childItems","selectedItem","editedItemSinceVersion","editedItemDeprecatedVersion","itemLabel","itemListLabel","itemDescription"})
	@Command
	public void newItem() {
		super.newItem();
		editedChildItem = null;
	}
	
	@Override
	public void setEditedItem(CodeList editedItem) {
		super.setEditedItem(editedItem);
		initTreeModel();
	}
	
	@NotifyChange({"childItems","editedChildItem","editingChildItem"})
	@Command
	public void addRootChildItem() {
		CodeListItem item = new CodeListItem();
		editedItem.addItem(item);
		editedChildItem = item;
		addTreeNode(item);
	}

	@NotifyChange({"childItems","editedChildItem","editingChildItem","childItemLabel","childItemDescription","childItemQualifiable","childItemSinceVersion","childItemDeprecatedVersion"})
	@Command
	public void addChildItem() {
		CodeListItem item = new CodeListItem();
		editedChildItem.addChildItem(item);
		editedChildItem = item;
		addTreeNode(item);
	}
	
	protected void addTreeNode(CodeListItem item) {
		CodeListItemTreeNode treeNode = new CodeListItemTreeNode(item);
		int[] selectionPath = treeModel.getSelectionPath();
		if ( selectionPath == null || item.getParentItem() == null ) {
			treeModel.getRoot().add(treeNode);
		} else {
			TreeNode<CodeListItem> selectedTreeNode = treeModel.getChild(selectionPath);
			selectedTreeNode.add(treeNode);
		}
		treeModel.addOpenObject(treeNode.getParent());
		treeModel.setSelection(Arrays.asList(treeNode));
	}
	
	@NotifyChange({"childItems","editedChildItem","editingChildItem"})
	@Command
	public void deleteChildItem() {
		CodeListItem parentItem = editedChildItem.getParentItem();
		int id = editedChildItem.getId();
		if ( parentItem != null ) {
			parentItem.removeChildItem(id);
		} else {
			editedItem.removeItem(id);
		}
		editedChildItem = null;
		removeSelectedTreeNode();
		//initTreeModel();
	}

	private void removeSelectedTreeNode() {
		int[] selectionPath = treeModel.getSelectionPath();
		TreeNode<CodeListItem> treeNode = treeModel.getChild(selectionPath);
		TreeNode<CodeListItem> parentTreeNode = treeNode.getParent();
		parentTreeNode.remove(treeNode);
	}
	
	@NotifyChange({"editedChildItem","editingChildItem","childItemLabel","childItemDescription","childItemQualifiable","childItemSinceVersion","childItemDeprecatedVersion"})
	@Command
	public void childItemSelected(@BindingParam("item") Treeitem item) {
		if ( item != null ) {
			CodeListItemTreeNode treeNode = item.getValue();
			editedChildItem = treeNode.getData();
		} else {
			editedChildItem = null;
		}
	}
	
	public String getItemListLabel() {
		return editedItem != null ? editedItem.getLabel(Type.LIST, selectedLanguageCode): null;
	}
	
	public void setItemListLabel(String label) {
		if ( editedItem != null ) {
			editedItem.setLabel(Type.LIST, selectedLanguageCode, label);
		}
	}
	
	public String getItemLabel() {
		return editedItem != null ? editedItem.getLabel(Type.ITEM, selectedLanguageCode): null;
	}
	
	public void setItemLabel(String label) {
		if ( editedItem != null ) {
			editedItem.setLabel(Type.ITEM, selectedLanguageCode, label);
		}
	}
	
	public String getItemDescription() {
		return editedItem != null ? editedItem.getDescription(selectedLanguageCode): null;
	}

	public void setItemDescription(String description) {
		if ( editedItem != null ) {
			editedItem.setDescription(selectedLanguageCode, description);
		}
	}

	public DefaultTreeModel<CodeListItem> getChildItems() {
		return treeModel;
    }

	private void initTreeModel() {
		if ( editedItem != null ) {
			List<CodeListItem> items = editedItem.getItems();
			List<TreeNode<CodeListItem>> treeNodes = CodeListItemTreeNode.fromList(items);
			TreeNode<CodeListItem> root = new CodeListItemTreeNode(null, treeNodes);
			treeModel = new DefaultTreeModel<CodeListItem>(root);
		} else {
			treeModel = null;
		}
	}
	
	public CodeListItem getEditedChildItem() {
		return editedChildItem;
	}
	
	public boolean isEditingChildItem() {
		return editedChildItem != null;
	}

	public void setEditedChildItem(CodeListItem editedChildItem) {
		this.editedChildItem = editedChildItem;
	}
	
	public String getChildItemLabel() {
		return editedChildItem != null ? editedChildItem.getLabel(selectedLanguageCode): null;
	}
	
	public void setChildItemLabel(String label) {
		if ( editedChildItem != null ) {
			editedChildItem.setLabel(selectedLanguageCode, label);
		}
	}
	
	public String getChildItemDescription() {
		return editedChildItem != null ? editedChildItem.getDescription(selectedLanguageCode): null;
	}
	
	public void setChildItemDescription(String description) {
		if ( editedChildItem != null ) {
			editedChildItem.setDescription(selectedLanguageCode, description);
		}
	}
	
	public boolean isChildItemQualifiable() {
		return editedChildItem != null && editedChildItem.isQualifiable();
	}
	
	public void setChildItemQualifiable(boolean value) {
		if ( editedChildItem != null ) {
			editedChildItem.setQualifiable(value);
		}
	}
	
	public ModelVersion getChildItemSinceVersion() {
		if ( editedChildItem != null && editedChildItem instanceof Versionable ) {
			return ( (Versionable) editedChildItem).getSinceVersion();
		} else {
			return null;
		}
	}
	
	public void setChildItemSinceVersion(ModelVersion value) {
		if ( editedChildItem != null && editedChildItem instanceof Versionable ) {
			ModelVersion modelVersion = value == FormObject.VERSION_EMPTY_SELECTION ? null: value;
			( (Versionable) editedChildItem).setSinceVersion(modelVersion);
		}
	}

	public ModelVersion getChildItemDeprecatedVersion() {
		if ( editedChildItem != null && editedChildItem instanceof Versionable ) {
			return ( (Versionable) editedChildItem).getDeprecatedVersion();
		} else {
			return null;
		}
	}

	public void setChildItemDeprecatedVersion(ModelVersion value) {
		if ( editedChildItem != null && editedChildItem instanceof Versionable ) {
			ModelVersion modelVersion = value == FormObject.VERSION_EMPTY_SELECTION ? null: value;
			( (Versionable) editedChildItem).setDeprecatedVersion(modelVersion);
		}
	}

	
	public static class CodeListItemTreeNode extends DefaultTreeNode<CodeListItem> {
	     
		private static final long serialVersionUID = 1L;
		
		public CodeListItemTreeNode(CodeListItem data) {
			this(data, null);
		}

		public CodeListItemTreeNode(CodeListItem data, Collection<TreeNode<CodeListItem>> children) {
			super(data, children);
		}

		@Override
		public List<TreeNode<CodeListItem>> getChildren() {
			CodeListItem codeListItem = getData();
			if ( codeListItem != null ) {
				List<CodeListItem> items = codeListItem.getChildItems();
				return fromList(items);
			} else {
				return super.getChildren();
			}
		}

		public static List<TreeNode<CodeListItem>> fromList(List<CodeListItem> items) {
			List<TreeNode<CodeListItem>> result = null;
			if ( items != null ) {
				result = new ArrayList<TreeNode<CodeListItem>>();
				for (CodeListItem item : items) {
					List<CodeListItem> childItems = item.getChildItems();
					List<TreeNode<CodeListItem>> childrenNodes = fromList(childItems);
					CodeListItemTreeNode node = new CodeListItemTreeNode(item, childrenNodes);
					result.add(node);
				}
			}
			return result;
		}

	}
}
