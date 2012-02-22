package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.controls.DateField;
	import mx.events.CalendarLayoutChangeEvent;
	import mx.events.DropdownEvent;
	import mx.managers.IFocusManagerComponent;
	
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.model.proxy.DateProxy;
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
		
		public function DateInputFieldPresenter(inputField:DateInputField) {
			_view = inputField;
			_view.separator1.text = separator;
			_view.separator2.text = separator;
			
			_view.dateField.formatString = dateFormat;

			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();

			_view.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			
			//year
			_view.year.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.year.addEventListener(Event.CHANGE, changeHandler);
			//month
			_view.month.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.month.addEventListener(Event.CHANGE, changeHandler);
			//day
			_view.day.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.day.addEventListener(Event.CHANGE, changeHandler);
			//dateField (calendar button)
			_view.dateField.addEventListener(CalendarLayoutChangeEvent.CHANGE, dateFieldChangeHandler);
			_view.dateField.addEventListener(DropdownEvent.OPEN, dateFieldOpenHandler);
		}

		override protected function focusOutHandler(event:FocusEvent):void {
			var focussedField:IFocusManagerComponent = _view.focusManager.getFocus();
			if(changed 
				&& focussedField != _view.year 
				&& focussedField != _view.month 
				&& focussedField != _view.day 
				) {
				applyChanges();
			}
		}
		
		override protected function createRequestValue():Array {
			var result:Array = new Array(3);
			result[0] = StringUtil.trim(_view.year.text);
			result[1] = StringUtil.trim(_view.month.text);
			result[2] = StringUtil.trim(_view.day.text);
			return result;
		}
		
		protected function dateFieldOpenHandler(event:Event):void {
			var date:Date = getDateFromFields();
			if(date != null){
				_view.dateField.selectedDate = date;
			}
		}
		
		protected function dateFieldChangeHandler(event:Event):void {
			var date:Date = (event.target as DateField).selectedDate;
			setDateOnFields(date.fullYear, date.month, date.date);
			
			applyChanges();
		}
		
		protected function setDateOnFields(year:Number, month:Number, day:Number):void {
			_view.year.text = StringUtil.zeroPad(year, 2);
			_view.month.text = StringUtil.zeroPad(month, 2);
			_view.day.text = StringUtil.zeroPad(day, 2);
		}
		
		
		protected function getDateFromFields():Date {
			//check if input text is valid
			if(StringUtil.isNotBlank(_view.day.text) && 
				StringUtil.isNotBlank(_view.month.text) && 
				StringUtil.isNotBlank(_view.year.text)) {
				var tempDate:Date = new Date(_view.year.text, _view.month.text, _view.day.text);
				return tempDate;
			}
			return null;
		}
		
		override protected function updateView():void {
			super.updateView();
			_view.year.text = _view.month.text = _view.day.text = "";
			if(_view.attribute != null) {
				var date:DateProxy = _view.attribute.value as DateProxy;
				if(_view.attribute.symbol != null && getReasonBlankShortKey(_view.attribute.symbol) != null) {
					_view.day.text = getReasonBlankShortKey(_view.attribute.symbol);
				} else if(date != null) {
					setDateOnFields(date.year, date.month, date.day);
				}
			}
		}
	}
}
