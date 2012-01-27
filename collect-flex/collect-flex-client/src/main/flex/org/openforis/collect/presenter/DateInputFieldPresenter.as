package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.controls.DateField;
	import mx.events.CalendarLayoutChangeEvent;
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.ui.component.input.DateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DateInputFieldPresenter extends InputFieldPresenter {
		
		[Bindable]
		public static var dateFormat:String = "DD/MM/YYYY";
		[Bindable]
		public static var separator:String = "/";

		private var _view:DateInputField;
		
		public function DateInputFieldPresenter(inputField:DateInputField = null) {
			_view = inputField;
			_view.separator1.text = separator;
			_view.separator2.text = separator;
			
			_view.dateField.formatString = dateFormat;

			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.year.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.year.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.month.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.month.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.day.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.day.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.dateField.addEventListener(CalendarLayoutChangeEvent.CHANGE, dateFieldChangeHandler);
			_view.dateField.addEventListener(DropdownEvent.OPEN, dateFieldOpenHandler);
		}
		
		override public function set value(value:*):void {
			/*
			_attributeValue = value;
			//this._inputField.attribute = attribute;
			this._dateInputField.year.text = value.text1;
			this._dateInputField.month.text = value.text2;
			this._dateInputField.day.text = value.text3;
			this._inputField.remarks = value.remarks;
			this._inputField.approved = value.approved;
			*/
		}
		
		override public function createValue():* {
			var result:* = null;
			return result;
			/*
			var newAttributeValue:AbstractValue = new AbstractValue();
			newAttributeValue.text1 = _dateInputField.year.text;
			newAttributeValue.text2 = _dateInputField.month.text;
			newAttributeValue.text3 = _dateInputField.day.text;
			
			if(value != null) {
				//copy old informations
				newAttributeValue.remarks = value.remarks;
			}
			return newAttributeValue;
			*/
		}
		
		protected function dateFieldOpenHandler(event:Event):void {
			var date:Date = getDateFromFields();
			if(date != null){
				_view.dateField.selectedDate = date;
			}
		}
		
		protected function dateFieldChangeHandler(event:Event):void {
			var date:Date = (event.target as DateField).selectedDate;
			setDateOnFields(date);
			
			applyChanges();
		}
		
		protected function setDateOnFields(date:Date):void {
			_view.year.text = String(date.fullYear);
			_view.month.text = String(date.month);
			_view.day.text = String(date.date);
		}
		
		protected function getDateFromFields():Date {
			//check if input text is valid
			if(StringUtil.isNotBlank(_view.day.text) && 
				StringUtil.isNotBlank(_view.month.text) && 
				StringUtil.isNotBlank(_view.year.text)) {
				var tempDate:Date = new Date(_view.year.text, _view.month.text, _view.day.text);
				//check that the generated date is valid
				if(String(tempDate.fullYear) == _view.year.text &&
					String(tempDate.month) == _view.month.text &&
					String(tempDate.day) == _view.day.text
				) {
					return tempDate;
				}
			}
			return null;
		}
		
	}
}
