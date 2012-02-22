package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.events.CalendarLayoutChangeEvent;
	
	import org.openforis.collect.ui.component.input.DateAttributeRenderer;
	import org.openforis.collect.ui.component.input.DateField;
	import org.openforis.collect.ui.component.input.TaxonAttributeRenderer;
	import org.openforis.collect.util.StringUtil;
	
	import spark.events.DropDownEvent;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DateAttributePresenter extends AbstractPresenter {
		
		private var _view:DateAttributeRenderer;
		
		public function DateAttributePresenter(view:DateAttributeRenderer) {
			_view = view;
			
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			//dateField (calendar button)
			_view.dateField.addEventListener(CalendarLayoutChangeEvent.CHANGE, dateFieldChangeHandler);
			_view.dateField.addEventListener(DropDownEvent.OPEN, dateFieldOpenHandler);
		}
		
		private function get view():DateAttributeRenderer {
			return DateAttributeRenderer(_view);
		}
		
		protected function dateFieldOpenHandler(event:Event):void {
			var date:Date = getDateFromFields();
			if(date != null){
				_view.dateField.selectedDate = date;
			}
		}
		
		protected function dateFieldChangeHandler(event:Event):void {
			var date:Date = (event.target as DateField).selectedDate;
			setDateOnFields(date.fullYear, date.month + 1, date.date);
		}
		
		protected function setDateOnFields(year:Number, month:Number, day:Number):void {
			_view.year.text = StringUtil.zeroPad(year, 2);
			_view.month.text = StringUtil.zeroPad(month, 2);
			_view.day.text = StringUtil.zeroPad(day, 2);
			view.year.applyChanges();
			view.month.applyChanges();
			view.day.applyChanges();
		}
		
		protected function getDateFromFields():Date {
			//check if input text is valid
			if(StringUtil.isNotBlank(_view.day.text) && 
				StringUtil.isNotBlank(_view.month.text) && 
				StringUtil.isNotBlank(_view.year.text)) {
				var tempDate:Date = new Date(_view.year.text, int(_view.month.text) - 1, _view.day.text);
				return tempDate;
			}
			return null;
		}
		

	}
}
