package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.core.FlexGlobals;
	import mx.events.FlexEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.view.ListView;
	import org.openforis.collect.ui.component.AddNewRecordPopUp;

	public class ListPresenter extends AbstractPresenter {
		
		private var _view:ListView;
		
		private var _newRecordPopUp:AddNewRecordPopUp;
		
		public function ListPresenter(view:ListView) {
			this._view = view;
			super();
		}
		
		override internal function initEventListeners():void{
			if(this._view != null) {
				this._view.newRecordButton.addEventListener(MouseEvent.CLICK, newRecordButtonClickHandler);
			}
		}
		
		protected function newRecordButtonClickHandler(event:MouseEvent):void {
			if(_newRecordPopUp == null) {
				_newRecordPopUp = new AddNewRecordPopUp();
				
			}
			PopUpManager.addPopUp(_newRecordPopUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(_newRecordPopUp);
		}
		
		
	}
}