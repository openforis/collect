package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.ui.view.MasterView;

	public class MasterPresenter extends AbstractPresenter {
		
		private var _view:MasterView;
		private var _dataClient:DataClient;
		
		public function MasterPresenter(view:MasterView) {
			super();
			
			this._view = view;
			this._dataClient = ClientFactory.dataClient;

			_view.currentState = MasterView.LOADING_STATE;
			
			/*
			wait for surveys and sessionState loading, then dispatch APPLICATION_INITIALIZED event
			if more than one survey is found, then whow surveySelection view
			*/
			
			/*
			flow: loading -> surveySelection (optional) -> rootEntitySelection (optional) -> list -> edit
			*/
		}
		
		override internal function initEventListeners():void {
			//eventDispatcher.addEventListener(ApplicationEvent.APPLICATION_INITIALIZED, applicationInitializedHandler);
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			eventDispatcher.addEventListener(UIEvent.BACK_TO_LIST, backToListHandler);
			eventDispatcher.addEventListener(UIEvent.RECORD_SELECTED, recordSelectedHandler);
			eventDispatcher.addEventListener(UIEvent.RECORD_CREATED, recordCreatedHandler);
		}
		
		/**
		 * RecordSummary selected from list page
		 * */
		internal function recordSelectedHandler(uiEvent:UIEvent):void {
			var record:RecordProxy = uiEvent.obj as RecordProxy;
			_dataClient.loadRecord(new AsyncResponder(loadRecordResultHandler, faultHandler, record), record.id, record.step);
		}
		
		/**
		 * New Record created
		 * */
		internal function recordCreatedHandler(uiEvent:UIEvent):void {
			var record:RecordProxy = uiEvent.obj as RecordProxy;
			setActiveRecord(record);
		}
		
		/**
		 * Record selected in list page loaded from server
		 * */
		protected function loadRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var record:RecordProxy = RecordProxy(event.result);
			record.init();
			record.showErrors();
			setActiveRecord(record);
		}
		
		protected function setActiveRecord(record:RecordProxy):void {
			Application.activeRecord = record;
			_view.currentState = MasterView.DETAIL_STATE;
			
			var uiEvent:UIEvent = new UIEvent(UIEvent.ACTIVE_RECORD_CHANGED);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		/**
		 * Root entity selected
		 * */
		internal function rootEntitySelectedHandler(event:UIEvent):void {
			var rootEntityDef:EntityDefinitionProxy = event.obj as EntityDefinitionProxy;
			Application.activeRootEntity = rootEntityDef;
			_view.currentState = MasterView.LIST_STATE;
			
			var uiEvent:UIEvent = new UIEvent(UIEvent.LOAD_RECORD_SUMMARIES);
			eventDispatcher.dispatchEvent(uiEvent);
		}

		
		internal function newRecordCreatedHandler(event:UIEvent):void {
			_view.currentState = MasterView.DETAIL_STATE;
		}
		
		internal function backToListHandler(event:UIEvent):void {
			//reload record summaries 
			_view.currentState = MasterView.LIST_STATE;
			var uiEvent:UIEvent = new UIEvent(UIEvent.RELOAD_RECORD_SUMMARIES);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
	}
}