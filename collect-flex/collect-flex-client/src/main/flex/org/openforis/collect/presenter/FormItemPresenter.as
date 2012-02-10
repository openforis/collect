package org.openforis.collect.presenter
{
	import flash.display.DisplayObject;
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.FormItem;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class FormItemPresenter extends AbstractPresenter {
		
		protected var _view:FormItem;
		
		public function FormItemPresenter(view:FormItem) {
			_view = view;
			
			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.MODEL_CHANGED, modelChangedHandler);
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function modelChangedHandler(event:ApplicationEvent):void {
		}
		
		protected function updateView():void {
			
		}
		
	}
}