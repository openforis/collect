package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.component.AddNewRecordPopUp;

	public class AddNewRecordPopUpPresenter extends AbstractPresenter {
		private var _view:AddNewRecordPopUp;
		
		private var _newRecordResponder:IResponder;
		
		
		public function AddNewRecordPopUpPresenter(view:AddNewRecordPopUp) {
			this._view = view;
			_newRecordResponder = new AsyncResponder(newRecordResultHandler, newRecordFaultHandler);
			super();
		}
		
		override internal function initEventListeners():void{
			//test data
			var versions:ArrayCollection = new ArrayCollection([
				{id: "1", label: "version 1"},
				{id: "2", label: "version 2"},
				{id: "3", label: "version 3"}
			]);
			_view.addEventListener(CloseEvent.CLOSE, cancelClickHandler);
			_view.versionsDropDownList.dataProvider = versions;
			_view.addButton.addEventListener(MouseEvent.CLICK, addClickHandler);
			_view.cancelButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
		}
		
		protected function addClickHandler(event:Event):void {
			var dataClient:DataClient = ClientFactory.dataClient;
			dataClient.newRecord(_newRecordResponder, _view.idTextInput.text, _view.versionsDropDownList.selectedItem.id);
		}
		
		protected function cancelClickHandler(event:Event):void {
			PopUpManager.removePopUp(_view);
		}
		
		protected function newRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var uiEvent:UIEvent = new UIEvent(UIEvent.NEW_RECORD_CREATED);
			/* TODO - take record from result */
			uiEvent.obj = {versionId: _view.versionsDropDownList.selectedItem.id};
			eventDispatcher.dispatchEvent(uiEvent);
			PopUpManager.removePopUp(_view);
		}
		
		protected function newRecordFaultHandler(event:FaultEvent, token:Object = null):void {
			/* TEST */
			
			var uiEvent:UIEvent = new UIEvent(UIEvent.NEW_RECORD_CREATED);
			uiEvent.obj = {versionId: _view.versionsDropDownList.selectedItem.id};
			eventDispatcher.dispatchEvent(uiEvent);
			PopUpManager.removePopUp(_view);
		}
		
	}
}