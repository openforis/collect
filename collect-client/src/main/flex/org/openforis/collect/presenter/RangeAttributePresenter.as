package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.collections.IList;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.model.proxy.AttributeUpdateRequestProxy;
	import org.openforis.collect.model.proxy.FieldUpdateRequestProxy;
	import org.openforis.collect.model.proxy.RecordUpdateRequestProxy;
	import org.openforis.collect.model.proxy.RecordUpdateRequestSetProxy;
	import org.openforis.collect.ui.component.input.RangeAttributeRenderer;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RangeAttributePresenter extends CompositeAttributePresenter {
		
		private var UNIT_FIELD_IDX:int = 3;
		
		public function RangeAttributePresenter(view:RangeAttributeRenderer) {
			_view = view;
			super(view);
			view.rangeInputField.applyChangesOnFocusOut = false;
			//depends on view.currentState
			if(view.unitInputField != null) {
				view.unitInputField.applyChangesOnFocusOut = false;
				view.unitInputField.dropDownList.addEventListener(Event.CHANGE, unitInputFieldChangeHandler);
			}
		}
		
		private function get view():RangeAttributeRenderer {
			return RangeAttributeRenderer(_view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			view.rangeInputField.addEventListener(FocusEvent.FOCUS_OUT, rangeInputFieldFocusOutHandler);
		}
		
		protected function rangeInputFieldFocusOutHandler(event:FocusEvent):void {
			if(view.rangeInputField.changed) {
				updateValue();
			}
		}
		
		protected function unitInputFieldChangeHandler(event:Event):void {
			if(! view.rangeInputField.isEmpty()) {
				updateValue();
			}
		}
		
		protected function updateValue():void {
			var updReqSet:RecordUpdateRequestSetProxy = new RecordUpdateRequestSetProxy();
			var updateValueOp:RecordUpdateRequestProxy = view.rangeInputField.presenter.createValueUpdateRequest();
			updReqSet.addRequest(updateValueOp);
			var updateUnitOp:FieldUpdateRequestProxy = createUpdateUnitOperation();
			if ( updateUnitOp != null ) {
				if ( updateValueOp is AttributeUpdateRequestProxy && AttributeUpdateRequestProxy(updateValueOp).value == null) {
					//clear unit
					updateUnitOp.value = null;
				}
				updReqSet.addRequest(updateUnitOp);
			}
			ClientFactory.dataClient.updateActiveRecord(updReqSet, null, faultHandler);
		}

		protected function createUpdateUnitOperation():FieldUpdateRequestProxy {
			var attrDefn:RangeAttributeDefinitionProxy = RangeAttributeDefinitionProxy(view.attributeDefinition);
			var result:FieldUpdateRequestProxy = null;
			if(view.unitInputField) {
				result = view.unitInputField.presenter.createValueUpdateRequest() as FieldUpdateRequestProxy;
			} else if ( attrDefn.defaultUnit != null ) {
				result = new FieldUpdateRequestProxy();
				result.nodeId = view.attribute.id;
				result.fieldIndex = UNIT_FIELD_IDX;
				result.value = String(attrDefn.defaultUnit.id);
			}
			return result;
		}
		
		override protected function initViewState():void {
			var attrDefn:RangeAttributeDefinitionProxy = RangeAttributeDefinitionProxy(view.attributeDefinition);
			var units:IList = attrDefn.units;
			if(units.length > 0) {
				if(units.length == 1) {
					if ( attrDefn.parentLayout == UIUtil.LAYOUT_FORM ) {
						view.currentState = RangeAttributeRenderer.SINGLE_UNIT_STATE;
						var unit:UnitProxy = UnitProxy(units.getItemAt(0));
						view.unitLabel.text = unit.getAbbreviation();
					} else {
						view.currentState = RangeAttributeRenderer.NO_UNIT_STATE;
					}
				} else {
					view.currentState = RangeAttributeRenderer.MULTIPLE_UNIT_STATE;
					view.unitInputField.dataProvider = units;
					if(attrDefn.defaultUnit != null) {
						view.unitInputField.defaultValue = String(attrDefn.defaultUnit.id);
					}
				}
			}
		}
	}
}
