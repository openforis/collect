package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.ui.Keyboard;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.events.PropertyChangeEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.DropDownInputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DropDownInputFieldPresenter extends InputFieldPresenter {
		
		public static const EMPTY_ITEM:Object = {name: "", label: Message.get('global.dropDownEmpty'), shortLabel: "", shortCut: "", separator: false};
		public static const BLANK_ON_FORM_ITEM:Object = {name: FieldSymbol.BLANK_ON_FORM.name, label: Message.get('edit.dropDownList.blankOnForm'), shortLabel: "*", shortCut: "*", separator: true};
		public static const DASH_ON_FORM_ITEM:Object = {name: FieldSymbol.DASH_ON_FORM.name, label: Message.get('edit.dropDownList.dashOnForm'), shortLabel: "-", shortCut: "-"};
		public static const ILLEGIBLE_ITEM:Object = {name: FieldSymbol.ILLEGIBLE.name, label: Message.get('edit.dropDownList.illegible'), shortLabel: "?", shortCut: "?"};

		public static const REASON_BLANK_ITEMS:Array = [BLANK_ON_FORM_ITEM, DASH_ON_FORM_ITEM, ILLEGIBLE_ITEM];
		public static const MISSING_VALUE_ITEMS:Array = [EMPTY_ITEM, BLANK_ON_FORM_ITEM, DASH_ON_FORM_ITEM, ILLEGIBLE_ITEM];
		
		public static function isReasonBlankItem(item:Object):Boolean {
			return REASON_BLANK_ITEMS.indexOf(item) >= 0;
		}
		
		public static function isMissingValueItem(item:Object):Boolean {
			return MISSING_VALUE_ITEMS.indexOf(item) >= 0;
		}
		
		public function DropDownInputFieldPresenter(inputField:DropDownInputField) {
			super(inputField);
		}
		
		private function get view():DropDownInputField {
			return DropDownInputField(_view);
		}
		
		override public function init():void {
			super.init();
			initInternalDataProvider();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			view.dropDownList.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.dropDownList.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			view.dropDownList.addEventListener(Event.CHANGE, changeHandler);
			view.dropDownList.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
			eventDispatcher.addEventListener(UIEvent.ACTIVE_RECORD_CHANGED, activeRecordChangeHandler);
			
			ChangeWatcher.watch(view, "dataProvider", dataProviderChangeHandler);
			ChangeWatcher.watch(view, "defaultValue", defaultValueChangeHandler);
		}
		
		override protected function changeHandler(event:Event):void {
			view.changed = true;
			if(view.applyChangesOnFocusOut) {
				updateValue();
			}
		}
		
		override protected function setFocusHandler(event:InputFieldEvent):void {
			if ( view.dropDownList != null && view.attribute != null && 
				view.attribute.id == event.attributeId && 
				view.fieldIndex == event.fieldIdx ) {
				view.dropDownList.setFocus();
			}
		}
		
		protected function activeRecordChangeHandler(event:Event):void {
			initInternalDataProvider();
			updateView();
		}
		
		protected function dataProviderChangeHandler(event:PropertyChangeEvent):void {
			initInternalDataProvider();
			updateView();
		}
		
		protected function defaultValueChangeHandler(event:PropertyChangeEvent):void {
			updateView();
		}
		
		protected function initInternalDataProvider():void {
			var temp:ArrayCollection = new ArrayCollection();
			temp.addItem(EMPTY_ITEM);
			if(view.dataProvider != null) {
				temp.addAll(view.dataProvider);
			}
			temp.addItem(BLANK_ON_FORM_ITEM);
			temp.addItem(DASH_ON_FORM_ITEM);
			temp.addItem(ILLEGIBLE_ITEM);
			
			view.internalDataProvider = temp;
		}
		
		override protected function textToRequestValue():String {
			var result:String = null;
			var selectedItem:* = view.dropDownList.selectedItem;
			if(selectedItem != null) {
				switch(selectedItem) {
					case EMPTY_ITEM:
						break;
					case BLANK_ON_FORM_ITEM:
					case DASH_ON_FORM_ITEM:
					case ILLEGIBLE_ITEM:
						result = selectedItem.shortCut;
						break;
					default:
						var value:* = ObjectUtil.getValue(selectedItem, view.dataField);
						if(value != null) {
							result = String(value);
						}
				}
			}
			return result;
		}
		
		override protected function updateView():void {
			var item:Object = null;
			var hasRemarks:Boolean = false;
			var attribute:AttributeProxy = view.attribute;
			if(attribute != null) {
				var field:FieldProxy = attribute.getField(view.fieldIndex);
				hasRemarks = StringUtil.isNotBlank(getRemarks());
				var value:Object = field.value;
				if(field.symbol != null && FieldProxy.isReasonBlankSymbol(field.symbol)) {
					switch(field.symbol) {
						case FieldSymbol.BLANK_ON_FORM:
							item = BLANK_ON_FORM_ITEM;
							break;
						case FieldSymbol.DASH_ON_FORM:
							item = DASH_ON_FORM_ITEM;
							break;
						case FieldSymbol.ILLEGIBLE:
							item = ILLEGIBLE_ITEM;
							break;
					}
				} else if(value != null) {
					item = getItem(value);
				}
			}
			if ( item == null && view.defaultValue != null && (view.attribute == null || view.attribute.empty) ) {
				item = getItem(view.defaultValue);
			}
			view.editable = Application.activeRecordEditable && ! view.attributeDefinition.calculated;
			view.dropDownList.selectedItem = item;
			view.hasRemarks = hasRemarks;
			contextMenu.updateItems();
		}
		
		override protected function keyDownHandler(event:KeyboardEvent):void {
			if ( event.charCode == Keyboard.TAB ) {
				var offset:int = event.shiftKey ? -1: 1;
				moveFocusOnNextField(true, offset);
			} else {
				var item:Object = null;
				var char:String = String.fromCharCode(event.charCode);
				switch(char) {
					case FieldProxy.SHORTCUT_BLANK_ON_FORM:
						item = BLANK_ON_FORM_ITEM;
						break;
					case FieldProxy.SHORTCUT_DASH_ON_FORM:
						item = DASH_ON_FORM_ITEM;
						break;
					case FieldProxy.SHORTCUT_ILLEGIBLE:
						item = ILLEGIBLE_ITEM;
						break;
				}
				if(item != null) {
					view.dropDownList.selectedItem = item;
					updateValue();
				}
			}
		}

		protected function getItem(value:*):Object {
			var dataProvider:IList = view.dropDownList.dataProvider;
			var dataField:String = view.dataField;
			var item:Object = CollectionUtil.getItem(dataProvider, dataField, value);
			return item;
		}
	}
}
