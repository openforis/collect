package org.openforis.collect.presenter.input {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.ui.component.input.CoordinateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CoordinateInputFieldPresenter extends InputFieldPresenter {
		
		private var _coordinateInputField:CoordinateInputField;
		
		public function CoordinateInputFieldPresenter(inputField:CoordinateInputField = null) {
			super();
			this.inputField = inputField;
		}
		
		override public function set inputField(value:InputField):void {
			super.inputField = value;
			
			_coordinateInputField = CoordinateInputField(value);
			
			if(_coordinateInputField != null) {
				_coordinateInputField.xTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_coordinateInputField.xTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				_coordinateInputField.yTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_coordinateInputField.yTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				_coordinateInputField.srsDropDown.labelFunction = srsDropDownLabelFunction;
				_coordinateInputField.srsDropDown.addEventListener(Event.CHANGE, srsDropDownChangeHandler);
				_coordinateInputField.srsDropDown.addEventListener(DropdownEvent.CLOSE, srsDropDownCloseHandler);
			}
		}
		
		override public function set value(value:*):void {
			_attributeValue = value;
			//this._inputField.attribute = attribute;
			this._coordinateInputField.srsDropDown.selectedItem = value.text1;
			this._coordinateInputField.xTextInput.text = value.text2;
			this._coordinateInputField.yTextInput.text = value.text3;
			this._inputField.remarks = value.remarks;
			this._inputField.approved = value.approved;
		}
		
		override public function createValue():* {
			var newAttributeValue:Object = new Object();
			newAttributeValue.text1 = srsDropDownLabelFunction(_coordinateInputField.srsDropDown.selectedItem);
			newAttributeValue.text2 = _coordinateInputField.xTextInput.text;
			newAttributeValue.text3 = _coordinateInputField.yTextInput.text;
			
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
