package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.IList;
	import mx.events.IndexChangedEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.view.DetailView;

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
			
			eventDispatcher.addEventListener(UIEvent.ACTIVE_RECORD_CHANGED, activeRecordChangedListener);
		}
		
		/**
		 * Active record changed
		 * */
		internal function activeRecordChangedListener(event:UIEvent):void {
			var activeRecord:RecordProxy = Application.activeRecord;
			var activeRootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			
			var keyValues:String = "";
			var keyAttributeDefinitions:IList = activeRootEntity.keyAttributeDefinitions;
			for each (var k:AttributeDefinitionProxy in keyAttributeDefinitions) {
				keyValues += activeRecord.rootEntityKeys.get(k.name);
			}
			
			var version:ModelVersionProxy = activeRecord.version;
			_view.keyAttributeValuesText.text = keyValues;
			_view.rootEntityDefinitionText.text = activeRootEntity.getLabelText();
			_view.formVersionText.text = version.getLabelText();
			
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
		
		internal function clearActiveRecordHandler(event:ResultEvent, token:Object = null):void {
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
	}
}