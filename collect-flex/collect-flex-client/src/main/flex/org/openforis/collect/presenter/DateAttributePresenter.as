package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ListCollectionView;
	import mx.events.CalendarLayoutChangeEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.input.DateAttributeRenderer;
	import org.openforis.collect.ui.component.input.DateField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.StringUtil;
	
	import spark.events.DropDownEvent;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DateAttributePresenter extends AttributePresenter {
		
		protected var _updateResponder:IResponder;
		private var _dataClient:DataClient;
		
		public function DateAttributePresenter(view:DateAttributeRenderer) {
			_dataClient = ClientFactory.dataClient;
			_updateResponder = new AsyncResponder(updateResultHandler, updateFaultHandler);
			
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			//dateField (calendar button)
			view.dateField.addEventListener(CalendarLayoutChangeEvent.CHANGE, dateFieldChangeHandler);
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
		
		protected function dateFieldChangeHandler(event:Event):void {
			var date:Date = (event.target as DateField).selectedDate;
			setDateOnFields(date.fullYear, date.month + 1, date.date);
		}
		
		protected function setDateOnFields(year:Number, month:Number, day:Number):void {
			view.year.text = StringUtil.zeroPad(year, 4);
			view.month.text = StringUtil.zeroPad(month, 2);
			view.day.text = StringUtil.zeroPad(day, 2);
			
			var fields:Array = [view.year, view.month, view.day];
			var operations:ListCollectionView = new ArrayCollection();
			for each (var field:InputField in fields) {
				var o:UpdateRequestOperation = field.presenter.getApplyValueOperation();
				operations.addItem(o);
			}
			var req:UpdateRequest = new UpdateRequest();
			req.operations = operations;
			dataClient.updateActiveRecord(_updateResponder, req);
		}
		
		protected function getDateFromFields():Date {
			//check if input text is valid
			if(StringUtil.isNotBlank(view.day.text) && 
				StringUtil.isNotBlank(view.month.text) && 
				StringUtil.isNotBlank(view.year.text)) {
				var tempDate:Date = new Date(view.year.text, int(view.month.text) - 1, view.day.text);
				return tempDate;
			}
			return null;
		}
		
		protected function updateResultHandler(event:ResultEvent, token:Object = null):void {
			var response:UpdateResponse = UpdateResponse(event.result);
			Application.activeRecord.update(response);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = response;
			eventDispatcher.dispatchEvent(appEvt);
		}
		
		protected function updateFaultHandler(event:FaultEvent, token:Object = null):void {
			//undoLastChange();
			faultHandler(event, token);
		}
		
		protected function get dataClient():DataClient {
			return _dataClient;
		}

	}
}
