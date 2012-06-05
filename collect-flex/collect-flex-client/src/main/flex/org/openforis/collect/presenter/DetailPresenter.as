package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.Keyboard;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.core.FlexGlobals;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.ErrorListPopUp;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.view.DetailView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;

	public class DetailPresenter extends AbstractPresenter {
	
		private var _dataClient:DataClient;
		private var _view:DetailView;
		private var _errorsListPopUp:ErrorListPopUp;
		
		public function DetailPresenter(view:DetailView) {
			this._view = view;
			this._dataClient = ClientFactory.dataClient;
			super();
		}
		
		override internal function initEventListeners():void {
			_view.backToListButton.addEventListener(MouseEvent.CLICK, backToListButtonClickHandler);
			_view.saveButton.addEventListener(MouseEvent.CLICK, saveButtonClickHandler);
			_view.submitButton.addEventListener(MouseEvent.CLICK, submitButtonClickHandler);
			_view.rejectButton.addEventListener(MouseEvent.CLICK, rejectButtonClickHandler);
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.addEventListener(UIEvent.ACTIVE_RECORD_CHANGED, activeRecordChangedListener);
			
			_view.stage.addEventListener(KeyboardEvent.KEY_DOWN, stageKeyDownHandler);
		}
		
		/**
		 * Active record changed
		 * */
		internal function activeRecordChangedListener(event:UIEvent):void {
			var activeRecord:RecordProxy = Application.activeRecord;
			var step:CollectRecord$Step = activeRecord.step;
			var version:ModelVersionProxy = activeRecord.version;
			var rootEntityDefn:EntityDefinitionProxy = Application.activeRootEntity;
			var rootEntity:EntityProxy = activeRecord.rootEntity;
			rootEntity.definition = rootEntityDefn;
			rootEntity.updateKeyText();
			updateRecordKeyLabel();
			ChangeWatcher.watch(rootEntity, "keyText", updateRecordKeyLabel);
			
			_view.formVersionText.text = version.getLabelText();
			_view.currentPhaseText.text = getStepLabel(step);
			
			var user:UserProxy = Application.user;
			var canSubmit:Boolean = user.canSubmit(activeRecord);
			_view.submitButton.visible = _view.submitButton.includeInLayout = canSubmit;
			
			var canReject:Boolean = user.canReject(activeRecord);
			_view.rejectButton.visible = _view.rejectButton.includeInLayout = canReject;
			
			_view.saveButton.visible = Application.activeRecordEditable;
			
			var form:FormContainer = null;
			if (_view.formsContainer.contatinsForm(version,rootEntityDefn)){
				_view.currentState = DetailView.EDIT_STATE;
				form = _view.formsContainer.getForm(version, rootEntityDefn);
			} else {
				//build form 
				_view.currentState = DetailView.LOADING_STATE;
				form = UIBuilder.buildForm(rootEntityDefn, version);
				_view.formsContainer.addForm(form, version, rootEntityDefn);
				_view.currentState = DetailView.EDIT_STATE;
			}
			
			form = _view.formsContainer.setActiveForm(version, rootEntityDefn);
			form.record = activeRecord;
		}
		
		protected function recordSavedHandler(event:ApplicationEvent):void {
			var rootEntityLabel:String = Application.activeRootEntity.getLabelText()
			_view.recordSavedMessage.text = Message.get("edit.recordSaved", [rootEntityLabel]);
			_view.recordSavedMessage.show();
		}
		
		protected function updateRecordKeyLabel(event:Event = null):void {
			var result:String;
			var rootEntityLabel:String = Application.activeRootEntity.getLabelText();
			var keyText:String = Application.activeRecord.rootEntity.keyText;
			if(StringUtil.isBlank(keyText) && isNaN(Application.activeRecord.id)) {
				result = Message.get('edit.newRecordKeyLabel', [rootEntityLabel]);
			} else {
				result = StringUtil.concat("  ", rootEntityLabel, keyText);
			}
			_view.recordKeyLabel.text = result;
		}
		
		/**
		 * Back to list
		 * */
		protected function backToListButtonClickHandler(event:Event):void {
			if(Application.activeRecord.updated) {
				AlertUtil.showConfirm("edit.confirmBackToList", null, null, performClearActiveRecord);
			} else {
				performClearActiveRecord();
			}
		}
		
		protected function performClearActiveRecord():void {
			_dataClient.clearActiveRecord(new AsyncResponder(clearActiveRecordHandler, faultHandler));
		}
		
		protected function saveButtonClickHandler(event:MouseEvent):void {
			performSaveActiveRecord();
		}
		
		protected function performSaveActiveRecord():void {
			_dataClient.saveActiveRecord(saveActiveRecordResultHandler, faultHandler);
		}
		
		protected function submitButtonClickHandler(event:MouseEvent):void {
			var r:RecordProxy = Application.activeRecord;
			r.showErrors();
			var applicationEvent:ApplicationEvent = new ApplicationEvent(ApplicationEvent.ASK_FOR_SUBMIT);
			eventDispatcher.dispatchEvent(applicationEvent);
			var totalErrors:int = r.errors + r.missingErrors + r.skipped;
			if ( totalErrors > 0 ) {
				openErrorsListPopUp();
			} else {
				var messageResource:String;
				if(r.step == CollectRecord$Step.ENTRY) {
					messageResource = "edit.confirmSubmitDataCleansing";
				} else if(r.step == CollectRecord$Step.CLEANSING) {
					messageResource = "edit.confirmSubmitDataAnalysis";
				}
				AlertUtil.showConfirm(messageResource, null, null, performSubmit);
			}
		}
		
		protected function openErrorsListPopUp():void {
			if ( _errorsListPopUp != null ) {
				PopUpManager.removePopUp(_errorsListPopUp);
				_errorsListPopUp = null;
			}
			_errorsListPopUp = ErrorListPopUp(PopUpUtil.createPopUp(ErrorListPopUp, false));
		}
		
		protected function rejectButtonClickHandler(event:MouseEvent):void {
			var messageResource:String;
			var r:RecordProxy = Application.activeRecord;
			if(r.step == CollectRecord$Step.CLEANSING) {
				messageResource = "edit.confirmRejectDataCleansing";
			} else if(r.step == CollectRecord$Step.ANALYSIS) {
				messageResource = "edit.confirmRejectDataAnalysis";
			}
			AlertUtil.showConfirm(messageResource, null, null, performReject);
		}
		
		protected function performSubmit():void {
			var responder:AsyncResponder = new AsyncResponder(promoteRecordResultHandler, faultHandler);
			_dataClient.promoteActiveRecord(responder);
		}
		
		protected function performReject():void {
			var responder:AsyncResponder = new AsyncResponder(rejectRecordResultHandler, faultHandler);
			_dataClient.demoteActiveRecord(responder);
		}
		
		internal function clearActiveRecordHandler(event:ResultEvent, token:Object = null):void {
			Application.activeRecord = null;
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		internal function saveActiveRecordResultHandler(event:ResultEvent, token:Object = null):void {
			Application.activeRecord.showErrors();
			Application.activeRecord.updated = false;
			var applicationEvent:ApplicationEvent = new ApplicationEvent(ApplicationEvent.RECORD_SAVED);
			eventDispatcher.dispatchEvent(applicationEvent);
		}
		
		internal function promoteRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var rootEntityLabel:String = rootEntity.getLabelText();
			var r:RecordProxy = Application.activeRecord;
			var keyLabel:String = r.rootEntity.keyText;
			var actualStep:CollectRecord$Step = getNextStep(r.step);
			var stepLabel:String = getStepLabel(actualStep).toLowerCase();
			AlertUtil.showMessage("edit.recordSubmitted", [rootEntityLabel, keyLabel, stepLabel], "edit.recordSubmittedTitle", [rootEntityLabel]);
			Application.activeRecord = null;
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		internal function rejectRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var rootEntityLabel:String = rootEntity.getLabelText();
			var r:RecordProxy = Application.activeRecord;
			var keyLabel:String = r.rootEntity.keyText;
			var actualStep:CollectRecord$Step = getPreviousStep(r.step);
			var stepLabel:String = getStepLabel(actualStep).toLowerCase();
			AlertUtil.showMessage("edit.recordRejected", [rootEntityLabel, keyLabel, stepLabel], "edit.recordRejectedTitle", [rootEntityLabel]);
			Application.activeRecord = null;
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			//update 
		}
		
		protected function stageKeyDownHandler(event:KeyboardEvent):void {
			if ( Application.activeRecordEditable && event.ctrlKey && event.keyCode == Keyboard.S ) {
				performSaveActiveRecord();
			}
		}
		
		public static function getNextStep(step:CollectRecord$Step):CollectRecord$Step {
			switch ( step ) {
				case CollectRecord$Step.ENTRY:
					return CollectRecord$Step.CLEANSING;
				case CollectRecord$Step.CLEANSING:
					return CollectRecord$Step.ANALYSIS;
				default:
					return null;
					
			}
		}
		
		public static function getPreviousStep(step:CollectRecord$Step):CollectRecord$Step {
			switch ( step ) {
				case CollectRecord$Step.CLEANSING:
					return CollectRecord$Step.ENTRY;
				case CollectRecord$Step.ANALYSIS:
					return CollectRecord$Step.CLEANSING;
				default:
					return null;
					
			}
		}

		public static function getStepLabel(step:CollectRecord$Step):String {
			switch ( step ) {
				case CollectRecord$Step.ENTRY:
					return Message.get("edit.dataEntry");
				case CollectRecord$Step.CLEANSING:
					return Message.get("edit.dataCleansing");
				case CollectRecord$Step.ANALYSIS:
					return Message.get("edit.dataAnalysis");
				default:
					return null;
			}
		}
	}
}