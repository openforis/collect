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
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class NumericAttributePresenter extends CompositeAttributePresenter {
		
		public function NumericAttributePresenter(view:NumericAttributeRenderer) {
			_view = view;
			initRestriction();
			super(view);
			view.numericInputField.applyChangesOnFocusOut = false;
			//depends on view.currentState
			if(view.unitInputField != null) {
				view.unitInputField.applyChangesOnFocusOut = false;
				view.unitInputField.dropDownList.addEventListener(Event.CHANGE, unitInputFieldChangeHandler);
			}
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
				updateValue();
			}
		}
		
		protected function updateValue():void {
			var updReq:UpdateRequest = new UpdateRequest();
			var updateValueOp:UpdateRequestOperation = view.numericInputField.presenter.createUpdateValueOperation();
			updReq.addOperation(updateValueOp);
			var updateUnitOp:UpdateRequestOperation = createUpdateUnitOperation();
			if ( updateUnitOp != null ) {
				if(updateValueOp.value == null) {
					//clear unit
					updateUnitOp.value = null;
				}
				updReq.addOperation(updateUnitOp);
			}
			ClientFactory.dataClient.updateActiveRecord(updReq, null, faultHandler);
		}
		
		protected function createUpdateUnitOperation():UpdateRequestOperation {
			var numberAttrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			var result:UpdateRequestOperation = null;
			if(view.unitInputField != null) {
				result = view.unitInputField.presenter.createUpdateValueOperation();
			} else if ( numberAttrDefn.defaultUnit != null ) {
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
		
		override protected function initViewState():void {
			var attrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			var units:IList = attrDefn.units;
			if(units.length > 0) {
				if(units.length == 1) {
					if ( view.attributeDefinition.parentLayout == UIUtil.LAYOUT_FORM ) {
						view.currentState = NumericAttributeRenderer.SINGLE_UNIT_STATE;
						var unit:UnitProxy = UnitProxy(units.getItemAt(0));
						view.unitLabel.text = unit.name;
					} else {
						view.currentState = NumericAttributeRenderer.NO_UNIT_STATE;
					}
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
