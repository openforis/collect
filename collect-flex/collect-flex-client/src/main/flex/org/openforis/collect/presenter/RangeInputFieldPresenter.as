package org.openforis.collect.presenter
{
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy$Type;
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

		private var _view:RangeInputField;
		
		public function RangeInputFieldPresenter(inputField:RangeInputField) {
			_view = inputField;
			_view.fieldIndex = -1;
			super(inputField);
			setRestriction();
		}
		
		override protected function getTextFromValue():String {
			var attribute:AttributeProxy = _view.attribute;
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
		
		protected function setRestriction():void {
			var type:RangeAttributeDefinitionProxy$Type = RangeAttributeDefinitionProxy(_view.attributeDefinition).type;
			switch(type) {
				case RangeAttributeDefinitionProxy$Type.INTEGER:
					_view.restrict = INTEGER_RESTRICT_PATTERN;
					break;
				default:
					_view.restrict = RESTRICT_PATTERN;
			}
		}
		
	}
}