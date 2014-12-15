package org.openforis.collect.presenter
{
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.RangeInputField;
	
	/**
	 * @author S. Ricci
	 */
	public class RangeInputFieldPresenter extends InputFieldPresenter {
		
		protected static const RESTRICT_PATTERN:String = "^(\\*|-|\\?|((-?\\d*\\.?\\d*)(-(-?\\d*\\.?\\d*))?))$";
		protected static const INTEGER_RESTRICT_PATTERN:String = "^(\\*|-|\\?|((-?\\d*)(-(-?\\d*))?))$";
		protected static const SEPARATOR:String = "-";

		public function RangeInputFieldPresenter(inputField:RangeInputField) {
			super(inputField);
			view.fieldIndex = -1;
		}
		
		override public function init():void {
			super.init();
			initFieldRestriction();
		}
		
		private function get view():RangeInputField {
			return RangeInputField(_view);
		}
		
		override protected function getTextFromValue():String {
			var attribute:AttributeProxy = view.attribute;
			if(attribute != null) {
				var field:FieldProxy = attribute.getField(0);
				if(field.symbol != null) {
					var shortKey:String = FieldProxy.getShortCutForReasonBlank(field.symbol);
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
		
		protected function initFieldRestriction():void {
			var integer:Boolean = RangeAttributeDefinitionProxy(view.attributeDefinition).integer;
			view.restrict = integer ? INTEGER_RESTRICT_PATTERN: RESTRICT_PATTERN;
		}
		
	}
}