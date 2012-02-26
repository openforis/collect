package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.core.IToolTip;
	import mx.core.UIComponent;
	
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.ValidationResultsProxy;
	import org.openforis.collect.ui.component.detail.AttributeItemRenderer;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.ToolTipUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class AttributePresenter extends AbstractPresenter {
		
		protected var _view:AttributeItemRenderer;
		private var _validationStateDisplay:UIComponent;
		private var _validationToolTipTrigger:UIComponent;
		private var _validationToolTip:IToolTip;
		
		public function AttributePresenter(view:AttributeItemRenderer) {
			_view = view;
			
			var inputField:InputField = _view.getElementAt(0) as InputField;
			_validationStateDisplay = inputField != null ? inputField.validationStateDisplay: view;
			_validationToolTipTrigger = _validationStateDisplay;
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			initValidationResultHandler();
			
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function attributeChangeHandler(event:Event):void {
			initValidationResultHandler();
		}
		
		protected function initValidationResultHandler():void {
			_validationToolTipTrigger.removeEventListener(MouseEvent.ROLL_OVER, mouseRollOverHandler);
			_validationToolTipTrigger.removeEventListener(MouseEvent.ROLL_OUT, mouseRollOutHandler);
			_validationStateDisplay.styleName = null;
			var a:AttributeProxy = _view.attribute;
			if(a != null && a.state != null) {
				var validationResults:ValidationResultsProxy = a.state.validationResults;
				var hasErrors:Boolean = a.state.hasErrors();
				var hasWarnings:Boolean = a.state.hasWarnings();
				
				if(hasErrors || hasWarnings) {
					if(! _validationToolTipTrigger.hasEventListener(MouseEvent.ROLL_OVER)) {
						_validationToolTipTrigger.addEventListener(MouseEvent.ROLL_OVER, mouseRollOverHandler);
						_validationToolTipTrigger.addEventListener(MouseEvent.ROLL_OUT, mouseRollOutHandler);
					}
					_validationStateDisplay.styleName = hasErrors ? AttributeItemRenderer.STYLE_NAME_ERROR: AttributeItemRenderer.STYLE_NAME_WARNING;
				}
			}
		}
		
		protected function mouseRollOverHandler(event:MouseEvent = null):void {
			if(_validationToolTip != null){
				ToolTipUtil.destroy(_validationToolTip);
			}
			var a:AttributeProxy = _view.attribute;
			if(a != null && a.state != null) {
				var hasErrors:Boolean = a.state.hasErrors();
				var hasWarnings:Boolean = a.state.hasWarnings();
				var styleName:String = hasErrors ? ToolTipUtil.STYLE_NAME_ERROR: ToolTipUtil.STYLE_NAME_WARNING;
				var message:String = a.state.validationMessage;
				_validationToolTip = ToolTipUtil.create(_validationStateDisplay, message, styleName);
			}
		}
		
		public function mouseRollOutHandler(event:MouseEvent=null):void {
			ToolTipUtil.destroy(_validationToolTip);
			_validationToolTip = null;
		}
		

	}
}
