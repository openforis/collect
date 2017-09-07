package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.component.BlockingMessagePopUp;
	import org.openforis.collect.ui.view.MasterView;
	import org.openforis.collect.util.AlertUtil;

	public class MasterPresenter extends AbstractPresenter {
		
		private static const CHECK_RECORDS_LOCK_FREQUENCY:Number = 30000;
		
		private var _dataClient:DataClient;
		private var _checkRecordsLockTimer:Timer;
		private var _editingBlockPopup:BlockingMessagePopUp;
		
		public function MasterPresenter(view:MasterView) {
			super(view);
			this._dataClient = ClientFactory.dataClient;
		}
		
		private function get view():MasterView {
			return MasterView(_view);
		}
		
		override public function init():void {
			super.init();
			view.currentState = MasterView.LOADING_STATE;
			
			_checkRecordsLockTimer = new Timer(CHECK_RECORDS_LOCK_FREQUENCY)
			_checkRecordsLockTimer.addEventListener(TimerEvent.TIMER, checkRecordsLockTimeoutHandler);
			_checkRecordsLockTimer.start();
			
			/*
			wait for surveys and sessionState loading, then dispatch APPLICATION_INITIALIZED event
			if more than one survey is found, then whow surveySelection view
			*/
			
			/*
			flow: loading -> surveySelection (optional) -> rootEntitySelection (optional) -> list -> edit
			*/
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
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
			eventDispatcher.addEventListener(UIEvent.LOAD_RECORD_FOR_EDIT, loadRecordForEditHandler);
		}
		
		protected function checkRecordsLockTimeoutHandler(event:TimerEvent = null, onSuccess:Function = null, onFault:Function = null):void {
			if (Application.serverOffline) {
				_checkRecordsLockTimer.stop();
				return;
			}
			var resp:AsyncResponder = new AsyncResponder(isActiveSurveyRecordsLockedResultHandler, faultHandler);
			
			function isActiveSurveyRecordsLockedResultHandler(event:ResultEvent, token:Object = null):void {
				var locked:Boolean = event.result as Boolean;
				var blockingPopUpVisible:Boolean = (view.currentState == MasterView.LIST_STATE ) && 
					locked && ! Application.serverOffline;
				if ( blockingPopUpVisible ) {
					if ( _editingBlockPopup == null ) {
						_editingBlockPopup = BlockingMessagePopUp.show(
							Message.get("list.recordsLockedPopUp.title"), 
							Message.get("list.recordsLockedPopUp.message"), 
							Message.get("list.recordsLockedPopUp.details"));
					}
					if ( onFault != null ) {
						onFault();
					}
				} else {
					if ( _editingBlockPopup != null ) {
						PopUpManager.removePopUp(_editingBlockPopup);
						_editingBlockPopup = null;
						//record locking ended
						dispatchLoadSummariesEvent();
						refreshGlobalViewSize();
					}
					if ( onSuccess != null ) {
						onSuccess();
					}
				}
			}
			checkRecordsLock(resp);
		}
		
		protected function checkRecordsLock(responder:IResponder):void {
			ClientFactory.modelClient.isActiveSurveyRecordsLocked(responder);
		}
		
		protected function refreshGlobalViewSize():void {
			eventDispatcher.dispatchEvent(new UIEvent(UIEvent.CHECK_VIEW_SIZE));
		}
		
		protected function showSpeciesImportModuleHandler(event:UIEvent):void {
			view.currentState = MasterView.SPECIES_IMPORT_STATE;
			view.speciesImportView.surveyId = event.obj.surveyId;
			view.speciesImportView.work = event.obj.work;
		}

		protected function showCodeListImportModuleHandler(event:UIEvent):void {
			view.currentState = MasterView.CODE_LIST_IMPORT_STATE;
			view.codeListImportView.surveyId = event.obj.surveyId;
			view.codeListImportView.work = event.obj.work;
			view.codeListImportView.codeListId = event.obj.codeListId;
		}

		protected function showSamplingDesignImportHandler(event:UIEvent):void {
			view.currentState = MasterView.SAMPLING_DESIGN_IMPORT_STATE;
			view.samplingDesignImportView.surveyId = event.obj.surveyId;
			view.samplingDesignImportView.work = event.obj.work;		
		}

		/**
		 * RecordSummary selected from list page
		 * */
		internal function recordSelectedHandler(uiEvent:UIEvent):void {
			var record:RecordProxy = uiEvent.obj as RecordProxy;
			if ( record.unassigned || Application.user.isOwner(record)
				|| Application.user.canViewAllRecords || Application.user.canEditNotOwnedRecords) {
				var recordInfo:Object = {id: record.id, step: record.step};
				var responder:AsyncResponder = new AsyncResponder(loadRecordResultHandler, loadRecordFaultHandler, recordInfo);
				
				if (Application.user.canEditRecords) {
					_dataClient.checkoutRecord(responder, record.id, record.step);
				} else {
					_dataClient.loadRecord(responder, record.id, record.step);
				}
			} else {
				AlertUtil.showError("list.error.cannotEdit.differentOwner", [record.owner.username]);
			}
		}
		
		/**
		 * Load record for edit event handler
		 * */
		internal function loadRecordForEditHandler(uiEvent:UIEvent):void {
			var recordId:int = uiEvent.obj.recordId;
			var recordInfo:Object = {id: recordId, step: null};
			var responder:AsyncResponder = new AsyncResponder(loadRecordResultHandler, loadRecordFaultHandler, recordInfo);
			_dataClient.checkoutRecord(responder, recordId);
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
			view.currentState = MasterView.DETAIL_STATE;
			
			var uiEvent:UIEvent = new UIEvent(UIEvent.ACTIVE_RECORD_CHANGED);
			eventDispatcher.dispatchEvent(uiEvent);
			
			refreshGlobalViewSize();
		}
		
		/**
		 * Root entity selected
		 * */
		internal function rootEntitySelectedHandler(event:UIEvent):void {
			var rootEntityDef:EntityDefinitionProxy = event.obj as EntityDefinitionProxy;
			Application.activeRootEntity = rootEntityDef;
			view.currentState = MasterView.LIST_STATE;
			
			checkRecordsLockTimeoutHandler(null, onNotLocked);
			
			function onNotLocked(): void {
				dispatchLoadSummariesEvent();
				refreshGlobalViewSize();
			}
		}
		
		private function dispatchLoadSummariesEvent():void {
			var uiEvent:UIEvent = new UIEvent(UIEvent.LOAD_RECORD_SUMMARIES);
			uiEvent.obj = {firstAccess: true};
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		internal function newRecordCreatedHandler(event:UIEvent):void {
			view.currentState = MasterView.DETAIL_STATE;
		}
		
		internal function showErrorPageHandler(event:UIEvent):void {
			Application.activeSurvey = null;
			Application.activeRootEntity = null;
			view.currentState = MasterView.ERROR_STATE;
			view.errorView.errorLabel.text = String(event.obj);
			
			refreshGlobalViewSize();
		}
		
		internal function backToHomeHandler(event:UIEvent):void {
			Application.activeSurvey = null;
			Application.activeRootEntity = null;
			view.currentState = MasterView.HOME_STATE;

			refreshGlobalViewSize();
		}
			
		internal function backToListHandler(event:UIEvent):void {
			//reload record summaries 
			view.currentState = MasterView.LIST_STATE;
			var uiEvent:UIEvent = new UIEvent(UIEvent.RELOAD_RECORD_SUMMARIES);
			eventDispatcher.dispatchEvent(uiEvent);
			
			refreshGlobalViewSize();
		}
		
		protected function loadRecordFaultHandler(event:FaultEvent, token:Object = null):void {
			var faultCode:String = event.fault.faultCode;
			if(faultCode == "org.openforis.collect.persistence.RecordLockedByActiveUserException" ||
					(faultCode == "org.openforis.collect.persistence.RecordLockedException" && Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN))) {
				AlertUtil.showConfirm('edit.confirmUnlock', null, null, forceUnlock, [token]);
			} else {
				AbstractPresenter.faultHandler(event, token);
			}
		}
		
		protected function forceUnlock(recordInfo:Object):void {
			_dataClient.checkoutRecord(new AsyncResponder(loadRecordResultHandler, loadRecordFaultHandler, recordInfo), 
				recordInfo.id, recordInfo.step, true);
		}
	}
}