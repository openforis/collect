package org.openforis.collect.presenter
{
	import mx.binding.utils.ChangeWatcher;
	
	import org.openforis.collect.metamodel.proxy.CoordinateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.ui.UIOptions$CoordinateAttributeFieldsOrder;
	import org.openforis.collect.metamodel.ui.UIOptions$Direction;
	import org.openforis.collect.ui.component.detail.CompositeAttributeRenderer;
	import org.openforis.collect.ui.component.input.CoordinateAttributeRenderer;
	import org.openforis.collect.ui.component.input.InputField;
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
					if ( attrDefn.fieldsOrder == UIOptions$CoordinateAttributeFieldsOrder.SRS_Y_X ) {
						view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_SRS_Y_X;
					} else {
						view.currentState = CompositeAttributeRenderer.STATE_VERTICAL;
					}
				} else {
					if ( attrDefn.fieldsOrder == UIOptions$CoordinateAttributeFieldsOrder.SRS_Y_X ) {
						view.currentState = CoordinateAttributeRenderer.STATE_HORIZONTAL_SRS_Y_X;
					} else {
						view.currentState = CompositeAttributeRenderer.STATE_HORIZONTAL;
					}
				}
			} else {
				if ( attrDefn.fieldsOrder == UIOptions$CoordinateAttributeFieldsOrder.SRS_Y_X ) {
					view.currentState = CoordinateAttributeRenderer.STATE_VERTICAL_FORM_SRS_Y_X;
				} else {
					_view.currentState = CompositeAttributeRenderer.STATE_VERTICAL_FORM;
				}
			}
		}
	}
}