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
		
		override protected function initViewState():void {
			var attrDefn:CoordinateAttributeDefinitionProxy = CoordinateAttributeDefinitionProxy(_view.attributeDefinition);
			var view:CoordinateAttributeRenderer = CoordinateAttributeRenderer(_view);
			if(attrDefn.parentLayout == UIUtil.LAYOUT_TABLE) {
				if ( attrDefn.parent.direction == UIOptions$Direction.BY_COLUMNS ) {
					if ( attrDefn.fieldsOrder == UIOptions$CoordinateAttributeFieldsOrder.SRS_X_Y ) {
						view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_SRS_X_Y;
					} else {
						view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_SRS_Y_X;
					}
				} else {
					if ( attrDefn.fieldsOrder == UIOptions$CoordinateAttributeFieldsOrder.SRS_X_Y ) {
						view.currentState = CoordinateAttributeRenderer.STATE_HORIZONTAL_SRS_X_Y;
					} else {
						view.currentState = CoordinateAttributeRenderer.STATE_HORIZONTAL_SRS_Y_X;
					}
				}
			} else {
				if ( attrDefn.fieldsOrder == UIOptions$CoordinateAttributeFieldsOrder.SRS_X_Y ) {
					view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_FORM_SRS_X_Y;
				} else {
					_view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_FORM_SRS_Y_X;
				}
			}
		}
	}
}