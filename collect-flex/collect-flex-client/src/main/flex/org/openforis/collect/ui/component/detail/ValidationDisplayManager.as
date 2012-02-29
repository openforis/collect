package org.openforis.collect.ui.component.detail
{
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;
	
	import mx.core.IToolTip;
	import mx.core.UIComponent;
	
	import org.openforis.collect.model.proxy.NodeStateProxy;
	import org.openforis.collect.model.proxy.ValidationResultsProxy;
	import org.openforis.collect.util.ToolTipUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *
	 */
	public class ValidationDisplayManager {
		
		/**
		 * Display of the error (stylename "error" or "warning" will be set on this component)
		 */
		private var _display:UIComponent;
		/**
		 * Component that triggers the tooltip opening with mouse events
		 */
		private var _toolTipTrigger:UIComponent;
		
		private var _state:NodeStateProxy;
		/**
		 * Current instance of the tooltip
		 */
		private var _toolTip:IToolTip;
		
		public function ValidationDisplayManager(toolTipTrigger:UIComponent, display:UIComponent, state:NodeStateProxy = null) {
			_toolTipTrigger = toolTipTrigger;
			_display = display;
			_state = state;
			init();
		}
		
		protected function init():void {
			hideToolTip();
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OVER, showToolTip);
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OUT, hideToolTip);
			UIUtil.removeStyleNames(_display, [
				AttributeItemRenderer.STYLE_NAME_ERROR, 
				AttributeItemRenderer.STYLE_NAME_WARNING, 
				AttributeItemRenderer.STYLE_NOT_RELEVANT
			]);
			if(_state != null) {
				var validationResults:ValidationResultsProxy = _state.validationResults;
				var hasErrors:Boolean = _state.hasErrors();
				var hasWarnings:Boolean = _state.hasWarnings();
				
				var styleName:String = null;
				if(! _state.relevant) {
					styleName = AttributeItemRenderer.STYLE_NOT_RELEVANT;
				} else if(hasErrors || hasWarnings) {
					if(! _toolTipTrigger.hasEventListener(MouseEvent.ROLL_OVER)) {
						_toolTipTrigger.addEventListener(MouseEvent.ROLL_OVER, showToolTip);
						_toolTipTrigger.addEventListener(MouseEvent.ROLL_OUT, hideToolTip);
					}
					styleName = hasErrors ? AttributeItemRenderer.STYLE_NAME_ERROR: AttributeItemRenderer.STYLE_NAME_WARNING;
				}
				if(styleName != null) {
					UIUtil.addStyleName(_display, styleName);
				}
			}
		}
		
		protected function showToolTip(event:MouseEvent = null):void {
			if(_toolTip != null){
				ToolTipUtil.destroy(_toolTip);
			}
			if(_state != null) {
				var hasErrors:Boolean = _state.hasErrors();
				var hasWarnings:Boolean = _state.hasWarnings();
				var styleName:String = hasErrors ? ToolTipUtil.STYLE_NAME_ERROR: ToolTipUtil.STYLE_NAME_WARNING;
				var message:String = _state.validationMessage;
				_toolTip = ToolTipUtil.create(_display, message, styleName);
			}
		}
		
		public function hideToolTip(event:MouseEvent=null):void {
			ToolTipUtil.destroy(_toolTip);
			_toolTip = null;
		}

		/**
		 * State of the node used to get the validation message and the type of error
		 */
		public function get state():NodeStateProxy {
			return _state;
		}

		public function set state(value:NodeStateProxy):void {
			_state = value;
			init();
		}
		
		
	}
}