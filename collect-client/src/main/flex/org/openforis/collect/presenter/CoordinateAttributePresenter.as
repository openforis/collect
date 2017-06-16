package org.openforis.collect.presenter
{
	import flash.events.FocusEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.CoordinateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.metamodel.ui.UIOptions$CoordinateAttributeFieldsOrder;
	import org.openforis.collect.metamodel.ui.UIOptions$Direction;
	import org.openforis.collect.model.proxy.FieldUpdateRequestProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestSetProxy;
	import org.openforis.collect.ui.component.input.CoordinateAttributeRenderer;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 * */
	public class CoordinateAttributePresenter extends CompositeAttributePresenter {
		
		private static const SRS_FIELD_IDX:int = 2;
		
		public function CoordinateAttributePresenter(view:CoordinateAttributeRenderer) {
			super(view);
			view.xTextInput.applyChangesOnFocusOut = false;
			view.yTextInput.applyChangesOnFocusOut = false;
		}
		
		private function get view():CoordinateAttributeRenderer {
			return CoordinateAttributeRenderer(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.xTextInput.addEventListener(FocusEvent.FOCUS_OUT, xInputFieldFocusOutHandler);
			view.yTextInput.addEventListener(FocusEvent.FOCUS_OUT, yInputFieldFocusOutHandler);
		}
		
		protected function xInputFieldFocusOutHandler(event:FocusEvent):void {
			if(view.xTextInput.changed) {
				updateValue();
			}
		}
		
		protected function yInputFieldFocusOutHandler(event:FocusEvent):void {
			if(view.yTextInput.changed) {
				updateValue();
			}
		}
		
		protected function updateValue():void {
			var updReqSet:NodeUpdateRequestSetProxy = new NodeUpdateRequestSetProxy();
			var xUpdateOp:NodeUpdateRequestProxy = view.xTextInput.presenter.createValueUpdateRequest();
			updReqSet.addRequest(xUpdateOp);
			var yUpdateOp:NodeUpdateRequestProxy = view.yTextInput.presenter.createValueUpdateRequest();
			updReqSet.addRequest(yUpdateOp);
			
			var srsUpdateOp:FieldUpdateRequestProxy = createSrsUpdateOperation();
			if ( srsUpdateOp != null ) {
				if ( xUpdateOp is FieldUpdateRequestProxy 
					&& FieldUpdateRequestProxy(xUpdateOp).value == null
					&& yUpdateOp is FieldUpdateRequestProxy 
					&& FieldUpdateRequestProxy(yUpdateOp).value == null) {
					//clear srs
					srsUpdateOp.value = null;
				}
				updReqSet.addRequest(srsUpdateOp);
			}
			ClientFactory.dataClient.updateActiveRecord(updReqSet, null, faultHandler);
		}
		
		protected function createSrsUpdateOperation():FieldUpdateRequestProxy {
			var attrDefn:CoordinateAttributeDefinitionProxy = CoordinateAttributeDefinitionProxy(view.attributeDefinition);
			var result:FieldUpdateRequestProxy = null;
			if(view.singleSrs) {
				result = new FieldUpdateRequestProxy();
				result.nodeId = view.attribute.id;
				result.fieldIndex = SRS_FIELD_IDX;
				var survey:SurveyProxy = attrDefn.survey;
				var srs:SpatialReferenceSystemProxy = SpatialReferenceSystemProxy(survey.spatialReferenceSystems.getItemAt(0));
				result.value = srs.id;
			} else {
				result = view.srsDropDownList.presenter.createValueUpdateRequest() as FieldUpdateRequestProxy;
			}
			return result;
		}
		
		override protected function initViewState():void {
			var attrDefn:CoordinateAttributeDefinitionProxy = CoordinateAttributeDefinitionProxy(view.attributeDefinition);
			var survey:SurveyProxy = attrDefn.survey;
			var singleSrs:Boolean = survey.spatialReferenceSystems.length == 1;
			view.singleSrs = singleSrs;
			if (singleSrs) {
				var srs:SpatialReferenceSystemProxy = SpatialReferenceSystemProxy(survey.spatialReferenceSystems.getItemAt(0));
				view.singleSrsLabel.text = srs.getLabelText();
				view.srsDropDownList.defaultValue = srs.id;
			}
			view.showSrsField = attrDefn.showSrsField || !singleSrs;
			
			if(attrDefn.parentLayout == UIUtil.LAYOUT_TABLE) {
				if ( attrDefn.parent.direction == UIOptions$Direction.BY_COLUMNS ) {
					switch (attrDefn.fieldsOrder) {
						case UIOptions$CoordinateAttributeFieldsOrder.SRS_X_Y:
							view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_SRS_X_Y;
							break;
						case UIOptions$CoordinateAttributeFieldsOrder.SRS_Y_X:
							view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_SRS_Y_X;
							break;
						case UIOptions$CoordinateAttributeFieldsOrder.X_Y_SRS:
							view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_X_Y_SRS;
							break;
						case UIOptions$CoordinateAttributeFieldsOrder.Y_X_SRS:
							view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_Y_X_SRS;
							break;
					}
				} else {
					switch (attrDefn.fieldsOrder) {
						case UIOptions$CoordinateAttributeFieldsOrder.SRS_X_Y:
							view.currentState = CoordinateAttributeRenderer.STATE_HORIZONTAL_SRS_X_Y;
							break;
						case UIOptions$CoordinateAttributeFieldsOrder.SRS_Y_X:
							view.currentState = CoordinateAttributeRenderer.STATE_HORIZONTAL_SRS_Y_X;
							break;
						case UIOptions$CoordinateAttributeFieldsOrder.X_Y_SRS:
							view.currentState = CoordinateAttributeRenderer.STATE_HORIZONTAL_X_Y_SRS;
							break;
						case UIOptions$CoordinateAttributeFieldsOrder.Y_X_SRS:
							view.currentState = CoordinateAttributeRenderer.STATE_HORIZONTAL_Y_X_SRS;
							break;
					}
				}
			} else {
				switch (attrDefn.fieldsOrder) {
					case UIOptions$CoordinateAttributeFieldsOrder.SRS_X_Y:
						view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_FORM_SRS_X_Y;
						break;
					case UIOptions$CoordinateAttributeFieldsOrder.SRS_Y_X:
						view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_FORM_SRS_Y_X;
						break;
					case UIOptions$CoordinateAttributeFieldsOrder.X_Y_SRS:
						view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_FORM_X_Y_SRS;
						break;
					case UIOptions$CoordinateAttributeFieldsOrder.Y_X_SRS:
						view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_FORM_Y_X_SRS;
						break;
				}
			}
		}
	}
}