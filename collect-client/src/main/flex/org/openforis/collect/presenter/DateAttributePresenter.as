package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ListCollectionView;
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.model.proxy.NodeUpdateRequestProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestSetProxy;
	import org.openforis.collect.ui.component.input.DateAttributeRenderer;
	import org.openforis.collect.ui.component.input.DateField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.StringUtil;
	
	import spark.events.DropDownEvent;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DateAttributePresenter extends CompositeAttributePresenter {
		
		private static var _dataClient:DataClient;
		
		{
			_dataClient = ClientFactory.dataClient;
		}
		
		public function DateAttributePresenter(view:DateAttributeRenderer) {
			
			super(view);
			
			view.day.textFormatFunction = twoCharsZeroPaddingFormatFunction;
			view.month.textFormatFunction = twoCharsZeroPaddingFormatFunction;
			view.year.textFormatFunction = fourCharsZeroPaddingFormatFunction;
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			//dateField (calendar button)
			view.dateField.addEventListener(DropdownEvent.CLOSE, dateFieldCloseHandler);
			view.dateField.addEventListener(DropDownEvent.OPEN, dateFieldOpenHandler);
		}
		
		private function get view():DateAttributeRenderer {
			return DateAttributeRenderer(_view);
		}
		
		protected function dateFieldOpenHandler(event:Event):void {
			var date:Date = getDateFromFields();
			if(date != null){
				view.dateField.selectedDate = date;
			}
		}
		
		protected function dateFieldCloseHandler(event:Event):void {
			if(Application.activeRecordEditable) {
				var date:Date = (event.target as DateField).dropdown.selectedDate;
				if(date != null) {
					setDateOnFields(date.fullYear, date.month + 1, date.date);
				}
			}
		}
		
		protected function setDateOnFields(year:Number, month:Number, day:Number):void {
			view.year.text = StringUtil.zeroPad(year, 4);
			view.month.text = StringUtil.zeroPad(month, 2);
			view.day.text = StringUtil.zeroPad(day, 2);
			
			var fields:Array = [view.year, view.month, view.day];
			var reqSet:NodeUpdateRequestSetProxy = new NodeUpdateRequestSetProxy();
			for each (var field:InputField in fields) {
				var r:NodeUpdateRequestProxy = field.presenter.createValueUpdateRequest();
				reqSet.addRequest(r);
			}
			_dataClient.updateActiveRecord(reqSet, null, faultHandler);
		}
		
		protected function getDateFromFields():Date {
			//check if input text is valid
			if(StringUtil.isNotBlank(view.day.text) && 
				StringUtil.isNotBlank(view.month.text) && 
				StringUtil.isNotBlank(view.year.text)) {
				var year:Number = Number(view.year.text);
				var month:Number = Number(view.month.text) - 1;
				var day:Number = Number(view.day.text);
				if(!isNaN(year) && !isNaN(month) && !isNaN(day)) {
					var tempDate:Date = new Date(year, month, day);
					if(! isNaN(tempDate.valueOf()) && tempDate.fullYear == year && tempDate.month == month && tempDate.date == day) {
						//valid date
						return tempDate;
					}
				}
			}
			return null;
		}

		protected function twoCharsZeroPaddingFormatFunction(value:String):String {
			return InputField.zeroPaddingFormatFunction(value, 2);
		}
		
		protected function fourCharsZeroPaddingFormatFunction(value:String):String {
			return InputField.zeroPaddingFormatFunction(value, 4);
		}
		
	}
}
