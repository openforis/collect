package org.openforis.collect.ui.component.detail
{
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;
	
	import mx.core.IToolTip;
	import mx.core.UIComponent;
	
	import org.openforis.collect.model.proxy.AttributeProxy;
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
		
		public static const STYLE_NAME_NOT_RELEVANT:String = "notRelevant";
		public static const STYLE_NAME_ERROR:String = "error"; 
		public static const STYLE_NAME_WARNING:String = "warning";

		/**
		 * Display of the error (stylename "error" or "warning" will be set on this component)
		 */
		private var _display:UIComponent;
		/**
		 * Component that triggers the tooltip opening with mouse events
		 */
		private var _toolTipTrigger:UIComponent;
		
		/**
		 * Current instance of the tooltip
		 */
		private var _toolTip:IToolTip;
		
		private var _toolTipStyleName:String;
		
		private var _toolTipMessage:String;
		
		private var _displayStyleName:String;
		
		public function ValidationDisplayManager(toolTipTrigger:UIComponent, display:UIComponent) {
			_toolTipTrigger = toolTipTrigger;
			_display = display;
		}
		
		public function init():void {
			hideToolTip();
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OVER, showToolTip);
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OUT, hideToolTip);
			UIUtil.removeStyleNames(_display, [
				STYLE_NAME_ERROR, 
				STYLE_NAME_WARNING, 
				STYLE_NAME_NOT_RELEVANT
			]);
			if(_toolTipStyleName != null) {
				if(! _toolTipTrigger.hasEventListener(MouseEvent.ROLL_OVER)) {
					_toolTipTrigger.addEventListener(MouseEvent.ROLL_OVER, showToolTip);
					_toolTipTrigger.addEventListener(MouseEvent.ROLL_OUT, hideToolTip);
				}
			}
			if(_displayStyleName != null) {
				UIUtil.addStyleName(_display, _displayStyleName);
			}
		}
		
		public function initByState(state:NodeStateProxy):void {
			_displayStyleName = null;
			_toolTipStyleName = null;
			_toolTipMessage = null;
			if(state != null) {
				var hasErrors:Boolean = state.hasErrors();
				var hasWarnings:Boolean = state.hasWarnings();
				
				if(! state.relevant) {
					_displayStyleName = STYLE_NAME_NOT_RELEVANT;
				} else if(hasErrors || hasWarnings) {
					_toolTipStyleName = hasErrors ? ToolTipUtil.STYLE_NAME_ERROR: ToolTipUtil.STYLE_NAME_WARNING;
					_toolTipMessage = state.validationMessage;
					_displayStyleName = hasErrors ? STYLE_NAME_ERROR: STYLE_NAME_WARNING;
				}
			}
			init();
		}
		
		public function initByAttribute(a:AttributeProxy):void {
			_displayStyleName = null;
			_toolTipStyleName = null;
			_toolTipMessage = null;
			if(a != null) {
				var hasErrors:Boolean = a.hasErrors();
				var hasWarnings:Boolean = a.hasWarnings();
				
				/*if(! state.relevant) {
					_displayStyleName = STYLE_NAME_NOT_RELEVANT;
				} else */
				if(hasErrors || hasWarnings) {
					_toolTipStyleName = hasErrors ? ToolTipUtil.STYLE_NAME_ERROR: ToolTipUtil.STYLE_NAME_WARNING;
					_toolTipMessage = a.validationMessage;
					_displayStyleName = hasErrors ? STYLE_NAME_ERROR: STYLE_NAME_WARNING;
				}
			}
			init();
		}
		
		protected function showToolTip(event:MouseEvent = null):void {
			if(_toolTip != null){
				ToolTipUtil.destroy(_toolTip);
			}
			if(_toolTipStyleName != null) {
				_toolTip = ToolTipUtil.create(_display, _toolTipMessage, _toolTipStyleName);
			}
		}
		
		public function hideToolTip(event:MouseEvent=null):void {
			ToolTipUtil.destroy(_toolTip);
			_toolTip = null;
		}

		public function get toolTipStyleName():String {
			return _toolTipStyleName;
		}

		public function set toolTipStyleName(value:String):void {
			_toolTipStyleName = value;
		}

		public function get toolTipMessage():String {
			return _toolTipMessage;
		}

		public function set toolTipMessage(value:String):void {
			_toolTipMessage = value;
		}

		public function get displayStyleName():String {
			return _displayStyleName;
		}

		public function set displayStyleName(value:String):void {
			_displayStyleName = value;
		}
		
	}
}