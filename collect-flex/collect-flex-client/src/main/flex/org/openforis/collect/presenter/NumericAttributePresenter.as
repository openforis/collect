package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.collections.IList;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy$Type;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
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
			view.numericInputField.applyChangesOnFocusOut = false;
			if(view.unitInputField != null) {
				view.unitInputField.applyChangesOnFocusOut = false;
				view.unitInputField.dropDownList.addEventListener(Event.CHANGE, unitInputFieldChangeHandler);
			}
			initRestriction();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			view.numericInputField.addEventListener(FocusEvent.FOCUS_OUT, numericInputFieldFocusOutHandler);
		}
		
		protected function numericInputFieldFocusOutHandler(event:FocusEvent):void {
			if(view.numericInputField.changed) {
				updateValue();
			}
		}
		
		protected function unitInputFieldChangeHandler(event:Event):void {
			if(! view.numericInputField.isEmpty()) {
				updateUnitField();
			}
		}
		
		protected function updateValue():void {
			view.numericInputField.presenter.updateValue();
			updateUnitField();
		}
		
		protected function updateUnitField():void {
			var reqOp:UpdateRequestOperation = createUpdateUnitOperation();
			if(view.numericInputField.isEmpty()) {
				//clear unit
				reqOp.value = null;
			}
			var updReq:UpdateRequest = new UpdateRequest();
			updReq.addOperation(reqOp);
			ClientFactory.dataClient.updateActiveRecord(updReq, null, faultHandler);
		}
		
		protected function createUpdateUnitOperation():UpdateRequestOperation {
			var numberAttrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			var result:UpdateRequestOperation = null;
			if(view.unitInputField) {
				result = view.unitInputField.presenter.createUpdateValueOperation();
			} else {
				result = new UpdateRequestOperation();
				result.method = UpdateRequestOperation$Method.UPDATE;
				result.parentEntityId = view.attribute.parentId;
				result.nodeName = view.attributeDefinition.name;
				result.nodeId = view.attribute.id;
				result.fieldIndex = 1;
				result.value = numberAttrDefn.defaultUnit.name;
			}
			return result;
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
			var attrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			var units:IList = attrDefn.units;
			if(units.length > 0) {
				if(units.length == 1) {
					view.currentState = NumericAttributeRenderer.SINGLE_UNIT_STATE;
					var unit:UnitProxy = UnitProxy(units.getItemAt(0));
					view.unitLabel.text = unit.name;
				} else {
					view.currentState = NumericAttributeRenderer.MULTIPLE_UNIT_STATE;
					view.unitInputField.dataProvider = units;
					if(attrDefn.defaultUnit != null) {
						view.unitInputField.defaultValue = attrDefn.defaultUnit.name;
					}
				}
			}
		}
	}
}
