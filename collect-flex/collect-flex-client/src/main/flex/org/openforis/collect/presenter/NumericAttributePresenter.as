package org.openforis.collect.presenter {
	import mx.collections.IList;
	
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy$Type;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.ui.component.input.IntegerInputField;
	import org.openforis.collect.ui.component.input.NumericAttributeRenderer;
	import org.openforis.collect.ui.component.input.NumericInputField;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class NumericAttributePresenter extends CompositeAttributePresenter {
		
		public function NumericAttributePresenter(view:NumericAttributeRenderer) {
			_view = view;
			super(view);
			initUnits();
			initRestriction();
		}
		
		private function get view():NumericAttributeRenderer {
			return NumericAttributeRenderer(_view);
		}
		
		protected function initRestriction():void {
			var numberAttrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			if(numberAttrDefn.type == NumberAttributeDefinitionProxy$Type.INTEGER) {
				view.numericInputField.restrict = IntegerInputField.RESTRICTION_PATTERN;
			} else {
				view.numericInputField.restrict = NumericInputField.RESTRICTION_PATTERN;
			}
		}
		
		protected function initUnits():void {
			var numberAttrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			var units:IList = numberAttrDefn.units;
			if(units.length > 0) {
				if(units.length == 1) {
					view.currentState = NumericAttributeRenderer.SINGLE_UNIT_STATE;
					var unit:UnitProxy = UnitProxy(units.getItemAt(0));
					view.unitLabel.text = unit.name;
				} else {
					view.currentState = NumericAttributeRenderer.MULTIPLE_UNIT_STATE;
					view.unitDropDownList.dataProvider = units;
					if(numberAttrDefn.defaultUnit != null) {
						view.unitDropDownList.dropDownList.selectedItem = numberAttrDefn.defaultUnit;
					}
				}
			}
		}
	}
}
