package org.openforis.collect.ui.component.input
{
	import flash.events.Event;
	
	import mx.collections.IList;
	
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.presenter.DropDownInputFieldPresenter;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;

	/**
	 * @author S. Ricci
	 */
	public class PreloadedCodeInputField extends CodeInputField {
		
		public static const STATE_LOADING:String = "loading";
		public static const STATE_DEFAULT:String = "default";
		public static const STATE_VALUES_SORTING_ALLOWED:String = "valuesSortingAllowed";
		
		private var _items:IList;
		private var _selectedItems:IList;
		private var _notSelectedItems:IList;
		private var _reasonBlankItems:IList;
		private var _multiple:Boolean;
		private var _direction:String;
		
		public function PreloadedCodeInputField() {
			super();
		}
		
		public function labelFunction(item:Object):String {
			if ( ArrayUtil.contains([
				DropDownInputFieldPresenter.EMPTY_ITEM, 
				DropDownInputFieldPresenter.BLANK_ON_FORM_ITEM,
				DropDownInputFieldPresenter.DASH_ON_FORM_ITEM,
				DropDownInputFieldPresenter.ILLEGIBLE_ITEM
			], item) ) {
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
			var item:CodeListItemProxy = CodeListItemProxy(event.obj);
			if ( ! multiple ) {
				for each(var itm:Object in items) {
					if(itm != item) {
						//reset selection
						itm.selected = false;
						CollectionUtil.removeItem(selectedItems, itm);
					}
				}
			}
			if ( item.selected ) {
				selectedItems.addItem(item);
			} else {
				CollectionUtil.removeItem(selectedItems, item);
			}
			dispatchEvent(new Event("apply"));
		}
		
		protected function applyHandler(event:Event):void {
			dispatchEvent(new Event("apply"));
		}
		
		protected function get prompt():String {
			return "";
		}
		
		[Bindable]
		public function get items():IList {
			return _items;
		}
		
		public function set items(value:IList):void {
			_items = value;
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
		
		/*
		[Bindable(event="selectedItemChange")]
		public function get selectedIndex():int {
			if ( CollectionUtil.isEmpty(dataProvider) ) {
				return -1;
			} else {
				var idx:int = dataProvider.getItemIndex(selectedItem);
				return idx;
			}
		}
		
		[Bindable(event="selectedItemChange")]
		public function get selectedValue():String {
			if ( selectedItem == null ) {
				return null;
			} else if ( selectedItem is CodeListItemProxy ) {
				return CodeListItemProxy(selectedItem).code;
			} else {
				return String(selectedItem.shortCut);
			}
		}
		*/
	}
}