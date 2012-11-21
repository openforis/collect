package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.openforis.collect.Application;
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
		
		public static const EMPTY_ITEM:Object = {label: Message.get('global.dropDownEmpty'), shortLabel: "", separator: false};
		public static const BLANK_ON_FORM_ITEM:Object = {label: Message.get('edit.dropDownList.blankOnForm'), shortLabel: "*", shortCut: "*", separator: true};
		public static const DASH_ON_FORM_ITEM:Object = {label: Message.get('edit.dropDownList.dashOnForm'), shortLabel: "-", shortCut: "-"};
		public static const ILLEGIBLE_ITEM:Object = {label: Message.get('edit.dropDownList.illegible'), shortLabel: "?", shortCut: "?"};

		private var _view:DropDownInputField;
		
		public function DropDownInputFieldPresenter(inputField:DropDownInputField) {
			_view = inputField;
			
			super(inputField);
			
			initInternalDataProvider();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.dropDownList.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.dropDownList.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.dropDownList.addEventListener(Event.CHANGE, changeHandler);
			_view.dropDownList.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
			eventDispatcher.addEventListener(UIEvent.ACTIVE_RECORD_CHANGED, activeRecordChangeHandler);
			
			BindingUtils.bindSetter(setDataProvider, _view, "dataProvider");
			BindingUtils.bindSetter(setDefaultValue, _view, "defaultValue");
		}
		
		override protected function changeHandler(event:Event):void {
			_view.changed = true;
			if(_view.applyChangesOnFocusOut) {
				updateValue();
			}
		}
		
		protected function activeRecordChangeHandler(event:Event):void {
			initInternalDataProvider();
			updateView();
		}
		
		protected function setDataProvider(value:IList):void {
			initInternalDataProvider();
			updateView();
		}
		
		protected function setDefaultValue(value:String):void {
			updateView();
		}
		
		protected function initInternalDataProvider():void {
			var temp:ArrayCollection = new ArrayCollection();
			temp.addItem(EMPTY_ITEM);
			if(_view.dataProvider != null) {
				temp.addAll(_view.dataProvider);
			}
			temp.addItem(BLANK_ON_FORM_ITEM);
			temp.addItem(DASH_ON_FORM_ITEM);
			temp.addItem(ILLEGIBLE_ITEM);
			
			_view.internalDataProvider = temp;
		}
		
		override protected function textToRequestValue():String {
			var result:String = null;
			var selectedItem:* = _view.dropDownList.selectedItem;
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
						var value:* = ObjectUtil.getValue(selectedItem, _view.dataField);
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
			var attribute:AttributeProxy = _view.attribute;
			if(attribute != null) {
				var field:FieldProxy = attribute.getField(_view.fieldIndex);
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
			if ( item == null && _view.defaultValue != null && (_view.attribute == null || _view.attribute.empty) ) {
				item = getItem(_view.defaultValue);
			}
			_view.editable = Application.activeRecordEditable;
			_view.dropDownList.selectedItem = item;
			_view.hasRemarks = hasRemarks;
			contextMenu.updateItems();
		}
		
		override protected function keyDownHandler(event:KeyboardEvent):void {
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
				_view.dropDownList.selectedItem = item;
				updateValue();
			}
		}

		protected function getItem(value:*):Object {
			var dataProvider:IList = _view.dropDownList.dataProvider;
			var dataField:String = _view.dataField;
			var item:Object = CollectionUtil.getItem(dataProvider, dataField, value);
			return item;
		}
	}
}
