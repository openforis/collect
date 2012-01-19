package org.openforis.collect.presenter.input {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.controls.DateField;
	import mx.events.CalendarLayoutChangeEvent;
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.ui.component.input.DateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TimeInputField;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TimeInputFieldPresenter extends InputFieldPresenter {
		
		private var _timeInputField:TimeInputField;
		
		public function TimeInputFieldPresenter(inputField:TimeInputField = null) {
			super();
			this.inputField = inputField;
		}
		
		override public function set inputField(value:InputField):void {
			super.inputField = value;
			
			_timeInputField = value as TimeInputField;
			
			if(_timeInputField != null) {
				_timeInputField.hoursTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_timeInputField.minutesTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				_timeInputField.hoursTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_timeInputField.minutesTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				_timeInputField.hoursTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_timeInputField.minutesTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
			}
		}
		
		override public function set value(value:*):void {
		}
		
		override public function createValue():* {
			var result:* = null;
			return result;
			/*
			var newAttributeValue:AbstractValue = new AbstractValue();
			newAttributeValue.text1 = _timeInputField.hoursTextInput.text;
			newAttributeValue.text2 = _timeInputField.minutesTextInput.text;
			
			if(value != null) {
				//copy old informations
				newAttributeValue.remarks = value.remarks;
			}
			return newAttributeValue;
			*/
		}
		
		protected function setTimeOnFields(time:Object):void {
			_timeInputField.hoursTextInput.text = String(time.hour);
			_timeInputField.minutesTextInput.text = String(time.minute);
		}
		
		protected function getTimeFromFields():Date {
			//check if input text is valid
			if(StringUtil.isNotBlank(_timeInputField.hoursTextInput.text) && 
				StringUtil.isNotBlank(_timeInputField.minutesTextInput.text)) {
			}
			return null;
		}
		
	}
}
