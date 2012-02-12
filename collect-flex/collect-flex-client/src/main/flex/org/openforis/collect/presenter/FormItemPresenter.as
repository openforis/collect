package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	
	import org.openforis.collect.event.ApplicationEvent;
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