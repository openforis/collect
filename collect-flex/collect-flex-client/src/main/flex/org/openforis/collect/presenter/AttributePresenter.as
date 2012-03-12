package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.core.UIComponent;
	import mx.events.PropertyChangeEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.detail.AttributeItemRenderer;
	import org.openforis.collect.ui.component.detail.RelevanceDisplayManager;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class AttributePresenter extends AbstractPresenter {
		
		protected var _view:AttributeItemRenderer;
		private var _validationDisplayManager:ValidationDisplayManager;
		private var _updating:Boolean = false;
		
		public function AttributePresenter(view:AttributeItemRenderer) {
			_view = view;
			var inputField:InputField = _view.getElementAt(0) as InputField;
			if(inputField != null) {
				ChangeWatcher.watch(inputField, "visited", fieldVisitedHandler);
				ChangeWatcher.watch(inputField, "updating", fieldUpdatingChangeHandler);
			}
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			eventDispatcher.addEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function initValidationDisplayManager():void {
			var inputField:InputField = _view.getElementAt(0) as InputField;
			var validationStateDisplay:UIComponent = inputField != null ? inputField.validationStateDisplay: _view;
			var validationToolTipTrigger:UIComponent = validationStateDisplay;
			_validationDisplayManager = new ValidationDisplayManager(validationToolTipTrigger, validationStateDisplay);
			if(_view.attribute != null) {
				updateValidationDisplayManager();
			}
		}
		
		protected function recordSavedHandler(event:ApplicationEvent):void {
			updateValidationDisplayManager();
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(_view.attribute != null) {
				var responses:IList = IList(event.result);
				for each (var response:UpdateResponse in responses) {
					if(response.nodeId == _view.attribute.id ||
						response.nodeId == _view.parentEntity.id) {
						updateValidationDisplayManager();
						break;
					}
				}
			}
		}
		
		protected function fieldVisitedHandler(event:PropertyChangeEvent):void {
			if(event.newValue == true && _view.attribute != null) {
				_view.attribute.visited = true;
			}
			updateValidationDisplayManager();
		}
		
		protected function fieldUpdatingChangeHandler(event:PropertyChangeEvent):void {
			_updating = event.newValue;
			updateValidationDisplayManager();
		}
		
		protected function attributeChangeHandler(event:Event):void {
			_view.visited = false;
			updateValidationDisplayManager();
		}
		
		protected function updateValidationDisplayManager():void {
			if(_validationDisplayManager == null) {
				initValidationDisplayManager();
			}
			var record:RecordProxy = Application.activeRecord;
			//to do - set visited=true to all attributes when record is saved
			var active:Boolean = !_updating && (!isNaN(record.id) || record.saved || (_view.attribute != null && _view.attribute.visited));
			if(active) {
				_validationDisplayManager.active = true;
				_validationDisplayManager.displayNodeValidation(_view.parentEntity, _view.attributeDefinition, _view.attribute);
			} else {
				_validationDisplayManager.active = false;
				_validationDisplayManager.reset();
			}
		}
		
	}
}
