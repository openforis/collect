package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.core.UIComponent;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.RelevanceDisplayManager;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class FormItemPresenter extends AbstractPresenter {
		
		protected var _view:CollectFormItem;
		protected var _validationDisplayManager:ValidationDisplayManager;
		protected var _relevanceDisplayManager:RelevanceDisplayManager;
		
		public function FormItemPresenter(view:CollectFormItem) {
			_view = view;
			_relevanceDisplayManager = new RelevanceDisplayManager(view);
			updateRelevanceDisplayManager();

			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
			ChangeWatcher.watch(_view, "modelVersion", modelVersionChangeHandler);
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
		}
		
		protected function recordSavedHandler(event:ApplicationEvent):void {
			updateValidationDisplayManager(true);
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function modelVersionChangeHandler(event:Event):void {
		}
		
		protected function updateView():void {
			updateRelevanceDisplayManager();
		}
		
		protected function initValidationDisplayManager():void {
			_validationDisplayManager = new ValidationDisplayManager(_view, _view);
		}
		
		protected function updateRelevanceDisplayManager():void {
			
		}
		
		protected function updateValidationDisplayManager(forceActivation:Boolean = false):void {
			if(_validationDisplayManager == null) {
				initValidationDisplayManager();
			}
		}
		
		protected function get validationDisplayManager():ValidationDisplayManager {
			return _validationDisplayManager;
		}
		
		protected function get relevanceDisplayManager():RelevanceDisplayManager {
			return _relevanceDisplayManager;
		}
	}
}