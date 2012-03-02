package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	
	import org.openforis.collect.ui.component.detail.AttributeItemRenderer;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class AttributePresenter extends AbstractPresenter {
		
		protected var _view:AttributeItemRenderer;
		private var _validationDisplayManager:ValidationDisplayManager;
		
		public function AttributePresenter(view:AttributeItemRenderer) {
			_view = view;
			
			//initValidationDisplayManager();
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		/*
		protected function initValidationDisplayManager():void {
			var inputField:InputField = _view.getElementAt(0) as InputField;
			var validationStateDisplay:UIComponent = inputField != null ? inputField.validationStateDisplay: view;
			var validationToolTipTrigger:UIComponent = validationStateDisplay;
			_validationDisplayManager = new ValidationDisplayManager(validationToolTipTrigger, validationStateDisplay);
			if(_view.attribute != null) {
				_validationDisplayManager.initByState(_view.attribute.state);
			}
		}
		*/
		protected function attributeChangeHandler(event:Event):void {
			//var nodeState:NodeStateProxy = _view.attribute != null ? _view.attribute.state: null;
			//_validationDisplayManager.initByState(nodeState);
		}
		

	}
}
