package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.collections.IList;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.component.input.RangeAttributeRenderer;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RangeAttributePresenter extends CompositeAttributePresenter {
		
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
			var updReq:UpdateRequest = new UpdateRequest();
			var updateValueOp:UpdateRequestOperation = view.rangeInputField.presenter.createUpdateValueOperation();
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
			var attrDefn:RangeAttributeDefinitionProxy = RangeAttributeDefinitionProxy(view.attributeDefinition);
			var result:UpdateRequestOperation = null;
			if(view.unitInputField) {
				result = view.unitInputField.presenter.createUpdateValueOperation();
			} else if ( attrDefn.defaultUnit != null ) {
				result = new UpdateRequestOperation();
				result.method = UpdateRequestOperation$Method.UPDATE;
				result.parentEntityId = view.attribute.parentId;
				result.nodeName = view.attributeDefinition.name;
				result.nodeId = view.attribute.id;
				result.fieldIndex = 3;
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
