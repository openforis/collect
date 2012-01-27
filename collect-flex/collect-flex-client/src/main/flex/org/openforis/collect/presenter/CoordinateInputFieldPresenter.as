package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.ui.component.input.CoordinateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CoordinateInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:CoordinateInputField;
		
		public function CoordinateInputFieldPresenter(inputField:CoordinateInputField = null) {
			_view = inputField;
			_view.srsDropDown.labelFunction = srsDropDownLabelFunction;
			
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.xTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.xTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.yTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.yTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.srsDropDown.addEventListener(Event.CHANGE, srsDropDownChangeHandler);
			_view.srsDropDown.addEventListener(DropdownEvent.CLOSE, srsDropDownCloseHandler);
		}
		
		override public function set value(value:*):void {
			_attributeValue = value;
			//this._inputField.attribute = attribute;
			this._view.srsDropDown.selectedItem = value.text1;
			this._view.xTextInput.text = value.text2;
			this._view.yTextInput.text = value.text3;
			this._view.remarks = value.remarks;
			this._view.approved = value.approved;
		}
		
		override public function createValue():* {
			var newAttributeValue:Object = new Object();
			newAttributeValue.text1 = srsDropDownLabelFunction(_view.srsDropDown.selectedItem);
			newAttributeValue.text2 = _view.xTextInput.text;
			newAttributeValue.text3 = _view.yTextInput.text;
			
			if(value != null) {
				//copy old informations
				newAttributeValue.remarks = value.remarks;
			}
			return newAttributeValue;
		}
		
		protected function srsDropDownLabelFunction(item:Object):String {
			if(item != null) {
				return item.label;
			} else {
				return null;
			}	
		}
		
		protected function srsDropDownChangeHandler(event:Event):void {
			applyChanges();
		}
		
		protected function srsDropDownCloseHandler(event:Event):void {
		}
		
	}
}
