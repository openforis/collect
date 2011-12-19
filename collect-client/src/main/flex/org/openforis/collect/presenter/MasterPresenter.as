package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import flash.events.MouseEvent;
	
	import mx.events.EventListenerRequest;
	
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.component.MasterView;

	public class MasterPresenter extends AbstractPresenter {
		
		private var _view:MasterView;
		
		public function MasterPresenter(view:MasterView) {
			this._view = view;
			super();
		}
		
		override internal function initEventListeners():void{
			eventDispatcher.addEventListener(UIEvent.NEW_RECORD_CREATED, newRecordCreatedHandler);
			eventDispatcher.addEventListener(UIEvent.BACK_TO_LIST, backToListHandler);
			
			
		}
		
		protected function newRecordCreatedHandler(event:UIEvent):void {
			_view.currentState = "detail";
		}
		
		protected function backToListHandler(event:UIEvent):void {
			_view.currentState = "list";
			
			//load clusters...
		}
			
	}
}