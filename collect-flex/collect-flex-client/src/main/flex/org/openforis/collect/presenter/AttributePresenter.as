package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
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
		private var _validationListener:UIComponent;
		private var _validationToolTip:IToolTip;
		
		public function AttributePresenter(view:AttributeItemRenderer) {
			_view = view;
			
			var inputField:InputField = _view.getElementAt(0) as InputField;
			_validationListener = inputField != null ? inputField.validationListener: view;
			
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
			_validationListener.removeEventListener(MouseEvent.ROLL_OVER, mouseRollOverHandler);
			_validationListener.removeEventListener(MouseEvent.ROLL_OUT, mouseRollOutHandler);
			_validationListener.styleName = "noError";
			var a:AttributeProxy = _view.attribute;
			if(a != null && a.state != null) {
				var validationResults:ValidationResultsProxy = a.state.validationResults;
				var invalidAttribute:Boolean = a.state.hasErrors() || a.state.hasWarnings();
				
				if(invalidAttribute) {
					if(! _validationListener.hasEventListener(MouseEvent.ROLL_OVER)) {
						_validationListener.addEventListener(MouseEvent.ROLL_OVER, mouseRollOverHandler);
						_validationListener.addEventListener(MouseEvent.ROLL_OUT, mouseRollOutHandler);
					}
				}
				_validationListener.styleName = "error";
			}
		}
		
		protected function mouseRollOverHandler(event:MouseEvent = null):void {
			if(_validationToolTip != null){
				ToolTipUtil.destroy(_validationToolTip);
			}
			var a:AttributeProxy = _view.attribute;
			if(a != null && a.state != null) {
				var styleName:String = "error";
				var message:String = a.state.validationMessage;
				_validationToolTip = ToolTipUtil.create(_validationListener, message, styleName);
			}
		}
		
		public function mouseRollOutHandler(event:MouseEvent=null):void {
			ToolTipUtil.destroy(_validationToolTip);
			_validationToolTip = null;
		}
		

	}
}
