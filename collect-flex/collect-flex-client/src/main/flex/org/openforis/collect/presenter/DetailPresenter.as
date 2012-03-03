package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.RecordProxy$Step;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.view.DetailView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.StringUtil;

	public class DetailPresenter extends AbstractPresenter {
	
		private var _dataClient:DataClient;
		private var _view:DetailView;
		
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
				
			eventDispatcher.addEventListener(UIEvent.ACTIVE_RECORD_CHANGED, activeRecordChangedListener);
		}
		
		/**
		 * Active record changed
		 * */
		internal function activeRecordChangedListener(event:UIEvent):void {
			var activeRecord:RecordProxy = Application.activeRecord;
			var activeRootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			
			var keys:Array = activeRecord.rootEntityKeys.toArray();
			var keyValues:String = StringUtil.concat(", ", keys);
			
			var version:ModelVersionProxy = activeRecord.version;
			_view.keyAttributeValuesText.text = keyValues;
			_view.rootEntityDefinitionText.text = activeRootEntity.getLabelText();
			_view.formVersionText.text = version.getLabelText();
			var submitButtonVisible:Boolean = activeRecord.step == RecordProxy$Step.ENTRY || 
				activeRecord.step == RecordProxy$Step.CLEANSING;
			_view.submitButton.visible = _view.submitButton.includeInLayout = submitButtonVisible;
			
			var rejectButtonVisible:Boolean = activeRecord.step == RecordProxy$Step.CLEANSING || 
				activeRecord.step == RecordProxy$Step.ANALYSIS;
			_view.rejectButton.visible = _view.rejectButton.includeInLayout = rejectButtonVisible;
			
			var form:FormContainer = null;
			if (_view.formsContainer.contatinsForm(version,activeRootEntity)){
				_view.currentState = DetailView.EDIT_STATE;
				form = _view.formsContainer.getForm(version, activeRootEntity);
			} else {
				//build form 
				_view.currentState = DetailView.LOADING_STATE;
				form = UIBuilder.buildForm(activeRootEntity, version);
				_view.formsContainer.addForm(form, version, activeRootEntity);
				_view.currentState = DetailView.EDIT_STATE;
			}
			
			form = _view.formsContainer.setActiveForm(version, activeRootEntity);
			form.record = activeRecord;
		}
		
		/**
		 * Back to list
		 * */
		protected function backToListButtonClickHandler(event:Event):void {
			_dataClient.clearActiveRecord(new AsyncResponder(clearActiveRecordHandler, faultHandler));
		}
		
		protected function saveButtonClickHandler(event:MouseEvent):void {
			_dataClient.saveActiveRecord(new AsyncResponder(saveActiveRecordResultHandler, faultHandler));
		}
		
		protected function submitButtonClickHandler(event:MouseEvent):void {
			var messageResource:String;
			var r:RecordProxy = Application.activeRecord;
			if(r.step == RecordProxy$Step.ENTRY) {
				messageResource = "edit.confirmSubmitDataCleansing";
			} else if(r.step == RecordProxy$Step.CLEANSING) {
				messageResource = "edit.confirmSubmitDataAnalysis";
			}
			AlertUtil.showConfirm(messageResource, null, null, performSubmit);
		}
		
		protected function rejectButtonClickHandler(event:MouseEvent):void {
			var messageResource:String;
			var r:RecordProxy = Application.activeRecord;
			if(r.step == RecordProxy$Step.CLEANSING) {
				messageResource = "edit.confirmRejectDataCleansing";
			} else if(r.step == RecordProxy$Step.ANALYSIS) {
				messageResource = "edit.confirmRejectDataAnalysis";
			}
			AlertUtil.showConfirm(messageResource, null, null, performReject);
		}
		
		protected function performSubmit():void {
			_dataClient.submitRecord(new AsyncResponder(promoteRecordResultHandler, faultHandler), 
				Application.activeRecord.id);
		}
		
		protected function performReject():void {
			_dataClient.rejectRecord(new AsyncResponder(rejectRecordResultHandler, faultHandler), 
				Application.activeRecord.id);
		}
		
		internal function clearActiveRecordHandler(event:ResultEvent, token:Object = null):void {
			Application.activeRecord = null;
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		internal function saveActiveRecordResultHandler(event:ResultEvent, token:Object = null):void {
			
		}
		
		internal function promoteRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var r:RecordProxy = Application.activeRecord;
			var keyLabel:String = r.rootEntity.getKeyLabel(Application.activeRootEntity);
			AlertUtil.showMessage("edit.recordSubmitted", [keyLabel]);
			Application.activeRecord = null;
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		internal function rejectRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var r:RecordProxy = Application.activeRecord;
			var keyLabel:String = r.rootEntity.getKeyLabel(Application.activeRootEntity);
			AlertUtil.showMessage("edit.recordRejected", [keyLabel]);
			Application.activeRecord = null;
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
	}
}