package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import flash.events.Event;
	
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.view.MasterView;
	import org.openforis.collect.util.AlertUtil;

	public class MasterPresenter extends AbstractPresenter {
		
		private var _view:MasterView;
		private var _dataClient:DataClient;
		
		public function MasterPresenter(view:MasterView) {
			this._view = view;
			super();
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
			eventDispatcher.addEventListener(UIEvent.SHOW_ERROR_PAGE, showErrorPageHandler);
			eventDispatcher.addEventListener(UIEvent.SHOW_HOME_PAGE, backToHomeHandler);
			eventDispatcher.addEventListener(UIEvent.BACK_TO_LIST, backToListHandler);
			eventDispatcher.addEventListener(UIEvent.SHOW_SPECIES_IMPORT, showSpeciesImportModuleHandler);
			eventDispatcher.addEventListener(UIEvent.SHOW_SAMPLING_DESIGN_IMPORT, showSamplingDesignImportHandler);
			eventDispatcher.addEventListener(UIEvent.SHOW_CODE_LIST_IMPORT, showCodeListImportModuleHandler);
			eventDispatcher.addEventListener(UIEvent.RECORD_SELECTED, recordSelectedHandler);
			eventDispatcher.addEventListener(UIEvent.RECORD_CREATED, recordCreatedHandler);
		}
		
		protected function showSpeciesImportModuleHandler(event:UIEvent):void {
			_view.currentState = MasterView.SPECIES_IMPORT_STATE;
			_view.speciesImportView.surveyId = event.obj.surveyId;
			_view.speciesImportView.work = event.obj.work;
		}

		protected function showCodeListImportModuleHandler(event:UIEvent):void {
			_view.currentState = MasterView.CODE_LIST_IMPORT_STATE;
			_view.codeListImportView.surveyId = event.obj.surveyId;
			_view.codeListImportView.work = event.obj.work;
			_view.codeListImportView.codeListId = event.obj.codeListId;
		}

		protected function showSamplingDesignImportHandler(event:UIEvent):void {
			_view.currentState = MasterView.SAMPLING_DESIGN_IMPORT_STATE;
			_view.samplingDesignImportView.surveyId = event.obj.surveyId;
			_view.samplingDesignImportView.work = event.obj.work;		}

		/**
		 * RecordSummary selected from list page
		 * */
		internal function recordSelectedHandler(uiEvent:UIEvent):void {
			var record:RecordProxy = uiEvent.obj as RecordProxy;
			var responder:AsyncResponder = new AsyncResponder(loadRecordResultHandler, loadRecordFaultHandler, record);
			_dataClient.loadRecord(responder, record.id, record.step);
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
			record.survey = Application.activeSurvey;
			record.init();
			record.showErrors();
			setActiveRecord(record);
		}
		
		protected function setActiveRecord(record:RecordProxy):void {
			Application.activeRecord = record;
			var editable:Boolean = false;
			if ( Application.preview ) {
				editable = true;
			} else {
				var user:UserProxy = Application.user;
				editable = user.canEdit(record);
			}
			Application.activeRecordEditable = editable;
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
			uiEvent.obj = {firstAccess: true};
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		internal function newRecordCreatedHandler(event:UIEvent):void {
			_view.currentState = MasterView.DETAIL_STATE;
		}
		
		internal function showErrorPageHandler(event:UIEvent):void {
			Application.activeSurvey = null;
			Application.activeRootEntity = null;
			_view.currentState = MasterView.ERROR_STATE;
			_view.errorView.errorLabel.text = String(event.obj);
		}
		
		internal function backToHomeHandler(event:UIEvent):void {
			Application.activeSurvey = null;
			Application.activeRootEntity = null;
			_view.currentState = MasterView.HOME_STATE;
		}
			
		internal function backToListHandler(event:UIEvent):void {
			//reload record summaries 
			_view.currentState = MasterView.LIST_STATE;
			var uiEvent:UIEvent = new UIEvent(UIEvent.RELOAD_RECORD_SUMMARIES);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		protected function loadRecordFaultHandler(event:FaultEvent, token:Object = null):void {
			var faultCode:String = event.fault.faultCode;
			if(faultCode == "org.openforis.collect.persistence.RecordLockedByActiveUserException" ||
				(faultCode == "org.openforis.collect.persistence.RecordLockedException" && Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN))) {
					AlertUtil.showConfirm('edit.confirmUnlock', null, null, performUnlock, [token as RecordProxy]);
			} else {
				AbstractPresenter.faultHandler(event, token);
			}
		}
		
		protected function performUnlock(record:RecordProxy):void {
			_dataClient.loadRecord(new AsyncResponder(loadRecordResultHandler, loadRecordFaultHandler, record), record.id, record.step, true);
		}
	}
}