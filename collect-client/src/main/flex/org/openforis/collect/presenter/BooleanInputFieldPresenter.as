package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.BooleanAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.BooleanInputField;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class BooleanInputFieldPresenter extends InputFieldPresenter {
		
		private static const TRUE:String = Message.get("edit.booleanValue.true");
		private static const FALSE:String = Message.get("edit.booleanValue.false");
		private static const RESTRICT_PATTERN:String = "^(\\*|-|\\?|" + TRUE + "|" + FALSE + ")$";
		
		public function BooleanInputFieldPresenter(inputField:BooleanInputField) {
			super(inputField);
			inputField.restrict = RESTRICT_PATTERN;
		}
		
		private function get view():BooleanInputField {
			return BooleanInputField(_view);
		}
		
		override public function init():void {
			super.init();
			initView();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.checkBox.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
			view.checkBox.addEventListener(Event.CHANGE, changeHandler);
			view.checkBox.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			view.checkBox.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			//key focus change managed by key down handler
			view.checkBox.addEventListener(FocusEvent.KEY_FOCUS_CHANGE, preventDefaultHandler);
		}
		
		private function initView():void {
			if ( BooleanAttributeDefinitionProxy(view.attributeDefinition).affirmativeOnly ) {
				view.currentState = BooleanInputField.CHECKBOX_STATE;
			} else {
				view.currentState = BooleanInputField.DEFAULT_STATE;
			}
		}
		
		
		override protected function textToRequestValue():String {
			var value:String = null;
			var text:String = StringUtil.trim(view.text);
			switch(text) {
				case TRUE:
				case TRUE.toLowerCase():
					value = "true";
					break;
				case FALSE:
				case FALSE.toLowerCase():
					value = "false";
					break;
				default:
					value = text;
			}
			return value;
		}
		
		override protected function getTextFromValue():String {
			var attribute:AttributeProxy = view.attribute;
			if(attribute != null) {
				var field:FieldProxy = attribute.getField(0);
				var value:Object = field.value;
				if(field.symbol != null) {
					var shortCut:String = FieldProxy.getShortCutForReasonBlank(field.symbol);
					if(shortCut != null) {
						return shortCut;
					}
				}
				if(value != null) {
					var textVal:String = value.toString();
					if(textVal == "true") {
						return TRUE;
					} else if(textVal == "false") {
						return FALSE;
					}
				}
			}
			return "";
		}
		
		override protected function setFocusOnInputField():void {
			switch ( view.currentState ) {
			case BooleanInputField.CHECKBOX_STATE:
				view.checkBox.setFocus();
				break;
			case BooleanInputField.DEFAULT_STATE:
				view.textInput.setFocus();
				break;
			default:
			}
		}
		
		override protected function changeHandler(event:Event):void {
			super.changeHandler(event);
			updateValue();
			dispatchVisitedEvent();
		}
		
	}
}
