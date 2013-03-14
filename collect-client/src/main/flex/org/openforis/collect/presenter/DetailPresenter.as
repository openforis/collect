package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.Keyboard;
	
	import mx.binding.utils.ChangeWatcher;
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
		private var _rootEntityKeyTextChangeWatcher:ChangeWatcher;
		
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
			var preview:Boolean = Application.preview;
			var activeRecord:RecordProxy = Application.activeRecord;
			var version:ModelVersionProxy = activeRecord.version;
			var rootEntity:EntityProxy = activeRecord.rootEntity;
			rootEntity.updateKeyText();
			updateRecordKeyLabel();
			if ( _rootEntityKeyTextChangeWatcher == null ) {
				_rootEntityKeyTextChangeWatcher = ChangeWatcher.watch(rootEntity, "keyText", updateRecordKeyLabel);
			} else {
				_rootEntityKeyTextChangeWatcher.reset(rootEntity);
			}
			_view.formVersionContainer.visible = version != null;
			if ( version != null ) {
				_view.formVersionText.text = version.getLabelText();
			}
			var step:CollectRecord$Step = activeRecord.step;
			_view.currentPhaseText.text = getStepLabel(step);
			
			var user:UserProxy = Application.user;
			var canSubmit:Boolean = !preview && user.canSubmit(activeRecord);
			var canReject:Boolean = !preview && user.canReject(activeRecord);
			var canSave:Boolean = !preview && Application.activeRecordEditable;
			
			_view.header.visible = _view.header.includeInLayout = !preview;
			_view.submitButton.visible = _view.submitButton.includeInLayout = canSubmit;
			_view.rejectButton.visible = _view.rejectButton.includeInLayout = canReject;
			if ( canReject ) {
				_view.rejectButton.label = step == CollectRecord$Step.ANALYSIS ? Message.get("edit.unlock"): Message.get("edit.reject");
			}
			_view.saveButton.visible = canSave;
			
			var rootEntityDefn:EntityDefinitionProxy = Application.activeRootEntity;
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
			var rootEntityLabel:String = Application.activeRootEntity.getInstanceOrHeadingLabelText()
			_view.messageDisplay.show(Message.get("edit.recordSaved", [rootEntityLabel]));
		}
		
		protected function updateRecordKeyLabel(event:Event = null):void {
			var result:String;
			var rootEntityLabel:String = Application.activeRootEntity.getInstanceOrHeadingLabelText();
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
			//reset immediately activeRecord to avoid concurrency problems
			//when keepAlive message is sent and record is being unlocked
			ClientFactory.sessionClient.cancelLastKeepAliveOperation();
			Application.activeRecord = null; 
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
					AlertUtil.showConfirm(messageResource, null, null, performSubmitToClenasing);
				} else if(r.step == CollectRecord$Step.CLEANSING) {
					messageResource = "edit.confirmSubmitDataAnalysis";
					AlertUtil.showConfirm(messageResource, null, null, performSubmitToAnalysis);
				}
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
				AlertUtil.showConfirm(messageResource, null, null, performRejectToEntry);
			} else if(r.step == CollectRecord$Step.ANALYSIS) {
				messageResource = "edit.confirmRejectDataAnalysis";
				AlertUtil.showConfirm(messageResource, null, null, performRejectToCleansing);
			}
		}
		
		protected function performSubmitToClenasing():void {
			var responder:AsyncResponder = new AsyncResponder(promoteRecordResultHandler, faultHandler);
			_dataClient.promoteToCleansing(responder);
		}
		
		protected function performSubmitToAnalysis():void {
			var responder:AsyncResponder = new AsyncResponder(promoteRecordResultHandler, faultHandler);
			_dataClient.promoteToAnalysis(responder);
		}
		
		protected function performRejectToCleansing():void {
			var responder:AsyncResponder = new AsyncResponder(rejectRecordResultHandler, faultHandler);
			_dataClient.demoteToCleansing(responder);
		}
		
		protected function performRejectToEntry():void {
			var responder:AsyncResponder = new AsyncResponder(rejectRecordResultHandler, faultHandler);
			_dataClient.demoteToEntry(responder);
		}

		internal function clearActiveRecordHandler(event:ResultEvent, token:Object = null):void {
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
			var rootEntityLabel:String = rootEntity.getInstanceOrHeadingLabelText();
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
			var rootEntityLabel:String = rootEntity.getInstanceOrHeadingLabelText();
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
			//save record pressing CTRL + s
			if ( !Application.preview && Application.activeRecordEditable && 
					event.ctrlKey && event.keyCode == Keyboard.S ) {
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