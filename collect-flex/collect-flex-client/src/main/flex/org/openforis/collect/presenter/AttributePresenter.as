package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.core.UIComponent;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.detail.AttributeItemRenderer;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;
	import org.openforis.collect.ui.component.input.InputField;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class AttributePresenter extends AbstractPresenter {
		
		protected var _view:AttributeItemRenderer;
		private var _validationDisplayManager:ValidationDisplayManager;
		
		public function AttributePresenter(view:AttributeItemRenderer) {
			_view = view;
			
			initValidationDisplayManager();
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function initValidationDisplayManager():void {
			var inputField:InputField = _view.getElementAt(0) as InputField;
			var validationStateDisplay:UIComponent = inputField != null ? inputField.validationStateDisplay: _view;
			var validationToolTipTrigger:UIComponent = validationStateDisplay;
			_validationDisplayManager = new ValidationDisplayManager(validationToolTipTrigger, validationStateDisplay);
			if(_view.attribute != null) {
				//_validationDisplayManager.initByState(_view.attribute.state);
				_validationDisplayManager.initByAttribute(_view.attribute);
			}
		}
		
		protected function attributeChangeHandler(event:Event):void {
			_validationDisplayManager.initByAttribute(_view.attribute);
			//var nodeState:NodeStateProxy = _view.attribute != null ? _view.attribute.state: null;
			//_validationDisplayManager.initByState(nodeState);
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(_view.attribute != null) {
				var responses:IList = IList(event.result);
				for each (var response:UpdateResponse in responses) {
					if(response.nodeId == _view.attribute.id) {
						_validationDisplayManager.initByAttribute(_view.attribute);
					}
				}
			}
		}

	}
}
