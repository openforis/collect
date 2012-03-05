package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.core.UIComponent;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class FormItemPresenter extends AbstractPresenter {
		
		protected var _view:CollectFormItem;
		protected var _validationDisplayManager:ValidationDisplayManager;
		
		public function FormItemPresenter(view:CollectFormItem) {
			_view = view;
			_validationDisplayManager = new ValidationDisplayManager(view, view);
			
			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
			ChangeWatcher.watch(_view, "modelVersion", modelVersionChangeHandler);
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function modelVersionChangeHandler(event:Event):void {
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
		}
		
		protected function updateView():void {
			
		}
		
		protected function initValidationDisplayManager():void {
		}
		
		protected function updateValidationDisplayManager():void {
		}
		
		protected function get validationDisplayManager():ValidationDisplayManager {
			return _validationDisplayManager;
		}
	}
}