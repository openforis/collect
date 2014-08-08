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
	import org.openforis.collect.metamodel.ui.UIOptions$Direction;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.InnerFormContainer;
	import org.openforis.collect.ui.component.detail.MultipleEntityAsTableFormItem;
	import org.openforis.collect.ui.component.detail.TabbedFormContainer;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 */
	public class InnerFormPresenter extends AbstractPresenter {
		
		private static const DEFAULT_LABEL_WIDTH:int = 150;
		private static const INDENT_WIDTH:int = 20;
		private static const INDENTED_LABEL_WIDTH:int = DEFAULT_LABEL_WIDTH - INDENT_WIDTH;
		
		private var _formItems:IList;

		public function InnerFormPresenter(view:InnerFormContainer) {
			super(view);
			_formItems = new ArrayCollection();
		}
		
		private function get view():InnerFormContainer {
			return InnerFormContainer(_view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			BindingUtils.bindSetter(setViewHeight, _view, "height");
			BindingUtils.bindSetter(setViewWidth, _view, "width");
			BindingUtils.bindSetter(nodeDefinitionsSetter, _view, "nodeDefinitions");
		}
		
		protected function setViewHeight(value:Number):void {
			limitTableFormItemsMaxSize();
		}

		protected function setViewWidth(value:Number):void {
			limitTableFormItemsMaxSize();
		}
		
		private function limitTableFormItemsMaxSize():void {
			for each ( var formItem:CollectFormItem in _formItems) {
				if ( formItem is MultipleEntityAsTableFormItem ) {
					limitTableFormItemSize(MultipleEntityAsTableFormItem(formItem));
				}
			}
		}
		
		private function limitTableFormItemSize(formItem:CollectFormItem):void {
			var tabbedFormContainer:TabbedFormContainer = UIUtil.getFirstAncestor(view, TabbedFormContainer);
			if ( tabbedFormContainer != null ) {
				var maxAvailableHeight:Number = tabbedFormContainer.height;
				var maxAvailableWidth:Number = tabbedFormContainer.width;
			}
			//var maxAvailableHeight:Number = UIUtil.getMaxAvailableHeight(view);
			//var maxAvailableWidth:Number = UIUtil.getMaxAvailableWidth(view);
			if ( ! isNaN(maxAvailableWidth) && view.useScroller ) {
				maxAvailableWidth -= INDENT_WIDTH;
			}
			formItem.maxHeight = maxAvailableHeight;
			formItem.maxWidth = maxAvailableWidth;
		}
		
		protected function updateCurrentState():void {
			view.useScroller = ! (containsOnlyOneMultipleEntity || insideTable);
			view.currentState = view.useScroller ? InnerFormContainer.STATE_USE_SCROLLER: InnerFormContainer.STATE_DEFAULT;
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
		
		private function get insideTable():Boolean {
			return view.entityDefinition != null && view.entityDefinition.parentLayout == UIUtil.LAYOUT_TABLE;
		}
		
		private function get insideMultipleForm():Boolean {
			return view.entityDefinition != null && view.entityDefinition.multiple && view.entityDefinition.parentLayout == UIUtil.LAYOUT_FORM;
		}
		
		private function get insideSingleEntityForm():Boolean {
			return view.entityDefinition != null && ! view.entityDefinition.multiple && view.entityDefinition.parentLayout == UIUtil.LAYOUT_FORM;
		}
		
		protected function nodeDefinitionsSetter(value:IList):void {
			if ( value != null ) {
				updateCurrentState();
				buildGrid();
			}
		}
		
		private function buildGrid():void {
			_formItems = new ArrayCollection();
			var row:GridRow = null;
			var lastCell:GridItem = null;
			var lastColPosition:int = 1;
			
			//display nodes inline when rendering a single entity inside a table
			var displayInline:Boolean = 
					! view.entityDefinition.multiple && 
					view.entityDefinition.parentLayout == UIUtil.LAYOUT_TABLE &&
					view.entityDefinition.direction == UIOptions$Direction.BY_ROWS;
			
			for each ( var nodeDefn:NodeDefinitionProxy in view.nodeDefinitions ) {
				var colSpan:int = nodeDefn.columnSpan;
				var colPosition:int = nodeDefn.column;
				
				if ( row == null || ( colPosition <= lastColPosition && ! displayInline ) ) {
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
				cell.colSpan = colSpan;

				var formItem:CollectFormItem = createFormItem(nodeDefn);
				cell.addElement(formItem);
				
				row.addElement(cell);
				
				_formItems.addItem(formItem);
				lastCell = cell;
				lastColPosition = colPosition + (colSpan - 1);
			}
			if ( view.useScroller ) {
				view.scrollerContent.addElement(view.grid);
			} else {
				if ( lastCell != null ) {
					row.percentHeight = 100;
					row.percentWidth = 100;
					lastCell.percentHeight = 100;
					lastCell.percentWidth = 100;
					
				}
				view.addElement(view.grid);
			}
		}
		
		private function createFormItem(defn:NodeDefinitionProxy):CollectFormItem {
			var formItem:CollectFormItem;
			if ( defn is AttributeDefinitionProxy ) {
				formItem = UIBuilder.getAttributeFormItem(AttributeDefinitionProxy(defn));
				formItem.labelWidth = insideSingleEntityForm ? INDENTED_LABEL_WIDTH : DEFAULT_LABEL_WIDTH;
			} else {
				formItem = UIBuilder.getEntityFormItem(EntityDefinitionProxy(defn));
				BindingUtils.bindProperty(formItem, "modelVersion", _view, "modelVersion");
			}
			BindingUtils.bindProperty(formItem, "parentEntity", _view, "parentEntity");
			BindingUtils.bindProperty(formItem, "occupyEntirePage", _view, "notUsingScroller");
			
			if ( formItem is MultipleEntityAsTableFormItem ) {
				limitTableFormItemSize(formItem);
			}
			return formItem;
		}
		
	}
}