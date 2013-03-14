package org.openforis.collect.ui.component.detail
{
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.IToolTip;
	import mx.core.UIComponent;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.util.StringUtil;
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

		private var _showMinMaxCountErrors:Boolean = false;
		/**
		 * Current instance of the tooltip
		 */
		private var _toolTip:IToolTip;
		private var _toolTipStyleName:String;
		private var _toolTipMessages:IList;
		private var _displayStyleName:String;
		
		public function ValidationDisplayManager(toolTipTrigger:UIComponent, display:UIComponent) {
			_toolTipTrigger = toolTipTrigger;
			_display = display;
		}
		
		public function displayAttributeValidation(parentEntity:EntityProxy, defn:NodeDefinitionProxy, attribute:AttributeProxy):void {
			var flag:ValidationResultFlag = null;
			var validationMessages:IList = null;
			if(parentEntity != null && defn != null) {
				var hasErrors:Boolean = attribute != null ? attribute.hasErrors(): false;
				var hasWarnings:Boolean = attribute != null ? attribute.hasWarnings(): false;
				var confirmedError:Boolean = false;
				if(hasErrors || hasWarnings) {
					if(hasErrors) {
						flag = ValidationResultFlag.ERROR;
					} else if(hasWarnings) {
						flag = ValidationResultFlag.WARNING;
						if ( attribute.errorConfirmed ) {
							confirmedError = true;
						}
					}
					validationMessages = attribute.validationResults.validationMessages;
					apply(flag, validationMessages, confirmedError);
				} else if(showMinMaxCountErrors) {
					displayMinMaxCountValidationErrors(parentEntity, defn);
				} else {
					reset();
				}
			} else {
				reset();
			}
		}
		
		public function displayAttributesValidation(parentEntity:EntityProxy, defn:NodeDefinitionProxy):void {
			var flag:ValidationResultFlag = null;
			var validationMessages:IList = null;
			if(parentEntity != null && defn != null) {
				var errorMessages:ArrayCollection = new ArrayCollection();
				var warningMessages:ArrayCollection = new ArrayCollection();
				var attributes:IList = parentEntity.getChildren(defn.name);
				var confirmedError:Boolean = false;
				for each (var a:AttributeProxy in attributes) {
					if (a.hasErrors()) {
						errorMessages.addAll(a.validationResults.validationMessages);
					}
					if (a.hasWarnings()) {
						warningMessages.addAll(a.validationResults.validationMessages);
						if ( a.errorConfirmed ) {
							confirmedError = true;
						}
					}
				}
				var hasErrors:Boolean = errorMessages.length > 0;
				var hasWarnings:Boolean = warningMessages.length > 0;
				if(hasErrors || hasWarnings) {
					if(hasErrors) {
						flag = ValidationResultFlag.ERROR;
						validationMessages = errorMessages;
					} else {
						flag = ValidationResultFlag.WARNING;
						validationMessages = warningMessages;
					}
					apply(flag, validationMessages, confirmedError);
				} else if(showMinMaxCountErrors) {
					displayMinMaxCountValidationErrors(parentEntity, defn);
				} else {
					reset();
				}
			} else {
				reset();
			}
		}
		
		public function displayMinMaxCountValidationErrors(parentEntity:EntityProxy, defn:NodeDefinitionProxy):void {
			var flag:ValidationResultFlag = null;
			var validationMessages:IList = null;
			var name:String = defn.name;
			var minCountValid:ValidationResultFlag = parentEntity.childrenMinCountValidationMap.get(name);
			var maxCountValid:ValidationResultFlag = parentEntity.childrenMaxCountValidationMap.get(name);
			if(minCountValid != ValidationResultFlag.OK || maxCountValid != ValidationResultFlag.OK) {
				if(minCountValid != ValidationResultFlag.OK) {
					flag = minCountValid;
					if ( defn.minCount == 1 ) {
						validationMessages = new ArrayCollection([Message.get("edit.validation.requiredField")]);
					} else {
						validationMessages = new ArrayCollection([Message.get("edit.validation.minCount", [defn.minCount])]);
					}
				} else {
					flag = maxCountValid;
					validationMessages = new ArrayCollection([Message.get("edit.validation.maxCount", [defn.maxCount > 0 ? defn.maxCount: 1])]);
				}
				apply(flag, validationMessages);
			} else {
				reset();
			}
		}

		protected function apply(flag:ValidationResultFlag, messages:IList, confirmedError:Boolean = false):void {
			if(_active) {
				var newStyleName:String;
				switch(flag) {
					case ValidationResultFlag.ERROR:
						_toolTipStyleName = ToolTipUtil.STYLE_NAME_ERROR;
						newStyleName = STYLE_NAME_ERROR;
						break;
					case ValidationResultFlag.WARNING:
						if ( confirmedError ) {
							_toolTipStyleName = ToolTipUtil.STYLE_NAME_WARNING_CONFIRMED_ERROR;
						} else {
							_toolTipStyleName = ToolTipUtil.STYLE_NAME_WARNING;
						}
						newStyleName = STYLE_NAME_WARNING;
						break;
					default:
						reset();
						return;
				}
				_toolTipMessages = messages;
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
			_toolTipMessages = null;
			_toolTipStyleName = null;
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OVER, showToolTip);
			_toolTipTrigger.removeEventListener(MouseEvent.ROLL_OUT, hideToolTip);
		}
		
		protected function showToolTip(event:MouseEvent = null):void {
			if(_toolTip != null){
				ToolTipUtil.destroy(_toolTip);
			}
			if(_toolTipStyleName != null) {
				_toolTip = ToolTipUtil.create(_display, _toolTipMessages, _toolTipStyleName);
			}
		}
		
		public function hideToolTip(event:MouseEvent = null):void {
			ToolTipUtil.destroy(_toolTip);
			_toolTip = null;
		}

		public function get active():Boolean {
			return _active;
		}

		public function set active(value:Boolean):void {
			_active = value;
		}

		public function get showMinMaxCountErrors():Boolean {
			return _showMinMaxCountErrors;
		}

		public function set showMinMaxCountErrors(value:Boolean):void {
			_showMinMaxCountErrors = value;
		}

		
	}
}