package org.openforis.collect.presenter {
	import org.openforis.collect.i18n.Message;
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
		
		private var _view:BooleanInputField;
		
		public function BooleanInputFieldPresenter(inputField:BooleanInputField) {
			_view = inputField;
			_view.restrict = RESTRICT_PATTERN;
			super(inputField);
		}
		
		override protected function textToRequestValue():String {
			var value:String = null;
			var text:String = StringUtil.trim(_view.text);
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
			var attribute:AttributeProxy = _view.attribute;
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
		
	}
}
