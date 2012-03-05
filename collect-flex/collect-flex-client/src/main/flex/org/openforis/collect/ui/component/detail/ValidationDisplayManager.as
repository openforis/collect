package org.openforis.collect.ui.component.detail
{
	import flash.events.MouseEvent;
	
	import mx.core.IToolTip;
	import mx.core.UIComponent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeStateProxy;
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
		
		public function init():void {
			reset();
			if(_active) {
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
		}
		
		public function reset():void {
			hideToolTip();
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OVER, showToolTip);
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OUT, hideToolTip);
			UIUtil.removeStyleNames(_display, [
				STYLE_NAME_ERROR, 
				STYLE_NAME_WARNING, 
				STYLE_NAME_NOT_RELEVANT
			]);
		}
		
		public function initByNode(parentEntity:EntityProxy, defn:NodeDefinitionProxy, attribute:AttributeProxy = null):void {
			_displayStyleName = null;
			_toolTipStyleName = null;
			_toolTipMessage = null;
			if(parentEntity != null && defn != null) {
				var hasErrors:Boolean = attribute != null ? attribute.hasErrors(): false;
				var hasWarnings:Boolean = attribute != null ? attribute.hasWarnings(): false;
				var name:String = defn.name;
				var minCountValid:Boolean = parentEntity.childrenMinCountValiditationMap.get(name);
				var maxCountValid:Boolean = parentEntity.childrenMaxCountValiditionMap.get(name);
				var relevant:Boolean = parentEntity.childrenRelevanceMap.get(name);
				var required:Boolean = parentEntity.childrenRequiredMap.get(name);
				if(hasErrors || hasWarnings) {
					_toolTipStyleName = hasWarnings ? ToolTipUtil.STYLE_NAME_WARNING: ToolTipUtil.STYLE_NAME_ERROR;
					_displayStyleName = hasWarnings ? STYLE_NAME_WARNING: STYLE_NAME_ERROR;
					_toolTipMessage = attribute.validationMessage;
				} else if(!minCountValid || !maxCountValid) {
					_toolTipStyleName = ToolTipUtil.STYLE_NAME_ERROR;
					_displayStyleName = STYLE_NAME_ERROR;
					if(!minCountValid) {
						_toolTipMessage = Message.get("edit.validation.minCount", [defn.minCount]);
					} else {
						_toolTipMessage = Message.get("edit.validation.maxCount", [defn.maxCount]);
					}
				}
				if(! relevant) {
					_displayStyleName += " " + ValidationDisplayManager.STYLE_NAME_NOT_RELEVANT;
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

		public function get active():Boolean {
			return _active;
		}

		public function set active(value:Boolean):void {
			_active = value;
		}
		
	}
}