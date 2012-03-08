package org.openforis.collect.presenter
{
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.RangeInputField;
	
	/**
	 * @author S. Ricci
	 */
	public class RangeInputFieldPresenter extends InputFieldPresenter {
		
		protected static const SEPARATOR:String = "-";

		private var _view:RangeInputField;
		
		public function RangeInputFieldPresenter(inputField:RangeInputField) {
			_view = inputField;
			_view.fieldIndex = -1;
			super(inputField);
		}
		
		override protected function getTextFromValue():String {
			var attribute:AttributeProxy = _view.attribute;
			if(attribute != null) {
				var field:FieldProxy = attribute.getField(0);
				if(field.symbol != null) {
					var shortKey:String = getShortCutForReasonBlank(field.symbol);
					if(shortKey != null) {
						return shortKey;
					}
				}
				var start:Object = attribute.getField(0).value;
				var end:Object = attribute.getField(1).value;
				if(start != null && !isNaN(Number(start))) {
					var result:String = start.toString();
					if(end != null && !isNaN(Number(end)) && start != end) {
						result += SEPARATOR + end.toString();
					}
					return result;
				}
			}
			return "";
		}
		
	}
}