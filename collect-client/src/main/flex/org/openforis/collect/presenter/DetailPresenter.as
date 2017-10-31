package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	import flash.net.navigateToURL;
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
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;

	public class DetailPresenter extends AbstractPresenter {
	
		private var _dataClient:DataClient;
		private var _errorsListPopUp:ErrorListPopUp;
		private var _rootEntityKeyTextChangeWatcher:ChangeWatcher;
		
		public function DetailPresenter(view:DetailView) {
			super(view);
			this._dataClient = ClientFactory.dataClient;
		}
		
		private function get view():DetailView {
			return DetailView(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.backToListButton.addEventListener(MouseEvent.CLICK, backToListButtonClickHandler);
			view.saveButton.addEventListener(MouseEvent.CLICK, saveButtonClickHandler);
			view.autoSaveCheckBox.addEventListener(MouseEvent.CLICK, autoSaveCheckBoxClickHandler);
			view.submitButton.addEventListener(MouseEvent.CLICK, submitButtonClickHandler);
			view.rejectButton.addEventListener(MouseEvent.CLICK, rejectButtonClickHandler);
			view.resizeBtn.addEventListener(MouseEvent.CLICK, toggleViewSizeClickHandler);
			view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			view.showErrorsListButton.addEventListener(MouseEvent.CLICK, showErrorsListButtonClickHandler);
			
			view.stage.addEventListener(KeyboardEvent.KEY_DOWN, stageKeyDownHandler);
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.addEventListener(UIEvent.ACTIVE_RECORD_CHANGED, activeRecordChangedListener);
		}
		
		override protected function removeBroadcastEventListeners():void {
			super.removeBroadcastEventListeners();
			eventDispatcher.removeEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			eventDispatcher.removeEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.removeEventListener(UIEvent.ACTIVE_RECORD_CHANGED, activeRecordChangedListener);
		}
		
		/**
		 * Active record changed
		 * */
		internal function activeRecordChangedListener(event:UIEvent):void {
			var preview:Boolean = Application.preview;
			var onlyOneRecordEdit:Boolean = Application.onlyOneRecordEdit;
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
			//view.formVersionContainer.visible = version != null;
			
			view.footer.formVersion = version == null ? null : version.getLabelText();
			
			var step:CollectRecord$Step = activeRecord.step;
			view.currentPhaseText.text = getStepLabel(step);
			
			var user:UserProxy = Application.user;
			var canSubmit:Boolean = !preview && user.canSubmit(activeRecord);
			var canReject:Boolean = !preview && user.canReject(activeRecord);
			var canSave:Boolean = !preview && Application.activeRecordEditable;
			var canViewAllRecords:Boolean = !preview && user.canViewAllRecords;
			
			var showHeader:Boolean = !(preview || onlyOneRecordEdit);
			var showBackToListButton:Boolean = !(preview || onlyOneRecordEdit) || canViewAllRecords;
			var showTopButtonBar:Boolean = showBackToListButton || (canSubmit || canReject);
			var showFooter:Boolean = !preview && canSave;

			FlexGlobals.topLevelApplication.header.visible = FlexGlobals.topLevelApplication.header.includeInLayout = showHeader;
			view.topButtonBar.visible = view.topButtonBar.includeInLayout = showTopButtonBar;
			view.bottomButtonBar.visible = view.bottomButtonBar.includeInLayout = showFooter;
			view.footer.userButtonVisible = showFooter && ! onlyOneRecordEdit;
			view.footer.footerImg.visible = showFooter && ! onlyOneRecordEdit;
			
			view.backToListButton.visible = view.backToListButton.includeInLayout = showBackToListButton;
			view.submitButton.visible = view.submitButton.includeInLayout = canSubmit;
			view.rejectButton.visible = view.rejectButton.includeInLayout = canReject;
			if ( canReject ) {
				view.rejectButton.label = step == CollectRecord$Step.ANALYSIS ? Message.get("edit.unlock"): Message.get("edit.reject");
			}
			view.saveButton.visible = canSave;
			view.updateStatusLabel.text = null;
			view.autoSaveCheckBox.visible = canSave;
			view.updateStatusLabel.visible = canSave;
			
			var rootEntityDefn:EntityDefinitionProxy = Application.activeRootEntity;
			//do not mantain more than one form in FormsContainer for performance issues
			view.currentState = DetailView.LOADING_STATE;
			var form:FormContainer = view.formsContainer.getForm(rootEntityDefn, version);
			if ( form == null ) {
				view.formsContainer.reset();
				form = UIBuilder.buildForm(rootEntityDefn, version);
				view.formsContainer.addForm(form, rootEntityDefn, version);
				view.formsContainer.selectedChild = form;
			}
			view.currentState = DetailView.EDIT_STATE;
			form.record = activeRecord;
		}
		
		protected function recordSavedHandler(event:ApplicationEvent):void {
			var rootEntityLabel:String = Application.activeRootEntity.getInstanceOrHeadingLabelText();
			view.messageDisplay.show(Message.get("edit.recordSaved", [rootEntityLabel]));
		}
		
		protected function autoSaveCheckBoxClickHandler(event:MouseEvent):void {
			Application.autoSave = view.autoSaveCheckBox.selected;
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
			view.recordKeyLabel.text = result;
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
		
		protected function toggleViewSizeClickHandler(event:Event):void {
			eventDispatcher.dispatchEvent(new UIEvent(UIEvent.TOGGLE_DETAIL_VIEW_SIZE));
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
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
			var recordId:int = Application.activeRecord.id;
			var surveyId:int = Application.activeRecord.survey.id;
			var step:int = toStepNumber(Application.activeRecord.step);
			
			var url:String = ApplicationConstants.URL + "surveys/" + surveyId + "/records/" + recordId + "/steps/" + step + "/csv_content.zip";
			
			var req:URLRequest = new URLRequest(url);
			req.data = new URLVariables();
			navigateToURL(req, "_new");
		}
		
		protected function showErrorsListButtonClickHandler(event:MouseEvent):void {
			var r:RecordProxy = Application.activeRecord;
			r.showErrors();
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.ASK_FOR_SUBMIT)); //to update validation feedback
			
			var totalErrors:int = r.errors + r.missingErrors + r.skipped;
			if ( totalErrors > 0 ) {
				openErrorsListPopUp();
			} else {
				AlertUtil.showMessage("edit.no_errors_found");
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
			updateStatusLabel();
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
			reloadRecordOrGoBackToList(r.id);
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
			reloadRecordOrGoBackToList(r.id);
		}

		private function reloadRecordOrGoBackToList(recordId:Number = NaN):void {
			var uiEvent:UIEvent;
			if (Application.onlyOneRecordEdit) {
				uiEvent = new UIEvent(UIEvent.LOAD_RECORD_FOR_EDIT);
				uiEvent.obj = {
					recordId: recordId
				};
			} else {
				uiEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			}
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			updateStatusLabel();
		}
		
		protected function updateStatusLabel():void {
			var updateMessageKey:String;
			if ( Application.activeRecord.updated ) {
				updateMessageKey = "edit.changes_not_saved";
			} else {
				updateMessageKey = "edit.all_changes_saved";
			}
			view.updateStatusLabel.text = Message.get(updateMessageKey);			
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

		private function toStepNumber(step:CollectRecord$Step):int {
			switch(step) {
			case CollectRecord$Step.ENTRY:
				return 1;
			case CollectRecord$Step.CLEANSING:
				return 2;
			case CollectRecord$Step.ANALYSIS:
				return 3;
			}
			return -1;
		}
		
	}
}