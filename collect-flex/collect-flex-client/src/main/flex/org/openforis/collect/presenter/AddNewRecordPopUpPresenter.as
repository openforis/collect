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
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.ui.component.AddNewRecordPopUp;

	public class AddNewRecordPopUpPresenter extends AbstractPresenter {
		private var _view:AddNewRecordPopUp;
		
		private var _newRecordResponder:IResponder;
		
		public function AddNewRecordPopUpPresenter(view:AddNewRecordPopUp) {
			this._view = view;
			_newRecordResponder = new AsyncResponder(newRecordResultHandler, newRecordFaultHandler);
			super();
		}
		
		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.SURVEY_SELECTED, surveySelectedHandler);
			_view.addEventListener(CloseEvent.CLOSE, cancelClickHandler);
			_view.addButton.addEventListener(MouseEvent.CLICK, addClickHandler);
			_view.cancelButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
			
			if(Application.activeSurvey != null) {
				var versions:ListCollectionView = Application.activeSurvey.versions;
				_view.versionsDropDownList.dataProvider = versions;
			}
		}
		
		protected function surveySelectedHandler(event:UIEvent):void {
			var survey:SurveyProxy = event.obj as SurveyProxy;
			if(survey != null) {
				var versions:ListCollectionView = survey.versions;
				_view.versionsDropDownList.dataProvider = versions;
			}
		}
		
		protected function addClickHandler(event:Event):void {
			var dataClient:DataClient = ClientFactory.dataClient;
			dataClient.newRecord(_newRecordResponder, _view.idTextInput.text, _view.versionsDropDownList.selectedItem.id);
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