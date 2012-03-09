package org.openforis.collect.ui.component.detail
{
	import flash.events.MouseEvent;
	
	import mx.core.IToolTip;
	import mx.core.UIComponent;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.util.ToolTipUtil;
	import org.openforis.collect.util.UIUtil;
	import org.openforis.idm.metamodel.validation.ValidationResultFlag;

	/**
	 * 
	 * @author S. Ricci
	 *
	 */
	public class ValidationDisplayManager {
		
		public static const STYLE_NAME_ERROR:String = "error"; 
		public static const STYLE_NAME_WARNING:String = "warning";

		private var _active:Boolean = false;
		
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
		
		public function displayNodeValidation(parentEntity:EntityProxy, defn:NodeDefinitionProxy, attribute:AttributeProxy = null):void {
			var flag:ValidationResultFlag = null;
			var message:String = null;
			if(parentEntity != null && defn != null) {
				var hasErrors:Boolean = attribute != null ? attribute.hasErrors(): false;
				var hasWarnings:Boolean = attribute != null ? attribute.hasWarnings(): false;
				var name:String = defn.name;
				//var required:Boolean = parentEntity.childrenRequiredMap.get(name);
				if(hasErrors || hasWarnings) {
					if(hasErrors) {
						flag = ValidationResultFlag.ERROR;
					} else if(hasWarnings) {
						flag = ValidationResultFlag.WARNING;
					}
					message = attribute.validationMessage;
				} else {
					var minCountValid:ValidationResultFlag = parentEntity.childrenMinCountValidationMap.get(name);
					var maxCountValid:ValidationResultFlag = parentEntity.childrenMaxCountValidationMap.get(name);
					if(minCountValid != ValidationResultFlag.OK || maxCountValid != ValidationResultFlag.OK) {
						if(minCountValid != ValidationResultFlag.OK) {
							flag = minCountValid;
							message = Message.get("edit.validation.minCount", [defn.minCount]);
						} else {
							flag = maxCountValid;
							message = Message.get("edit.validation.maxCount", [defn.maxCount]);
						}
					}
				}
			}
			apply(flag, message);
		}

		protected function apply(flag:ValidationResultFlag, message:String):void {
			if(_active) {
				var newStyleName:String;
				switch(flag) {
					case ValidationResultFlag.ERROR:
						_toolTipStyleName = ToolTipUtil.STYLE_NAME_ERROR;
						newStyleName = STYLE_NAME_ERROR;
						break;
					case ValidationResultFlag.WARNING:
						_toolTipStyleName = ToolTipUtil.STYLE_NAME_WARNING;
						newStyleName = STYLE_NAME_WARNING;
						break;
					default:
						reset();
						return;
				}
				_toolTipMessage = message;
				if(! _toolTipTrigger.hasEventListener(MouseEvent.ROLL_OVER)) {
					_toolTipTrigger.addEventListener(MouseEvent.ROLL_OVER, showToolTip);
					_toolTipTrigger.addEventListener(MouseEvent.ROLL_OUT, hideToolTip);
				}
				if(newStyleName != _displayStyleName) {
					_displayStyleName = newStyleName;
					UIUtil.replaceStyleNames(_display, [_displayStyleName], [STYLE_NAME_ERROR, STYLE_NAME_WARNING]);
				}
			} else {
				reset();
			}
		}
		
		public function reset():void {
			_displayStyleName = null;
			removeToolTip();
			UIUtil.removeStyleNames(_display, [
				STYLE_NAME_ERROR, 
				STYLE_NAME_WARNING
			]);
		}
		
		protected function removeToolTip():void {
			hideToolTip();
			_toolTipMessage = null;
			_toolTipStyleName = null;
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OVER, showToolTip);
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OUT, hideToolTip);
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

		public function get active():Boolean {
			return _active;
		}

		public function set active(value:Boolean):void {
			_active = value;
		}
		
	}
}