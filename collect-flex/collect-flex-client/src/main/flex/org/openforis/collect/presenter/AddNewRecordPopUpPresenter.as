package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.granite.collections.IMap;
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.ui.component.AddNewRecordPopUp;
	
	import spark.components.FormItem;
	import spark.components.TextInput;
	import spark.components.supportClasses.ItemRenderer;

	public class AddNewRecordPopUpPresenter extends AbstractPresenter {
		private var _view:AddNewRecordPopUp;
		
		private var _newRecordResponder:IResponder;
		
		public function AddNewRecordPopUpPresenter(view:AddNewRecordPopUp) {
			this._view = view;
			_newRecordResponder = new AsyncResponder(newRecordResultHandler, newRecordFaultHandler);
			super();
			
			if(Application.activeSurvey != null) {
				var versions:ListCollectionView = Application.activeSurvey.versions;
				_view.versionsDropDownList.dataProvider = versions;
			}
			if(Application.activeRootEntity != null) {
				_view.keyDataGroup.dataProvider = Application.activeRootEntity.keyAttributeDefinitions;
			}
		}
		
		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.SURVEY_SELECTED, surveySelectedHandler);
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			_view.addEventListener(CloseEvent.CLOSE, cancelClickHandler);
			_view.addButton.addEventListener(MouseEvent.CLICK, addClickHandler);
			_view.cancelButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
			
		}
		
		protected function surveySelectedHandler(event:UIEvent):void {
			var survey:SurveyProxy = event.obj as SurveyProxy;
			if(survey != null) {
				var versions:ListCollectionView = survey.versions;
				_view.versionsDropDownList.dataProvider = versions;
			}
		}
		
		protected function rootEntitySelectedHandler(event:UIEvent):void {
			var rootEntity:EntityDefinitionProxy = event.obj as EntityDefinitionProxy;
			_view.keyDataGroup.dataProvider = rootEntity.keyAttributeDefinitions;
		}
		
		protected function addClickHandler(event:Event):void {
			var dataClient:DataClient = ClientFactory.dataClient;
			var keyMap:Object = new Object();
			var keyAttributeDefs:IList = _view.keyDataGroup.dataProvider;
			for(var index:int = 0; index < keyAttributeDefs.length; index ++) {
				var keyAttributeDef:AttributeDefinitionProxy = AttributeDefinitionProxy(keyAttributeDefs.getItemAt(index));
				var keyItemRenderer:ItemRenderer = _view.keyDataGroup.getElementAt(index) as ItemRenderer;
				var formItem:FormItem = keyItemRenderer.getElementAt(0) as FormItem;
				var textInput:TextInput = formItem.getElementAt(0) as TextInput;
				keyMap[keyAttributeDef.name] = textInput.text;
			}
			var version:ModelVersionProxy = ModelVersionProxy(_view.versionsDropDownList.selectedItem);
			dataClient.newRecord(_newRecordResponder, keyMap, Application.activeRootEntity.name, version.name);
		}
		
		protected function cancelClickHandler(event:Event):void {
			PopUpManager.removePopUp(_view);
		}
		
		protected function newRecordResultHandler(event:ResultEvent, token:Object = null):void {
/*			var uiEvent:UIEvent = new UIEvent(UIEvent.NEW_RECORD_CREATED);
			
			uiEvent.obj = {versionId: _view.versionsDropDownList.selectedItem.id};
			eventDispatcher.dispatchEvent(uiEvent);
			PopUpManager.removePopUp(_view);*/
		}
		
		protected function newRecordFaultHandler(event:FaultEvent, token:Object = null):void {
			
/*			var uiEvent:UIEvent = new UIEvent(UIEvent.NEW_RECORD_CREATED);
			uiEvent.obj = {versionId: _view.versionsDropDownList.selectedItem.id};
			eventDispatcher.dispatchEvent(uiEvent);
			PopUpManager.removePopUp(_view);*/
		}
		
	}
}