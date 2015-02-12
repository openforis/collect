package org.openforis.collect.ui.component.input
{
	import flash.events.Event;
	
	import mx.collections.IList;
	
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.presenter.DropDownInputFieldPresenter;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;

	/**
	 * @author S. Ricci
	 */
	public class PreloadedCodeInputField extends CodeInputField {
		
		public static const STATE_LOADING:String = "loading";
		public static const STATE_DEFAULT:String = "default";
		public static const STATE_VALUES_SORTING_ALLOWED:String = "valuesSortingAllowed";
		
		private var _attributes:IList;
		private var _items:IList;
		private var _selectableItems:IList;
		private var _selectedItems:IList;
		private var _notSelectedItems:IList;
		private var _reasonBlankItems:IList;
		private var _multiple:Boolean;
		private var _direction:String;
		private var _multipleSelectionAllowed:Boolean;
		
		public function PreloadedCodeInputField() {
			super();
		}
		
		public function labelFunction(item:Object):String {
			if ( DropDownInputFieldPresenter.isMissingValueItem(item) ) {
				return item.label;
			} else {
				var codeItem:CodeListItemProxy = CodeListItemProxy(item);
				var label:String = codeItem.getLabelText();
				var parts:Array = [];
				if ( CodeAttributeDefinitionProxy(attributeDefinition).showCode || StringUtil.isBlank(label) ) {
					parts.push(codeItem.code);
				}
				if ( StringUtil.isNotBlank(label) ) {
					parts.push(label);
				}
				var result:String = StringUtil.concat(" - ", parts);
				return result;
			}
		}
		
		public function selectionChangeHandler(event:UIEvent):void {
			var selectedItem:Object = event.obj;
			if ( ! multiple || ! multipleSelectionAllowed) {
				for each(var itm:Object in selectedItems) {
					if (itm != selectedItem) {
						//reset selection
						if (itm.hasOwnProperty("selected")) {
							itm.selected = false;
						}
						CollectionUtil.removeItem(selectedItems, itm);
					}
				}
			}
			if ( ! CollectionUtil.contains(selectedItems, selectedItem) && (! selectedItem.hasOwnProperty("selected") || selectedItem.selected) ) {
				selectedItems.addItem(selectedItem);
			} else {
				CollectionUtil.removeItem(selectedItems, selectedItem);
			}
			dispatchEvent(new Event("apply"));
		}
		
		[Bindable(event="selectedItemsChange")]
		public function get selectedIndex():int {
			trace("attribute: " + attributeDefinition.name);
			if ( selectableItems != null && selectedItems != null && selectedItems.length == 1 ) {
				var selectedItem:Object = selectedItems.getItemAt(0);
				var index:int = getSelectableItemIndex(selectedItem);
				return index;
			} else {
				return -1;
			}
		}
		
		private function getSelectableItemIndex(item:Object):int {
			var index:int;
			if (item is CodeListItemProxy) {
				index = CollectionUtil.getItemIndex(selectableItems, "code", item.code);
			} else if (DropDownInputFieldPresenter.isMissingValueItem(item)) { 
				index = CollectionUtil.getItemIndex(selectableItems, "shortCut", item.shortCut);
			} else {
				index = -1;
			}
			return index;
		}
		
		protected function applyHandler(event:Event):void {
			dispatchEvent(new Event("apply"));
		}
		
		protected function get prompt():String {
			return "";
		}
		
		[Bindable]
		public function get attributes():IList {
			return _attributes;
		}
		
		public function set attributes(value:IList):void {
			_attributes = value;
		}
		
		[Bindable]
		public function get items():IList {
			return _items;
		}
		
		public function set items(value:IList):void {
			_items = value;
		}
		
		[Bindable]
		public function get selectableItems():IList {
			return _selectableItems;
		}
		
		public function set selectableItems(value:IList):void {
			_selectableItems = value;
		}
		
		[Bindable(event="selectedItemsChange")]
		public function get selectedItems():IList {
			return _selectedItems;
		}
		
		public function set selectedItems(value:IList):void {
			_selectedItems = value;
			dispatchEvent(new Event("selectedItemsChange"));
		}

		[Bindable]
		public function get notSelectedItems():IList {
			return _notSelectedItems;
		}
		
		public function set notSelectedItems(value:IList):void {
			_notSelectedItems = value;
		}
		
		[Bindable]
		public function get reasonBlankItems():IList {
			return _reasonBlankItems;
		}
		
		public function set reasonBlankItems(value:IList):void {
			_reasonBlankItems = value;
		}

		[Bindable]
		public function get multiple():Boolean {
			return _multiple;
		}
		
		public function set multiple(value:Boolean):void {
			_multiple = value;
		}
		
		[Bindable]
		public function get direction():String {
			return _direction;
		}
		
		public function set direction(value:String):void {
			_direction = value;
		}
		
		[Bindable]
		public function get multipleSelectionAllowed():Boolean {
			return _multipleSelectionAllowed;
		}
		
		public function set multipleSelectionAllowed(value:Boolean):void {
			_multipleSelectionAllowed = value;
		}
		
	}
}