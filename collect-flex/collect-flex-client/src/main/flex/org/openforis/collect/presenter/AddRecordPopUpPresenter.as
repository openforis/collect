package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ListCollectionView;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.granite.collections.BasicMap;
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.ui.component.AddRecordPopUp;

	public class AddRecordPopUpPresenter extends AbstractPresenter {
		private var _view:AddRecordPopUp;
		
		private var _newRecordResponder:IResponder;
		
		public function AddRecordPopUpPresenter(view:AddRecordPopUp) {
			this._view = view;
			_newRecordResponder = new AsyncResponder(createRecordResultHandler, faultHandler);
			super();
			
			if(Application.activeSurvey != null) {
				var versions:ListCollectionView = Application.activeSurvey.versions;
				_view.versionsDropDownList.dataProvider = versions;
			}
			if(Application.activeRootEntity != null) {
				updatePopUp();
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
			updatePopUp();
		}
		
		protected function addClickHandler(event:Event):void {
			var dataClient:DataClient = ClientFactory.dataClient;
			var version:ModelVersionProxy = ModelVersionProxy(_view.versionsDropDownList.selectedItem);
			var rootEntityName:String = Application.activeRootEntity.name;
			dataClient.createNewRecord(_newRecordResponder, rootEntityName, version.name);
		}
		
		protected function cancelClickHandler(event:Event):void {
			PopUpManager.removePopUp(_view);
		}
		
		protected function createRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var record:RecordProxy = event.result as RecordProxy;
			record.rootEntityKeys = new BasicMap();
			var uiEvent:UIEvent = new UIEvent(UIEvent.RECORD_CREATED);
			uiEvent.obj = record;
			eventDispatcher.dispatchEvent(uiEvent);
			PopUpManager.removePopUp(_view);
		}
		
		protected function updatePopUp():void {
			if(Application.activeRootEntity != null) {
				_view.title = Message.get('list.newRecordPopUp.title', [Application.activeRootEntity.getLabelText()]);
			}
		}
		
	}
}