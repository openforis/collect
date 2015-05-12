package org.openforis.collect.presenter
{
	import org.openforis.collect.metamodel.proxy.CoordinateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.ui.UIOptions$CoordinateAttributeFieldsOrder;
	import org.openforis.collect.metamodel.ui.UIOptions$Direction;
	import org.openforis.collect.ui.component.input.CoordinateAttributeRenderer;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 * */
	public class CoordinateAttributePresenter extends CompositeAttributePresenter {
		
		public function CoordinateAttributePresenter(view:CoordinateAttributeRenderer) {
			super(view);
		}
		
		private function get view():CoordinateAttributeRenderer {
			return CoordinateAttributeRenderer(_view);
		}
		
		override protected function initViewState():void {
			var attrDefn:CoordinateAttributeDefinitionProxy = CoordinateAttributeDefinitionProxy(view.attributeDefinition);
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