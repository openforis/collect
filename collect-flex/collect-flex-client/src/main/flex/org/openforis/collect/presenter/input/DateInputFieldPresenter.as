package org.openforis.collect.presenter.input {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.controls.DateField;
	import mx.events.CalendarLayoutChangeEvent;
	import mx.events.DropdownEvent;
	import mx.events.FlexEvent;
	
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.input.DateInputFieldEvent;
	import org.openforis.collect.idm.model.impl.AbstractValue;
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.ui.component.detail.input.DateInputField;
	import org.openforis.collect.ui.component.detail.input.InputField;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DateInputFieldPresenter extends InputFieldPresenter {
		
		[Bindable]
		public static var dateFormat:String = "DD/MM/YYYY";
		[Bindable]
		public static var separator:String = "/";

		private var _dateInputField:DateInputField;
		
		public function DateInputFieldPresenter(inputField:DateInputField = null) {
			super();
			this.inputField = inputField;
		}
		
		override public function set inputField(value:InputField):void {
			super.inputField = value;
			
			_dateInputField = value as DateInputField;
			
			if(_dateInputField != null) {
				_dateInputField.year.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_dateInputField.year.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				_dateInputField.month.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_dateInputField.month.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				_dateInputField.day.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_dateInputField.day.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				_dateInputField.dateField.addEventListener(CalendarLayoutChangeEvent.CHANGE, dateFieldChangeHandler);
				_dateInputField.dateField.addEventListener(DropdownEvent.OPEN, dateFieldOpenHandler);
				
				_dateInputField.separator1.text = separator;
				_dateInputField.separator2.text = separator;
				
				_dateInputField.dateField.formatString = dateFormat;
			}
		}
		
		override public function set value(value:AbstractValue):void {
			_attributeValue = value;
			//this._inputField.attribute = attribute;
			this._dateInputField.year.text = value.text1;
			this._dateInputField.month.text = value.text2;
			this._dateInputField.day.text = value.text3;
			this._inputField.remarks = value.remarks;
			this._inputField.approved = value.approved;
		}
		
		override public function createValue():AbstractValue {
			var newAttributeValue:AbstractValue = new AbstractValue();
			newAttributeValue.text1 = _dateInputField.year.text;
			newAttributeValue.text2 = _dateInputField.month.text;
			newAttributeValue.text3 = _dateInputField.day.text;
			
			if(value != null) {
				//copy old informations
				newAttributeValue.remarks = value.remarks;
			}
			return newAttributeValue;
		}
		
		protected function dateFieldOpenHandler(event:Event):void {
			var date:Date = getDateFromFields();
			if(date != null){
				_dateInputField.dateField.selectedDate = date;
			}
		}
		
		protected function dateFieldChangeHandler(event:Event):void {
			var date:Date = (event.target as DateField).selectedDate;
			setDateOnFields(date);
			
			applyChanges();
		}
		
		protected function setDateOnFields(date:Date):void {
			_dateInputField.year.text = String(date.fullYear);
			_dateInputField.month.text = String(date.month);
			_dateInputField.day.text = String(date.date);
		}
		
		protected function getDateFromFields():Date {
			//check if input text is valid
			if(StringUtil.isNotBlank(_dateInputField.day.text) && 
				StringUtil.isNotBlank(_dateInputField.month.text) && 
				StringUtil.isNotBlank(_dateInputField.year.text)) {
				var tempDate:Date = new Date(_dateInputField.year.text, _dateInputField.month.text, _dateInputField.day.text);
				//check that the generated date is valid
				if(String(tempDate.fullYear) == _dateInputField.year.text &&
					String(tempDate.month) == _dateInputField.month.text &&
					String(tempDate.day) == _dateInputField.day.text
				) {
					return tempDate;
				}
			}
			return null;
		}
		
	}
}
