package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.ui.Keyboard;
	
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	
	import org.openforis.collect.ui.component.PopUp;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class PopUpPresenter extends AbstractPresenter {
		
		protected var _view:PopUp;
		
		public function PopUpPresenter(view:PopUp) {
			this._view = view;

			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			_view.addEventListener(CloseEvent.CLOSE, closeHandler);
			_view.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
		}
		
		protected function closeHandler(event:Event = null):void {
			PopUpManager.removePopUp(_view);
		}
		
		protected function keyDownHandler(event:KeyboardEvent):void {
			switch(event.keyCode) {
				case Keyboard.ESCAPE:
					_view.dispatchEvent(new CloseEvent(CloseEvent.CLOSE));
					break;
			}
		}
		
	}
}