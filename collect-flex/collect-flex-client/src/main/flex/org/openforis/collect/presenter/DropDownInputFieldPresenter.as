package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.collections.IList;
	
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.DropDownInputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DropDownInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:DropDownInputField;
		
		public function DropDownInputFieldPresenter(inputField:DropDownInputField) {
			_view = inputField;
			
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			//TODO binding
			_view.dropDownList.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.dropDownList.addEventListener(Event.CHANGE, changeHandler);
			
		}
		
		override protected function changeHandler(event:Event):void {
			applyValue();
		}
		
		override protected function textToRequestValue():String {
			var value:String = null;
			var selectedItem:* = _view.dropDownList.selectedItem;
			if(selectedItem != null) {
				value = String(ObjectUtil.getValue(selectedItem, _view.dataField));
			}
			return value;
		}
		
		override protected function updateView():void {
			var item:Object = null;
			var attribute:AttributeProxy = _view.attribute;
			if(attribute != null) {
				var field:FieldProxy = attribute.getField(_view.fieldIndex);
				var value:Object = field.value;
				if(field.symbol != null) {
				}
				item = getItem(value);
			}
			_view.dropDownList.selectedItem = item;
		}
		
		protected function getItem(value:*):Object {
			var dataProvider:IList = _view.dropDownList.dataProvider;
			var dataField:String = _view.dataField;
			var item:Object = CollectionUtil.getItem(dataProvider, dataField, value);
			return item;
		}
		
		
	}
}
