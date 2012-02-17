package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.core.UIComponent;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.FormItemEvent;
	import org.openforis.collect.ui.component.detail.CollectFormItem;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class FormItemPresenter extends AbstractPresenter {
		
		protected var _view:CollectFormItem;
		
		public function FormItemPresenter(view:CollectFormItem) {
			_view = view;
			
			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, modelChangedHandler);
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
			
			_view.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
			_view.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function modelChangedHandler(event:ApplicationEvent):void {
		}
		
		protected function updateView():void {
			
		}
		
		protected function mouseOverHandler(event:MouseEvent):void {
			var target:UIComponent = event.currentTarget as UIComponent;
			if(target != null && target.document != null) {
				var evt:FormItemEvent = new FormItemEvent(FormItemEvent.FORM_ITEM_MOUSE_OVER);
				evt.formItem = _view;
				eventDispatcher.dispatchEvent(evt);
			}
		}
		
		protected function mouseOutHandler(event:MouseEvent):void {
			var target:UIComponent = event.currentTarget as UIComponent;
			if(target != null && target.document != null) {
				var evt:FormItemEvent = new FormItemEvent(FormItemEvent.FORM_ITEM_MOUSE_OUT);
				evt.formItem = _view;
				eventDispatcher.dispatchEvent(evt);
			}
		}
		
	}
}