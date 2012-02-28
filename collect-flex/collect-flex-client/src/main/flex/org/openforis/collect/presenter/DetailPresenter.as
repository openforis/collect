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
			_view.promoteButton.addEventListener(MouseEvent.CLICK, promoteButtonClickHandler);
				
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
			var promoteButtonVisible:Boolean = activeRecord.step == RecordProxy$Step.ENTRY || 
				activeRecord.step == RecordProxy$Step.CLEANSING;
			_view.promoteButton.visible = _view.promoteButton.includeInLayout = promoteButtonVisible;
			
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
		
		protected function promoteButtonClickHandler(event:MouseEvent):void {
			AlertUtil.showConfirm("edit.confirmPromote", null, null, performPromote);
		}
		
		protected function performPromote():void {
			_dataClient.promoteRecord(new AsyncResponder(promoteRecordResultHandler, faultHandler), 
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
			var nextStep:RecordProxy$Step;
			if(r.step == RecordProxy$Step.ENTRY) {
				nextStep = RecordProxy$Step.CLEANSING;
			} else {
				nextStep = RecordProxy$Step.ANALYSIS;
			}
			AlertUtil.showMessage("edit.recordPromoted", [keyLabel, nextStep]);
			Application.activeRecord = null;
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
	}
}