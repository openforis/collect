package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.collections.IList;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.model.proxy.FieldUpdateRequestProxy;
	import org.openforis.collect.model.proxy.RecordUpdateRequestProxy;
	import org.openforis.collect.model.proxy.RecordUpdateRequestSetProxy;
	import org.openforis.collect.ui.component.input.IntegerInputField;
	import org.openforis.collect.ui.component.input.NumericAttributeRenderer;
	import org.openforis.collect.ui.component.input.NumericInputField;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class NumericAttributePresenter extends CompositeAttributePresenter {
		
		private static const UNIT_FIELD_IDX:int = 2;
		
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
			var updReqSet:RecordUpdateRequestSetProxy = new RecordUpdateRequestSetProxy();
			var updateValueOp:RecordUpdateRequestProxy = view.numericInputField.presenter.createValueUpdateRequest();
			updReqSet.addRequest(updateValueOp);
			var updateUnitOp:FieldUpdateRequestProxy = createUpdateUnitOperation();
			if ( updateUnitOp != null ) {
				if ( updateValueOp is FieldUpdateRequestProxy &&
					FieldUpdateRequestProxy(updateValueOp).value == null) {
					//clear unit
					updateUnitOp.value = null;
				}
				updReqSet.addRequest(updateUnitOp);
			}
			ClientFactory.dataClient.updateActiveRecord(updReqSet, null, faultHandler);
		}
		
		protected function createUpdateUnitOperation():FieldUpdateRequestProxy {
			var attrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			var result:FieldUpdateRequestProxy = null;
			if(view.unitInputField != null) {
				result = view.unitInputField.presenter.createValueUpdateRequest() as FieldUpdateRequestProxy;
			} else if ( attrDefn.defaultUnit != null ) {
				result = new FieldUpdateRequestProxy();
				result.nodeId = view.attribute.id;
				result.fieldIndex = UNIT_FIELD_IDX;
				result.value = String(attrDefn.defaultUnit.id);
			}
			return result;
		}
		
		private function get view():NumericAttributeRenderer {
			return NumericAttributeRenderer(_view);
		}
		
		protected function initRestriction():void {
			var numberAttrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			view.numericInputField.restrict = numberAttrDefn.integer ? IntegerInputField.RESTRICTION_PATTERN: 
				NumericInputField.RESTRICTION_PATTERN;
		}
		
		override protected function initViewState():void {
			var attrDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(view.attributeDefinition);
			var units:IList = attrDefn.units;
			if(units.length > 0) {
				if(units.length == 1) {
					if ( view.attributeDefinition.parentLayout == UIUtil.LAYOUT_FORM ) {
						view.currentState = NumericAttributeRenderer.SINGLE_UNIT_STATE;
						var unit:UnitProxy = UnitProxy(units.getItemAt(0));
						view.unitLabel.text = unit.getAbbreviation();
					} else {
						view.currentState = NumericAttributeRenderer.NO_UNIT_STATE;
					}
				} else {
					view.currentState = NumericAttributeRenderer.MULTIPLE_UNIT_STATE;
					view.unitInputField.dataProvider = units;
					if(attrDefn.defaultUnit != null) {
						view.unitInputField.defaultValue = String(attrDefn.defaultUnit.id);
					}
				}
			}
		}
	}
}
