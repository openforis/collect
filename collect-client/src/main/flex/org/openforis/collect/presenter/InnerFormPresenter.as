package org.openforis.collect.presenter
{
	import flash.display.DisplayObject;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.containers.GridItem;
	import mx.containers.GridRow;
	import mx.core.IVisualElement;
	
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.InnerFormContainer;
	import org.openforis.collect.ui.component.detail.MultipleEntityAsTableFormItem;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 */
	public class InnerFormPresenter extends AbstractPresenter {
		
		private static const LABEL_WIDTH:int = 150;
		
		private var _formItems:IList;

		public function InnerFormPresenter(view:DisplayObject) {
			super(view);
			_formItems = new ArrayCollection();
		}
		
		private function get view():InnerFormContainer {
			return InnerFormContainer(_view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			BindingUtils.bindSetter(nodeDefinitionsSetter, _view, "nodeDefinitions");
			BindingUtils.bindSetter(setViewHeight, _view, "height");
			BindingUtils.bindSetter(setViewWidth, _view, "width");
		}
		
		protected function setViewHeight(value:Number):void {
			if ( view.occupyAllAvailableSpace ) {
				var formItem:CollectFormItem = CollectFormItem(_formItems.getItemAt(0));
				formItem.parent.height = value;
				//formItem.height = value;
			}
			updateMultipleEntitiesMaxHeight(value);
		}

		protected function setViewWidth(value:Number):void {
			if ( view.occupyAllAvailableSpace ) {
				var formItem:CollectFormItem = CollectFormItem(_formItems.getItemAt(0));
				formItem.parent.width = value;
				//formItem.width = value;
			}
			updateMultipleEntitiesMaxWidth(value);
		}
		
		protected function updateMultipleEntitiesMaxHeight(value:Number):void {
			var maxAvailableHeight:Number = UIUtil.getMaxAvailableHeight(view);
			if ( ! isNaN(maxAvailableHeight) ) {
				for each ( var formItem:CollectFormItem in _formItems) {
					if ( formItem is MultipleEntityAsTableFormItem ) {
						formItem.maxHeight = maxAvailableHeight;
					}
				}
			}
		}
		
		protected function updateMultipleEntitiesMaxWidth(value:Number):void {
			var maxAvailableWidth:Number = UIUtil.getMaxAvailableWidth(view);
			if ( ! isNaN(maxAvailableWidth) ) {
				if ( ! view.occupyAllAvailableSpace ) {
					maxAvailableWidth -= 15;
				}
				for each ( var formItem:CollectFormItem in _formItems) {
					if ( formItem is MultipleEntityAsTableFormItem ) {
						formItem.maxWidth = maxAvailableWidth;
					}
				}
			}
		}

		protected function updateCurrentState():void {
			view.occupyAllAvailableSpace = containsOnlyOneMultipleEntity/* || isInsideFormLayoutEntity()*/;
			view.currentState = view.occupyAllAvailableSpace ? InnerFormContainer.STATE_ENLARGED: InnerFormContainer.STATE_DEFAULT;
		}
		
		protected function get containsOnlyOneMultipleEntity():Boolean {
			var defns:IList = view.nodeDefinitions;
			if ( defns != null && defns.length == 1) {
				var firstNode:NodeDefinitionProxy = NodeDefinitionProxy(defns.getItemAt(0));
				return firstNode is EntityDefinitionProxy && firstNode.multiple;
			} else {
				return false;
			}
		}
		
		private function isInsideFormLayoutEntity():Boolean {
			if ( view.entityDefinition != null && view.entityDefinition.parent != null && 
				view.entityDefinition.parent.multiple && view.entityDefinition.parentLayout == UIUtil.LAYOUT_FORM ) {
				return true;
			} else {
				return false;
			}
		}
		
		protected function nodeDefinitionsSetter(value:IList):void {
			updateCurrentState();
			buildGrid();
		}
		
		private function buildGrid():void {
			_formItems = new ArrayCollection();
			var row:GridRow = null;
			var lastCell:GridItem = null;
			var lastColPosition:int = 1;
			
			for each ( var nodeDefn:NodeDefinitionProxy in view.nodeDefinitions ) {
				var colSpan:int = 1;
				var colPosition:int = nodeDefn.column;
				if ( row == null || colPosition <= lastColPosition ) {
					//change row
					row = new GridRow();
					view.grid.addElement(row);
					if ( colPosition > 1 ) {
						//add empty cell
						var emptyCell:GridItem = new GridItem();
						emptyCell.colSpan = colPosition - 1;
						row.addElement(emptyCell);
					}
				} else if ( colPosition > (lastColPosition + 1) ) {
					//add span to last cell
					lastCell.colSpan += (colPosition - lastColPosition);
				}
				//create cell
				var cell:GridItem = new GridItem();
				var formItem:CollectFormItem = createFormItem(nodeDefn);
				_formItems.addItem(formItem);
				cell.addElement(formItem);
				row.addElement(cell);
				
				lastCell = cell;
				lastColPosition = colPosition + (colSpan - 1);
			}
		}
		
		private function createFormItem(defn:NodeDefinitionProxy):CollectFormItem {
			var formItem:CollectFormItem;
			if ( defn is AttributeDefinitionProxy ) {
				formItem = UIBuilder.getAttributeFormItem(AttributeDefinitionProxy(defn));
				formItem.labelWidth = LABEL_WIDTH;
			} else {
				formItem = UIBuilder.getEntityFormItem(EntityDefinitionProxy(defn));
				BindingUtils.bindProperty(formItem, "modelVersion", _view, "modelVersion");
			}
			BindingUtils.bindProperty(formItem, "parentEntity", _view, "parentEntity");
			BindingUtils.bindProperty(formItem, "occupyEntirePage", _view, "occupyAllAvailableSpace");
			return formItem;
		}
		
	}
}