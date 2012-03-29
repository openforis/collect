package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.UpdateRequestToken;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.component.input.RangeAttributeRenderer;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RangeAttributePresenter extends CompositeAttributePresenter {
		
		public function RangeAttributePresenter(view:RangeAttributeRenderer) {
			_view = view;
			super(view);
			initUnits();
			view.rangeInputField.applyChangesOnFocusOut = false;
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
				updateUnitField();
			}
		}
		
		protected function updateValue():void {
			view.rangeInputField.presenter.updateValue();
			updateUnitField();
		}
		
		protected function updateUnitField():void {
			var reqOp:UpdateRequestOperation = createUpdateUnitOperation();
			if(view.rangeInputField.isEmpty()) {
				//clear unit
				reqOp.value = null;
			}
			var token:UpdateRequestToken = new UpdateRequestToken(UpdateRequestToken.UPDATE_VALUE);
			token.symbol = reqOp.symbol;
			var field:FieldProxy = view.attribute.fields[2];
			token.updatedFields = new ArrayCollection([field]);
			var updReq:UpdateRequest = new UpdateRequest();
			updReq.addOperation(reqOp);
			ClientFactory.dataClient.updateActiveRecord(updReq, token, null, faultHandler);
		}
		
		protected function createUpdateUnitOperation():UpdateRequestOperation {
			var attrDefn:RangeAttributeDefinitionProxy = RangeAttributeDefinitionProxy(view.attributeDefinition);
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
				result.value = attrDefn.defaultUnit.name;
			}
			return result;
		}
		
		protected function initUnits():void {
			var attrDefn:RangeAttributeDefinitionProxy = RangeAttributeDefinitionProxy(view.attributeDefinition);
			var units:IList = attrDefn.units;
			if(units.length > 0) {
				if(units.length == 1) {
					view.currentState = RangeAttributeRenderer.SINGLE_UNIT_STATE;
					var unit:UnitProxy = UnitProxy(units.getItemAt(0));
					view.unitLabel.text = unit.name;
				} else {
					view.currentState = RangeAttributeRenderer.MULTIPLE_UNIT_STATE;
					view.unitInputField.dataProvider = units;
					if(attrDefn.defaultUnit != null) {
						view.unitInputField.defaultValue = attrDefn.defaultUnit.name;
					}
				}
			}
		}
	}
}
