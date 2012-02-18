package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.controls.DateField;
	import mx.controls.TextInput;
	import mx.events.CalendarLayoutChangeEvent;
	import mx.events.DropdownEvent;
	import mx.managers.IFocusManagerComponent;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.model.proxy.DateProxy;
	import org.openforis.collect.ui.component.input.BooleanInputField;
	import org.openforis.collect.ui.component.input.DateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class BooleanInputFieldPresenter extends InputFieldPresenter {
		
		private static const TRUE:String = Message.get("edit.booleanValue.true");
		private static const FALSE:String = Message.get("edit.booleanValue.false");
		private static const RESTRICT_PATTERN:String = "[" + TRUE + FALSE + TRUE.toLowerCase() + FALSE.toLowerCase() + "\*\-\?]";
		
		private var _view:BooleanInputField;
		
		public function BooleanInputFieldPresenter(inputField:BooleanInputField) {
			_view = inputField;
			
			(inputField.textInput as TextInput).restrict = RESTRICT_PATTERN;
			super(inputField);
		}
		
		override protected function createValue():* {
			var result:* = null;
			var text:String = _view.text;
			switch(text) {
				case TRUE:
				case TRUE.toLowerCase():
					result = true;
					break;
				case FALSE:
				case FALSE.toLowerCase():
					result = false;
					break;
				default:
					result = text;
			}
			return result;
		}
		
		override protected function getTextValue():String {
			var attribute:AttributeProxy = _view.attribute;
			if(attribute != null) {
				var value:Object = attribute.value;
				if(attribute.symbol != null) {
					var shortKey:String = getReasonBlankShortKey(attribute.symbol);
					if(shortKey != null) {
						return shortKey;
					}
				} else if(value != null) {
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
		
	}
}
