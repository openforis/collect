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
	import org.openforis.collect.util.UIUtil;
	
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
			if(_view.parentEntity != null) {
				updateRelevance();
			}
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
				_validationDisplayManager.initByAttribute(_view.attribute);
			}
		}
		
		protected function attributeChangeHandler(event:Event):void {
			_validationDisplayManager.initByAttribute(_view.attribute);
			updateRelevance();
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(_view.attribute != null) {
				var responses:IList = IList(event.result);
				for each (var response:UpdateResponse in responses) {
					if(response.nodeId == _view.attribute.id) {
						_validationDisplayManager.initByAttribute(_view.attribute);
					} else if(response.nodeId == _view.parentEntity.id) {
						updateRelevance();
					}
				}
			}
		}
		
		protected function updateRelevance():void {
			if(_view.parentEntity != null && _view.attributeDefinition != null) {
				var relevant:Boolean = _view.parentEntity.childrenRelevanceMap.get(_view.attributeDefinition.name);
				if(relevant) {
					UIUtil.removeStyleName(_view, ValidationDisplayManager.STYLE_NAME_NOT_RELEVANT);
				} else {
					UIUtil.addStyleName(_view, ValidationDisplayManager.STYLE_NAME_NOT_RELEVANT);
				}
			}
		}
	}
}
