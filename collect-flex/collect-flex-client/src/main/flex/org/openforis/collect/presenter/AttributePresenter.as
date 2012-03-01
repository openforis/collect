package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.core.UIComponent;
	
	import org.openforis.collect.model.proxy.NodeStateProxy;
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
			
			var inputField:InputField = _view.getElementAt(0) as InputField;
			var validationStateDisplay:UIComponent = inputField != null ? inputField.validationStateDisplay: view;
			var validationToolTipTrigger:UIComponent = validationStateDisplay;
			var state:NodeStateProxy = view.attribute != null ? view.attribute.state: null;
			_validationDisplayManager = new ValidationDisplayManager(validationToolTipTrigger, validationStateDisplay, null);
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function attributeChangeHandler(event:Event):void {
			var nodeState:NodeStateProxy = _view.attribute != null ? _view.attribute.state: null;
			_validationDisplayManager.state = nodeState;
		}
		

	}
}
