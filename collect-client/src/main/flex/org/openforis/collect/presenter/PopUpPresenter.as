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
		
		public function PopUpPresenter(view:PopUp) {
			super(view);
		}
		
		private function get view():PopUp {
			return PopUp(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.addEventListener(CloseEvent.CLOSE, closeHandler);
			view.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
		}
		
		protected function closeHandler(event:Event = null):void {
			PopUpManager.removePopUp(view);
		}
		
		protected function keyDownHandler(event:KeyboardEvent):void {
			switch(event.keyCode) {
				case Keyboard.ESCAPE:
					view.dispatchEvent(new CloseEvent(CloseEvent.CLOSE));
					break;
			}
		}
		
	}
}